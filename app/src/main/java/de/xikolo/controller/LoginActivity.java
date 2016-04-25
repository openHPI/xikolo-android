package de.xikolo.controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import de.xikolo.R;
import de.xikolo.controller.dialogs.ProgressDialog;
import de.xikolo.data.entities.User;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.model.events.LoginEvent;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class LoginActivity extends BaseActivity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    private UserModel userModel;
    private Result<Void> loginResult;
    private Result<User> userResult;

    private EditText editTextEmail;
    private EditText editTextPassword;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActionBar();

        setTitle(getString(R.string.title_section_login));

        editTextEmail = (EditText) findViewById(R.id.editEmail);
        editTextPassword = (EditText) findViewById(R.id.editPassword);
        Button buttonLogin = (Button) findViewById(R.id.btnLogin);
        Button buttonNew = (Button) findViewById(R.id.btnNew);
        TextView textReset = (TextView) findViewById(R.id.textForgotPw);

        progressDialog = ProgressDialog.getInstance();

        userModel = new UserModel(jobManager);
        loginResult = new Result<Void>() {
            @Override
            protected void onSuccess(Void result, DataSource dataSource) {
                userModel.getUser(userResult);
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                progressDialog.dismiss();
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast();
                } else {
                    ToastUtil.show(R.string.toast_log_in_failed);
                }
                userModel.logout();
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
                userModel.logout();
            }
        };

        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideKeyboard(view);
                    String email = editTextEmail.getText().toString().trim();
                    String password = editTextPassword.getText().toString();
                    if (isEmailValid(email)) {
                        if (!password.equals("")) {
                            userModel.login(loginResult, email, password);
                            progressDialog.show(getSupportFragmentManager(), ProgressDialog.TAG);
                        } else {
                            editTextPassword.setError(getString(R.string.error_password));
                        }
                    } else {
                        editTextEmail.setError(getString(R.string.error_email));
                    }
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
