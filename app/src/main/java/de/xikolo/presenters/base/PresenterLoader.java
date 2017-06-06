package de.xikolo.presenters.base;

import android.content.Context;
import android.support.v4.content.Loader;
import android.util.Log;

public class PresenterLoader<T extends Presenter> extends Loader<T> {

    public static final String TAG = PresenterLoader.class.getSimpleName();

    private final PresenterFactory<T> factory;

    private T presenter;

    public PresenterLoader(Context context, PresenterFactory<T> factory) {
        super(context);
        this.factory = factory;
    }

    @Override
    protected void onStartLoading() {
        Log.i(TAG, "onStartLoading");

        // if we already own a presenter instance, simply deliver it.
        if (presenter != null) {
            deliverResult(presenter);
        } else {
            // Otherwise, force a load
            forceLoad();
        }
    }

    @Override
    protected void onForceLoad() {
        Log.i(TAG, "onForceLoad");

        // Create the Presenter using the Factory
        presenter = factory.create();

        // Deliver the result
        deliverResult(presenter);
    }

    @Override
    public void deliverResult(T data) {
        super.deliverResult(data);
        Log.i(TAG, "deliverResult");
    }

    @Override
    protected void onStopLoading() {
        Log.i(TAG, "onStopLoading");
    }

    @Override
    protected void onReset() {
        Log.i(TAG, "onReset");
        if (presenter != null) {
            presenter.onDestroyed();
            presenter = null;
        }
    }

}
