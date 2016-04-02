package de.xikolo.util;

import android.net.Uri;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.VideoItemDetail;

public class CastUtil {

    public static MediaInfo buildCastMetadata(Item<VideoItemDetail> video) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, video.title);

        WebImage image = new WebImage(Uri.parse(video.detail.stream.poster));

        // small size image used for notification, mini­controller and Lock Screen on JellyBean
        mediaMetadata.addImage(image);

        // large image, used on the Cast Player page and Lock Screen on KitKat
        mediaMetadata.addImage(image);

        return new MediaInfo.Builder(
                video.detail.stream.hd_url)
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
    }

}
