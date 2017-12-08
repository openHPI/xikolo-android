package de.xikolo.controllers.login;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.yatatsu.autobundle.AutoBundleField;

import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;

public class LoginActivity extends BaseActivity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    @AutoBundleField(required = false) String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle(null);

        String tag = "login";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, LoginFragmentAutoBundle.builder().token(token).build(), tag);
            transaction.commit();
        }
    }

}
