package de.xikolo.presenters.course;

import java.util.List;

import de.xikolo.models.Course;
import de.xikolo.models.DownloadAsset;
import de.xikolo.presenters.base.LoadingStateView;

public interface CertificatesView extends LoadingStateView {

    void showCertificates(Course course, List<DownloadAsset.Course.Certificate> certificates);

}
