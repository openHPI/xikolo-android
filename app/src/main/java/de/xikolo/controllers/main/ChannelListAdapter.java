package de.xikolo.controllers.main;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
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
import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.managers.CourseManager;
import de.xikolo.models.Channel;
import de.xikolo.models.Course;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ChannelViewHolder> {

    public static final String TAG = ChannelListAdapter.class.getSimpleName();

    private List<Channel> channelList =  new ArrayList<>();
    private OnChannelCardClickListener callback;

    public ChannelListAdapter(OnChannelCardClickListener callback){
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

        holder.textTitle.setText(channel.name);
        holder.textDescription.setText(channel.description);
        holder.layout.setOnClickListener(v -> callback.onChannelClicked(channel.id));
        holder.buttonChannelCourses.setOnClickListener(v -> callback.onChannelClicked(channel.id));

        new CourseManager().listCoursesForChannel(channel.id, Realm.getDefaultInstance(), new RealmChangeListener<RealmResults<Course>>() {
            @Override
            public void onChange(@NonNull RealmResults<Course> courses) {
                holder.scrollContainer.removeAllViews();
                for(int i = 0; i < Math.min(7, courses.size()); i++){
                    Course course = courses.get(i);
                    GlideApp.with(App.getInstance()).load(course.imageUrl).into(holder.imageView);//ToDo add image from model


                    View view = LayoutInflater.from(App.getInstance()).inflate(R.layout.item_channel_list_scroll, holder.scrollContainer, false);
                    TextView textTitle = view.findViewById(R.id.textTitle);
                    textTitle.setText(course.title);
                    ImageView imageView = view.findViewById(R.id.imageView);
                    GlideApp.with(App.getInstance()).load(course.imageUrl).into(imageView);
                    view.setOnClickListener(v -> callback.onCourseClicked(course.id));
                    holder.scrollContainer.addView(view);
                }
                View view = LayoutInflater.from(App.getInstance()).inflate(R.layout.item_channel_list_scroll_more, holder.scrollContainer, false);
                ((CardView) view).setCardBackgroundColor(Color.parseColor(channel.color));
                view.setOnClickListener(v -> callback.onMoreCoursesClicked(channel.id));
                holder.scrollContainer.addView(view);
            }
        });
    }

    public interface OnChannelCardClickListener {

        void onChannelClicked(String channelId);

        void onCourseClicked(String courseId);

        void onMoreCoursesClicked(String channelId);
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
