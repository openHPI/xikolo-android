package de.xikolo.controllers.course_items;

import android.os.Bundle;

import de.xikolo.controllers.base.BaseFragment;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.ItemDetail;
import de.xikolo.models.Section;

public abstract class PagerFragment<T extends ItemDetail> extends BaseFragment {

    public static final String ARG_COURSE = "arg_course";
    public static final String ARG_MODULE = "arg_module";
    public static final String ARG_ITEM = "arg_item";

    protected Course course;
    protected Section module;
    protected Item<T> item;

    public PagerFragment() {
        // Required empty public constructor
    }

    protected static PagerFragment newInstance(PagerFragment fragment, Course course, Section module, Item item) {
        Bundle args = new Bundle();
//        args.putParcelable(ARG_COURSE, course);
        args.putParcelable(ARG_MODULE, module);
        args.putParcelable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            course = getArguments().getParcelable(ARG_COURSE);
            module = getArguments().getParcelable(ARG_MODULE);
            item = getArguments().getParcelable(ARG_ITEM);
        }
    }

}
