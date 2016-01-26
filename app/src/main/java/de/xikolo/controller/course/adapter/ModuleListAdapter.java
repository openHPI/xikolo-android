package de.xikolo.controller.course.adapter;

import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.xikolo.R;
import de.xikolo.controller.helper.ModuleDownloadController;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Module;
import de.xikolo.util.DateUtil;
import de.xikolo.util.DisplayUtil;
import de.xikolo.view.NonScrollableGridView;

public class ModuleListAdapter extends RecyclerView.Adapter<ModuleListAdapter.ModuleViewHolder> {

    public static final String TAG = ModuleListAdapter.class.getSimpleName();

    private List<Module> mModules;

    private FragmentActivity mActivity;
    private Course mCourse;

    private ItemListAdapter.OnItemButtonClickListener mItemCallback;
    private OnModuleButtonClickListener mModuleCallback;

    public ModuleListAdapter(FragmentActivity activity, Course course, OnModuleButtonClickListener moduleCallback,
                             ItemListAdapter.OnItemButtonClickListener itemCallback) {
        this.mActivity = activity;
        this.mModules = new ArrayList<>();
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
    public int getItemCount() {
        return mModules.size();
    }

    @Override
    public ModuleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ModuleViewHolder holder, int position) {
        final Module module = mModules.get(position);

        holder.title.setText(module.name);

        ItemListAdapter itemAdapter = new ItemListAdapter(mActivity, mCourse, module, mItemCallback);
        holder.listView.setAdapter(itemAdapter);
        ViewCompat.setNestedScrollingEnabled(holder.listView, false);

        if ((module.available_from != null && DateUtil.nowIsBefore(module.available_from)) ||
                module.items == null) {
            contentLocked(module, holder);
        } else if (module.items.size() > 0) {
            contentAvailable(module, holder);
            itemAdapter.updateItems(module.items);
        } else {
            contentLocked(module, holder);
        }
    }

    private void contentAvailable(final Module module, ModuleViewHolder holder) {
        holder.progress.setVisibility(View.GONE);
        holder.moduleNotificationContainer.setVisibility(View.GONE);
        holder.title.setTextColor(ContextCompat.getColor(mActivity, R.color.text_main));
        holder.separator.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.apptheme_main));

        TypedValue outValue = new TypedValue();
        mActivity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        holder.container.setForeground(ContextCompat.getDrawable(mActivity, outValue.resourceId));
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mModuleCallback.onModuleButtonClicked(mCourse, module);
            }
        });
        holder.download.setVisibility(View.VISIBLE);
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModuleDownloadController moduleDownloadController = new ModuleDownloadController(mActivity);
                moduleDownloadController.initModuleDownloads(mCourse, module);
            }
        });
    }

    private void contentLocked(Module module, ModuleViewHolder holder) {
        holder.progress.setVisibility(View.GONE);
        holder.moduleNotificationContainer.setVisibility(View.VISIBLE);
        holder.title.setTextColor(ContextCompat.getColor(mActivity, R.color.text_light));
        holder.separator.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.text_light));
        holder.container.setClickable(false);
        holder.container.setForeground(null);
        holder.download.setVisibility(View.GONE);
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

    public interface OnModuleButtonClickListener {

        void onModuleButtonClicked(Course course, Module module);

    }

    static class ModuleViewHolder extends RecyclerView.ViewHolder {

        FrameLayout container;
        TextView title;
        NonScrollableGridView listView;
        ProgressBar progress;
        View separator;

        View moduleNotificationContainer;
        TextView moduleNotificationLabel;

        View download;

        public ModuleViewHolder(View view) {
            super(view);

            container = (FrameLayout) view.findViewById(R.id.container);
            title = (TextView) view.findViewById(R.id.textTitle);
            listView = (NonScrollableGridView) view.findViewById(R.id.listView);
            progress = (ProgressBar) view.findViewById(R.id.containerProgress);
            separator = view.findViewById(R.id.separator);
            moduleNotificationContainer = view.findViewById(R.id.moduleNotificationContainer);
            moduleNotificationLabel = (TextView) view.findViewById(R.id.moduleNotificationLabel);
            download = view.findViewById(R.id.downloadBtn);
        }

    }

}
