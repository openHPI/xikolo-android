package de.xikolo.controllers.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.FeatureConfig
import de.xikolo.controllers.dialogs.ConfirmDeleteDialog
import de.xikolo.controllers.dialogs.ConfirmDeleteDialogAutoBundle
import de.xikolo.controllers.helper.NetworkStateHelper
import de.xikolo.extensions.observe
import de.xikolo.managers.DownloadManager
import de.xikolo.managers.PermissionManager
import de.xikolo.models.Storage
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.utils.extensions.*
import java.io.File
import java.util.*

class DownloadsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, DownloadsAdapter.OnDeleteButtonClickedListener, NetworkStateHelper.NetworkStateOwner {

    companion object {
        val TAG: String = DownloadsFragment::class.java.simpleName
    }

    private var adapter: DownloadsAdapter? = null

    private var downloadManager: DownloadManager? = null

    private var permissionManager: PermissionManager? = null

    override lateinit var networkStateHelper: NetworkStateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let { activity ->
            downloadManager = DownloadManager(activity)
            permissionManager = PermissionManager(activity)
            adapter = DownloadsAdapter(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_downloads, container, false)

        val recyclerView = layout.findViewById<RecyclerView>(R.id.content_view)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        networkStateHelper = NetworkStateHelper(activity, layout, this)

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.instance.state.permission.of(PermissionManager.REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
            .observe(viewLifecycleOwner) {
                fetchItems()
            }
    }

    override fun onStart() {
        super.onStart()
        fetchItems()
    }

    override fun onRefresh() {
        fetchItems()
        networkStateHelper.hideAnyProgress()
    }

    private fun fetchItems() {
        activity?.let { activity ->
            adapter?.clear()
            if (permissionManager?.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                networkStateHelper.showContent()

                // total items

                var internalAddition = ""
                var sdcardAddition = ""

                val sdcardStorageAvailable = activity.sdcardStorage != null

                if (sdcardStorageAvailable) {
                    if (activity.preferredStorage.file == activity.sdcardStorage?.file) {
                        sdcardAddition = " " + getString(R.string.settings_storage_addition)
                    } else {
                        internalAddition = " " + getString(R.string.settings_storage_addition)
                    }
                }

                fun buildTotalItem(appFolder: String, title: String): DownloadsAdapter.FolderItem {
                    // clean up the storage before fetching items
                    Storage(File(appFolder)).clean()

                    return DownloadsAdapter.FolderItem(
                        title,
                        appFolder
                    )
                }

                var list: MutableList<DownloadsAdapter.FolderItem> = ArrayList()

                val storage = activity.internalStorage
                storage.file.createIfNotExists()
                list.add(buildTotalItem(
                    storage.file.absolutePath,
                    getString(R.string.settings_title_storage_internal) + internalAddition
                ))

                activity.sdcardStorage?.let { sdcardStorage ->
                    sdcardStorage.file.createIfNotExists()
                    list.add(buildTotalItem(
                        sdcardStorage.file.absolutePath,
                        getString(R.string.settings_title_storage_external) + sdcardAddition
                    ))
                }

                adapter?.addItem(getString(R.string.overall), list)

                // documents

                if (FeatureConfig.DOCUMENTS) {
                    list = ArrayList()

                    list.add(buildTotalItem(
                        activity.internalStorage.file.absolutePath + File.separator + "Documents",
                        getString(R.string.settings_title_storage_internal) + internalAddition
                    ))

                    activity.sdcardStorage?.let { sdcardStorage ->
                        list.add(buildTotalItem(
                            sdcardStorage.file.absolutePath + File.separator + "Documents",
                            getString(R.string.settings_title_storage_external) + sdcardAddition
                        ))
                    }

                    adapter?.addItem(getString(R.string.tab_documents), list)
                }

                // certificates

                list = ArrayList()

                list.add(buildTotalItem(
                    activity.internalStorage.file.absolutePath + File.separator + "Certificates",
                    getString(R.string.settings_title_storage_internal) + internalAddition
                ))

                activity.sdcardStorage?.let { sdcardStorage ->
                    list.add(buildTotalItem(
                        sdcardStorage.file.absolutePath + File.separator + "Certificates",
                        getString(R.string.settings_title_storage_external) + sdcardAddition
                    ))
                }

                adapter?.addItem(getString(R.string.tab_certificates), list)

                // course folders

                fun buildCourseItems(storage: File): List<DownloadsAdapter.FolderItem> {
                    val folders = downloadManager?.getFoldersWithDownloads(
                        File(storage.absolutePath + File.separator + "Courses")
                    )
                    val folderList: MutableList<DownloadsAdapter.FolderItem> = ArrayList()
                    if (folders?.isNotEmpty() == true) {
                        for (folder in folders) {
                            val name = try {
                                folder.substring(
                                    folder.lastIndexOf(File.separator) + 1,
                                    folder.lastIndexOf("_")
                                )
                            } catch (e: Exception) {
                                folder
                            }

                            val item = DownloadsAdapter.FolderItem(name, folder)
                            folderList.add(item)
                        }
                    }
                    return folderList
                }

                val internalCourseTitle = if (sdcardStorageAvailable) {
                    getString(R.string.courses) + " (" + getString(R.string.settings_title_storage_internal) + ")"
                } else {
                    getString(R.string.courses)
                }
                adapter?.addItem(
                    internalCourseTitle,
                    buildCourseItems(activity.internalStorage.file)
                )

                activity.sdcardStorage?.let { sdcardStorage ->
                    val sdcardCourseTitle = if (sdcardStorageAvailable) {
                        getString(R.string.courses) + " (" + getString(R.string.settings_title_storage_external) + ")"
                    } else {
                        getString(R.string.courses)
                    }
                    adapter?.addItem(
                        sdcardCourseTitle,
                        buildCourseItems(sdcardStorage.file)
                    )
                }
            } else {
                networkStateHelper.setMessageTitle(R.string.dialog_title_permissions)
                networkStateHelper.setMessageSummary(R.string.dialog_permissions)
                networkStateHelper.setMessageOnClickListener(View.OnClickListener {
                    PermissionManager.startAppInfo(activity)
                })
                networkStateHelper.showMessage()
            }
        }
    }

    override fun onDeleteButtonClicked(item: DownloadsAdapter.FolderItem) {
        activity?.let { activity ->
            val appPreferences = ApplicationPreferences()

            if (appPreferences.confirmBeforeDeleting) {
                val dialog = ConfirmDeleteDialogAutoBundle.builder(true).build()
                dialog.listener = object : ConfirmDeleteDialog.Listener {
                    override fun onDialogPositiveClick(dialog: DialogFragment) {
                        deleteFolder(item)
                    }

                    override fun onDialogPositiveAndAlwaysClick(dialog: DialogFragment) {
                        appPreferences.confirmBeforeDeleting = false
                        deleteFolder(item)
                    }
                }
                dialog.show(activity.supportFragmentManager, ConfirmDeleteDialog.TAG)
            } else {
                deleteFolder(item)
            }
        }
    }

    private fun deleteFolder(item: DownloadsAdapter.FolderItem) {
        val dir = File(item.path)

        if (dir.exists()) {
            dir.deleteAll()
        } else {
            showToast(R.string.error)
        }

        fetchItems()
    }

}
