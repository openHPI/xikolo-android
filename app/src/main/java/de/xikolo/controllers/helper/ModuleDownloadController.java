package de.xikolo.controllers.helper;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import de.xikolo.GlobalApplication;
import de.xikolo.controllers.dialogs.MobileDownloadDialog;
import de.xikolo.controllers.dialogs.ModuleDownloadDialog;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.managers.DownloadManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Module;
import de.xikolo.models.VideoItemDetail;
import de.xikolo.storages.preferences.ApplicationPreferences;
import de.xikolo.managers.ItemManager;
import de.xikolo.managers.Result;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.NetworkUtil;

public class ModuleDownloadController {

    private FragmentActivity activity;
    
    private boolean sdVideo;

    private boolean hdVideo;

    private boolean slides;

    private GlobalApplication application;

    private DownloadManager downloadManager;

    public ModuleDownloadController(FragmentActivity activity) {
        this.application = GlobalApplication.getInstance();
        this.activity = activity;
        this.downloadManager = new DownloadManager(application.getJobManager(), activity);
    }

    public void initModuleDownloads(final Course course, final Module module) {
        ModuleDownloadDialog listDialog = ModuleDownloadDialog.getInstance(module.name);
        listDialog.setModuleDownloadDialogListener(new ModuleDownloadDialog.ModuleDownloadDialogListener() {
            @Override
            public void onDialogPositiveClick(DialogFragment dialog, boolean hdVideo, boolean sdVideo, boolean slides) {
                ModuleDownloadController.this.hdVideo = hdVideo;
                ModuleDownloadController.this.sdVideo = sdVideo;
                ModuleDownloadController.this.slides = slides;

                final ApplicationPreferences appPreferences = (ApplicationPreferences) GlobalApplication.getStorage(StorageType.APP);

                if (hdVideo || sdVideo || slides) {
                    if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                        if (NetworkUtil.getConnectivityStatus(application) == NetworkUtil.TYPE_MOBILE &&
                                appPreferences.isDownloadNetworkLimitedOnMobile()) {
                            MobileDownloadDialog permissionDialog = MobileDownloadDialog.getInstance();
                            permissionDialog.setMobileDownloadDialogListener(new MobileDownloadDialog.MobileDownloadDialogListener() {
                                @Override
                                public void onDialogPositiveClick(DialogFragment dialog) {
                                    appPreferences.setIsDownloadNetworkLimitedOnMobile(false);
                                    startModuleDownloads(course, module);
                                }
                            });
                            permissionDialog.show(activity.getSupportFragmentManager(), MobileDownloadDialog.TAG);
                        } else {
                            startModuleDownloads(course, module);
                        }
                    } else {
                        NetworkUtil.showNoConnectionToast();
                    }
                }
            }
        });
        listDialog.show(activity.getSupportFragmentManager(), ModuleDownloadDialog.TAG);
    }
    
    private void startModuleDownloads(final Course course, final Module module) {
        ItemManager itemManager = new ItemManager(application.getJobManager());

        LanalyticsUtil.trackDownloadedSection(module.id, course.id, hdVideo, sdVideo, slides);

        final ProgressDialog dialog = ProgressDialog.getInstance();
        dialog.show(activity.getSupportFragmentManager(), ProgressDialog.TAG);

        boolean downloadStarted = false;
        for (Item item : module.items) {
            if (item.type.equals(Item.TYPE_VIDEO)) {
                Result<Item> result = new Result<Item>() {
                    @Override
                    protected void onSuccess(Item result, DataSource dataSource) {
                        @SuppressWarnings("unchecked")
                        Item<VideoItemDetail> video = (Item<VideoItemDetail>) result;

                        if (dataSource == DataSource.NETWORK) {
                            dialog.dismiss();
                            if (sdVideo) {
                                startDownload(video.detail.stream.sd_url, DownloadManager.DownloadFileType.VIDEO_SD,
                                        course, module, result);
                            }
                            if (hdVideo) {
                                startDownload(video.detail.stream.hd_url, DownloadManager.DownloadFileType.VIDEO_HD,
                                        course, module, result);
                            }
                            if (slides) {
                                startDownload(video.detail.slides_url, DownloadManager.DownloadFileType.SLIDES,
                                        course, module, result);
                            }
                        }
                    }
                };
                itemManager.getItemDetail(result, course, module, item, item.type);
                downloadStarted = true;
            }
        }
        if (!downloadStarted) {
            dialog.dismiss();
        }

    }

    private void startDownload(String uri, DownloadManager.DownloadFileType downloadFileType, Course course, Module module, Item item) {
        if (uri != null
                && !downloadManager.downloadExists(downloadFileType, course, module, item)
                && !downloadManager.downloadRunning(downloadFileType, course, module, item)) {
            downloadManager.startDownload(uri, downloadFileType, course, module, item);
        }
    }

}
