package de.xikolo.controllers.settings

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.controllers.dialogs.StorageMigrationDialog
import de.xikolo.controllers.dialogs.StorageMigrationDialogAutoBundle
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.events.LoginEvent
import de.xikolo.events.LogoutEvent
import de.xikolo.managers.UserManager
import de.xikolo.services.DownloadService
import de.xikolo.utils.FileUtil
import de.xikolo.utils.StorageUtil
import de.xikolo.utils.ToastUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName
    }

    private var loginOut: Preference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        super.onResume()
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(getString(R.string.preference_storage))) {
            val newStoragePreference = sharedPreferences?.getString(getString(R.string.preference_storage), getString(R.string.settings_default_value_storage))!!
            findPreference(getString(R.string.preference_storage)).summary = newStoragePreference

            val newStorageType = StorageUtil.toStorageType(App.getInstance(), newStoragePreference)
            var oldStorageType = StorageUtil.StorageType.INTERNAL
            var oldStorage = StorageUtil.getInternalStorage(App.getInstance())
            if (newStorageType == StorageUtil.StorageType.INTERNAL) {
                oldStorageType = StorageUtil.StorageType.SDCARD
                oldStorage = StorageUtil.getSdcardStorage(App.getInstance())!!
            }

            // clean up before
            StorageUtil.cleanStorage(oldStorage)

            val fileCount = FileUtil.folderFileNumber(oldStorage)
            if (fileCount > 0) {
                val dialog = StorageMigrationDialogAutoBundle.builder(oldStorageType).build()
                dialog.listener = object : StorageMigrationDialog.Listener {
                    override fun onDialogPositiveClick() {
                        val progressDialog = ProgressDialog(activity)
                        progressDialog.setTitle(R.string.dialog_storage_migration_title)
                        progressDialog.setMessage(App.getInstance().getString(R.string.dialog_storage_migration_message))
                        progressDialog.setCancelable(false)
                        progressDialog.setCanceledOnTouchOutside(false)
                        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL)
                        progressDialog.max = fileCount
                        progressDialog.show()

                        val migrationCallback = object : StorageUtil.StorageMigrationCallback {
                            override fun onProgressChanged(count: Int) {
                                activity?.runOnUiThread { progressDialog.progress = count }
                            }

                            override fun onCompleted(success: Boolean) {
                                activity?.runOnUiThread {
                                    if (success) {
                                        ToastUtil.show(R.string.dialog_storage_migration_successful)
                                    } else {
                                        ToastUtil.show(R.string.error_plain)
                                    }
                                    progressDialog.dismiss()
                                }
                            }
                        }

                        if (newStorageType == StorageUtil.StorageType.INTERNAL) {
                            StorageUtil.migrateAsync(
                                StorageUtil.getSdcardStorage(App.getInstance())!!,
                                StorageUtil.getInternalStorage(App.getInstance()),
                                migrationCallback
                            )
                        } else {
                            StorageUtil.migrateAsync(
                                StorageUtil.getInternalStorage(App.getInstance()),
                                StorageUtil.getSdcardStorage(App.getInstance())!!,
                                migrationCallback
                            )
                        }
                    }
                }


                dialog.show(activity?.supportFragmentManager, StorageMigrationDialog.TAG)
            }
        }
    }

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        findPreference(getString(R.string.preference_storage)).summary = prefs.getString(getString(R.string.preference_storage), getString(R.string.settings_default_value_storage))!!
        findPreference(getString(R.string.preference_storage)).onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, _ ->
                    if (DownloadService.getInstance() != null && DownloadService.getInstance().isDownloading) {
                        ToastUtil.show(R.string.notification_storage_locked)
                        return@OnPreferenceChangeListener false
                    }
                    true
                }

        // Android does not support multiple external storages below KITKAT
        // Determining the states of multiple storages requires LOLLIPOP
        if (Build.VERSION.SDK_INT < 21 || StorageUtil.getStorages(App.getInstance()).size < 2) {
            val general = findPreference(getString(R.string.preference_category_general)) as PreferenceCategory
            val storagePref = findPreference(getString(R.string.preference_storage))
            general.removePreference(storagePref)
        }

        val copyright = findPreference(getString(R.string.preference_copyright))
        copyright.title = String.format(copyright.title.toString(), Calendar.getInstance().get(Calendar.YEAR))
        copyright.setOnPreferenceClickListener { _ ->
            openUrl(Config.COPYRIGHT_URL)
            true
        }

        val imprint = findPreference(getString(R.string.preference_imprint))
        if (Config.IMPRINT_URL != null) {
            imprint.setOnPreferenceClickListener { _ ->
                openUrl(Config.IMPRINT_URL)
                true
            }
        } else {
            val info = findPreference(getString(R.string.preference_category_info)) as PreferenceCategory
            info.removePreference(imprint)
        }

        val privacy = findPreference(getString(R.string.preference_privacy))
        if (Config.PRIVACY_URL != null) {
            privacy.setOnPreferenceClickListener { _ ->
                openUrl(Config.PRIVACY_URL)
                true
            }
        } else {
            val info = findPreference(getString(R.string.preference_category_info)) as PreferenceCategory
            info.removePreference(privacy)
        }

        val termsOfUse = findPreference(getString(R.string.preference_terms_of_use))
        if (Config.TERMS_OF_USE_URL != null) {
            termsOfUse.setOnPreferenceClickListener { _ ->
                openUrl(Config.TERMS_OF_USE_URL)
                true
            }
        } else {
            val info = findPreference(getString(R.string.preference_category_info)) as PreferenceCategory
            info.removePreference(termsOfUse)
        }

        val buildVersion = findPreference(getString(R.string.preference_build_version))
        buildVersion.summary = (buildVersion.summary.toString()
            + " "
            + BuildConfig.VERSION_NAME)

        val licenses = findPreference(getString(R.string.preference_open_source_licenses))
        licenses.setOnPreferenceClickListener { _ ->
            LicensesDialog.Builder(activity)
                .setNotices(R.raw.notices)
                .setTitle(R.string.settings_title_licenses)
                .build()
                .show()
            true
        }

        val sendFeedback = findPreference(getString(R.string.preference_send_feedback))
        sendFeedback.setOnPreferenceClickListener { _ ->
            startFeedbackIntent()
            true
        }

        loginOut = findPreference(getString(R.string.preference_login_out))
        if (UserManager.isAuthorized) {
            buildLogoutView(loginOut)
        } else {
            buildLoginView(loginOut)
        }
    }

    private fun buildLoginView(pref: Preference?) {
        if (pref != null) {
            pref.title = getString(R.string.login)
            pref.setOnPreferenceClickListener { _ ->
                val intent = LoginActivityAutoBundle.builder().build(activity!!)
                startActivity(intent)
                true
            }
        }
    }

    private fun buildLogoutView(pref: Preference?) {
        if (pref != null) {
            pref.title = getString(R.string.logout)
            pref.setOnPreferenceClickListener { _ ->
                UserManager.logout()
                ToastUtil.show(R.string.toast_successful_logout)
                true
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginEvent(event: LoginEvent) {
        buildLogoutView(loginOut)
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        buildLoginView(loginOut)
    }

    private fun openUrl(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(App.getInstance(), R.color.apptheme_main))
            .build()
        customTabsIntent.launchUrl(activity, Uri.parse(url))
    }

    private fun startFeedbackIntent() {
        val osVersion = Build.VERSION.RELEASE.toString()
        val deviceName = String.format("%s %s", Build.MANUFACTURER, Build.MODEL)
        val brand = resources.getString(R.string.app_name)
        val versionName = BuildConfig.VERSION_NAME
        val buildId = BuildConfig.VERSION_CODE.toString()

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf(resources.getString(R.string.settings_send_app_feedback_mail_address))
        )
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            String.format(resources.getString(R.string.settings_send_app_feedback_subject), brand)
        )
        intent.putExtra(
            Intent.EXTRA_TEXT,
            String.format(
                resources.getString(R.string.settings_send_app_feedback_message),
                osVersion,
                deviceName,
                brand,
                versionName,
                buildId
            )
        )

        startActivity(
            Intent.createChooser(
                intent,
                resources.getString(R.string.settings_send_app_feedback)
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}
