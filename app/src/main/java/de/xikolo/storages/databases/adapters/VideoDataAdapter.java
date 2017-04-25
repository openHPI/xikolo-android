package de.xikolo.storages.databases.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import de.xikolo.models.Video;
import de.xikolo.storages.databases.DatabaseHelper;
import de.xikolo.storages.databases.tables.Table;
import de.xikolo.storages.databases.tables.VideoTable;

public class VideoDataAdapter extends DataAdapter<Video> {

    public VideoDataAdapter(DatabaseHelper databaseHelper, Table table) {
        super(databaseHelper, table);
    }

    @Override
    protected Video buildEntity(Cursor cursor) {
        Video video = new Video();

        video.id = cursor.getString(0);
        video.title = cursor.getString(1);
        video.minutes = cursor.getString(2);
        video.seconds = cursor.getString(3);
        video.progress = cursor.getInt(4);
        video.url = cursor.getString(5);
        video.download_url = cursor.getString(6);
        video.slides_url = cursor.getString(7);
        video.transcript_url = cursor.getString(8);
        video.stream.hd_url = cursor.getString(9);
        video.stream.sd_url = cursor.getString(10);
        video.stream.vimeo_id = cursor.getString(11);
        video.stream.poster = cursor.getString(12);

        return video;
    }

    @Override
    protected ContentValues buildContentValues(Video video) {
        ContentValues values = new ContentValues();

        values.put(VideoTable.COLUMN_ID, video.id);
        values.put(VideoTable.COLUMN_TITLE, video.title);
        values.put(VideoTable.COLUMN_MINUTES, video.minutes);
        values.put(VideoTable.COLUMN_SECONDS, video.seconds);
        if (video.progress > 0) {
            values.put(VideoTable.COLUMN_PROGRESS, video.progress);
        }
        values.put(VideoTable.COLUMN_URL, video.url);
        values.put(VideoTable.COLUMN_DOWNLOAD_URL, video.download_url);
        values.put(VideoTable.COLUMN_SLIDES_URL, video.slides_url);
        values.put(VideoTable.COLUMN_TRANSCRIPT_URL, video.transcript_url);
        values.put(VideoTable.COLUMN_HD_URL, video.stream.hd_url);
        values.put(VideoTable.COLUMN_SD_URL, video.stream.sd_url);
        values.put(VideoTable.COLUMN_VIMEO_ID, video.stream.vimeo_id);
        values.put(VideoTable.COLUMN_POSTER_IMAGE_URL, video.stream.poster);

        return values;
    }

}
