package de.xikolo.presenters.section;

import java.util.List;

import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.presenters.base.View;

public interface CourseItemsView extends View {

    void setupView(List<Item> itemList);

    void setCurrentItem(int index);

    void startSectionDownload(Course course, Section section);

}
