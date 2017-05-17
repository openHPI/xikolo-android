package de.xikolo.controllers.course.adapter;

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

import de.xikolo.R;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Module;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.ItemTitle;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {

    public static final String TAG = ItemListAdapter.class.getSimpleName();

    private List<Item> items;

    private Context context;
    private Course course;
    private Module module;

    private OnItemButtonClickListener callback;

    public ItemListAdapter(Context context, Course course, Module module, OnItemButtonClickListener callback) {
        this.context = context;
        this.items = new ArrayList<>();
        this.course = course;
        this.module = module;
        this.callback = callback;
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
        final Item item = items.get(position);

        holder.textTitle.setText(ItemTitle.format(module.name, item.title));

        holder.textIcon.setText(Item.getIcon(context, item.type, item.exercise_type));

        if (!item.progress.visited) {
            holder.viewUnseenIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.viewUnseenIndicator.setVisibility(View.GONE);
        }

        if ((item.available_from != null && DateUtil.nowIsBefore(item.available_from)) ||
                (item.available_to != null && DateUtil.nowIsAfter(item.available_to)) ||
                (module.available_from != null && DateUtil.nowIsBefore(module.available_from)) ||
                (module.available_to != null && DateUtil.nowIsAfter(module.available_to)) ||
                item.locked) {
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
                    callback.onItemButtonClicked(course, module, item);
                }
            });
        }
    }

    public interface OnItemButtonClickListener {

        void onItemButtonClicked(Course course, Module module, Item item);

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
