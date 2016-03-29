package de.xikolo.controller.course.adapter;

import android.app.Activity;
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
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.util.DateUtil;
import de.xikolo.util.ItemTitle;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {

    public static final String TAG = ItemListAdapter.class.getSimpleName();

    private List<Item> mItems;

    private Activity mActivity;
    private Course mCourse;
    private Module mModule;

    private OnItemButtonClickListener mCallback;

    public ItemListAdapter(Activity activity, Course course, Module module, OnItemButtonClickListener callback) {
        this.mActivity = activity;
        this.mItems = new ArrayList<>();
        this.mCourse = course;
        this.mModule = module;
        this.mCallback = callback;
    }

    public void updateItems(List<Item> items) {
        this.mItems = items;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final Item item = mItems.get(position);

        holder.title.setText(ItemTitle.format(mModule.name, item.title));

        holder.icon.setText(Item.getIcon(mActivity, item.type, item.exercise_type));

        if (!item.progress.visited) {
            holder.unseenIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.unseenIndicator.setVisibility(View.GONE);
        }

        if (!DateUtil.nowIsBetween(mModule.available_from, mModule.available_to)
                || !DateUtil.nowIsBetween(item.available_from, item.available_to)
                || item.locked) {
            holder.container.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.transparent));
            holder.container.setForeground(null);
            holder.title.setTextColor(ContextCompat.getColor(mActivity, R.color.text_light));
            holder.icon.setTextColor(ContextCompat.getColor(mActivity, R.color.text_light));
            holder.unseenIndicator.setVisibility(View.GONE);
            holder.container.setEnabled(false);
        } else {
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onItemButtonClicked(mCourse, mModule, item);
                }
            });
        }
    }

    public interface OnItemButtonClickListener {

        void onItemButtonClicked(Course course, Module module, Item item);

    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView icon;

        View unseenIndicator;

        FrameLayout container;

        public ItemViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.textTitle);
            icon = (TextView) view.findViewById(R.id.textIcon);
            container = (FrameLayout) view.findViewById(R.id.container);
            unseenIndicator = view.findViewById(R.id.unseenIndicator);
        }
    }

}
