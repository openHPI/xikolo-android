package de.xikolo.utils.extensions

import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.Target
import de.xikolo.config.Config
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.glide.GlideImagesPlugin
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

fun <T : TextView> T.setMarkdownText(markdown: String?) {
    if (markdown != null) {
        fun getAbsoluteUrl(url: String): String {
            try {
                val uri = URI(url)
                if (!uri.isAbsolute) {
                    return URL(URL(Config.HOST_URL), url).toString()
                }
            } catch (e: URISyntaxException) {
                return url
            } catch (e: MalformedURLException) {
                return url
            }
            return url
        }

        val formatter = Markwon.builder(context)
            .usePlugin(
                GlideImagesPlugin.create(object : GlideImagesPlugin.GlideStore {
                    override fun cancel(target: Target<*>) {
                        Glide.with(context).clear(target)
                    }

                    override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable> {
                        return Glide.with(context).load(
                            getAbsoluteUrl(drawable.destination)
                        )
                    }
                })
            )
            .usePlugin(
                object : AbstractMarkwonPlugin() {
                    override fun configureTheme(builder: MarkwonTheme.Builder) {
                        // remove heading underline
                        builder.headingBreakHeight(0)
                    }
                }
            )
            .build()

        formatter.setMarkdown(this, markdown)
        movementMethod = LinkMovementMethod.getInstance()

        (text as SpannableString).apply {
            getSpans(0, length, URLSpan::class.java).forEach {
                val start = getSpanStart(it)
                val end = getSpanEnd(it)
                removeSpan(it)
                setSpan(
                    object : URLSpan(it.url) {
                        override fun getURL(): String {
                            return getAbsoluteUrl(super.getURL())
                        }
                    },
                    start,
                    end,
                    0
                )
            }
        }
    } else {
        text = markdown
    }
}
