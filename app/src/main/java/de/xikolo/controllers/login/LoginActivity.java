package de.xikolo.controllers.login;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.yatatsu.autobundle.AutoBundleField;

import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.controllers.dialogs.CreateTicketDialog;
import de.xikolo.controllers.dialogs.CreateTicketDialogAutoBundle;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.helpdesk, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_helpdesk) {
            CreateTicketDialog dialog = CreateTicketDialogAutoBundle.builder().build();
            dialog.show(getSupportFragmentManager(), CreateTicketDialog.TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
