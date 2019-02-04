package de.xikolo.config;

import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

@GlideExtension
public class GlideApiExtension {

    private GlideApiExtension() { }

    @NonNull
    @GlideOption
    public static RequestOptions noPlaceholders(RequestOptions options) {
        return options
                .placeholder(0)
                .error(0)
                .fallback(0);
    }

    @NonNull
    @GlideOption
    public static RequestOptions allPlaceholders(RequestOptions options, @DrawableRes int resourceId) {
        return options
                .placeholder(resourceId)
                .error(resourceId)
                .fallback(resourceId);
    }

}
