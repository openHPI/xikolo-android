package de.xikolo.controllers.login;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.yatatsu.autobundle.AutoBundleField;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.controllers.dialogs.CreateTicketDialog;
import de.xikolo.controllers.dialogs.CreateTicketDialogAutoBundle;
import de.xikolo.controllers.webview.WebViewFragmentAutoBundle;

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

        App.getInstance().getState().getLogin()
            .observe(this, isLoggedIn -> {
                if (isLoggedIn) {
                    finish();
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.helpdesk, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_helpdesk:
                CreateTicketDialog dialog = CreateTicketDialogAutoBundle.builder().build();
                dialog.show(getSupportFragmentManager(), CreateTicketDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
