package de.xikolo.controllers.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_list, parent, false);
        return new ChannelListAdapter.ChannelViewHolder(view);
    }

    @SuppressWarnings("SetTextI18n")
    @Override
    public void onBindViewHolder(ChannelListAdapter.ChannelViewHolder holder, int position) {
        final Channel channel = channelList.get(position);

        holder.title.setText(channel.name);
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container) ViewGroup layout;
        @BindView(R.id.title) TextView title;
        @BindView(R.id.date) TextView date;
        @BindView(R.id.bullet) TextView bullet;
        @BindView(R.id.course) TextView course;
        @BindView(R.id.text) TextView text;
        @BindView(R.id.unseen_indicator) View unseenIndicator;

        public ChannelViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
