package de.xikolo.controller.course.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

public class ItemListAdapter extends BaseAdapter {

    public static final String TAG = ItemListAdapter.class.getSimpleName();

    private List<Item> mItems;

    private Activity mActivity;
    private Course mCourse;
    private Module mModule;

    private OnItemButtonClickListener mCallback;

    public ItemListAdapter(Activity activity, Course course, Module module, OnItemButtonClickListener callback) {
        this.mActivity = activity;
        this.mItems = new ArrayList<Item>();
        this.mCourse = course;
        this.mModule = module;
        this.mCallback = callback;
    }

    public void updateItems(List<Item> items) {
        this.mItems = items;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int i) {
        return mItems.get(i);
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
            rowView = inflater.inflate(R.layout.item_module_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.textTitle);
            viewHolder.icon = (TextView) rowView.findViewById(R.id.textIcon);
            viewHolder.container = (FrameLayout) rowView.findViewById(R.id.container);
            viewHolder.unseenIndicator = rowView.findViewById(R.id.unseenIndicator);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        final Item item = (Item) getItem(i);

        holder.title.setText(ItemTitle.format(mModule.name, item.title));

        if (item.type.equals(Item.TYPE_TEXT)) {
            holder.icon.setText(mActivity.getString(R.string.icon_text));
        } else if (item.type.equals(Item.TYPE_VIDEO)) {
            holder.icon.setText(mActivity.getString(R.string.icon_video));
        } else if (item.type.equals(Item.TYPE_SELFTEST)) {
            holder.icon.setText(mActivity.getString(R.string.icon_selftest));
        } else if (item.type.equals(Item.TYPE_ASSIGNMENT) || item.type.equals(Item.TYPE_EXAM)) {
            holder.icon.setText(mActivity.getString(R.string.icon_assignment));
        } else if (item.type.equals(Item.TYPE_LTI)) {
            holder.icon.setText(mActivity.getString(R.string.icon_lti));
        }

        if (!item.progress.visited) {
            holder.unseenIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.unseenIndicator.setVisibility(View.GONE);
        }

        if (!DateUtil.nowIsBetween(mModule.available_from, mModule.available_to)
                || !DateUtil.nowIsBetween(item.available_from, item.available_to)) {
            holder.container.setBackgroundColor(mActivity.getResources().getColor(R.color.transparent));
            holder.container.setForeground(null);
            holder.title.setTextColor(mActivity.getResources().getColor(R.color.gray_light));
            holder.icon.setTextColor(mActivity.getResources().getColor(R.color.gray_light));
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

        return rowView;
    }

    public interface OnItemButtonClickListener {

        public void onItemButtonClicked(Course course, Module module, Item item);

    }

    static class ViewHolder {
        TextView title;
        TextView icon;

        View unseenIndicator;

        FrameLayout container;
    }

}
