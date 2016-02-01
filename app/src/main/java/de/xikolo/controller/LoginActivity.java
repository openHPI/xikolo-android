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

    private UserModel mUserModel;
    private Result<Void> mLoginResult;
    private Result<User> mUserResult;

    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mBtnLogin;
    private Button mBtnNew;
    private TextView mTextReset;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActionBar();

        setTitle(getString(R.string.title_section_login));

        mEditEmail = (EditText) findViewById(R.id.editEmail);
        mEditPassword = (EditText) findViewById(R.id.editPassword);
        mBtnLogin = (Button) findViewById(R.id.btnLogin);
        mBtnNew = (Button) findViewById(R.id.btnNew);
        mTextReset = (TextView) findViewById(R.id.textForgotPw);

        dialog = ProgressDialog.getInstance();

        mUserModel = new UserModel(jobManager);
        mLoginResult = new Result<Void>() {
            @Override
            protected void onSuccess(Void result, DataSource dataSource) {
                mUserModel.getUser(mUserResult);
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                dialog.dismiss();
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast(LoginActivity.this);
                } else {
                    ToastUtil.show(LoginActivity.this, R.string.toast_log_in_failed);
                }
                mUserModel.logout();
            }
        };
        mUserResult = new Result<User>() {
            @Override
            protected void onSuccess(User result, DataSource dataSource) {
                EventBus.getDefault().post(new LoginEvent());
                dialog.dismiss();
                LoginActivity.this.finish();
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                dialog.dismiss();
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast(LoginActivity.this);
                } else {
                    ToastUtil.show(LoginActivity.this, R.string.toast_log_in_failed);
                }
                mUserModel.logout();
            }
        };

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                String email = mEditEmail.getText().toString().trim();
                String password = mEditPassword.getText().toString();
                if (isEmailValid(email)) {
                    if (!password.equals("")) {
                        mUserModel.login(mLoginResult, email, password);
                        dialog.show(getSupportFragmentManager(), ProgressDialog.TAG);
                    } else {
                        mEditPassword.setError(getString(R.string.error_password));
                    }
                } else {
                    mEditEmail.setError(getString(R.string.error_email));
                }
            }
        });
        mBtnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                startUrlIntent(Config.URI + Config.ACCOUNT + Config.NEW);
            }
        });
        mTextReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                startUrlIntent(Config.URI + Config.ACCOUNT + Config.RESET);
            }
        });

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
