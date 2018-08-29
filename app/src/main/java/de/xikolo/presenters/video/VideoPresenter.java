package de.xikolo.presenters.video;

import java.util.List;

import de.xikolo.managers.ItemManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.SubtitleTrack;
import de.xikolo.models.Video;
import de.xikolo.presenters.base.Presenter;
import io.realm.Realm;

public class VideoPresenter extends Presenter<VideoView> {

    public static final String TAG = VideoPresenter.class.getSimpleName();

    private ItemManager itemManager;

    private Realm realm;

    private String courseId;
    private String sectionId;
    private String itemId;
    private String videoId;

    private Course course;
    private Section section;
    private Item item;
    private Video video;
    private List<SubtitleTrack> subtitles;

    VideoPresenter(String courseId, String sectionId, String itemId, String videoId) {
        this.itemManager = new ItemManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.itemId = itemId;
        this.videoId = videoId;

        loadModels();
    }

    @Override
    public void onViewAttached(VideoView view) {
        super.onViewAttached(view);

        getViewOrThrow().setupVideo(course, section, item, video);
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    private void loadModels() {
        if (course == null) {
            course = Course.get(courseId);
        }
        if (section == null) {
            section = Section.get(sectionId);
        }
        if (item == null) {
            item = Item.get(itemId);
        }
        if (video == null) {
            video = Video.get(videoId);
        }
    }

    public void onPause(int progress) {
        itemManager.updateVideoProgress(video, progress, realm);
    }

}
