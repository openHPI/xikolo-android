package de.xikolo.controllers.login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.BasePresenterFragment;
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminate;
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminateAutoBundle;
import de.xikolo.managers.UserManager;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.login.LoginPresenter;
import de.xikolo.presenters.login.LoginPresenterFactory;
import de.xikolo.presenters.login.LoginView;
import de.xikolo.utils.ToastUtil;

public class LoginFragment extends BasePresenterFragment<LoginPresenter, LoginView> implements LoginView {

    public static final String TAG = LoginFragment.class.getSimpleName();

    @AutoBundleField(required = false) String token;

    @BindView(R.id.toolbar) Toolbar tb;

    @BindView(R.id.editEmail) TextInputEditText editTextEmail;
    @BindView(R.id.editPassword) TextInputEditText editTextPassword;
    @BindView(R.id.top_image) ImageView topImage;
    @BindView(R.id.text_credentials) TextView textCredentials;

    @BindView(R.id.btnSSO) Button buttonSSO;
    @BindView(R.id.ssoContainer) View containerSSO;

    private ProgressDialogIndeterminate progressDialog;

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

        GlideApp.with(this)
                .load(R.drawable.login_header_v2)
                .dontAnimate()
                .noPlaceholders()
                .fitCenter()
                .into(topImage);
    }

    @Override
    protected void onPresenterCreatedOrRestored(@NonNull LoginPresenter presenter) {
        if (token != null) {
            presenter.externalLoginCallback(token);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (UserManager.isAuthorized()) {
            getActivity().finish();
        }
    }

    @SuppressWarnings("unused")
    @OnEditorAction(R.id.editPassword)
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
        startUrlIntent(Config.HOST_URL + Config.ACCOUNT + Config.NEW);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.textForgotPw)
    protected void forgotPasswordClicked(View view) {
        hideKeyboard(view);
        startUrlIntent(Config.HOST_URL + Config.ACCOUNT + Config.RESET);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btnSSO)
    protected void ssoClicked(View view) {
        hideKeyboard(view);
        presenter.onSSOClicked();
    }

    @Override
    public void showSSOView() {
        containerSSO.setVisibility(View.VISIBLE);
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
    public void startSSOLogin(String strategy) {
        Intent intent = SsoLoginActivityAutoBundle.builder(
                Config.HOST_URL + "?in_app=true&redirect_to=" + strategy,
                getString(R.string.login_sso)
        ).build(getActivity());
        startActivity(intent);
    }

    @Override
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialogIndeterminateAutoBundle.builder().build();
        }
        progressDialog.show(getChildFragmentManager(), ProgressDialogIndeterminate.TAG);
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null) {
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
