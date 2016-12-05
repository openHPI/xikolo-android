package de.xikolo.controllers.helper;

import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;

import de.xikolo.GlobalApplication;
import de.xikolo.R;

@SuppressWarnings("unused")
public class ImageController {

    public final static int DEFAULT_PLACEHOLDER = R.drawable.gradient_default_image;

    private static DrawableTypeRequest init(String string) {
        return Glide.with(GlobalApplication.getInstance())
                .load(string);
    }

    private static DrawableTypeRequest init(int resourceId) {
        return Glide.with(GlobalApplication.getInstance())
                .load(resourceId);
    }

    private static void apply(DrawableRequestBuilder builder, final ImageView imageView, int placeholder, int width, int height) {
        builder.centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(width, height)
                .into(imageView);
    }

    private static void applyDontAnimate(DrawableRequestBuilder builder, final ImageView imageView, int placeholder, int width, int height) {
        builder.centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(width, height)
                .dontAnimate()
                .into(imageView);
    }

    public static void load(final String string, final ImageView imageView) {
        load(string, imageView, DEFAULT_PLACEHOLDER);
    }

    public static void load(final String string, final ImageView imageView, int placeholder) {
        apply(init(string), imageView, placeholder, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
    }

    public static void load(final String string, final ImageView imageView, int placeholder, int width, int height) {
        apply(init(string), imageView, placeholder, width, height);
    }

    public static void load(final int resourceId, final ImageView imageView) {
        load(resourceId, imageView, DEFAULT_PLACEHOLDER);
    }

    public static void load(final int resourceId, final ImageView imageView, int placeholder) {
        apply(init(resourceId), imageView, placeholder, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
    }

    public static void load(final int resourceId, final ImageView imageView, int placeholder, boolean animate) {
        if (animate) {
            apply(init(resourceId), imageView, placeholder, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        } else {
            applyDontAnimate(init(resourceId), imageView, placeholder, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        }
    }

    public static void load(final int resourceId, final ImageView imageView, int placeholder, int width, int height) {
        apply(init(resourceId), imageView, placeholder, width, height);
    }

    private static void applyRounded(DrawableTypeRequest builder, final ImageView imageView, final int width, final int height) {
        builder.asBitmap()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(width, height)
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

    public static void loadRounded(final String string, final ImageView imageView) {
        applyRounded(init(string), imageView, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
    }

    public static void loadRounded(final String string, final ImageView imageView, int width, int height) {
        applyRounded(init(string), imageView, width, height);
    }

    public static void loadRounded(final int resourceId, final ImageView imageView) {
        applyRounded(init(resourceId), imageView, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
    }

    public static void loadRounded(final int resourceId, final ImageView imageView, int width, int height) {
        applyRounded(init(resourceId), imageView, width, height);
    }

}
