package de.xikolo.controller.helper;

import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import de.xikolo.GlobalApplication;
import de.xikolo.R;

public class ImageController {

    public static void load(final String url, final ImageView imageView) {
        load(url, imageView, R.drawable.gradient_default_image);
    }

    public static void load(final String url, final ImageView imageView, int placeholder) {
        Glide.with(GlobalApplication.getInstance())
                .load(url)
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    public static void load(final int id, final ImageView imageView) {
        load(id, imageView, R.drawable.gradient_default_image);
    }

    public static void load(final int id, final ImageView imageView, int placeholder) {
        Glide.with(GlobalApplication.getInstance())
                .load(id)
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    public static void loadRounded(final String url, final ImageView imageView, int placeholder) {
        Glide.with(GlobalApplication.getInstance())
                .load(url)
                .asBitmap()
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(GlobalApplication.getInstance().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public static void loadRounded(final String url, final ImageView imageView, int placeholder, int x, int y) {
        Glide.with(GlobalApplication.getInstance())
                .load(url)
                .asBitmap()
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .override(x, y)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(GlobalApplication.getInstance().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public static void loadRounded(final int id, final ImageView imageView, int placeholder) {
        Glide.with(GlobalApplication.getInstance())
                .load(id)
                .asBitmap()
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(GlobalApplication.getInstance().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public static void loadRounded(final int id, final ImageView imageView, int placeholder, int x, int y) {
        Glide.with(GlobalApplication.getInstance())
                .load(id)
                .asBitmap()
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .override(x, y)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(GlobalApplication.getInstance().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

}
