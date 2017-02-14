package de.xikolo.managers;

public abstract class NetworkCallback {

    protected void onSuccess() {

    }

    protected void onWarning(WarnCode warnCode) {

    }

    protected void onError(ErrorCode errorCode) {

    }

    public enum ErrorCode {
        NO_NETWORK, NO_RESULT, ERROR, NO_AUTH
    }

    public enum WarnCode {
        NO_NETWORK
    }

}
