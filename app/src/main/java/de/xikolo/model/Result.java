package de.xikolo.model;

import android.os.Handler;
import android.os.Looper;

import de.greenrobot.event.EventBus;
import de.xikolo.model.events.NetworkStateEvent;

public abstract class Result<T> {

    private ResultFilter resultFilter;

    public void setResultFilter(ResultFilter resultFilter) {
        this.resultFilter = resultFilter;
    }

    protected void onSuccess(T result, DataSource dataSource) {

    }

    protected void onWarning(WarnCode warnCode) {

    }

    protected void onError(ErrorCode errorCode) {

    }

    public final void success(final T result, final DataSource dataSource) {
        if (dataSource == DataSource.NETWORK) {
            EventBus.getDefault().postSticky(new NetworkStateEvent(true));
        }

        final T filteredResult;
        if (resultFilter != null) {
            filteredResult = resultFilter.onFilter(result, dataSource);
        } else {
            filteredResult = result;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                onSuccess(filteredResult, dataSource);
            }
        });
    }

    public final void warn(final WarnCode warnCode) {
        if (warnCode == WarnCode.NO_NETWORK) {
            EventBus.getDefault().postSticky(new NetworkStateEvent(false));
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                onWarning(warnCode);
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

    public enum ErrorCode {
        NO_NETWORK, NO_RESULT, ERROR, NO_AUTH
    }

    public enum DataSource {
        NETWORK, LOCAL
    }

    public enum WarnCode {
        NO_NETWORK
    }

    public abstract class ResultFilter {

        public abstract T onFilter(T result, DataSource dataSource);

    }

}
