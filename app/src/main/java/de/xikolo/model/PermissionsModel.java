package de.xikolo.model;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.path.android.jobqueue.JobManager;

import de.xikolo.GlobalApplication;
import de.xikolo.controller.BaseActivity;
import de.xikolo.controller.dialogs.PermissionsDialog;

public class PermissionsModel extends BaseModel {

    private BaseActivity mActivity;
    private boolean isPermissionPending;

    public static final String TAG = DownloadModel.class.getSimpleName();

    public PermissionsModel(JobManager jobManager, BaseActivity parentActivity) {
        super(jobManager);
        this.mActivity = parentActivity;
        this.isPermissionPending = false;
    }

    private int getPermission(String requestedPermission) {
        //Here, this Activity is the current activity
        if (ContextCompat.checkSelfPermission(GlobalApplication.getInstance(),
                requestedPermission)
                != PackageManager.PERMISSION_GRANTED) {

            //Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                    requestedPermission)) {
                if(getPermissionCode(requestedPermission) == 92){
                    PermissionsDialog pdialog = new PermissionsDialog();
                    pdialog.show(mActivity.getFragmentManager(), TAG);
                }
                    //Show an explanation to the user *asynchronously* -- don't block
                    //this thread waiting for the user's response! After the user
                    //sees the explanation, try again to request the permission.
            } else {
                //No explanation needed, we can request the permission.
                isPermissionPending = true;
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{requestedPermission}, getPermissionCode(requestedPermission));
                return getPermissionCode(requestedPermission); //indicates pending Permission
            }
        } else {
            return 1;
        }
        return 0;
    }

    public int getPermissionCode(String requestedPermissionName) {
        switch(requestedPermissionName){
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return 92; // Code for group 9, permission 2, refer to
                // https://developer.android.com/guide/topics/security/permissions.html#perm-groups
        }
        return 0;
    }
}

