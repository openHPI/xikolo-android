package de.xikolo.controllers.main

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import de.xikolo.R
import de.xikolo.controllers.dialogs.*
import de.xikolo.network.jobs.CheckHealthJob
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.utils.FileUtil
import de.xikolo.utils.StorageUtil
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    companion object {
        val TAG: String = SplashActivity::class.java.simpleName
    }

    private val healthCheckCallback: RequestJobCallback
        get() = object : RequestJobCallback() {
            override fun onSuccess() {
                startApp()
            }

            override fun onError(code: RequestJobCallback.ErrorCode) {
                when (code) {
                    ErrorCode.NO_NETWORK ->             startApp()
                    ErrorCode.API_VERSION_EXPIRED ->    showApiVersionExpiredDialog()
                    ErrorCode.MAINTENANCE ->            showServerMaintenanceDialog()
                    else ->                             showServerErrorDialog()
                }
            }

            override fun onDeprecated(deprecationDate: Date) {
                val now = Date()
                val distance = deprecationDate.time - now.time
                val days = TimeUnit.DAYS.convert(distance, TimeUnit.MILLISECONDS)

                if (days <= 14) {
                    showApiVersionDeprecatedDialog(deprecationDate)
                } else {
                    startApp()
                }
            }
        }

    private fun migrateStorage() {
        if (!ApplicationPreferences().contains(getString(R.string.preference_storage))) {
            val old = File(FileUtil.getPublicAppStorageFolderPath())
            val new = File(FileUtil.createStorageFolderPath(
                File(StorageUtil.getInternalStorage(this).absolutePath + File.separator + "Courses"))
            )
            val fileCount = FileUtil.folderFileNumber(old)

            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage(getString(R.string.dialog_app_being_prepared))
            progressDialog.setTitle(R.string.app_name)
            progressDialog.setCancelable(false)
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL)
            progressDialog.max = 100
            progressDialog.show()

            StorageUtil.migrateAsync(old, new, object : StorageUtil.StorageMigrationCallback {
                override fun onProgressChanged(count: Int) {
                    runOnUiThread { progressDialog.progress = Math.ceil(100.0 * count / fileCount).toInt() }
                }

                override fun onCompleted(success: Boolean) {
                    runOnUiThread {
                        StorageUtil.cleanStorage(old)
                        StorageUtil.cleanStorage(new)
                        progressDialog.dismiss()
                        ApplicationPreferences().setToDefault(getString(R.string.preference_storage), getString(R.string.settings_default_value_storage))
                        CheckHealthJob(healthCheckCallback).run()
                    }
                }
            })
        } else {
            CheckHealthJob(healthCheckCallback).run()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val dialog = Android4UnsupportedDialog()
            dialog.listener = object : Android4UnsupportedDialog.Listener {
                override fun onConfirmed() {
                    closeApp()
                }
            }
            showDialog(dialog, Android4UnsupportedDialog.TAG)
        } else {
            migrateStorage()
        }
    }

    private fun showApiVersionExpiredDialog() {
        val dialog = ApiVersionExpiredDialog()
        dialog.listener = object : ApiVersionExpiredDialog.Listener {
            override fun onOpenPlayStoreClicked() {
                openPlayStore()
            }

            override fun onDismissed() {
                closeApp()
            }
        }
        showDialog(dialog, ApiVersionExpiredDialog.TAG)
    }

    private fun showApiVersionDeprecatedDialog(deprecationDate: Date) {
        val dialog = ApiVersionDeprecatedDialogAutoBundle.builder(deprecationDate).build()
        dialog.listener = object : ApiVersionDeprecatedDialog.Listener {
            override fun onOpenPlayStoreClicked() {
                openPlayStore()
            }

            override fun onDismissed() {
                startApp()
            }
        }
        showDialog(dialog, ApiVersionDeprecatedDialog.TAG)
    }

    private fun showServerMaintenanceDialog() {
        val dialog = ServerMaintenanceDialog()
        dialog.listener = object : ServerMaintenanceDialog.Listener {
            override fun onDismissed() {
                closeApp()
            }
        }
        showDialog(dialog, ServerMaintenanceDialog.TAG)
    }

    private fun showServerErrorDialog() {
        val dialog = ServerErrorDialog()
        dialog.listener = object : ServerErrorDialog.Listener {
            override fun onDismissed() {
                closeApp()
            }
        }
        showDialog(dialog, ServerErrorDialog.TAG)
    }

    private fun startApp() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun closeApp() {
        finish()
    }

    private fun openPlayStore() {
        val appPackageName = packageName
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }

        finish()
    }

    // bug fix workaround
    private fun showDialog(dialogFragment: DialogFragment, tag: String) {
        val ft = supportFragmentManager.beginTransaction()
        ft.add(dialogFragment, tag)
        ft.commitAllowingStateLoss()
    }

}
