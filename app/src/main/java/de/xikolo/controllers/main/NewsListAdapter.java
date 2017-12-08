package de.xikolo.controllers.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Announcement;
import de.xikolo.models.Course;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.AnnouncementViewHolder> {

    public static final String TAG = NewsListAdapter.class.getSimpleName();

    private List<Announcement> announcementList;

    private OnAnnouncementClickListener callback;

    private boolean global;

    public NewsListAdapter(OnAnnouncementClickListener callback, boolean global) {
        this.global = global;
        this.callback = callback;
        this.announcementList = new ArrayList<>();
    }

    public NewsListAdapter(OnAnnouncementClickListener callback) {
        this(callback, true);
    }

    public void update(List<Announcement> announcementList) {
        this.announcementList = announcementList;
        notifyDataSetChanged();
    }

    public void clear() {
        this.announcementList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }

    @Override
    public NewsListAdapter.AnnouncementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_list, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsListAdapter.AnnouncementViewHolder holder, int position) {
        final Announcement announcement = announcementList.get(position);

        holder.title.setText(announcement.title);

        if (announcement.text != null) {
            holder.text.setText(announcement.text.replaceAll(System.getProperty("line.separator"), ""));
        } else {
            holder.text.setText(null);
        }

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        holder.date.setText(dateFormat.format(announcement.publishedAt));

        Course course = Course.get(announcement.courseId);
        if (course != null && global) {
            holder.course.setText(course.title);
            holder.course.setVisibility(View.VISIBLE);
            holder.bullet.setVisibility(View.VISIBLE);
        } else {
            holder.course.setVisibility(View.GONE);
            holder.bullet.setVisibility(View.GONE);
        }

        if (announcement.visited || !UserManager.isAuthorized()) {
            holder.unseenIndicator.setVisibility(View.INVISIBLE);
        } else {
            holder.unseenIndicator.setVisibility(View.VISIBLE);
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onAnnouncementClicked(announcement.id);
            }
        });
    }

    public interface OnAnnouncementClickListener {

        void onAnnouncementClicked(String announcementId);

    }

    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container) ViewGroup layout;
        @BindView(R.id.title) TextView title;
        @BindView(R.id.date) TextView date;
        @BindView(R.id.bullet) TextView bullet;
        @BindView(R.id.course) TextView course;
        @BindView(R.id.text) TextView text;
        @BindView(R.id.unseen_indicator) View unseenIndicator;

        public AnnouncementViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
