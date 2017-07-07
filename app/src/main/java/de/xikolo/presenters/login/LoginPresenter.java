package de.xikolo.presenters.login;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.events.LoginEvent;
import de.xikolo.managers.UserManager;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.presenters.base.Presenter;

public class LoginPresenter extends Presenter<LoginView> {

    public static final String TAG = LoginPresenter.class.getSimpleName();

    private UserManager userManager;

    LoginPresenter() {
        this.userManager = new UserManager();
    }

    public void login(String email, String password) {
        getViewOrThrow().showProgressDialog();
        userManager.login(loginCallback(), email, password);
    }

    private JobCallback loginCallback() {
        return new JobCallback() {
            @Override
            public void onSuccess() {
                userManager.requestUserWithProfile(profileCallback());
            }

            @Override
            public void onError(ErrorCode code) {
                UserManager.logout();
                if (getView() != null) {
                    getView().hideProgressDialog();
                    if (code == ErrorCode.NO_NETWORK) {
                        getView().showNoNetworkToast();
                    } else {
                        getView().showLoginFailedToast();
                    }
                }
            }
        };
    }

    private JobCallback profileCallback() {
        return new JobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgressDialog();
                    getView().finishActivity();
                }
                EventBus.getDefault().post(new LoginEvent());
            }

            @Override
            public void onError(ErrorCode code) {
                UserManager.logout();
                if (getView() != null) {
                    getView().hideProgressDialog();
                    if (code == ErrorCode.NO_NETWORK) {
                        getView().showNoNetworkToast();
                    } else {
                        getView().showLoginFailedToast();
                    }
                }
            }
        };
    }

}
