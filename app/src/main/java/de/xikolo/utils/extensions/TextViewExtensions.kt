package de.xikolo.utils.extensions

import `in`.uncod.android.bypass.Bypass
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Log
import android.widget.TextView
import de.xikolo.App
import de.xikolo.config.Config
import de.xikolo.config.GlideApp
import de.xikolo.config.GlideRequests
import java.lang.ref.WeakReference
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

fun <T : TextView> T.setMarkdownText(markdown: String?) {
    if (markdown != null) {
        val bypass = Bypass(App.instance)
        val imageGetter = BypassGlideImageGetter(this, GlideApp.with(this.context))
        val spannable = bypass.markdownToSpannable(markdown, imageGetter)

        text = spannable
        movementMethod = LinkMovementMethod.getInstance()

        val current = text as SpannableString
        val spans = current.getSpans(0, current.length, URLSpan::class.java)

        for (span in spans) {
            val start = current.getSpanStart(span)
            val end = current.getSpanEnd(span)
            current.removeSpan(span)
            current.setSpan(URLSpanFix(span.url), start, end, 0)
        }
    } else {
        text = markdown
    }
}

private class URLSpanFix internal constructor(url: String) : URLSpan(url) {

    override fun getURL(): String {
        try {
            val uri = URI(super.getURL())

            if (!uri.isAbsolute) {
                return URL(URL(Config.HOST_URL), super.getURL()).toString()
            }
        } catch (e: URISyntaxException) {
            return super.getURL()
        } catch (e: MalformedURLException) {
            return super.getURL()
        }

        return super.getURL()
    }

}

// taken from https://github.com/Commit451/BypassGlideImageGetter and migrated to glide v4
private class BypassGlideImageGetter internal constructor(textView: TextView, private val glideRequests: GlideRequests) : Bypass.ImageGetter {

    companion object {
        val TAG: String = BypassGlideImageGetter::class.java.simpleName
    }

    private val textViewWeakReference: WeakReference<TextView> = WeakReference(textView)

    private var maxWidth = -1

    override fun getDrawable(source: String): Drawable {

        val result = BitmapDrawablePlaceHolder()

        object : AsyncTask<Void, Void, Bitmap>() {

            override fun doInBackground(vararg meh: Void): Bitmap? {
                var uri = Uri.parse(source)
                if (uri.isRelative) {
                    uri = Uri.Builder()
                        .scheme("https")
                        .authority(Config.HOST)
                        .path(uri.path)
                        .query(uri.query)
                        .build()
                }
                try {
                    return glideRequests
                        .asBitmap()
                        .load(uri)
                        .centerCrop()
                        .noPlaceholders()
                        .dontAnimate()
                        .submit()
                        .get()
                } catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                    return null
                }

            }

            override fun onPostExecute(bitmap: Bitmap) {
                val textView = textViewWeakReference.get() ?: return
                try {
                    if (maxWidth == -1) {
                        val horizontalPadding = textView.paddingLeft + textView.paddingRight
                        maxWidth = textView.measuredWidth - horizontalPadding
                        if (maxWidth == 0) {
                            maxWidth = Integer.MAX_VALUE
                        }
                    }

                    val drawable = BitmapDrawable(textView.resources, bitmap)
                    val aspectRatio = 1.0 * drawable.intrinsicWidth / drawable.intrinsicHeight

                    // real image width in pixel scaled based on screen density
                    val scaledWidth = Math.round(drawable.intrinsicWidth * Resources.getSystem().displayMetrics.density)

                    val width = Math.min(maxWidth, scaledWidth)
                    val height = (width / aspectRatio).toInt()

                    drawable.setBounds(0, 0, width, height)

                    result.drawable = drawable
                    result.setBounds(0, 0, width, height)

                    // invalidate() doesn't work correctly...
                    textView.text = textView.text
                } catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                }

            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        return result
    }

    @Suppress("DEPRECATION")
    internal class BitmapDrawablePlaceHolder : BitmapDrawable() {

        var drawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            if (drawable != null) {
                drawable?.draw(canvas)
            }
        }
    }

}
