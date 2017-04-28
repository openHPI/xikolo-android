package de.xikolo.controllers.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import de.xikolo.R;
import de.xikolo.controllers.base.BasePresenterFragment;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.controllers.helper.ImageHelper;
import de.xikolo.presenters.LoginPresenter;
import de.xikolo.presenters.LoginPresenterFactory;
import de.xikolo.presenters.LoginView;
import de.xikolo.presenters.PresenterFactory;
import de.xikolo.utils.Config;
import de.xikolo.utils.ToastUtil;

public class LoginFragment extends BasePresenterFragment<LoginPresenter, LoginView> implements LoginView {

    public static final String TAG = LoginFragment.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar tb;

    @BindView(R.id.editEmail) EditText editTextEmail;
    @BindView(R.id.editPassword) EditText editTextPassword;
    @BindView(R.id.top_image) ImageView topImage;
    @BindView(R.id.text_credentials) TextView textCredentials;

    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(tb);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        textCredentials.setText(String.format(getString(R.string.login_with_credentials), Config.HOST));

        ImageHelper.load(R.drawable.login_header, topImage, 0, false);
    }

    @SuppressWarnings("unused")
    @OnEditorAction
    protected boolean editPasswordAction(TextView v, int actionId) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            login(v);
            handled = true;
        }
        return handled;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btnLogin)
    protected void loginClicked(View view) {
        login(view);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btnNew)
    protected void newAccountClicked(View view) {
        hideKeyboard(view);
        startUrlIntent(Config.URI + Config.ACCOUNT + Config.NEW);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.textForgotPw)
    protected void forgotPasswordClicked(View view) {
        hideKeyboard(view);
        startUrlIntent(Config.URI + Config.ACCOUNT + Config.RESET);
    }

    private void login(View view) {
        hideKeyboard(view);
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        if (isEmailValid(email)) {
            if (!password.equals("")) {
                presenter.login(email, password);
            } else {
                editTextPassword.setError(getString(R.string.error_password));
            }
        } else {
            editTextEmail.setError(getString(R.string.error_email));
        }
    }


    @Override
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.getInstance();
        }
        if (!progressDialog.getDialog().isShowing()) {
            progressDialog.show(getChildFragmentManager(), ProgressDialog.TAG);
        }
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.getDialog().isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showNoNetworkToast() {
        ToastUtil.show(R.string.toast_no_network);
    }

    @Override
    public void showLoginFailedToast() {
        ToastUtil.show(R.string.toast_log_in_failed);
    }

    @Override
    public void finishActivity() {
        getActivity().finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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

    @NonNull
    @Override
    protected PresenterFactory<LoginPresenter> getPresenterFactory() {
        return new LoginPresenterFactory();
    }

}
