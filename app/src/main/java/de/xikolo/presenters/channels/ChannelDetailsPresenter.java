package de.xikolo.presenters.channels;

import de.xikolo.managers.ChannelManager;
import de.xikolo.models.Channel;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;

public class ChannelDetailsPresenter extends LoadingStatePresenter<ChannelDetailsView> {

    public static final String TAG = ChannelDetailsPresenter.class.getSimpleName();

    private ChannelManager channelManager;

    private Realm realm;

    private Channel channelPromise;

    private String channelId;

    private Channel channel;

    ChannelDetailsPresenter(String channelId) {
        this.channelManager = new ChannelManager();
        this.realm = Realm.getDefaultInstance();
        this.channelId = channelId;
    }

    @Override
    public void onRefresh() {
        requestChannel(true);
    }

    @Override
    public void onViewAttached(ChannelDetailsView v) {
        super.onViewAttached(v);

        if (channel == null) {
            requestChannel(false);
        }

        channelPromise = channelManager.getChannel(channelId, realm, new RealmChangeListener<Channel>() {
            @Override
            public void onChange(Channel c) {
                channel = c;
                getViewOrThrow().showContent();
                getViewOrThrow().setupView(channel);
            }
        });
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (channelPromise != null) {
            channelPromise.removeAllChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    private void requestChannel(boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
        channelManager.requestChannel(channelId, getDefaultJobCallback(userRequest));
    }

}
