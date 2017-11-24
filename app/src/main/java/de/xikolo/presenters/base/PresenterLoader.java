package de.xikolo.presenters.base;

import android.content.Context;
import android.support.v4.content.Loader;
import android.util.Log;

import static de.xikolo.config.Config.PRESENTER_LIFECYCLE_LOGGING;

public class PresenterLoader<T extends Presenter> extends Loader<T> {

    public static final String TAG = PresenterLoader.class.getSimpleName();

    private final PresenterFactory<T> factory;
    private final String tag;
    private T presenter;

    public PresenterLoader(Context context, PresenterFactory<T> factory, String tag) {
        super(context);
        this.factory = factory;
        this.tag = tag;
    }

    @Override
    protected void onStartLoading() {
        if (PRESENTER_LIFECYCLE_LOGGING) Log.i(TAG, "onStartLoading-" + tag);

        // if we already own a presenter instance, simply deliver it.
        if (presenter != null) {
            deliverResult(presenter);
            return;
        }

        // Otherwise, force a load
        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        if (PRESENTER_LIFECYCLE_LOGGING) Log.i(TAG, "onForceLoad-" + tag);

        // Create the Presenter using the Factory
        presenter = factory.create();

        // Deliver the result
        deliverResult(presenter);
    }


    @Override
    public void deliverResult(T data) {
        super.deliverResult(data);
        if (PRESENTER_LIFECYCLE_LOGGING) Log.i(TAG, "deliverResult-" + tag);
    }

    @Override
    protected void onStopLoading() {
        if (PRESENTER_LIFECYCLE_LOGGING) Log.i(TAG, "onStopLoading-" + tag);
    }

    @Override
    protected void onReset() {
        if (PRESENTER_LIFECYCLE_LOGGING) Log.i(TAG, "onReset-" + tag);
        if (presenter != null) {
            presenter.onDestroyed();
            presenter = null;
        }
    }

    public T getPresenter() {
        return presenter;
    }

}
