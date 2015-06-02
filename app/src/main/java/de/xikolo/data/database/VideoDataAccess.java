package de.xikolo.data.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.data.entities.VideoItemDetail;

public class VideoDataAccess extends DataAccess {

    public VideoDataAccess(DatabaseHelper databaseHelper) {
        super(databaseHelper);
    }

    public void addVideo(VideoItemDetail video) {
        getDatabase().insert(VideoTable.TABLE_NAME, null, buildContentValues(video));
    }

    public void addOrUpdateVideo(VideoItemDetail video) {
        if (updateVideo(video) < 1) {
            addVideo(video);
        }
    }

    public VideoItemDetail getVideo(String id) {
        Cursor cursor = getDatabase().query(
                VideoTable.TABLE_NAME,
                new String[]{
                        VideoTable.COLUMN_ID,
                        VideoTable.COLUMN_TITLE,
                        VideoTable.COLUMN_MINUTES,
                        VideoTable.COLUMN_SECONDS,
                        VideoTable.COLUMN_PROGRESS,
                        VideoTable.COLUMN_URL,
                        VideoTable.COLUMN_DOWNLOAD_URL,
                        VideoTable.COLUMN_SLIDES_URL,
                        VideoTable.COLUMN_TRANSCRIPT_URL,
                        VideoTable.COLUMN_HD_URL,
                        VideoTable.COLUMN_SD_URL,
                        VideoTable.COLUMN_VIMEO_ID,
                        VideoTable.COLUMN_POSTER_IMAGE_URL,
                },
                VideoTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(id)}, null, null, null, null);

        VideoItemDetail video = null;
        if (cursor.moveToFirst()) {
            video = buildVideo(cursor);
        }
        cursor.close();

        return video;
    }

    public List<VideoItemDetail> getAllVideos() {
        List<VideoItemDetail> videoList = new ArrayList<VideoItemDetail>();

        String selectQuery = "SELECT * FROM " + VideoTable.TABLE_NAME;

        Cursor cursor = getDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                VideoItemDetail video = buildVideo(cursor);
                videoList.add(video);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return videoList;
    }

    private VideoItemDetail buildVideo(Cursor cursor) {
        VideoItemDetail video = new VideoItemDetail();

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

    private ContentValues buildContentValues(VideoItemDetail video) {
        ContentValues values = new ContentValues();
        values.put(VideoTable.COLUMN_ID, video.id);
        values.put(VideoTable.COLUMN_TITLE, video.title);
        values.put(VideoTable.COLUMN_MINUTES, video.minutes);
        values.put(VideoTable.COLUMN_SECONDS, video.seconds);
        values.put(VideoTable.COLUMN_PROGRESS, video.progress);
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

    public int getVideosCount() {
        String countQuery = "SELECT * FROM " + VideoTable.TABLE_NAME;
        Cursor cursor = getDatabase().rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    public int updateVideo(VideoItemDetail video) {
        int affected = getDatabase().update(
                VideoTable.TABLE_NAME,
                buildContentValues(video),
                VideoTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(video.id)});

        return affected;
    }

    public void deleteVideo(VideoItemDetail video) {
        getDatabase().delete(
                VideoTable.TABLE_NAME,
                VideoTable.COLUMN_ID + " =? ",
                new String[]{String.valueOf(video.id)});
    }

}
