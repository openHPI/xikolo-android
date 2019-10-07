package de.xikolo.config

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import de.xikolo.R
import de.xikolo.network.ApiService
import okhttp3.OkHttpClient
import java.io.InputStream

@GlideModule
class GlideConfig : AppGlideModule() {

    companion object {
        private const val DEFAULT_PLACEHOLDER = R.drawable.gradient_placeholder
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val options = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .placeholder(DEFAULT_PLACEHOLDER)
            .error(DEFAULT_PLACEHOLDER)
            .fallback(DEFAULT_PLACEHOLDER)
            .centerCrop()

        builder.setDefaultRequestOptions(options)
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val client = OkHttpClient.Builder()
            .addInterceptor(ApiService.userAgentInterceptor)
            .build()
        val factory = OkHttpUrlLoader.Factory(client)
        registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }

}
