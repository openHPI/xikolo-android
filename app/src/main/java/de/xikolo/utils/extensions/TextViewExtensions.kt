package de.xikolo.utils.extensions

import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.Target
import de.xikolo.config.Config
import de.xikolo.controllers.webview.WebViewActivityAutoBundle
import de.xikolo.managers.UserManager
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
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
            .usePlugin(MarkwonInlineParserPlugin.create())
            .usePlugin(JLatexMathPlugin.create(textSize))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(
                GlideImagesPlugin.create(object : GlideImagesPlugin.GlideStore {
                    private var glide = Glide.with(context)

                    override fun cancel(target: Target<*>) {
                        glide.clear(target)
                    }

                    override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable> {
                        return glide
                            .load(getAbsoluteUrl(drawable.destination))
                            .override(
                                context.displaySize.x,
                                Target.SIZE_ORIGINAL
                            )
                            .dontTransform()
                            .fitCenter()
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

                        override fun onClick(widget: View) {
                            val uri = Uri.parse(url)
                            val context = widget.context

                            val intent = WebViewActivityAutoBundle.builder("", url)
                                .inAppLinksEnabled(false)
                                .externalLinksEnabled(false)
                                .build(context)

                            if (uri.host == Config.HOST) {
                                intent.includeAuthToken(UserManager.token!!)
                            }

                            context.startActivity(intent)
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
