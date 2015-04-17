package de.xikolo.controller.course.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.xikolo.R;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Module;
import de.xikolo.util.DateUtil;
import de.xikolo.util.DisplayUtil;

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

    public void clear() {
        this.mModules.clear();
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
            viewHolder.container = (FrameLayout) rowView.findViewById(R.id.container);
            viewHolder.title = (TextView) rowView.findViewById(R.id.textTitle);
            viewHolder.listView = (AbsListView) rowView.findViewById(R.id.listView);
            viewHolder.progress = (ProgressBar) rowView.findViewById(R.id.containerProgress);
            viewHolder.separator = rowView.findViewById(R.id.separator);
            viewHolder.moduleNotificationContainer = rowView.findViewById(R.id.moduleNotificationContainer);
            viewHolder.moduleNotificationLabel = (TextView) rowView.findViewById(R.id.moduleNotificationLabel);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        final Module module = (Module) getItem(i);

        holder.title.setText(module.name);

        ItemListAdapter itemAdapter = new ItemListAdapter(mActivity, mCourse, module, mItemCallback);
        holder.listView.setAdapter(itemAdapter);

        if ((module.available_from != null && DateUtil.nowIsBefore(module.available_from)) ||
                module.items == null) {
            contentLocked(module, holder);
        } else if (module.items.size() > 0) {
            contentAvailable(module, holder);
            itemAdapter.updateItems(module.items);
        } else {
            contentLocked(module, holder);
        }

        return rowView;
    }

    private void contentAvailable(final Module module, ViewHolder holder) {
        holder.progress.setVisibility(View.GONE);
        holder.moduleNotificationContainer.setVisibility(View.GONE);
        holder.title.setTextColor(mActivity.getResources().getColor(R.color.text_color));
        holder.separator.setBackgroundColor(mActivity.getResources().getColor(R.color.apptheme_main));
        holder.container.setForeground(mActivity.getResources().getDrawable(R.drawable.bg_tabs));
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mModuleCallback.onModuleButtonClicked(mCourse, module);
            }
        });
    }

    private void contentLocked(Module module, ViewHolder holder) {
        holder.progress.setVisibility(View.GONE);
        holder.moduleNotificationContainer.setVisibility(View.VISIBLE);
        holder.title.setTextColor(mActivity.getResources().getColor(R.color.gray_light));
        holder.separator.setBackgroundColor(mActivity.getResources().getColor(R.color.gray_light));
        holder.container.setClickable(false);
        holder.container.setForeground(null);
        if (module.available_from != null && DateUtil.nowIsBefore(module.available_from)) {
            DateFormat dateOut;
            if (DisplayUtil.is7inchTablet(mActivity)) {
                dateOut = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
            } else {
                dateOut = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            }

            Date date = DateUtil.parse(module.available_from);

            holder.moduleNotificationLabel.setText(String.format(mActivity.getString(R.string.available_at),
                    dateOut.format(date)));
        } else {
            holder.moduleNotificationLabel.setText(mActivity.getString(R.string.module_notification_no_content));
        }
    }

    private void contentLoading(Module module, ViewHolder holder) {
        holder.progress.setVisibility(View.VISIBLE);
        holder.moduleNotificationContainer.setVisibility(View.GONE);
        holder.title.setTextColor(mActivity.getResources().getColor(R.color.text_color));
        holder.separator.setBackgroundColor(mActivity.getResources().getColor(R.color.apptheme_main));
        holder.container.setClickable(false);
        holder.container.setForeground(null);
    }

    public interface OnModuleButtonClickListener {

        public void onModuleButtonClicked(Course course, Module module);

    }

    static class ViewHolder {
        FrameLayout container;
        TextView title;
        AbsListView listView;
        ProgressBar progress;
        View separator;

        View moduleNotificationContainer;
        TextView moduleNotificationLabel;
    }

}
