package de.xikolo.controller.helper;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import de.xikolo.GlobalApplication;
import de.xikolo.controller.dialogs.MobileDownloadDialog;
import de.xikolo.controller.dialogs.ModuleDownloadDialog;
import de.xikolo.controller.dialogs.ProgressDialog;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.data.preferences.AppPreferences;
import de.xikolo.model.DownloadModel;
import de.xikolo.model.ItemModel;
import de.xikolo.model.Result;
import de.xikolo.util.NetworkUtil;

public class ModuleDownloadController {

    private FragmentActivity activity;
    
    private boolean sdVideo;

    private boolean hdVideo;

    private boolean slides;

    private GlobalApplication app;

    private DownloadModel downloadModel;

    public ModuleDownloadController(FragmentActivity activity) {
        this.app = GlobalApplication.getInstance();
        this.activity = activity;
        this.downloadModel = new DownloadModel(app.getJobManager(), activity);
    }

    public void initModuleDownloads(final Course course, final Module module) {
        ModuleDownloadDialog listDialog = ModuleDownloadDialog.getInstance(module.name);
        listDialog.setModuleDownloadDialogListener(new ModuleDownloadDialog.ModuleDownloadDialogListener() {
            @Override
            public void onDialogPositiveClick(DialogFragment dialog, boolean hdVideo, boolean sdVideo, boolean slides) {
                ModuleDownloadController.this.hdVideo = hdVideo;
                ModuleDownloadController.this.sdVideo = sdVideo;
                ModuleDownloadController.this.slides = slides;

                final AppPreferences appPreferences = GlobalApplication.getInstance().getPreferencesFactory().getAppPreferences();

                if (hdVideo || sdVideo || slides) {
                    if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                        if (NetworkUtil.getConnectivityStatus(app) == NetworkUtil.TYPE_MOBILE &&
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
        ItemModel itemModel = new ItemModel(app.getJobManager());

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
                                startDownload(video.detail.stream.sd_url, DownloadModel.DownloadFileType.VIDEO_SD,
                                        course, module, result);
                            }
                            if (hdVideo) {
                                startDownload(video.detail.stream.hd_url, DownloadModel.DownloadFileType.VIDEO_HD,
                                        course, module, result);
                            }
                            if (slides) {
                                startDownload(video.detail.slides_url, DownloadModel.DownloadFileType.SLIDES,
                                        course, module, result);
                            }
                        }
                    }
                };
                itemModel.getItemDetail(result, course, module, item, item.type);
                downloadStarted = true;
            }
        }
        if (!downloadStarted) {
            dialog.dismiss();
        }

    }

    private void startDownload(String uri, DownloadModel.DownloadFileType downloadFileType, Course course, Module module, Item item) {
        if (uri != null
                && !downloadModel.downloadExists(downloadFileType, course, module, item)
                && !downloadModel.downloadRunning(downloadFileType, course, module, item)) {
            downloadModel.startDownload(uri, downloadFileType, course, module, item);
        }
    }

}
