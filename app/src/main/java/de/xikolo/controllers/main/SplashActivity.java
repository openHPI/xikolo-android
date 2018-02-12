package de.xikolo.controllers.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.xikolo.controllers.dialogs.ApiVersionDeprecatedDialog;
import de.xikolo.controllers.dialogs.ApiVersionDeprecatedDialogAutoBundle;
import de.xikolo.controllers.dialogs.ApiVersionExpiredDialog;
import de.xikolo.controllers.dialogs.ServerErrorDialog;
import de.xikolo.controllers.dialogs.ServerMaintenanceDialog;
import de.xikolo.jobs.CheckHealthJob;
import de.xikolo.jobs.base.RequestJobCallback;

public class SplashActivity extends AppCompatActivity {

    public static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new CheckHealthJob(getHealthCheckCallback()).run();
    }

    private RequestJobCallback getHealthCheckCallback() {
        return new RequestJobCallback() {
            @Override
            protected void onSuccess() {
                startApp();
            }

            @Override
            protected void onError(@NotNull ErrorCode code) {
                switch (code) {
                    case NO_NETWORK:
                        startApp();
                        break;
                    case API_VERSION_EXPIRED:
                        showApiVersionExpiredDialog();
                        break;
                    case MAINTENANCE:
                        showServerMaintenanceDialog();
                        break;
                    case ERROR:
                        showServerErrorDialog();
                        break;
                }
            }

            @Override
            protected void onDeprecated(@NonNull Date deprecationDate) {
                Date now = new Date();
                long distance = deprecationDate.getTime() - now.getTime();
                long days = TimeUnit.DAYS.convert(distance, TimeUnit.MILLISECONDS);

                if (days <= 14) {
                    showApiVersionDeprecatedDialog(deprecationDate);
                } else {
                    startApp();
                }
            }
        };
    }

    private void showApiVersionExpiredDialog() {
        ApiVersionExpiredDialog dialog = new ApiVersionExpiredDialog();
        dialog.setDialogListener(new ApiVersionExpiredDialog.ApiVersionExpiredDialogListener() {
            @Override
            public void onOpenPlayStoreClicked() {
                openPlayStore();
            }

            @Override
            public void onDismissed() {
                closeApp();
            }
        });
        showDialog(dialog, ApiVersionExpiredDialog.TAG);
    }

    private void showApiVersionDeprecatedDialog(Date deprecationDate) {
        ApiVersionDeprecatedDialog dialog = ApiVersionDeprecatedDialogAutoBundle.builder(deprecationDate).build();
        dialog.setDialogListener(new ApiVersionDeprecatedDialog.ApiVersionDeprecatedDialogListener() {
            @Override
            public void onOpenPlayStoreClicked() {
                openPlayStore();
            }

            @Override
            public void onDismissed() {
                startApp();
            }
        });
        showDialog(dialog, ApiVersionDeprecatedDialog.TAG);
    }

    private void showServerMaintenanceDialog() {
        ServerMaintenanceDialog dialog = new ServerMaintenanceDialog();
        dialog.setDialogListener(this::closeApp);
        showDialog(dialog, ServerMaintenanceDialog.TAG);
    }

    private void showServerErrorDialog() {
        ServerErrorDialog dialog = new ServerErrorDialog();
        dialog.setDialogListener(this::closeApp);
        showDialog(dialog, ServerErrorDialog.TAG);
    }

    private void startApp() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void closeApp() {
        finish();
    }

    private void openPlayStore() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
        finish();
    }

    // bug fix workaround
    private void showDialog(DialogFragment dialogFragment, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(dialogFragment, tag);
        ft.commitAllowingStateLoss();
    }

}
