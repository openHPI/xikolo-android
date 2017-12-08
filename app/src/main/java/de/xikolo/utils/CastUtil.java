package de.xikolo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.images.WebImage;

import de.xikolo.App;
import de.xikolo.controllers.cast.CastActivity;
import de.xikolo.models.Video;

public class CastUtil {

    public static boolean isConnected() {
        Context context = App.getInstance();

        if (PlayServicesUtil.checkPlayServices(context)) {
            CastContext castContext = CastContext.getSharedInstance(context);
            SessionManager sessionManager = castContext.getSessionManager();

            return sessionManager.getCurrentCastSession() != null && sessionManager.getCurrentCastSession().isConnected();
        } else {
            return false;
        }
    }

    public static boolean isAvailable() {
        Context context = App.getInstance();

        if (PlayServicesUtil.checkPlayServices(context)) {
            CastContext castContext = CastContext.getSharedInstance(context);

            return castContext.getCastState() != CastState.NO_DEVICES_AVAILABLE;
        } else {
            return false;
        }
    }

    public static MediaInfo buildCastMetadata(Video video) {
        if (!PlayServicesUtil.checkPlayServices(App.getInstance())) {
            return null;
        }

        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, video.title);

        WebImage image = new WebImage(Uri.parse(video.thumbnailUrl));

        // small size image used for notification, mini­controller and Lock Screen on JellyBean
        mediaMetadata.addImage(image);

        // large image, used on the Cast Player page and Lock Screen on KitKat
        mediaMetadata.addImage(image);

        return new MediaInfo.Builder(video.singleStream.hdUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(mediaMetadata)
                .build();
    }

    public static PendingResult<RemoteMediaClient.MediaChannelResult> loadMedia(final Activity activity, Video video, boolean autoPlay) {
        if (!PlayServicesUtil.checkPlayServices(App.getInstance())) {
            return null;
        }

        CastContext castContext = CastContext.getSharedInstance(App.getInstance());
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

                @Override
                public void onAdBreakStatusUpdated() {
                }
            });

            return remoteMediaClient.load(buildCastMetadata(video), autoPlay, video.progress);
        } else {
            return null;
        }
    }

}
