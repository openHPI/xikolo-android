package de.xikolo.model;

import android.os.Handler;
import android.os.Looper;

public abstract class Result<T> {

    public enum ErrorCode {
        NO_NETWORK, NO_RESULT, ERROR, NO_AUTH
    }

    public enum DataSource {
        NETWORK, LOCAL
    }

    public enum WarnCode {
        NO_NETWORK
    }

    protected void onSuccess(T result, DataSource dataSource) {

    }

    protected void onWarning(WarnCode warnCode) {

    }

    protected void onError(ErrorCode errorCode) {

    }

    public final void success(final T result, final DataSource dataSource) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                onSuccess(result, dataSource);
            }
        });
    }

    public final void warn(final WarnCode warnCode) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                onWarning(warnCode);
            }
        });
    }

    public final void error(final ErrorCode errorCode) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                onError(errorCode);
            }
        });
    }

}
