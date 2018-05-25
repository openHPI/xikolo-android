package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.models.Course;
import de.xikolo.presenters.base.LoadingStateView;

public interface CertificateListView extends LoadingStateView {

    void showCertificateList(List<Course> coursesWithCertificates);

    void showNoCertificatesMessage();

}
