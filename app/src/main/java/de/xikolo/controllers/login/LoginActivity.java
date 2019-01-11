package de.xikolo.controllers.login;

import android.os.Bundle;

import com.yatatsu.autobundle.AutoBundleField;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
