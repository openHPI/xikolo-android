package de.xikolo.controller.course.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.data.entities.Module;

public class ProgressListAdapter extends RecyclerView.Adapter<ProgressListAdapter.ProgressViewHolder> {

    public static final String TAG = ProgressListAdapter.class.getSimpleName();

    private List<Module> mModules;

    private Activity mActivity;

    public ProgressListAdapter(Activity activity) {
        mActivity = activity;
        mModules = new ArrayList<>();
    }

    public void updateModules(List<Module> modules) {
        if (modules == null) {
            throw new NullPointerException("Modules can't be null");
        }
        mModules.clear();
        mModules.addAll(modules);
        addTotalModule();
        notifyDataSetChanged();
    }

    public void clear() {
        mModules.clear();
    }

    private void addTotalModule() {
        int count_visited = 0;
        int count_available = 0;

        float self_tests_points_scored = 0;
        float self_tests_points_possible = 0;

        float assignments_points_scored = 0;
        float assignments_points_possible = 0;

        for (Module module : mModules) {
            count_visited += module.progress.items.count_visited;
            count_available += module.progress.items.count_available;

            self_tests_points_scored += module.progress.self_tests.points_scored;
            self_tests_points_possible += module.progress.self_tests.points_possible;

            assignments_points_scored += module.progress.assignments.points_scored;
            assignments_points_possible += module.progress.assignments.points_possible;
        }
        Module total = new Module();

        total.name = mActivity.getString(R.string.total);

        total.progress.items.count_visited = count_visited;
        total.progress.items.count_available = count_available;

        total.progress.self_tests.points_scored = self_tests_points_scored;
        total.progress.self_tests.points_possible = self_tests_points_possible;

        total.progress.assignments.points_scored = assignments_points_scored;
        total.progress.assignments.points_possible = assignments_points_possible;

        mModules.add(total);
    }

    @Override
    public int getItemCount() {
        return mModules.size();
    }

    @Override
    public ProgressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProgressViewHolder holder, int position) {
        Module module = mModules.get(position);

        holder.title.setText(module.name);

        int percentage;

        holder.count1.setText(String.format(mActivity.getString(R.string.self_test_points),
                module.progress.self_tests.points_scored,
                module.progress.self_tests.points_possible));

        percentage = getPercentage(module.progress.self_tests.points_scored,
                module.progress.self_tests.points_possible);
        holder.percentage1.setText(String.format(mActivity.getString(R.string.percentage), percentage));
        holder.progress1.setProgress(percentage);

        holder.count2.setText(String.format(mActivity.getString(R.string.assignments_points),
                module.progress.assignments.points_scored,
                module.progress.assignments.points_possible));

        percentage = getPercentage(module.progress.assignments.points_scored,
                module.progress.assignments.points_possible);
        holder.percentage2.setText(String.format(mActivity.getString(R.string.percentage), percentage));
        holder.progress2.setProgress(percentage);

        holder.count3.setText(String.format(mActivity.getString(R.string.items_visited),
                module.progress.items.count_visited,
                module.progress.items.count_available));

        percentage = getPercentage(module.progress.items.count_visited,
                module.progress.items.count_available);
        holder.percentage3.setText(String.format(mActivity.getString(R.string.percentage), percentage));
        holder.progress3.setProgress(percentage);

        if (module.name.equals(mActivity.getString(R.string.total))) {
            holder.separator.setVisibility(View.VISIBLE);
        } else {
            holder.separator.setVisibility(View.GONE);
        }
    }

    private int getPercentage(int state, int max) {
        int percentage;
        if (max > 0) {
            percentage = (int) (state / (max / 100.));
        } else {
            percentage = 100;
        }
        return percentage;
    }

    private int getPercentage(float state, float max) {
        int percentage;
        if (max > 0) {
            percentage = (int) (state / (max / 100.));
        } else {
            percentage = 100;
        }
        return percentage;
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView count1;
        TextView count2;
        TextView count3;
        ProgressBar progress1;
        ProgressBar progress2;
        ProgressBar progress3;
        TextView percentage1;
        TextView percentage2;
        TextView percentage3;
        View separator;

        public ProgressViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.textTitle);
            count1 = (TextView) view.findViewById(R.id.textCount1);
            count2 = (TextView) view.findViewById(R.id.textCount2);
            count3 = (TextView) view.findViewById(R.id.textCount3);
            progress1 = (ProgressBar) view.findViewById(R.id.progress1);
            progress2 = (ProgressBar) view.findViewById(R.id.progress2);
            progress3 = (ProgressBar) view.findViewById(R.id.progress3);
            percentage1 = (TextView) view.findViewById(R.id.textPercentage1);
            percentage2 = (TextView) view.findViewById(R.id.textPercentage2);
            percentage3 = (TextView) view.findViewById(R.id.textPercentage3);
            separator = view.findViewById(R.id.viewSeparator);
        }

    }

}
