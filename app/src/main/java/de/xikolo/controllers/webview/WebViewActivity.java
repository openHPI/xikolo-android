package de.xikolo.controllers.webview;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.yatatsu.autobundle.AutoBundleField;

import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.controllers.dialogs.CreateTicketDialog;
import de.xikolo.controllers.dialogs.CreateTicketDialogAutoBundle;

public class WebViewActivity extends BaseActivity {

    public static final String TAG = WebViewActivity.class.getSimpleName();

    @AutoBundleField String title;
    @AutoBundleField String url;
    @AutoBundleField(required = false) boolean inAppLinksEnabled;
    @AutoBundleField(required = false) boolean externalLinksEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        setupActionBar();

        setTitle(title);

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = WebViewFragmentAutoBundle.builder(url)
                    .inAppLinksEnabled(inAppLinksEnabled)
                    .externalLinksEnabled(externalLinksEnabled)
                    .build();
            transaction.replace(R.id.content, fragment, tag);
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_helpdesk:
                CreateTicketDialog dialog = CreateTicketDialogAutoBundle.builder().build();
                dialog.show(getSupportFragmentManager(), CreateTicketDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
