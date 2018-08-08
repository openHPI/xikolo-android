package de.xikolo.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import de.xikolo.App;
import de.xikolo.config.Config;
import de.xikolo.config.GlideApp;
import de.xikolo.config.GlideRequests;
import in.uncod.android.bypass.Bypass;

public class MarkdownUtil {

    public static void formatAndSet(String markdown, TextView textView) {
        if (markdown != null) {
            Bypass bypass = new Bypass(App.getInstance());
            BypassGlideImageGetter imageGetter = new BypassGlideImageGetter(textView, GlideApp.with(textView.getContext()));
            CharSequence spannable = bypass.markdownToSpannable(markdown, imageGetter);
            textView.setText(spannable);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            textView.setText(null);
        }
    }

    // taken from https://github.com/Commit451/BypassGlideImageGetter and migrated to glide v4
    private static class BypassGlideImageGetter implements Bypass.ImageGetter {

        public static final String TAG = BypassGlideImageGetter.class.getSimpleName();

        private GlideRequests glideRequests;

        private final WeakReference<TextView> textViewWeakReference;

        private int maxWidth = -1;

        BypassGlideImageGetter(final TextView textView, GlideRequests glideRequests) {
            this.textViewWeakReference = new WeakReference<>(textView);
            this.glideRequests = glideRequests;
        }

        @Override
        public Drawable getDrawable(final String source) {

            final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder();

            new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(final Void... meh) {
                    Uri uri = Uri.parse(source);
                    if (uri.isRelative()) {
                        uri = new Uri.Builder()
                                .scheme("https")
                                .authority(Config.HOST)
                                .path(uri.getPath())
                                .query(uri.getQuery())
                                .build();
                    }
                    try {
                        return glideRequests
                                .asBitmap()
                                .load(uri)
                                .centerCrop()
                                .noPlaceholders()
                                .dontAnimate()
                                .submit()
                                .get();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(final Bitmap bitmap) {
                    TextView textView = textViewWeakReference.get();
                    if (textView == null) {
                        return;
                    }
                    try {
                        if (maxWidth == -1) {
                            int horizontalPadding = textView.getPaddingLeft() + textView.getPaddingRight();
                            maxWidth = textView.getMeasuredWidth() - horizontalPadding;
                            if (maxWidth == 0) {
                                maxWidth = Integer.MAX_VALUE;
                            }
                        }

                        final BitmapDrawable drawable = new BitmapDrawable(textView.getResources(), bitmap);
                        final double aspectRatio = 1.0 * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();

                        // real image width in pixel scaled based on screen density
                        final int scaledWidth = Math.round(drawable.getIntrinsicWidth() * Resources.getSystem().getDisplayMetrics().density);

                        final int width = Math.min(maxWidth, scaledWidth);
                        final int height = (int) (width / aspectRatio);

                        drawable.setBounds(0, 0, width, height);

                        result.setDrawable(drawable);
                        result.setBounds(0, 0, width, height);

                        // invalidate() doesn't work correctly...
                        textView.setText(textView.getText());
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            return result;
        }

        @SuppressWarnings("deprecation")
        static class BitmapDrawablePlaceHolder extends BitmapDrawable {

            protected Drawable drawable;

            @Override
            public void draw(final Canvas canvas) {
                if (drawable != null) {
                    drawable.draw(canvas);
                }
            }

            public void setDrawable(Drawable drawable) {
                this.drawable = drawable;
            }

        }

    }

}
