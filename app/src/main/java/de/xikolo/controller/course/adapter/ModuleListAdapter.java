package de.xikolo.controller.course.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Module;
import de.xikolo.util.DateUtil;

public class ModuleListAdapter extends BaseAdapter {

    public static final String TAG = ModuleListAdapter.class.getSimpleName();

    private List<Module> mModules;

    private Activity mActivity;
    private Course mCourse;

    private ItemListAdapter.OnItemButtonClickListener mItemCallback;
    private OnModuleButtonClickListener mModuleCallback;

    public ModuleListAdapter(Activity activity, Course course, OnModuleButtonClickListener moduleCallback,
                             ItemListAdapter.OnItemButtonClickListener itemCallback) {
        this.mActivity = activity;
        this.mModules = new ArrayList<Module>();
        this.mCourse = course;
        this.mModuleCallback = moduleCallback;
        this.mItemCallback = itemCallback;
    }

    public void updateModules(List<Module> modules) {
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
            LayoutInflater inflater = mActivity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_module, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.textTitle);
            viewHolder.listView = (AbsListView) rowView.findViewById(R.id.listView);
            viewHolder.progress = (ProgressBar) rowView.findViewById(R.id.containerProgress);
            viewHolder.separator = rowView.findViewById(R.id.separator);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        final Module module = (Module) getItem(i);

        holder.title.setText(module.name);

        ItemListAdapter itemAdapter = new ItemListAdapter(mActivity, mCourse, module, mItemCallback);
        holder.listView.setAdapter(itemAdapter);
        if (module.items != null && module.items.size() > 0) {
            holder.progress.setVisibility(View.GONE);
            itemAdapter.updateItems(module.items);

            if (!DateUtil.nowIsBetween(module.available_from, module.available_to)) {
                holder.title.setTextColor(mActivity.getResources().getColor(R.color.gray_light));
                holder.separator.setBackgroundColor(mActivity.getResources().getColor(R.color.gray_light));
                holder.title.setClickable(false);
            } else {
                holder.title.setTextColor(mActivity.getResources().getColor(R.color.text_color));
                holder.separator.setBackgroundColor(mActivity.getResources().getColor(R.color.apptheme_main));
                holder.title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mModuleCallback.onModuleButtonClicked(mCourse, module);
                    }
                });
            }
        } else {
            holder.progress.setVisibility(View.VISIBLE);
        }

        return rowView;
    }

    public interface OnModuleButtonClickListener {

        public void onModuleButtonClicked(Course course, Module module);

    }

    static class ViewHolder {
        TextView title;
        AbsListView listView;
        ProgressBar progress;
        View separator;
    }

}
