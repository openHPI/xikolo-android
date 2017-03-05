package de.xikolo.managers.jobs;

public interface JobCallback {

    enum ErrorCode {
        ERROR, CANCEL, NO_NETWORK, NO_AUTH
    }

    void onSuccess();

    void onError(ErrorCode code);

}
