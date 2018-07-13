package de.xikolo.controllers.helper;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import de.xikolo.controllers.dialogs.MobileDownloadDialog;
import de.xikolo.controllers.dialogs.ModuleDownloadDialog;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.jobs.base.RequestJobCallback;
import de.xikolo.managers.DownloadManager;
import de.xikolo.managers.ItemManager;
import de.xikolo.models.Course;
import de.xikolo.models.DownloadAsset;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.storages.ApplicationPreferences;
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
        listDialog.setModuleDownloadDialogListener((dialog, hdVideo, sdVideo, slides) -> {
            SectionDownloadHelper.this.hdVideo = hdVideo;
            SectionDownloadHelper.this.sdVideo = sdVideo;
            SectionDownloadHelper.this.slides = slides;

            final ApplicationPreferences appPreferences = new ApplicationPreferences();

            if (hdVideo || sdVideo || slides) {
                if (NetworkUtil.isOnline()) {
                    if (NetworkUtil.getConnectivityStatus() == NetworkUtil.TYPE_MOBILE &&
                            appPreferences.isDownloadNetworkLimitedOnMobile()) {
                        MobileDownloadDialog permissionDialog = MobileDownloadDialog.getInstance();
                        permissionDialog.setMobileDownloadDialogListener(dialog1 -> {
                            appPreferences.setDownloadNetworkLimitedOnMobile(false);
                            startSectionDownloads(course, section);
                        });
                        permissionDialog.show(activity.getSupportFragmentManager(), MobileDownloadDialog.TAG);
                    } else {
                        startSectionDownloads(course, section);
                    }
                } else {
                    NetworkUtil.showNoConnectionToast();
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

        itemManager.requestItemsWithContentForSection(section.id, new RequestJobCallback() {
            @Override
            public void onSuccess() {
                dialog.dismissAllowingStateLoss();
                for (Item item : section.getAccessibleItems()) {
                    if (item.contentType.equals(Item.TYPE_VIDEO)) {
                        if (sdVideo) {
                            startDownload(new DownloadAsset.Course.Item.VideoSD(item, Video.getForContentId(item.contentId)));
                        }
                        if (hdVideo) {
                            startDownload(new DownloadAsset.Course.Item.VideoHD(item, Video.getForContentId(item.contentId)));
                        }
                        if (slides) {
                            startDownload(new DownloadAsset.Course.Item.Slides(item, Video.getForContentId(item.contentId)));
                        }
                    }
                }
            }

            @Override
            public void onError(@NonNull ErrorCode code) {
                dialog.dismiss();
            }
        });

    }

    private void startDownload(DownloadAsset.Course.Item item) {
        if (!downloadManager.downloadExists(item)
            && !downloadManager.downloadRunning(item)) {
            downloadManager.startAssetDownload(item);
        }
    }

}
