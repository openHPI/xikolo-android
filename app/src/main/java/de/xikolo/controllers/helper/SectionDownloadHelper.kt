package de.xikolo.controllers.helper

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import de.xikolo.R
import de.xikolo.controllers.dialogs.*
import de.xikolo.managers.DownloadManager
import de.xikolo.models.Course
import de.xikolo.models.DownloadAsset
import de.xikolo.models.Item
import de.xikolo.models.Section
import de.xikolo.models.dao.VideoDao
import de.xikolo.network.jobs.ListItemsWithContentForSectionJob
import de.xikolo.network.jobs.base.NetworkCode
import de.xikolo.network.jobs.base.NetworkState
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.utils.extensions.ConnectivityType
import de.xikolo.utils.extensions.connectivityType
import de.xikolo.utils.extensions.isOnline
import de.xikolo.utils.extensions.showToast

class SectionDownloadHelper(private val activity: FragmentActivity) {

    private val downloadManager: DownloadManager = DownloadManager(activity)

    fun initSectionDownloads(course: Course, section: Section) {
        val listDialog = ModuleDownloadDialogAutoBundle.builder(section.title).build()

        listDialog.listener = object : ModuleDownloadDialog.ItemSelectionListener {

            override fun onSelected(dialog: DialogFragment, hdVideo: Boolean, sdVideo: Boolean, slides: Boolean) {
                val appPreferences = ApplicationPreferences()
                if (hdVideo || sdVideo || slides) {
                    if (activity.isOnline) {
                        if (activity.connectivityType === ConnectivityType.CELLULAR && appPreferences.isDownloadNetworkLimitedOnMobile) {
                            val permissionDialog = MobileDownloadDialog()

                            permissionDialog.listener = object : MobileDownloadDialog.MobileDownloadGrantedListener {

                                override fun onGranted(dialog: DialogFragment) {
                                    appPreferences.isDownloadNetworkLimitedOnMobile = false
                                    startSectionDownloads(course, section, hdVideo, sdVideo, slides)
                                }
                            }
                            permissionDialog.show(activity.supportFragmentManager, MobileDownloadDialog.TAG)
                        } else {
                            startSectionDownloads(course, section, hdVideo, sdVideo, slides)
                        }
                    } else {
                        activity.showToast(R.string.toast_no_network)
                    }
                }
            }
        }

        listDialog.show(activity.supportFragmentManager, ModuleDownloadDialog.TAG)
    }

    private fun startSectionDownloads(course: Course, section: Section, hdVideo: Boolean, sdVideo: Boolean, slides: Boolean) {
        LanalyticsUtil.trackDownloadedSection(section.id, course.id, hdVideo, sdVideo, slides)

        val dialog = ProgressDialogIndeterminateAutoBundle.builder().build()
        dialog.show(activity.supportFragmentManager, ProgressDialogIndeterminate.TAG)

        val itemRequestNetworkState = NetworkStateLiveData()
        itemRequestNetworkState.observeForever(object : Observer<NetworkState> {
            override fun onChanged(networkState: NetworkState) {
                if (networkState.code === NetworkCode.STARTED) {
                    return
                }

                if (networkState.code === NetworkCode.SUCCESS) {
                    dialog.dismissAllowingStateLoss()
                    for (item in section.accessibleItems) {
                        if (item.contentType == Item.TYPE_VIDEO) {
                            if (sdVideo) {
                                startDownload(DownloadAsset.Course.Item.VideoSD(item, VideoDao.Unmanaged.find(item.contentId)!!))
                            }
                            if (hdVideo) {
                                startDownload(DownloadAsset.Course.Item.VideoHD(item, VideoDao.Unmanaged.find(item.contentId)!!))
                            }
                            if (slides) {
                                startDownload(DownloadAsset.Course.Item.Slides(item, VideoDao.Unmanaged.find(item.contentId)!!))
                            }
                        }
                    }
                } else {
                    dialog.dismiss()
                }

                itemRequestNetworkState.removeObserver(this)
            }
        })
        ListItemsWithContentForSectionJob(section.id, itemRequestNetworkState, false).run()
    }

    private fun startDownload(item: DownloadAsset.Course.Item) {
        if (!downloadManager.downloadExists(item) && !downloadManager.downloadRunning(item)) {
            downloadManager.startAssetDownload(item)
        }
    }

}
