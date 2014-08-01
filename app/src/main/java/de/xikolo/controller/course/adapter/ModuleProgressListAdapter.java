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
        this.mContext = context;
        this.mModules = new ArrayList<Module>();
    }

    public void updateModules(List<Module> modules) {
        if (modules == null)
            throw new NullPointerException("Modules can't be null");
        this.mModules = modules;
        this.notifyDataSetChanged();
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
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        Module module = (Module) getItem(i);

        holder.title.setText(module.name);

        holder.count1.setText(module.progress.self_tests.points_scored + " " +
                mContext.getString(R.string.of) + " " + module.progress.self_tests.points_possible + " " +
                mContext.getString(R.string.self_tests) + " " + mContext.getString(R.string.points));

        holder.count2.setText(module.progress.assignments.points_scored + " " +
                mContext.getString(R.string.of) + " " + module.progress.assignments.points_possible + " " +
                mContext.getString(R.string.assignments) + " " + mContext.getString(R.string.points));

        holder.count3.setText(module.progress.items.count_visited + " " +
                mContext.getString(R.string.of) + " " + module.progress.items.count_available + " " +
                mContext.getString(R.string.visited));

        return rowView;
    }

    static class ViewHolder {
        TextView title;
        TextView count1;
        TextView count2;
        TextView count3;
    }

}
