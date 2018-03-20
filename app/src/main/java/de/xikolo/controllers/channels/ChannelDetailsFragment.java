package de.xikolo.controllers.channels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.models.Channel;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.channels.ChannelDetailsPresenter;
import de.xikolo.presenters.channels.ChannelDetailsPresenterFactory;
import de.xikolo.presenters.channels.ChannelDetailsView;
import de.xikolo.utils.MarkdownUtil;

public class ChannelDetailsFragment extends LoadingStatePresenterFragment<ChannelDetailsPresenter, ChannelDetailsView> implements ChannelDetailsView {

    public static final String TAG = ChannelDetailsFragment.class.getSimpleName();

    @AutoBundleField String channelId;

    @BindView(R.id.layout_header) FrameLayout layoutHeader;
    @BindView(R.id.image_channel) ImageView imageChannel;
    @BindView(R.id.text_title) TextView textTitle;
    @BindView(R.id.text_description) TextView textDescription;
    @BindView(R.id.course_list_container) FrameLayout courseListContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_channel_details;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int videoId = item.getItemId();
        switch (videoId) {
            case R.id.action_refresh:
                presenter.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setupView(Channel channel) {
        if (getActivity() instanceof ChannelDetailsActivity) {
            layoutHeader.setVisibility(View.GONE);
        }
        else {
            GlideApp.with(this).load(R.mipmap.ic_launcher_circle).into(imageChannel); //ToDo get from model
            textTitle.setText(channel.name);
        }

        MarkdownUtil.formatAndSet(channel.description, textDescription);
    }

    @NonNull
    @Override
    protected PresenterFactory<ChannelDetailsPresenter> getPresenterFactory() {
        return new ChannelDetailsPresenterFactory(channelId);
    }

}
