package de.xikolo.presenters.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Presenter<V extends View> {

    private V view;

    public void onViewAttached(V view) {
        this.view = view;
    }

    public void onViewDetached() {
        this.view = null;
    }

    public void onDestroyed() {

    }

    public boolean isViewAttached() {
        return getView() != null;
    }

    @Nullable
    public V getView() {
        return view;
    }

    @NonNull
    public V getViewOrThrow() {
        final V view = getView();
        if (view == null) {
            throw new IllegalStateException("view not attached");
        }
        return view;
    }

}
