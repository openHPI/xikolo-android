package de.xikolo.controllers;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controllers.helper.ImageController;
import de.xikolo.storages.preferences.ApplicationPreferences;
import de.xikolo.storages.preferences.StorageType;

public class OnboardingActivity extends BaseActivity {

    public static final String TAG = OnboardingActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        if (tb != null) {
            setSupportActionBar(tb);
        }

       ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        setTitle(null);

        Button buttonStart = (Button) findViewById(R.id.btnStart);
        ImageView topImage = (ImageView) findViewById(R.id.top_image);

        ImageController.load(R.drawable.login_header, topImage, 0, false);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnboardingActivity.this.finish();
            }
        });

        ApplicationPreferences appPreferences = (ApplicationPreferences) GlobalApplication.getStorage(StorageType.APP);
        appPreferences.setOnboardingShown(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
