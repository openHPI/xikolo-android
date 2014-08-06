package de.xikolo.controller.course.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.model.Module;

public class ModuleProgressListAdapter extends BaseAdapter {

    public static final String TAG = ModuleProgressListAdapter.class.getSimpleName();

    private List<Module> mModules;

    private Activity mContext;

    public ModuleProgressListAdapter(Activity context) {
        mContext = context;
        mModules = new ArrayList<Module>();
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

    private void addTotalModule() {
        int count_visited = 0;
        int count_available = 0;

        int self_tests_points_scored = 0;
        int self_tests_points_possible = 0;

        int assignments_points_scored = 0;
        int assignments_points_possible = 0;

        for (Module module : mModules) {
            count_visited += module.progress.items.count_visited;
            count_available += module.progress.items.count_available;

            self_tests_points_scored += module.progress.self_tests.points_scored;
            self_tests_points_possible += module.progress.self_tests.points_possible;

            assignments_points_scored += module.progress.assignments.points_scored;
            assignments_points_possible += module.progress.assignments.points_possible;
        }
        Module total = new Module();

        total.name = mContext.getString(R.string.total);

        total.progress.items.count_visited = count_visited;
        total.progress.items.count_available = count_available;

        total.progress.self_tests.points_scored = self_tests_points_scored;
        total.progress.self_tests.points_possible = self_tests_points_possible;

        total.progress.assignments.points_scored = assignments_points_scored;
        total.progress.assignments.points_possible = assignments_points_possible;

        mModules.add(total);
    }

    @Override
    public int getCount() {
        return mModules.size();
    }

    @Override
    public Object getItem(int i) {
        return mModules.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = view;
        if (rowView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_module_progress, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.textTitle);
            viewHolder.count1 = (TextView) rowView.findViewById(R.id.textCount1);
            viewHolder.count2 = (TextView) rowView.findViewById(R.id.textCount2);
            viewHolder.count3 = (TextView) rowView.findViewById(R.id.textCount3);
            viewHolder.progress1 = (ProgressBar) rowView.findViewById(R.id.progress1);
            viewHolder.progress2 = (ProgressBar) rowView.findViewById(R.id.progress2);
            viewHolder.progress3 = (ProgressBar) rowView.findViewById(R.id.progress3);
            viewHolder.percentage1 = (TextView) rowView.findViewById(R.id.textPercentage1);
            viewHolder.percentage2 = (TextView) rowView.findViewById(R.id.textPercentage2);
            viewHolder.percentage3 = (TextView) rowView.findViewById(R.id.textPercentage3);
            viewHolder.separator = rowView.findViewById(R.id.viewSeparator);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        Module module = (Module) getItem(i);

        holder.title.setText(module.name);

        int percentage;

        holder.count1.setText(module.progress.self_tests.points_scored + " " +
                mContext.getString(R.string.of) + " " + module.progress.self_tests.points_possible + " " +
                mContext.getString(R.string.self_tests) + " " + mContext.getString(R.string.points));
        percentage = getPercentage(module.progress.self_tests.points_scored,
                module.progress.self_tests.points_possible);
        holder.percentage1.setText(percentage + "%");
        holder.progress1.setProgress(percentage);

        holder.count2.setText(module.progress.assignments.points_scored + " " +
                mContext.getString(R.string.of) + " " + module.progress.assignments.points_possible + " " +
                mContext.getString(R.string.assignments) + " " + mContext.getString(R.string.points));
        percentage = getPercentage(module.progress.assignments.points_scored,
                module.progress.assignments.points_possible);
        holder.percentage2.setText(percentage + "%");
        holder.progress2.setProgress(percentage);

        holder.count3.setText(module.progress.items.count_visited + " " +
                mContext.getString(R.string.of) + " " + module.progress.items.count_available + " " +
                mContext.getString(R.string.visited));
        percentage = getPercentage(module.progress.items.count_visited,
                module.progress.items.count_available);
        holder.percentage3.setText(percentage + "%");
        holder.progress3.setProgress(percentage);

        if (module.name.equals(mContext.getString(R.string.total))) {
            holder.separator.setVisibility(View.VISIBLE);
        } else {
            holder.separator.setVisibility(View.GONE);
        }

        return rowView;
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

    static class ViewHolder {
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
    }

}
