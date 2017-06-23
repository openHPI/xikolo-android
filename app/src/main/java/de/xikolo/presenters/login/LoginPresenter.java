package de.xikolo.presenters.login;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.events.LoginEvent;
import de.xikolo.managers.UserManager;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.presenters.base.Presenter;

public class LoginPresenter implements Presenter<LoginView> {

    public static final String TAG = LoginPresenter.class.getSimpleName();

    private LoginView view;

    private UserManager userManager;

    LoginPresenter() {
        this.userManager = new UserManager();
    }

    @Override
    public void onViewAttached(LoginView v) {
        this.view = v;
    }

    @Override
    public void onViewDetached() {
        this.view = null;
    }

    @Override
    public void onDestroyed() {
    }

    public void login(String email, String password) {
        view.showProgressDialog();
        userManager.login(new JobCallback() {
            @Override
            public void onSuccess() {
                userManager.requestProfile(new JobCallback() {
                    @Override
                    public void onSuccess() {
                        view.hideProgressDialog();
                        EventBus.getDefault().post(new LoginEvent());
                        view.finishActivity();
                    }

                    @Override
                    public void onError(ErrorCode code) {
                        userManager.logout();
                        view.hideProgressDialog();
                        if (code == ErrorCode.NO_NETWORK) {
                            view.showNoNetworkToast();
                        } else {
                            view.showLoginFailedToast();
                        }
                    }
                });
            }

            @Override
            public void onError(ErrorCode code) {
                userManager.logout();
                view.hideProgressDialog();
                if (code == ErrorCode.NO_NETWORK) {
                    view.showNoNetworkToast();
                } else {
                    view.showLoginFailedToast();
                }
            }
        }, email, password);
    }

}
