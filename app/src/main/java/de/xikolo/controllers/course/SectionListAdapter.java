package de.xikolo.controllers.course;

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
import java.util.List;
import java.util.Locale;

import de.xikolo.R;
import de.xikolo.models.Section;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.DisplayUtil;
import de.xikolo.views.AutofitRecyclerView;
import de.xikolo.views.SpaceItemDecoration;

public class SectionListAdapter extends RecyclerView.Adapter<SectionListAdapter.SectionViewHolder> {

    public static final String TAG = SectionListAdapter.class.getSimpleName();

    private List<Section> sections;

    private FragmentActivity activity;

    private ItemListAdapter.OnItemClickListener itemClickListener;
    private OnSectionClickListener sectionClickListener;

    public SectionListAdapter(FragmentActivity activity, OnSectionClickListener moduleCallback,
                              ItemListAdapter.OnItemClickListener itemCallback) {
        this.activity = activity;
        this.sections = new ArrayList<>();
        this.sectionClickListener = moduleCallback;
        this.itemClickListener = itemCallback;
    }

    public void updateModules(List<Section> modules) {
        this.sections = modules;
        this.notifyDataSetChanged();
    }

    public void clear() {
        this.sections.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SectionViewHolder holder, int position) {
        final Section section = sections.get(position);

        holder.textTitle.setText(section.title);

        final ItemListAdapter itemAdapter = new ItemListAdapter(section, itemClickListener);
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

        if (!section.accessible || section.getAccessibleItems().size() == 0) {
            contentLocked(section, holder);
        } else {
            contentAvailable(section, holder);
            itemAdapter.updateItems(section.getAccessibleItems());
        }
    }

    private void contentAvailable(final Section section, SectionViewHolder holder) {
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
                sectionClickListener.onSectionClicked(section.id);
            }
        });

        if (section.hasDownloadableContent()) {
            holder.viewDownloadButton.setVisibility(View.VISIBLE);
            holder.viewDownloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sectionClickListener.onSectionDownloadClicked(section.id);
                }
            });
        } else {
            holder.viewDownloadButton.setVisibility(View.GONE);
        }
    }

    private void contentLocked(Section section, SectionViewHolder holder) {
        holder.progressBar.setVisibility(View.GONE);
        holder.viewModuleNotification.setVisibility(View.VISIBLE);
        holder.viewHeader.setBackgroundColor(ContextCompat.getColor(activity, R.color.apptheme_section_header_bg_locked));
        holder.textTitle.setTextColor(ContextCompat.getColor(activity, R.color.apptheme_section_header_text_locked));

        holder.layout.setClickable(false);
        holder.layout.setForeground(null);
        holder.viewDownloadButton.setVisibility(View.GONE);
        if (section.startDate != null && DateUtil.nowIsBefore(section.startDate)) {
            DateFormat dateOut;
            if (DisplayUtil.is7inchTablet(activity)) {
                dateOut = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
            } else {
                dateOut = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            }

            holder.textModuleNotification.setText(String.format(activity.getString(R.string.available_at),
                    dateOut.format(section.startDate)));
        } else {
            holder.textModuleNotification.setText(activity.getString(R.string.module_notification_no_content));
        }
    }

    public interface OnSectionClickListener {

        void onSectionClicked(String sectionId);

        void onSectionDownloadClicked(String sectionId);

    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {

        FrameLayout layout;
        TextView textTitle;
        AutofitRecyclerView recyclerView;
        ProgressBar progressBar;
        View viewHeader;

        View viewModuleNotification;
        TextView textModuleNotification;

        View viewDownloadButton;

        public SectionViewHolder(View view) {
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
