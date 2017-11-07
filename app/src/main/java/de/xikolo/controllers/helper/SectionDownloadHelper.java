package de.xikolo.controllers.helper;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import de.xikolo.controllers.dialogs.MobileDownloadDialog;
import de.xikolo.controllers.dialogs.ModuleDownloadDialog;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.DownloadManager;
import de.xikolo.managers.ItemManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.storages.ApplicationPreferences;
import de.xikolo.utils.DownloadUtil;
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
                for (Item item : section.getAccessibleItems()) {
                    if (Item.TYPE_VIDEO.equals(item.type)) {
                        Video video = (Video) item.getContent();

                        if (sdVideo) {
                            startDownload(item.id, DownloadUtil.VideoAssetType.VIDEO_SD);
                        }
                        if (hdVideo) {
                            startDownload(item.id, DownloadUtil.VideoAssetType.VIDEO_HD);
                        }
                        if (slides) {
                            startDownload(item.id, DownloadUtil.VideoAssetType.SLIDES);
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

    private void startDownload(String itemId, DownloadUtil.VideoAssetType type) {
        if (!downloadManager.downloadExists(itemId, type)
                && !downloadManager.downloadRunning(itemId, type)) {
            downloadManager.startItemAssetDownload(itemId, type);
        }
    }

}
