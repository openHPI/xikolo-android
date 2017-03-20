package de.xikolo.controllers.course.adapter;

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
import de.xikolo.controllers.helper.ModuleDownloadController;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.DisplayUtil;
import de.xikolo.views.AutofitRecyclerView;
import de.xikolo.views.SpaceItemDecoration;

public class ModuleListAdapter extends RecyclerView.Adapter<ModuleListAdapter.ModuleViewHolder> {

    public static final String TAG = ModuleListAdapter.class.getSimpleName();

    private List<Section> modules;

    private FragmentActivity activity;
    private Course course;

    private ItemListAdapter.OnItemButtonClickListener itemButtonClickListener;
    private OnModuleButtonClickListener moduleButtonClickListener;

    public ModuleListAdapter(FragmentActivity activity, Course course, OnModuleButtonClickListener moduleCallback,
                             ItemListAdapter.OnItemButtonClickListener itemCallback) {
        this.activity = activity;
        this.modules = new ArrayList<>();
        this.course = course;
        this.moduleButtonClickListener = moduleCallback;
        this.itemButtonClickListener = itemCallback;
    }

    public void updateModules(List<Section> modules) {
        this.modules = modules;
        this.notifyDataSetChanged();
    }

    public void clear() {
        this.modules.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    @Override
    public ModuleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ModuleViewHolder holder, int position) {
        final Section module = modules.get(position);

        holder.textTitle.setText(module.name);

        final ItemListAdapter itemAdapter = new ItemListAdapter(activity, course, module, itemButtonClickListener);
        holder.recyclerView.setAdapter(itemAdapter);
        holder.recyclerView.clearItemDecorations();
        holder.recyclerView.addItemDecoration(new SpaceItemDecoration(
                activity.getResources().getDimensionPixelSize(R.dimen.card_horizontal_margin) / 2,
                activity.getResources().getDimensionPixelSize(R.dimen.card_vertical_margin) / 2,
                false,
                new SpaceItemDecoration.RecyclerViewInfo() {
                    @Override
                    public boolean isHeader(int position) {
                        return false;
                    }

                    @Override
                    public int getSpanCount() {
                        return holder.recyclerView.getSpanCount();
                    }

                    @Override
                    public int getItemCount() {
                        return itemAdapter.getItemCount();
                    }
                }
        ));
        ViewCompat.setNestedScrollingEnabled(holder.recyclerView, false);

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

    private void contentAvailable(final Section module, ModuleViewHolder holder) {
        holder.progressBar.setVisibility(View.GONE);
        holder.viewModuleNotification.setVisibility(View.GONE);
        holder.viewHeader.setBackgroundColor(ContextCompat.getColor(activity, R.color.apptheme_section_header_bg));
        holder.textTitle.setTextColor(ContextCompat.getColor(activity, R.color.apptheme_section_header_text));

        TypedValue outValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        holder.layout.setForeground(ContextCompat.getDrawable(activity, outValue.resourceId));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moduleButtonClickListener.onModuleButtonClicked(course, module);
            }
        });

        boolean downloadableContent = false;
        for (Item item : module.items) {
            if (item.type.equals(Item.TYPE_VIDEO)) {
                downloadableContent = true;
                break;
            }
        }
        if (downloadableContent) {
            holder.viewDownloadButton.setVisibility(View.VISIBLE);
            holder.viewDownloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ModuleDownloadController moduleDownloadController = new ModuleDownloadController(activity);
                    moduleDownloadController.initModuleDownloads(course, module);
                }
            });
        } else {
            holder.viewDownloadButton.setVisibility(View.GONE);
        }
    }

    private void contentLocked(Section module, ModuleViewHolder holder) {
        holder.progressBar.setVisibility(View.GONE);
        holder.viewModuleNotification.setVisibility(View.VISIBLE);
        holder.viewHeader.setBackgroundColor(ContextCompat.getColor(activity, R.color.apptheme_section_header_bg_locked));
        holder.textTitle.setTextColor(ContextCompat.getColor(activity, R.color.apptheme_section_header_text_locked));

        holder.layout.setClickable(false);
        holder.layout.setForeground(null);
        holder.viewDownloadButton.setVisibility(View.GONE);
        if (module.available_from != null && DateUtil.nowIsBefore(module.available_from)) {
            DateFormat dateOut;
            if (DisplayUtil.is7inchTablet(activity)) {
                dateOut = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
            } else {
                dateOut = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            }

            Date date = DateUtil.parse(module.available_from);

            holder.textModuleNotification.setText(String.format(activity.getString(R.string.available_at),
                    dateOut.format(date)));
        } else {
            holder.textModuleNotification.setText(activity.getString(R.string.module_notification_no_content));
        }
    }

    public interface OnModuleButtonClickListener {

        void onModuleButtonClicked(Course course, Section module);

    }

    static class ModuleViewHolder extends RecyclerView.ViewHolder {

        FrameLayout layout;
        TextView textTitle;
        AutofitRecyclerView recyclerView;
        ProgressBar progressBar;
        View viewHeader;

        View viewModuleNotification;
        TextView textModuleNotification;

        View viewDownloadButton;

        public ModuleViewHolder(View view) {
            super(view);

            layout = (FrameLayout) view.findViewById(R.id.container);
            textTitle = (TextView) view.findViewById(R.id.textTitle);
            recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerView);
            progressBar = (ProgressBar) view.findViewById(R.id.containerProgress);
            viewHeader = view.findViewById(R.id.header);
            viewModuleNotification = view.findViewById(R.id.moduleNotificationContainer);
            textModuleNotification = (TextView) view.findViewById(R.id.moduleNotificationLabel);
            viewDownloadButton = view.findViewById(R.id.downloadBtn);
        }

    }

}
