package de.xikolo.controllers.course;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.yatatsu.autobundle.AutoBundleField;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.xikolo.R;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.models.CourseProgress;
import de.xikolo.models.SectionProgress;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course.ProgressPresenter;
import de.xikolo.presenters.course.ProgressPresenterFactory;
import de.xikolo.presenters.course.ProgressView;
import de.xikolo.views.SpaceItemDecoration;

public class ProgressFragment extends LoadingStatePresenterFragment<ProgressPresenter, ProgressView> implements ProgressView {

    public static final String TAG = ProgressFragment.class.getSimpleName();

    @AutoBundleField String courseId;

    private ProgressListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_progress;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.content_view);

        adapter = new ProgressListAdapter(getActivity());

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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int videoId = item.getItemId();
        switch (videoId) {
            case R.id.action_refresh:
                presenter.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setupView(CourseProgress cp, List<SectionProgress> spList) {
        adapter.update(cp, spList);
    }

    @NonNull
    @Override
    protected PresenterFactory<ProgressPresenter> getPresenterFactory() {
        return new ProgressPresenterFactory(courseId);
    }

}
