package de.xikolo.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import de.xikolo.GlobalApplication;
import de.xikolo.controllers.CastActivity;
import de.xikolo.models.Item;
import de.xikolo.models.VideoItemDetail;

public class CastUtil {

    public static boolean isConnected() {
        CastContext castContext = CastContext.getSharedInstance(GlobalApplication.getInstance());
        SessionManager sessionManager = castContext.getSessionManager();

        return sessionManager.getCurrentCastSession() != null && sessionManager.getCurrentCastSession().isConnected();
    }

    public static boolean isAvailable() {
        return GlobalApplication.getInstance().getCastState() != CastState.NO_DEVICES_AVAILABLE;
    }

    public static MediaInfo buildCastMetadata(Item<VideoItemDetail> video) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, video.title);

        WebImage image = new WebImage(Uri.parse(video.detail.stream.poster));

        // small size image used for notification, mini­controller and Lock Screen on JellyBean
        mediaMetadata.addImage(image);

        // large image, used on the Cast Player page and Lock Screen on KitKat
        mediaMetadata.addImage(image);

        return new MediaInfo.Builder(video.detail.stream.hd_url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(mediaMetadata)
                .build();
    }

    public static void loadMedia(final Activity activity, Item<VideoItemDetail> video, boolean autoPlay, int position) {
        CastContext castContext = CastContext.getSharedInstance(GlobalApplication.getInstance());
        SessionManager sessionManager = castContext.getSessionManager();
        CastSession session = sessionManager.getCurrentCastSession();

        if (session != null) {
            final RemoteMediaClient remoteMediaClient = session.getRemoteMediaClient();

            remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
                @Override
                public void onStatusUpdated() {
                    Intent intent = new Intent(activity, CastActivity.class);
                    activity.startActivity(intent);
                    remoteMediaClient.removeListener(this);
                }

                @Override
                public void onMetadataUpdated() {

                }

                @Override
                public void onQueueStatusUpdated() {

                }

                @Override
                public void onPreloadStatusUpdated() {

                }

                @Override
                public void onSendingRemoteMediaRequest() {

                }
            });

            remoteMediaClient.load(buildCastMetadata(video), autoPlay, position);
        }
    }


}
