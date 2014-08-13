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
import de.xikolo.entities.Course;
import de.xikolo.entities.Module;

public class ModuleListAdapter extends BaseAdapter {

    public static final String TAG = ModuleListAdapter.class.getSimpleName();

    private List<Module> mModules;

    private Activity mContext;
    private Course mCourse;

    private ItemListAdapter.OnItemButtonClickListener mItemCallback;
    private OnModuleButtonClickListener mModuleCallback;

    public ModuleListAdapter(Activity context, Course course, OnModuleButtonClickListener moduleCallback,
                             ItemListAdapter.OnItemButtonClickListener itemCallback) {
        this.mContext = context;
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
            LayoutInflater inflater = mContext.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_module, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.textTitle);
            viewHolder.listView = (AbsListView) rowView.findViewById(R.id.listView);
            viewHolder.progress = (ProgressBar) rowView.findViewById(R.id.progress);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        final Module module = (Module) getItem(i);

        holder.title.setText(module.name);
        ItemListAdapter itemAdapter = new ItemListAdapter(mContext, mCourse, module, mItemCallback);
        holder.listView.setAdapter(itemAdapter);
        if (module.items != null && module.items.size() > 0) {
            holder.progress.setVisibility(View.GONE);
            itemAdapter.updateItems(module.items);
            holder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mModuleCallback.onModuleButtonClicked(mCourse, module);
                }
            });
        }

        return rowView;
    }

    static class ViewHolder {
        TextView title;
        AbsListView listView;
        ProgressBar progress;
    }

    public interface OnModuleButtonClickListener {

        public void onModuleButtonClicked(Course course, Module module);

    }

}
