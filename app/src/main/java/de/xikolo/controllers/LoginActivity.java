package de.xikolo.controllers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.BuildConfig;
import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.controllers.helper.ImageController;
import de.xikolo.events.LoginEvent;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.models.AccessToken;
import de.xikolo.models.User;
import de.xikolo.storages.preferences.StorageType;
import de.xikolo.storages.preferences.UserStorage;
import de.xikolo.utils.BuildFlavor;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ToastUtil;

public class LoginActivity extends BaseActivity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    private UserManager userManager;
    private Result<Void> loginResult;
    private Result<User> userResult;

    private EditText editTextEmail;
    private EditText editTextPassword;

    private ProgressDialog progressDialog;

    public static final String ARG_TOKEN = "arg_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        editTextEmail = (EditText) findViewById(R.id.editEmail);
        editTextPassword = (EditText) findViewById(R.id.editPassword);
        Button buttonLogin = (Button) findViewById(R.id.btnLogin);
        Button buttonNew = (Button) findViewById(R.id.btnNew);
        Button buttonSSO = (Button) findViewById(R.id.btnSSO);
        View containerSSO = findViewById(R.id.ssoContainer);
        TextView textReset = (TextView) findViewById(R.id.textForgotPw);
        ImageView topImage = (ImageView) findViewById(R.id.top_image);
        TextView textCredentials = (TextView) findViewById(R.id.text_credentials);

        textCredentials.setText(String.format(getString(R.string.login_with_credentials), Config.HOST));

        ImageController.load(R.drawable.login_header, topImage, 0, false);

        progressDialog = ProgressDialog.getInstance();

        userManager = new UserManager(jobManager);
        loginResult = new Result<Void>() {
            @Override
            protected void onSuccess(Void result, DataSource dataSource) {
                userManager.getUser(userResult);
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                progressDialog.dismiss();
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast();
                } else {
                    ToastUtil.show(R.string.toast_log_in_failed);
                }
                userManager.logout();
            }
        };
        userResult = new Result<User>() {
            @Override
            protected void onSuccess(User result, DataSource dataSource) {
                EventBus.getDefault().post(new LoginEvent());
                progressDialog.dismiss();
                LoginActivity.this.finish();
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                progressDialog.dismiss();
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast();
                } else {
                    ToastUtil.show(R.string.toast_log_in_failed);
                }
                userManager.logout();
            }
        };

        editTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login(v);
                    handled = true;
                }
                return handled;
            }
        });


        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    login(view);
                }
            });
        }

        if (buttonNew != null) {
            buttonNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideKeyboard(view);
                    startUrlIntent(Config.URI + Config.ACCOUNT + Config.NEW);
                }
            });
        }

        if (textReset != null) {
            textReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideKeyboard(view);
                    startUrlIntent(Config.URI + Config.ACCOUNT + Config.RESET);
                }
            });
        }

        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
            containerSSO.setVisibility(View.VISIBLE);
            buttonSSO.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideKeyboard(view);
                    startSSOLogin("/auth/who");
                }
            });
        }

        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP) {
            containerSSO.setVisibility(View.VISIBLE);
            buttonSSO.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideKeyboard(view);
                    startSSOLogin("/auth/sap");
                }
            });
        }

        Bundle b = getIntent().getExtras();
        if (b != null && b.getString(ARG_TOKEN) != null) {
            externalLoginCallback(b.getString(ARG_TOKEN));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (UserManager.isLoggedIn()) {
            finish();
        }
    }

    private void login(View view) {
        hideKeyboard(view);
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        if (isEmailValid(email)) {
            if (!password.equals("")) {
                userManager.login(loginResult, email, password);
                progressDialog.show(getSupportFragmentManager(), ProgressDialog.TAG);
            } else {
                editTextPassword.setError(getString(R.string.error_password));
            }
        } else {
            editTextEmail.setError(getString(R.string.error_email));
        }
    }

    private void startSSOLogin(String strategy) {
        Intent intent = new Intent(this, SsoLoginActivity.class);
        intent.putExtra(SsoLoginActivity.ARG_URL, Config.URI + "?in_app=true&redirect_to=" + strategy);
        intent.putExtra(SsoLoginActivity.ARG_TITLE, getString(R.string.login_sso));
        startActivity(intent);
    }

    private void externalLoginCallback(String token) {
        progressDialog.show(getSupportFragmentManager(), ProgressDialog.TAG);

        AccessToken at = new AccessToken();
        at.token = token;
        UserStorage userStorage = (UserStorage) GlobalApplication.getStorage(StorageType.USER);
        userStorage.saveAccessToken(at);

        userManager.getUser(userResult);

        EventBus.getDefault().post(new LoginEvent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.module, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar module clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void startUrlIntent(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}
