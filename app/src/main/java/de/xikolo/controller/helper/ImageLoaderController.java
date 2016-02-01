package de.xikolo.controller.helper;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.view.RoundCornersDrawable;

public class ImageLoaderController {

    public static void loadImage(final String url, final ImageView imageView) {
        ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                imageView.setImageDrawable(ContextCompat.getDrawable(GlobalApplication.getInstance(), R.drawable.gradient_default_image));
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                imageView.setImageDrawable(ContextCompat.getDrawable(GlobalApplication.getInstance(), R.drawable.gradient_default_image));
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageView.setImageDrawable(new BitmapDrawable(GlobalApplication.getInstance().getResources(), loadedImage));
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                imageView.setImageDrawable(ContextCompat.getDrawable(GlobalApplication.getInstance(), R.drawable.gradient_default_image));
            }
        });
    }

    public static void loadCourseImage(final String url, final ImageView imageView, final ViewGroup cardView) {
        ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                imageView.setImageDrawable(ContextCompat.getDrawable(GlobalApplication.getInstance(), R.drawable.gradient_default_image));
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                imageView.setImageDrawable(ContextCompat.getDrawable(GlobalApplication.getInstance(), R.drawable.gradient_default_image));
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageView.setImageDrawable(getDrawable(loadedImage, cardView));
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                imageView.setImageDrawable(ContextCompat.getDrawable(GlobalApplication.getInstance(), R.drawable.gradient_default_image));
            }
        });
    }

    private static Drawable getDrawable(Bitmap image, ViewGroup cardView) {
        float radius;

        try {
            radius = ((CardView)cardView).getRadius();
        } catch (ClassCastException e) {
            radius = 4;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new BitmapDrawable(GlobalApplication.getInstance().getResources(), image);
        } else {
            return new RoundCornersDrawable(image, radius, 0);
        }
    }

}
