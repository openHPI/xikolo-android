package de.xikolo.controllers.base;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import de.xikolo.presenters.base.Presenter;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.base.PresenterLoader;
import de.xikolo.presenters.base.View;

import static de.xikolo.config.Config.PRESENTER_LIFECYCLE_LOGGING;

public abstract class BasePresenterFragment<P extends Presenter<V>, V extends View> extends BaseFragment implements View {

    private static final String TAG = BasePresenterFragment.class.getSimpleName();

    private static final int LOADER_ID = 101;

    protected P presenter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // LoaderCallbacks as an object, so no hint regarding loader will be leak to the subclasses.
        getLoaderManager().initLoader(loaderId(), null, new LoaderManager.LoaderCallbacks<P>() {
            @Override
            public final Loader<P> onCreateLoader(int id, Bundle args) {
                if (PRESENTER_LIFECYCLE_LOGGING) Log.i(TAG, "onCreateLoader");
                return new PresenterLoader<>(getContext(), getPresenterFactory());
            }

            @Override
            public final void onLoadFinished(Loader<P> loader, P presenter) {
                if (PRESENTER_LIFECYCLE_LOGGING) Log.i(TAG, "onLoadFinished");
                BasePresenterFragment.this.presenter = presenter;
                onPresenterPrepared(presenter);
            }

            @Override
            public final void onLoaderReset(Loader<P> loader) {
                if (PRESENTER_LIFECYCLE_LOGGING) Log.i(TAG, "onLoaderReset");
                BasePresenterFragment.this.presenter = null;
                onPresenterDestroyed();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onViewAttached(getPresenterView());
    }

    @Override
    public void onPause() {
        presenter.onViewDetached();
        super.onPause();
    }

    @Override
    public void setTitle(CharSequence title) {
        getActivity().setTitle(title);
    }

    /**
     * Instance of {@link PresenterFactory} use to create a Presenter when needed. This instance should
     * not contain {@link android.app.Activity} context reference since it will be keep on rotations.
     */
    @NonNull
    protected abstract PresenterFactory<P> getPresenterFactory();

    /**
     * Hook for subclasses that deliver the {@link Presenter} before its View is attached.
     * Can be use to initialize the Presenter or simple hold a reference to it.
     */
    protected void onPresenterPrepared(@NonNull P presenter) {
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
