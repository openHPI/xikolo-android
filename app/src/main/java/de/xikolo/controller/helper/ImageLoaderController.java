package de.xikolo.controller.helper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import de.xikolo.GlobalApplication;
import de.xikolo.R;

public class ImageLoaderController {

    public static void loadImage(final String url, final ImageView imageView) {
        ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                imageView.setImageDrawable(GlobalApplication.getInstance().getResources().getDrawable(R.drawable.gradient_default_image));
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                imageView.setImageDrawable(GlobalApplication.getInstance().getResources().getDrawable(R.drawable.gradient_default_image));
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageView.setImageDrawable(new BitmapDrawable(GlobalApplication.getInstance().getResources(), loadedImage));
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                imageView.setImageDrawable(GlobalApplication.getInstance().getResources().getDrawable(R.drawable.gradient_default_image));
            }
        });
    }

}
