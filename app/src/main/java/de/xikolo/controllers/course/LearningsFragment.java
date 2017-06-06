package de.xikolo.controllers.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yatatsu.autobundle.AutoBundleField;

import java.util.List;

import de.xikolo.R;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.controllers.helper.SectionDownloadHelper;
import de.xikolo.models.Course;
import de.xikolo.models.Section;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course.LearningsPresenter;
import de.xikolo.presenters.course.LearningsPresenterFactory;
import de.xikolo.presenters.course.LearningsView;
import de.xikolo.views.SpaceItemDecoration;

public class LearningsFragment extends LoadingStatePresenterFragment<LearningsPresenter, LearningsView> implements LearningsView, SectionListAdapter.OnSectionClickListener, ItemListAdapter.OnItemClickListener {

    public final static String TAG = LearningsFragment.class.getSimpleName();

    @AutoBundleField(required = false) String courseId;

    private SectionListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        presenter.onCreate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learnings, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        adapter = new SectionListAdapter(getActivity(), this, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new SpaceItemDecoration(
                0,
                getActivity().getResources().getDimensionPixelSize(R.dimen.card_vertical_margin),
                false,
                new SpaceItemDecoration.RecyclerViewInfo() {
                    @Override
                    public boolean isHeader(int position) {
                        return false;
                    }

                    @Override
                    public int getSpanCount() {
                        return 1;
                    }

                    @Override
                    public int getItemCount() {
                        return adapter.getItemCount();
                    }
                }
        ));

        return view;
    }

    @Override
    public void onSectionClicked(String sectionId) {
        presenter.onSectionClicked(sectionId);
    }

    @Override
    public void onSectionDownloadClicked(String sectionId) {
        presenter.onSectionDownloadClicked(sectionId);
    }

    @Override
    public void onItemClicked(String sectionId, String itemId) {
        presenter.onItemClicked(sectionId, itemId);
    }

    @Override
    public void startCourseItemsActivity(String courseId, String sectionId, String itemId) {
        Intent intent = CourseItemsActivityAutoBundle.builder(courseId, sectionId).itemId(itemId).build(this);
        getActivity().startActivity(intent);
    }

    @Override
    public void startSectionDownload(Course course, Section section) {
        SectionDownloadHelper sectionDownloadHelper = new SectionDownloadHelper(getActivity());
        sectionDownloadHelper.initSectionDownloads(course, section);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(String title) {
        getActivity().setTitle(title);
    }

    @Override
    public void setupSections(List<Section> sectionList) {
        adapter.updateModules(sectionList);
    }

    @NonNull
    @Override
    protected PresenterFactory<LearningsPresenter> getPresenterFactory() {
        return new LearningsPresenterFactory(courseId);
    }

}
