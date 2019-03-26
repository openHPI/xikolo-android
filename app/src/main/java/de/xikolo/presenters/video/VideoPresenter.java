package de.xikolo.presenters.video;

import de.xikolo.managers.ItemManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.models.dao.CourseDao;
import de.xikolo.models.dao.ItemDao;
import de.xikolo.models.dao.SectionDao;
import de.xikolo.models.dao.VideoDao;
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
            course = CourseDao.Unmanaged.find(courseId);
        }
        if (section == null) {
            section = SectionDao.Unmanaged.find(sectionId);
        }
        if (item == null) {
            item = ItemDao.Unmanaged.find(itemId);
        }
        if (video == null) {
            video = VideoDao.Unmanaged.find(videoId);
        }
    }

    public void onPause(int progress) {
        itemManager.updateVideoProgress(video, progress, realm);
    }

}
