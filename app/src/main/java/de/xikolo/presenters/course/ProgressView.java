package de.xikolo.presenters.course;

import java.util.List;

import de.xikolo.models.CourseProgress;
import de.xikolo.models.SectionProgress;
import de.xikolo.presenters.base.LoadingStateView;

public interface ProgressView extends LoadingStateView {

    void setupView(CourseProgress cp, List<SectionProgress> spList);

}
