package de.xikolo.jobs.base;

import android.os.Handler;
import android.os.Looper;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.events.NetworkStateEvent;

public abstract class JobCallback {

    public enum ErrorCode {
        ERROR, CANCEL, NO_NETWORK, NO_AUTH
    }

    public abstract void onSuccess();

    public abstract void onError(ErrorCode code);

    public final void success() {
        EventBus.getDefault().postSticky(new NetworkStateEvent(true));

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                onSuccess();
            }
        });
    }

    public final void error(final ErrorCode errorCode) {
        if (errorCode == ErrorCode.NO_NETWORK) {
            EventBus.getDefault().postSticky(new NetworkStateEvent(false));
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                onError(errorCode);
            }
        });
    }

}
