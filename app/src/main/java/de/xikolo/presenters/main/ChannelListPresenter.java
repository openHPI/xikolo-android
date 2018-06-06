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

        this.channelListPromise = channelManager.listChannels(realm, getChannelListRealmChangeListener());
    }

    private RealmChangeListener<RealmResults<Channel>> getChannelListRealmChangeListener() {
        return results -> {
            if (results.size() > 0) {
                channelList = results;

                if (isViewAttached()) {
                    getView().showContent();
                    getView().showChannelList(channelList);
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
        channelManager.requestChannelListWithCourses(getDefaultJobCallback(userRequest));
    }

}