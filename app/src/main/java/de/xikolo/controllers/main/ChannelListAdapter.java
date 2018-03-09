package de.xikolo.controllers.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.controllers.de.xikolo.controllers.channels.ChannelListItemPagerFragment;
import de.xikolo.models.Channel;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ChannelViewHolder> {

    public static final String TAG = ChannelListAdapter.class.getSimpleName();

    private List<Channel> channelList =  new ArrayList<>();

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
        holder.textLanguage.setText("English"); //ToDo get from model
        holder.textDescription.setText("This is some fancy description"); //ToDo get from model
        holder.buttonChannelCourses.setOnClickListener(v -> {
            //ToDO do something here
        });

        ViewPager pager = holder.pagerCourses;
        pager.setAdapter(new FragmentStatePagerAdapter(new FragmentActivity().getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 1; //ToDO
            }

            @Override
            public Fragment getItem(int position) {
                ChannelListItemPagerFragment fragment = new ChannelListItemPagerFragment();
                //fragment.setContent(); //ToDO
                return fragment;
            }
        });
        //GlideApp.with(fragment).load(course.imageUrl).into(viewHolder.image);//ToDo add image
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container) ViewGroup layout;
        @BindView(R.id.textTitle) TextView textTitle;
        @BindView(R.id.textLanguage) TextView textLanguage;
        @BindView(R.id.textDescription) TextView textDescription;
        @BindView(R.id.imageView) ImageView imageView;
        @BindView(R.id.button_channel_courses) Button buttonChannelCourses;
        @BindView(R.id.pagerCourses) ViewPager pagerCourses;

        public ChannelViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
