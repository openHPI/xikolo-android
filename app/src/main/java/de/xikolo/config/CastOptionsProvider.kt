package de.xikolo.config

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent

import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions
import de.xikolo.App

import de.xikolo.controllers.cast.CastActivity
import de.xikolo.utils.extensions.getString

@Suppress("UNUSED")
class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(appContext: Context): CastOptions {
        val notificationOptions = NotificationOptions.Builder()
            .setTargetActivityClassName(CastActivity::class.java.name)
            .build()

        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .setExpandedControllerActivityClassName(CastActivity::class.java.name)
            .build()

        val castAppId = if (Feature.enabled("custom_cast_application_id")) {
            App.instance.getString("custom_cast_application_id")
        } else {
            CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
        }

        return CastOptions.Builder()
            .setReceiverApplicationId(castAppId)
            .setCastMediaOptions(mediaOptions)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }

}
