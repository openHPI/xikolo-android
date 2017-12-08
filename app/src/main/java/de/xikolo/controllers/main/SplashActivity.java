package de.xikolo.controllers.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.xikolo.controllers.dialogs.ApiVersionDeprecatedDialog;
import de.xikolo.controllers.dialogs.ApiVersionDeprecatedDialogAutoBundle;
import de.xikolo.controllers.dialogs.ApiVersionExpiredDialog;
import de.xikolo.controllers.dialogs.ServerErrorDialog;
import de.xikolo.controllers.dialogs.ServerMaintenanceDialog;
import de.xikolo.jobs.CheckHealthJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.jobs.base.JobHelper;

public class SplashActivity extends AppCompatActivity {

    public static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JobHelper.getJobManager().addJobInBackground(new CheckHealthJob(getHealthCheckCallback()));
    }

    private JobCallback getHealthCheckCallback() {
        return new JobCallback() {
            @Override
            protected void onSuccess() {
                startApp();
            }

            @Override
            protected void onError(ErrorCode code) {
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
            protected void onDeprecated(Date deprecationDate) {
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
        dialog.show(getSupportFragmentManager(), ApiVersionExpiredDialog.TAG);
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
        dialog.show(getSupportFragmentManager(), ApiVersionDeprecatedDialog.TAG);
    }

    private void showServerMaintenanceDialog() {
        ServerMaintenanceDialog dialog = new ServerMaintenanceDialog();
        dialog.setDialogListener(new ServerMaintenanceDialog.ServerMaintenanceDialogListener() {
            @Override
            public void onDismissed() {
                closeApp();
            }
        });
        dialog.show(getSupportFragmentManager(), ServerMaintenanceDialog.TAG);
    }

    private void showServerErrorDialog() {
        ServerErrorDialog dialog = new ServerErrorDialog();
        dialog.setDialogListener(new ServerErrorDialog.ServerErrorDialogListener() {
            @Override
            public void onDismissed() {
                closeApp();
            }
        });
        dialog.show(getSupportFragmentManager(), ServerErrorDialog.TAG);
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

}
