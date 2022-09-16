package de.xikolo.controllers.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.AnyRes
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.config.Feature
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.controllers.login.SsoLoginActivityAutoBundle
import de.xikolo.utils.extensions.*

object LoginHelper {

    fun loginIntent(context: Context): Intent {
        val intent = if (Feature.enabled("sso_provider") && Feature.enabled("disabled_password_login")) {
            ssoLoginIntent(context)
        } else {
            LoginActivityAutoBundle.builder().build(context)
        }
        return intent
    }

    fun ssoLoginIntent(context: Context): Intent {
        val strategy = context.getString("sso_provider")
        return SsoLoginActivityAutoBundle.builder(
            Config.HOST_URL + "auth/" + strategy + "?in_app=true&redirect_to=/auth/" + strategy,
            context.getString(R.string.login_sso)
        ).build(context)
    }

}
