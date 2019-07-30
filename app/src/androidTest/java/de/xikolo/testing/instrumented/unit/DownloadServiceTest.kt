package de.xikolo.testing.instrumented.unit

import android.content.Intent
import android.os.Bundle
import androidx.test.filters.LargeTest
import androidx.test.rule.ServiceTestRule
import de.xikolo.services.DownloadService
import de.xikolo.testing.instrumented.mocking.SingleObjects
import de.xikolo.testing.instrumented.mocking.base.BaseTest
import de.xikolo.testing.instrumented.ui.helper.NavigationHelper.WAIT_LOADING_LONG
import de.xikolo.utils.StorageUtil
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

@LargeTest
class DownloadServiceTest : BaseTest() {

    private val TEST_DOWNLOAD_TITLE: String = DownloadServiceTest::class.java.simpleName
    private val TEST_DOWNLOAD_URL: String = SingleObjects.testVideoStreamHdUrl
    private val TEST_DOWNLOAD_SIZE: Long = SingleObjects.testVideoStreamHdSize.toLong()
    private val TEST_DOWNLOAD_PATH: String = StorageUtil.getStorage(context).absolutePath + File.separator + "test"
    private val TEST_DOWNLOAD_BUNDLE: Bundle = Bundle().let { bundle ->
        bundle.putString(DownloadService.ARG_TITLE, TEST_DOWNLOAD_TITLE)
        bundle.putString(DownloadService.ARG_URL, TEST_DOWNLOAD_URL)
        bundle.putString(DownloadService.ARG_FILE_PATH, TEST_DOWNLOAD_PATH)
        bundle.putBoolean(DownloadService.ARG_SHOW_NOTIFICATION, true)
        bundle
    }

    @Rule
    @JvmField
    val serviceRule = ServiceTestRule()

    private fun startService() {
        val intent = Intent(context, DownloadService::class.java)
        intent.putExtras(TEST_DOWNLOAD_BUNDLE)
        context.startService(intent)

        Thread.sleep(WAIT_LOADING_LONG) // wait for the service and download to start
    }

    private fun destroyService() {
        DownloadService.instance?.stopSelf()

        Thread.sleep(WAIT_LOADING_LONG) // wait for the service and download to stop
    }

    @Test
    fun testInstanceCreationAndDeletion() {
        assertNull(DownloadService.instance)

        startService()

        assertNotNull(DownloadService.instance)

        destroyService()

        assertNull(DownloadService.instance)
    }

    @Test
    fun testDownloadStatus() {
        startService()
        val instance = DownloadService.instance!!

        assertTrue(instance.isDownloading)
        assertTrue(instance.isDownloading(TEST_DOWNLOAD_URL))

        instance.cancelDownload(TEST_DOWNLOAD_URL)

        assertFalse(instance.isDownloading)
        assertFalse(instance.isDownloading(TEST_DOWNLOAD_URL))
        assertNull(instance.getDownload(TEST_DOWNLOAD_URL))

        destroyService()
    }

    @Test
    fun testDownloadProperties() {
        startService()
        val instance = DownloadService.instance!!

        assertNotNull(instance.getDownload(TEST_DOWNLOAD_URL))
        val download = instance.getDownload(TEST_DOWNLOAD_URL)!!

        assertTrue(download.url == TEST_DOWNLOAD_URL)
        assertTrue(download.title == TEST_DOWNLOAD_TITLE)
        assertTrue(download.filePath == TEST_DOWNLOAD_PATH)
        assertTrue(download.showNotification)

        assertTrue(download.totalBytes == TEST_DOWNLOAD_SIZE)

        destroyService()
    }

}
