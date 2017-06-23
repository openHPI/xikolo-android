package de.xikolo.jobs.base;

public interface JobCallback {

    enum ErrorCode {
        ERROR, CANCEL, NO_NETWORK, NO_AUTH
    }

    void onSuccess();

    void onError(ErrorCode code);

}
