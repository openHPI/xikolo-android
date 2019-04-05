package de.xikolo.config

import androidx.annotation.DrawableRes
import com.bumptech.glide.annotation.GlideExtension
import com.bumptech.glide.annotation.GlideOption
import com.bumptech.glide.request.BaseRequestOptions

@GlideExtension
object GlideApiExtension {

    @JvmStatic
    @GlideOption
    fun noPlaceholders(options: BaseRequestOptions<*>): BaseRequestOptions<*> =
        options
            .placeholder(0)
            .error(0)
            .fallback(0)

    @JvmStatic
    @GlideOption
    fun allPlaceholders(options: BaseRequestOptions<*>, @DrawableRes resourceId: Int): BaseRequestOptions<*> =
        options
            .placeholder(resourceId)
            .error(resourceId)
            .fallback(resourceId)

}
