package de.xikolo.presenters.course;

import android.arch.lifecycle.LifecycleOwner;

import de.xikolo.presenters.base.PresenterFactory;

public class CoursePresenterFactory implements PresenterFactory<CoursePresenter> {

    private LifecycleOwner lifecycleOwner;

    public CoursePresenterFactory(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public CoursePresenter create() {
        return new CoursePresenter(lifecycleOwner);
    }

}
