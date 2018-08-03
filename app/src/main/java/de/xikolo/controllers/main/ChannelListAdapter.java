package de.xikolo.controllers.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.App;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;
import de.xikolo.config.GlideApp;
import de.xikolo.managers.CourseManager;
import de.xikolo.models.Channel;
import de.xikolo.models.Course;
import io.realm.Realm;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ChannelViewHolder> {

    public static final String TAG = ChannelListAdapter.class.getSimpleName();

    public static final int PREVIEW_COURSES_COUNT = 7;

    private List<Channel> channelList = new ArrayList<>();
    private OnChannelCardClickListener callback;

    public ChannelListAdapter(OnChannelCardClickListener callback) {
        this.callback = callback;
    }

    public void update(List<Channel> channelList) {
        this.channelList = channelList;
        notifyDataSetChanged();
    }

    public void clear() {
        channelList.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return channelList.size();
    }

    @Override
    public ChannelListAdapter.ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel_list, parent, false);
        return new ChannelListAdapter.ChannelViewHolder(view);
    }

    @SuppressWarnings("SetTextI18n")
    @Override
    public void onBindViewHolder(ChannelListAdapter.ChannelViewHolder holder, int position) {
        final Channel channel = channelList.get(position);

        int channelColor = channel.getColorOrDefault();

        holder.textTitle.setText(channel.title);

        holder.textTitle.setTextColor(channelColor);

        holder.textDescription.setText(channel.description);
        holder.buttonChannelCourses.setTextColor(channelColor);

        holder.buttonChannelCourses.setOnClickListener(v -> callback.onChannelClicked(channel.id));
        holder.layout.setOnClickListener(v -> callback.onChannelClicked(channel.id));

        if (channel.imageUrl != null)
            GlideApp.with(App.getInstance()).load(channel.imageUrl).into(holder.imageView);
        else
            holder.imageView.setVisibility(View.GONE);

        CourseManager courseManager = new CourseManager();
        courseManager.listCoursesForChannel(channel.id, Realm.getDefaultInstance(), courses -> {
            Realm realm = Realm.getDefaultInstance();
            holder.scrollContainer.removeAllViews();

            List<Course> courseList = new ArrayList<>();
            if (courses.size() > 0) {
                courseList.clear();
                List<Course> subList;

                if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
                    subList = courseManager.listFutureCoursesForChannel(realm, channel.id);
                    if (subList.size() > 0)
                        courseList.addAll(subList);

                    subList = courseManager.listCurrentAndPastCoursesForChannel(realm, channel.id);
                    if (subList.size() > 0)
                        courseList.addAll(subList);
                } else {
                    subList = courseManager.listCurrentAndFutureCoursesForChannel(realm, channel.id);
                    if (subList.size() > 0)
                        courseList.addAll(subList);

                    subList = courseManager.listPastCoursesForChannel(realm, channel.id);
                    if (subList.size() > 0)
                        courseList.addAll(subList);
                }
            }

            int courseCount = Math.min(PREVIEW_COURSES_COUNT, courseList.size());

            for (int i = 0; i < courseCount; i++) {
                Course course = courseList.get(i);

                View listItem = LayoutInflater.from(App.getInstance()).inflate(R.layout.item_channel_list_scroll, holder.scrollContainer, false);

                TextView textTitle = listItem.findViewById(R.id.textTitle);
                textTitle.setText(course.title);

                ImageView imageView = listItem.findViewById(R.id.imageView);
                GlideApp.with(App.getInstance()).load(course.imageUrl).into(imageView);

                listItem.setOnClickListener(v -> callback.onCourseClicked(course));

                holder.scrollContainer.addView(listItem);
            }

            if (courseList.size() > PREVIEW_COURSES_COUNT) {
                View showMoreCard = LayoutInflater.from(App.getInstance()).inflate(R.layout.item_channel_list_scroll_more, holder.scrollContainer, false);

                ImageView imageView = showMoreCard.findViewById(R.id.imageView);
                GlideApp.with(App.getInstance()).load(channel.imageUrl).into(imageView);

                showMoreCard.setOnClickListener(v -> callback.onMoreCoursesClicked(channel.id, courseCount));

                View card = showMoreCard.findViewById(R.id.card_view);
                ViewGroup.LayoutParams params = card.getLayoutParams();
                params.width += App.getInstance().getResources().getDimension(R.dimen.corner_radius);
                card.setLayoutParams(params);

                holder.scrollContainer.addView(showMoreCard);
                holder.scrollContainer.setPadding(
                        holder.scrollContainer.getPaddingLeft(),
                        holder.scrollContainer.getPaddingTop(),
                        0,
                        holder.scrollContainer.getPaddingBottom()
                );
            }

            realm.close();
        });
    }

    public interface OnChannelCardClickListener {

        void onChannelClicked(String channelId);

        void onCourseClicked(Course course);

        void onMoreCoursesClicked(String channelId, int scrollPosition);
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container) ViewGroup layout;
        @BindView(R.id.textTitle) TextView textTitle;
        @BindView(R.id.textDescription) TextView textDescription;
        @BindView(R.id.imageView) ImageView imageView;
        @BindView(R.id.button_channel_courses) Button buttonChannelCourses;
        @BindView(R.id.scrollContainer) LinearLayout scrollContainer;

        public ChannelViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
