package de.xikolo.presenters.login;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.BuildConfig;
import de.xikolo.config.BuildFlavor;
import de.xikolo.events.LoginEvent;
import de.xikolo.jobs.base.RequestJobCallback;
import de.xikolo.managers.UserManager;
import de.xikolo.presenters.base.Presenter;
import de.xikolo.storages.UserStorage;

public class LoginPresenter extends Presenter<LoginView> {

    public static final String TAG = LoginPresenter.class.getSimpleName();

    private UserManager userManager;

    LoginPresenter() {
        this.userManager = new UserManager();
    }

    @Override
    public void onViewAttached(LoginView view) {
        super.onViewAttached(view);
        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP) {
            view.showSSOView();
        }
    }

    public void login(String email, String password) {
        getViewOrThrow().showProgressDialog();
        userManager.login(email, password, loginCallback());
    }

    public void onSSOClicked() {
        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
            getViewOrThrow().startSSOLogin("/auth/who");
        }
        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP) {
            getViewOrThrow().startSSOLogin("/auth/sap");
        }
    }

    public void externalLoginCallback(String token) {
        getViewOrThrow().showProgressDialog();

        UserStorage userStorage = new UserStorage();
        userStorage.setAccessToken(token);

        userManager.requestUserWithProfile(profileCallback());
    }

    private RequestJobCallback loginCallback() {
        return new RequestJobCallback() {
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

    private RequestJobCallback profileCallback() {
        return new RequestJobCallback() {
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
