package de.xikolo.presenters.course;

import android.support.annotation.Nullable;

import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.presenters.base.LoadingStateView;

public interface CertificatesView extends LoadingStateView {

    void showCertificates(Course course, @Nullable Enrollment enrollment);

}
