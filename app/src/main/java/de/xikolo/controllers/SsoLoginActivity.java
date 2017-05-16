package de.xikolo.controllers;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.xikolo.R;
import de.xikolo.events.LoginEvent;

public class SsoLoginActivity extends BaseActivity {

    public static final String TAG = SsoLoginActivity.class.getSimpleName();

    public static final String ARG_URL = "arg_url";
    public static final String ARG_TITLE = "arg_title";

    public String url;
    public String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        setupActionBar();

        Bundle b = getIntent().getExtras();
        this.url = b.getString(ARG_URL);
        this.title = b.getString(ARG_TITLE);

        if (title != null) {
            setTitle(title);
        }

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, WebViewFragment.newInstance(url, true, true), tag);
            transaction.commit();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        finish();
    }

}
