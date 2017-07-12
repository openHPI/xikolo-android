package de.xikolo.controllers.course;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
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
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClicked(section.id, item.id);
                }
            });
        }
    }

    public interface OnItemClickListener {

        void onItemClicked(String sectionId, String itemId);

    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle;
        TextView textIcon;

        View viewUnseenIndicator;

        FrameLayout layout;

        public ItemViewHolder(View view) {
            super(view);

            textTitle = (TextView) view.findViewById(R.id.textTitle);
            textIcon = (TextView) view.findViewById(R.id.textIcon);
            layout = (FrameLayout) view.findViewById(R.id.container);
            viewUnseenIndicator = view.findViewById(R.id.unseenIndicator);
        }
    }

}
