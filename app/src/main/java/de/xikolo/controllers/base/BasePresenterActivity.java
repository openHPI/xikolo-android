package de.xikolo.controllers.base;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import de.xikolo.presenters.base.Presenter;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.base.PresenterLoader;
import de.xikolo.presenters.base.View;

import static de.xikolo.config.Config.PRESENTER_LIFECYCLE_LOGGING;

public abstract class BasePresenterActivity<P extends Presenter<V>, V extends View> extends BaseActivity {

    private static final String TAG = BasePresenterActivity.class.getSimpleName();

    private static final int LOADER_ID = 101;

    protected P presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Loader<P> loader = LoaderManager.getInstance(this).getLoader(loaderId());
        if (loader == null) {
            initLoader();
        } else {
            this.presenter = ((PresenterLoader<P>) loader).getPresenter();
            onPresenterCreatedOrRestored(presenter);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        LoaderManager.getInstance(this).destroyLoader(loaderId());
        initLoader();
    }

    private void initLoader() {
        // LoaderCallbacks as an object, so no hint regarding Loader will be leak to the subclasses.
        LoaderManager.getInstance(this).initLoader(loaderId(), null, new LoaderManager.LoaderCallbacks<P>() {
            @NonNull
            @Override
            public final Loader<P> onCreateLoader(int id, Bundle args) {
                if (PRESENTER_LIFECYCLE_LOGGING) Log.i(TAG, "onCreateLoader");
                return new PresenterLoader<>(BasePresenterActivity.this, getPresenterFactory(), tag());
            }

            @Override
            public final void onLoadFinished(@NonNull Loader<P> loader, P presenter) {
                if (PRESENTER_LIFECYCLE_LOGGING) Log.i(TAG, "onLoadFinished");
                BasePresenterActivity.this.presenter = presenter;
                presenter.onViewAttached(getPresenterView());
                onPresenterCreatedOrRestored(presenter);
            }

            @Override
            public final void onLoaderReset(@NonNull Loader<P> loader) {
                if (PRESENTER_LIFECYCLE_LOGGING) Log.i(TAG, "onLoaderReset");
                BasePresenterActivity.this.presenter = null;
                onPresenterDestroyed();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (presenter != null) presenter.onViewAttached(getPresenterView());
    }

    @Override
    protected void onStop() {
        if (presenter != null) presenter.onViewDetached();
        super.onStop();
    }

    /**
     * String tag use for log purposes.
     */
    @NonNull
    protected String tag() {
        return this.getClass().getSimpleName();
    }

    /**
     * Instance of {@link PresenterFactory} use to create a Presenter when needed. This instance should
     * not contain {@link android.app.Activity} context reference since it will be keep on rotations.
     */
    @NonNull
    protected abstract PresenterFactory<P> getPresenterFactory();

    /**
     * Hook for subclasses that deliver the {@link Presenter} when created or restored.
     * Can be use to initialize the Presenter or simple hold a reference to it.
     */
    protected void onPresenterCreatedOrRestored(@NonNull P presenter) {
    }

    /**
     * Hook for subclasses before the screen gets destroyed.
     */
    protected void onPresenterDestroyed() {
    }

    /**
     * Override in case of fragment not implementing Presenter<View> interface
     */
    @NonNull
    protected V getPresenterView() {
        return (V) this;
    }

    /**
     * Use this method in case you want to specify a spefic ID for the {@link PresenterLoader}.
     * By default its value would be {@link #LOADER_ID}.
     */
    protected int loaderId() {
        return LOADER_ID;
    }

}
