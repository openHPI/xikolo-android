package de.xikolo.controller.downloads.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.util.FileUtil;

public class DownlodsAdapter extends BaseAdapter {

    public static final String TAG = DownlodsAdapter.class.getSimpleName();

    private Activity mActivity;

    private List<Item> items;

    private OnDeleteButtonClickedListener callback;

    public DownlodsAdapter(Activity activity, OnDeleteButtonClickedListener callback) {
        this(activity, callback, new ArrayList<Item>());
    }

    public DownlodsAdapter(Activity activity, OnDeleteButtonClickedListener callback, List<Item> items) {
        this.mActivity = activity;
        this.callback = callback;
        this.items = items;
    }

    public void updateItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Item item = items.get(i);

        View rowView = view;
        if (rowView == null) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            if (item instanceof FolderItem) {
                rowView = inflater.inflate(R.layout.item_download, null);
                ViewHolderFolder viewHolderFolder = new ViewHolderFolder();
                viewHolderFolder.title = (TextView) rowView.findViewById(R.id.title);
                viewHolderFolder.subTitle = (TextView) rowView.findViewById(R.id.subTitle);
                viewHolderFolder.separator = rowView.findViewById(R.id.separator);
                viewHolderFolder.delete = (TextView) rowView.findViewById(R.id.deleteBtn);
                rowView.setTag(viewHolderFolder);
            } else if (item instanceof SectionItem) {
                rowView = inflater.inflate(R.layout.item_section_header, null);
                ViewHolderSection viewHolderSection = new ViewHolderSection();
                viewHolderSection.title = (TextView) rowView.findViewById(R.id.sectionHeader);
                rowView.setTag(viewHolderSection);
            }
        }

        if (item instanceof FolderItem) {
            final ViewHolderFolder holder = (ViewHolderFolder) rowView.getTag();

            final FolderItem folderItem = (FolderItem) item;
            File dir = new File(folderItem.getPath());
            holder.title.setText(folderItem.getTitle());

            long numberOfFiles = FileUtil.folderFileNumber(dir);

            if (numberOfFiles > 0) {
                holder.subTitle.setText(numberOfFiles + " " + mActivity.getString(R.string.files) + ": "
                        + FileUtil.getFormattedFileSize(FileUtil.folderSize(dir)));
            } else {
                holder.subTitle.setText(numberOfFiles + " " + mActivity.getString(R.string.files));
            }

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onDeleteButtonClicked(folderItem);
                }
            });

            if (i == getCount() - 1 || getItem(i + 1) instanceof SectionItem) {
                holder.separator.setVisibility(View.INVISIBLE);
            }
        } else if (item instanceof SectionItem) {
            final ViewHolderSection holder = (ViewHolderSection) rowView.getTag();

            SectionItem sectionItem = (SectionItem) item;

            holder.title.setText(sectionItem.getTitle());
        }

        rowView.setEnabled(false);
        rowView.setOnClickListener(null);

        return rowView;
    }

    public static interface Item {

        public boolean isSection();

    }

    static class ViewHolderFolder {
        TextView title;
        TextView subTitle;
        TextView delete;
        View separator;
    }

    static class ViewHolderSection {
        TextView title;
    }

    public static class FolderItem implements Item {

        private String title;

        private String path;

        public FolderItem(String title, String path) {
            this.title = title;
            this.path = path;
        }

        public String getTitle() {
            return title;
        }

        public String getPath() {
            return path;
        }

        @Override
        public boolean isSection() {
            return false;
        }
    }

    public static class SectionItem implements Item {

        private String title;

        public SectionItem(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public boolean isSection() {
            return true;
        }

    }

    public static interface OnDeleteButtonClickedListener {

        public void onDeleteButtonClicked(FolderItem item);

    }

}
