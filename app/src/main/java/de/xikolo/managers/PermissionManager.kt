package de.xikolo.managers

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import de.xikolo.App
import de.xikolo.controllers.dialogs.PermissionsDialog

class PermissionManager(private val activity: FragmentActivity) {

    companion object {
        val TAG = PermissionManager::class.java.simpleName

        const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 92
        const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

        fun getPermissionCode(requestedPermissionName: String): Int {
            when (requestedPermissionName) {
                WRITE_EXTERNAL_STORAGE -> return REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            }
            return 0
        }

        fun startAppInfo(activity: Activity?) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", activity?.packageName, null)
            intent.data = uri
            activity?.startActivity(intent)
        }

        @TargetApi(26)
        fun hasPipPermission(context: Context?): Boolean {
            if(context == null){
                return false
            }

            try {
                val manager = ContextCompat.getSystemService(context, AppOpsManager::class.java)
                if (manager != null) {
                    val status = manager.checkOp(
                        AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).uid,
                        context.packageName
                    )
                    if (status == AppOpsManager.MODE_ALLOWED) {
                        return true
                    }
                }
            } catch (ignored: Exception) {
            }

            return false
        }
    }

    fun requestPermission(requestedPermission: String): Int {
        Log.d(TAG, "Request Permission $requestedPermission")

        //Here, this Activity is the current activity
        if (ContextCompat.checkSelfPermission(App.instance, requestedPermission) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission not granted yet")

            //Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestedPermission)) {
                Log.d(TAG, "Permission explanation expected")

                //Show an explanation to the user *asynchronously* -- don't block
                //this thread waiting for the user's response! After the user
                //sees the explanation, try again to request the permission.
                if (getPermissionCode(requestedPermission) == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
                    val permDialog = PermissionsDialog()
                    permDialog.show(activity.supportFragmentManager, TAG)
                }
            } else {
                Log.d(TAG, "Permission explanation expected")

                //No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(requestedPermission),
                    getPermissionCode(requestedPermission)
                )
            }

            return 0 // Permission not granted yet
        } else {
            Log.d(TAG, "Permission already granted")

            return 1 // Permission already granted
        }
    }
}
