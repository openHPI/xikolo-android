package de.xikolo.managers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import de.xikolo.App;
import de.xikolo.controllers.dialogs.PermissionsDialog;

public class PermissionManager {

    public static final String TAG = PermissionManager.class.getSimpleName();

    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 92;

    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private FragmentActivity activity;

    public PermissionManager(FragmentActivity parentActivity) {
        super();
        this.activity = parentActivity;
    }

    public int requestPermission(String requestedPermission) {
        Log.d(TAG, "Request Permission " + requestedPermission);

        //Here, this Activity is the current activity
        if (ContextCompat.checkSelfPermission(App.getInstance(),
                requestedPermission) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission not granted yet");

            //Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    requestedPermission)) {
                Log.d(TAG, "Permission explanation expected");

                //Show an explanation to the user *asynchronously* -- don't block
                //this thread waiting for the user's response! After the user
                //sees the explanation, try again to request the permission.
                if (getPermissionCode(requestedPermission) == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
                    PermissionsDialog permDialog = new PermissionsDialog();
                    permDialog.show(activity.getSupportFragmentManager(), TAG);
                }
            } else {
                Log.d(TAG, "Permission explanation expected");

                //No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{requestedPermission},
                        getPermissionCode(requestedPermission));
            }

            return 0; // Permission not granted yet
        } else {
            Log.d(TAG, "Permission already granted");

            return 1; // Permission already granted
        }
    }

    public static int getPermissionCode(String requestedPermissionName) {
        switch (requestedPermissionName) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return REQUEST_CODE_WRITE_EXTERNAL_STORAGE;
        }
        return 0;
    }

    public static void startAppInfo(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

}
