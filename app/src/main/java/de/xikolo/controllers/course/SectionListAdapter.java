package de.xikolo.controllers.course;

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

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
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

    public void updateSections(List<Section> sections) {
        this.sections = sections;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SectionViewHolder holder, int position) {
        final Section section = sections.get(position);

        holder.textTitle.setText(section.title);

        final ItemListAdapter itemAdapter = new ItemListAdapter(section, itemClickListener);
        holder.recyclerView.setAdapter(itemAdapter);
        holder.recyclerView.setHasFixedSize(false);
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

        if (section.hasAccessibleItems()) {
            contentAvailable(section, holder);
            itemAdapter.updateItems(section.getAccessibleItems());
        } else {
            contentLocked(section, holder);
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
        holder.layout.setOnClickListener((view) -> sectionClickListener.onSectionClicked(section.id));

        if (section.hasDownloadableContent()) {
            holder.viewDownloadButton.setVisibility(View.VISIBLE);
            holder.viewDownloadButton.setOnClickListener((view) -> sectionClickListener.onSectionDownloadClicked(section.id));
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
        if (section.startDate != null && DateUtil.isFuture(section.startDate)) {
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

        @BindView(R.id.container) FrameLayout layout;
        @BindView(R.id.textTitle) TextView textTitle;
        @BindView(R.id.recyclerView) AutofitRecyclerView recyclerView;
        @BindView(R.id.containerProgress) ProgressBar progressBar;
        @BindView(R.id.header) View viewHeader;

        @BindView(R.id.moduleNotificationContainer) View viewModuleNotification;
        @BindView(R.id.moduleNotificationLabel) TextView textModuleNotification;

        @BindView(R.id.downloadBtn) View viewDownloadButton;

        public SectionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
