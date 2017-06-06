package de.xikolo.controllers.helper;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import de.xikolo.controllers.dialogs.MobileDownloadDialog;
import de.xikolo.controllers.dialogs.ModuleDownloadDialog;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.managers.DownloadManager;
import de.xikolo.managers.ItemManager;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.storages.preferences.ApplicationPreferences;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.NetworkUtil;

public class SectionDownloadHelper {

    private FragmentActivity activity;

    private boolean sdVideo;

    private boolean hdVideo;

    private boolean slides;

    private DownloadManager downloadManager;

    public SectionDownloadHelper(FragmentActivity activity) {
        this.activity = activity;
        this.downloadManager = new DownloadManager(activity);
    }

    public void initSectionDownloads(final Course course, final Section section) {
        ModuleDownloadDialog listDialog = ModuleDownloadDialog.getInstance(section.title);
        listDialog.setModuleDownloadDialogListener(new ModuleDownloadDialog.ModuleDownloadDialogListener() {
            @Override
            public void onDialogPositiveClick(DialogFragment dialog, boolean hdVideo, boolean sdVideo, boolean slides) {
                SectionDownloadHelper.this.hdVideo = hdVideo;
                SectionDownloadHelper.this.sdVideo = sdVideo;
                SectionDownloadHelper.this.slides = slides;

                final ApplicationPreferences appPreferences = new ApplicationPreferences();

                if (hdVideo || sdVideo || slides) {
                    if (NetworkUtil.isOnline()) {
                        if (NetworkUtil.getConnectivityStatus() == NetworkUtil.TYPE_MOBILE &&
                                appPreferences.isDownloadNetworkLimitedOnMobile()) {
                            MobileDownloadDialog permissionDialog = MobileDownloadDialog.getInstance();
                            permissionDialog.setMobileDownloadDialogListener(new MobileDownloadDialog.MobileDownloadDialogListener() {
                                @Override
                                public void onDialogPositiveClick(DialogFragment dialog) {
                                    appPreferences.setIsDownloadNetworkLimitedOnMobile(false);
                                    startSectionDownloads(course, section);
                                }
                            });
                            permissionDialog.show(activity.getSupportFragmentManager(), MobileDownloadDialog.TAG);
                        } else {
                            startSectionDownloads(course, section);
                        }
                    } else {
                        NetworkUtil.showNoConnectionToast();
                    }
                }
            }
        });
        listDialog.show(activity.getSupportFragmentManager(), ModuleDownloadDialog.TAG);
    }

    private void startSectionDownloads(final Course course, final Section section) {
        ItemManager itemManager = new ItemManager();

        LanalyticsUtil.trackDownloadedSection(section.id, course.id, hdVideo, sdVideo, slides);

        final ProgressDialog dialog = ProgressDialog.getInstance();
        dialog.show(activity.getSupportFragmentManager(), ProgressDialog.TAG);

        itemManager.requestItemsWithContentForSection(section.id, new JobCallback() {
            @Override
            public void onSuccess() {
                dialog.dismiss();
                for (Item item : section.getItems()) {
                    if (Item.TYPE_VIDEO.equals(item.type)) {
                        Video video = (Video) item.getContent();

                        if (sdVideo) {
                            startDownload(video.singleStream.sdUrl, DownloadManager.DownloadFileType.VIDEO_SD,
                                    course, section, item);
                        }
                        if (hdVideo) {
                            startDownload(video.singleStream.hdUrl, DownloadManager.DownloadFileType.VIDEO_HD,
                                    course, section, item);
                        }
                        if (slides) {
                            startDownload(video.slidesUrl, DownloadManager.DownloadFileType.SLIDES,
                                    course, section, item);
                        }
                    }
                }
            }

            @Override
            public void onError(ErrorCode code) {
                dialog.dismiss();
            }
        });

    }

    private void startDownload(String uri, DownloadManager.DownloadFileType downloadFileType, Course course, Section module, Item item) {
        if (uri != null
                && !downloadManager.downloadExists(downloadFileType, course, module, item)
                && !downloadManager.downloadRunning(downloadFileType, course, module, item)) {
            downloadManager.startDownload(uri, downloadFileType, course, module, item);
        }
    }

}
