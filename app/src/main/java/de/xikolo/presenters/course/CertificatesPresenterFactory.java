package de.xikolo.presenters.course;

import de.xikolo.presenters.base.PresenterFactory;

public class CertificatesPresenterFactory implements PresenterFactory<CertificatesPresenter> {

    private final String courseId;

    public CertificatesPresenterFactory(String courseId) {
        this.courseId = courseId;
    }

    @Override
    public CertificatesPresenter create() {
        return new CertificatesPresenter(courseId);
    }

}
