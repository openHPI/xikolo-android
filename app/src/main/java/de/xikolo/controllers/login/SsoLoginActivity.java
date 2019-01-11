package de.xikolo.controllers.login;

import android.os.Bundle;

import com.yatatsu.autobundle.AutoBundleField;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.controllers.webview.WebViewFragmentAutoBundle;
import de.xikolo.events.LoginEvent;

public class SsoLoginActivity extends BaseActivity {

    public static final String TAG = SsoLoginActivity.class.getSimpleName();

    @AutoBundleField String url;
    @AutoBundleField String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        setupActionBar();

        setTitle(title);

        String tag = "content";

        Fragment fragment = WebViewFragmentAutoBundle.builder(url)
                .inAppLinksEnabled(true)
                .externalLinksEnabled(true)
                .build();

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, fragment, tag);
            transaction.commit();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        finish();
    }

}
