package de.xikolo.controllers.course.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.models.Section;

public class ProgressListAdapter extends RecyclerView.Adapter<ProgressListAdapter.ProgressViewHolder> {

    public static final String TAG = ProgressListAdapter.class.getSimpleName();

    private List<Section> modules;

    private Context context;

    public ProgressListAdapter(Context context) {
        this.context = context;
        this.modules = new ArrayList<>();
    }

    public void updateModules(List<Section> modules) {
        if (modules == null) {
            throw new NullPointerException("Modules can't be null");
        }
        this.modules.clear();
        this.modules.addAll(modules);
        addTotalModule();
        notifyDataSetChanged();
    }

    public void clear() {
        modules.clear();
    }

    private void addTotalModule() {
        int count_visited = 0;
        int count_available = 0;

        float self_tests_points_scored = 0;
        float self_tests_points_possible = 0;

        float assignments_points_scored = 0;
        float assignments_points_possible = 0;

        for (Section module : modules) {
            count_visited += module.progress.items.count_visited;
            count_available += module.progress.items.count_available;

            self_tests_points_scored += module.progress.self_tests.points_scored;
            self_tests_points_possible += module.progress.self_tests.points_possible;

            assignments_points_scored += module.progress.assignments.points_scored;
            assignments_points_possible += module.progress.assignments.points_possible;
        }
        Section total = new Section();

        total.name = context.getString(R.string.total);

        total.progress.items.count_visited = count_visited;
        total.progress.items.count_available = count_available;

        total.progress.self_tests.points_scored = self_tests_points_scored;
        total.progress.self_tests.points_possible = self_tests_points_possible;

        total.progress.assignments.points_scored = assignments_points_scored;
        total.progress.assignments.points_possible = assignments_points_possible;

        modules.add(total);
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    @Override
    public ProgressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProgressViewHolder holder, int position) {
        Section module = modules.get(position);

        holder.textTitle.setText(module.name);

        int percentage;

        holder.textCount1.setText(String.format(context.getString(R.string.self_test_points),
                module.progress.self_tests.points_scored,
                module.progress.self_tests.points_possible));

        percentage = getPercentage(module.progress.self_tests.points_scored,
                module.progress.self_tests.points_possible);
        holder.textPercentage1.setText(String.format(context.getString(R.string.percentage), percentage));
        holder.progressBar1.setProgress(percentage);

        holder.textCount2.setText(String.format(context.getString(R.string.assignments_points),
                module.progress.assignments.points_scored,
                module.progress.assignments.points_possible));

        percentage = getPercentage(module.progress.assignments.points_scored,
                module.progress.assignments.points_possible);
        holder.textPercentage2.setText(String.format(context.getString(R.string.percentage), percentage));
        holder.progressBar2.setProgress(percentage);

        holder.textCount3.setText(String.format(context.getString(R.string.items_visited),
                module.progress.items.count_visited,
                module.progress.items.count_available));

        percentage = getPercentage(module.progress.items.count_visited,
                module.progress.items.count_available);
        holder.textPercentage3.setText(String.format(context.getString(R.string.percentage), percentage));
        holder.progressBar3.setProgress(percentage);

        if (module.name.equals(context.getString(R.string.total))) {
            holder.viewSeparator.setVisibility(View.VISIBLE);
        } else {
            holder.viewSeparator.setVisibility(View.GONE);
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

        TextView textTitle;
        TextView textCount1;
        TextView textCount2;
        TextView textCount3;
        ProgressBar progressBar1;
        ProgressBar progressBar2;
        ProgressBar progressBar3;
        TextView textPercentage1;
        TextView textPercentage2;
        TextView textPercentage3;
        View viewSeparator;

        public ProgressViewHolder(View view) {
            super(view);

            textTitle = (TextView) view.findViewById(R.id.textTitle);
            textCount1 = (TextView) view.findViewById(R.id.textCount1);
            textCount2 = (TextView) view.findViewById(R.id.textCount2);
            textCount3 = (TextView) view.findViewById(R.id.textCount3);
            progressBar1 = (ProgressBar) view.findViewById(R.id.progress1);
            progressBar2 = (ProgressBar) view.findViewById(R.id.progress2);
            progressBar3 = (ProgressBar) view.findViewById(R.id.progress3);
            textPercentage1 = (TextView) view.findViewById(R.id.textPercentage1);
            textPercentage2 = (TextView) view.findViewById(R.id.textPercentage2);
            textPercentage3 = (TextView) view.findViewById(R.id.textPercentage3);
            viewSeparator = view.findViewById(R.id.viewSeparator);
        }

    }

}
