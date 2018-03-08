package de.xikolo.presenters.main;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.managers.ChannelManager;
import de.xikolo.models.Channel;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ChannelListPresenter extends LoadingStatePresenter<ChannelListView> {

    protected ChannelManager channelManager;

    protected Realm realm;

    protected List<Channel> channelList;

    protected RealmResults channelListPromise;

    ChannelListPresenter() {
        this.channelManager = new ChannelManager();
        this.realm = Realm.getDefaultInstance();
        this.channelList = new ArrayList<>();
    }

    @Override
    public void onViewAttached(ChannelListView view) {
        super.onViewAttached(view);

        if (channelList == null || channelList.size() == 0) {
            requestChannels(false);
        }

        this.channelListPromise = channelManager.listChannels(realm, getChannelListRealmChangeLictener());
    }

    private RealmChangeListener<RealmResults<Channel>> getChannelListRealmChangeLictener() {
        return new RealmChangeListener<RealmResults<Channel>>() {
            @Override
            public void onChange(RealmResults<Channel> results) {
                if (results.size() > 0) {
                    channelList = results;
                    getViewOrThrow().showContent();
                    getViewOrThrow().showChannelList(channelList);
                }
            }
        };
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (channelListPromise != null) {
            channelListPromise.removeAllChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestChannels(true);
    }

    private void requestChannels(boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
            channelManager.requestChannelList(getDefaultJobCallback(userRequest));
    }

}