package de.xikolo.controllers.course;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.utils.ItemTitleUtil;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {

    public static final String TAG = ItemListAdapter.class.getSimpleName();

    private List<Item> items;

    private Section section;

    private OnItemClickListener listener;

    public ItemListAdapter(Section section, OnItemClickListener listener) {
        this.items = new ArrayList<>();
        this.section = section;
        this.listener = listener;
    }

    public void updateItems(List<Item> items) {
        this.items = items;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        Context context = App.getInstance();

        final Item item = items.get(position);

        holder.textTitle.setText(ItemTitleUtil.format(section.title, item.title));

        holder.textIcon.setText(item.getIconRes());

        if (!item.visited) {
            holder.viewUnseenIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.viewUnseenIndicator.setVisibility(View.GONE);
        }

        if (!item.accessible) {
            holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
            holder.layout.setForeground(null);
            holder.textTitle.setTextColor(ContextCompat.getColor(context, R.color.text_light));
            holder.textIcon.setTextColor(ContextCompat.getColor(context, R.color.text_light));
            holder.viewUnseenIndicator.setVisibility(View.GONE);
            holder.layout.setEnabled(false);
        } else {
            holder.layout.setOnClickListener((view) -> listener.onItemClicked(section.id, holder.getAdapterPosition()));
        }
    }

    public interface OnItemClickListener {

        void onItemClicked(String sectionId, int position);

    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textTitle) TextView textTitle;
        @BindView(R.id.textIcon) TextView textIcon;

        @BindView(R.id.unseenIndicator) View viewUnseenIndicator;

        @BindView(R.id.container) FrameLayout layout;

        public ItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
