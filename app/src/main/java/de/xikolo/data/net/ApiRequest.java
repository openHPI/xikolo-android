package de.xikolo.data.net;

import android.content.Context;

import de.xikolo.GlobalApplication;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;

public class ApiRequest extends NetworkRequest {

    public ApiRequest(String url) {
        super(url);
        addDefaultHeader();
        authorize();
    }

    private void addDefaultHeader() {
        builder.addHeader(Config.HEADER_ACCEPT, Config.HEADER_ACCEPT_VALUE)
                .addHeader(Config.HEADER_USER_PLATFORM, Config.HEADER_USER_PLATFORM_VALUE);
    }

    private boolean authorize() {
        Context context = GlobalApplication.getInstance();
        if (UserModel.isLoggedIn(context)) {
            builder.addHeader(Config.HEADER_AUTHORIZATION, Config.HEADER_AUTHORIZATION_PREFIX + UserModel.getToken(context));
            return true;
        } else {
            return false;
        }
    }

}
