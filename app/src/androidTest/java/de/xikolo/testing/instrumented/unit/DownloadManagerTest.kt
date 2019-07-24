package de.xikolo.testing.instrumented.unit

import android.content.Intent
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import de.xikolo.controllers.main.SplashActivity
import de.xikolo.managers.DownloadManager
import de.xikolo.models.DownloadAsset
import de.xikolo.testing.instrumented.mocking.SingleObjects
import de.xikolo.testing.instrumented.mocking.base.BaseTest
import de.xikolo.utils.StorageUtil
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class DownloadManagerTest : BaseTest() {

    private val TEST_DOWNLOAD_TITLE: String = DownloadManagerTest::class.java.simpleName

    private val TEST_DOWNLOAD_URL_SECONDARY: String = SingleObjects.testVideoStream.thumbnailUrl
    private val TEST_DOWNLOAD_SIZE_SECONDARY: Long = 0L
    private val TEST_DOWNLOAD_ASSET_SECONDARY: DownloadAsset = TestDownloadAsset(
        TEST_DOWNLOAD_URL_SECONDARY,
        TEST_DOWNLOAD_TITLE,
        TEST_DOWNLOAD_SIZE_SECONDARY,
        mutableSetOf()
    )

    private val TEST_DOWNLOAD_URL: String = SingleObjects.testVideoStream.sdUrl
    private val TEST_DOWNLOAD_SIZE: Long = SingleObjects.testVideoStream.sdSize.toLong()
    private val TEST_DOWNLOAD_ASSET: DownloadAsset = TestDownloadAsset(
        TEST_DOWNLOAD_URL,
        TEST_DOWNLOAD_TITLE,
        TEST_DOWNLOAD_SIZE,
        mutableSetOf(TEST_DOWNLOAD_ASSET_SECONDARY)
    )

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(SplashActivity::class.java, false, false)

    lateinit var manager: DownloadManager

    private fun waitForDownload(manager: DownloadManager, asset: DownloadAsset) {
        while (manager.downloadRunningWithSecondaryAssets(asset)) {
            Thread.sleep(10)
        }
    }

    @Before
    fun setUp() {
        activityTestRule.launchActivity(
            Intent(context, SplashActivity::class.java)
        )

        manager = DownloadManager(activityTestRule.activity)

        manager.deleteAssetDownload(TEST_DOWNLOAD_ASSET, true)
    }

    @After
    fun cleanUp() {
        manager.cancelAssetDownload(TEST_DOWNLOAD_ASSET)
        manager.cancelAssetDownload(TEST_DOWNLOAD_ASSET_SECONDARY)

        manager.deleteAssetDownload(TEST_DOWNLOAD_ASSET)
        manager.deleteAssetDownload(TEST_DOWNLOAD_ASSET_SECONDARY)
    }

    @Test
    fun testDownloadAndDelete() {
        val asset = TEST_DOWNLOAD_ASSET_SECONDARY

        manager.startAssetDownload(asset)
        Thread.sleep(1000)

        assertTrue(manager.downloadRunning(asset))

        waitForDownload(manager, asset)

        assertTrue(manager.getDownloadTotalBytes(asset) == asset.size)

        assertFalse(manager.downloadRunning(asset))
        assertTrue(manager.downloadExists(asset))

        assertNotNull(manager.getDownloadFile(asset))
        val file = manager.getDownloadFile(asset)!!

        assertTrue(file.nameWithoutExtension == TEST_DOWNLOAD_TITLE)
        assertTrue(manager.getFoldersWithDownloads(StorageUtil.getStorage(context)).isNotEmpty())

        manager.deleteAssetDownload(asset)

        assertFalse(manager.downloadExists(asset))
        assertNull(manager.getDownloadFile(asset))
    }

    @Test
    fun testDownloadWithSecondary() {
        val asset = TEST_DOWNLOAD_ASSET
        val secondary = asset.secondaryAssets.first()

        manager.startAssetDownload(asset)
        Thread.sleep(1000)

        assertTrue(manager.downloadRunning(asset))
        assertTrue(manager.downloadRunningWithSecondaryAssets(asset))

        waitForDownload(manager, asset)

        assertTrue(manager.getDownloadTotalBytes(asset) == asset.sizeWithSecondaryAssets)

        assertFalse(manager.downloadRunning(asset))
        assertFalse(manager.downloadRunningWithSecondaryAssets(asset))

        assertTrue(manager.downloadExists(asset))
        assertTrue(manager.downloadExists(secondary))

        manager.deleteAssetDownload(asset, true)
    }

    @Test
    fun testDownloadWithSecondaryAndDeleteSecondary() {
        val asset = TEST_DOWNLOAD_ASSET
        val secondary = asset.secondaryAssets.first()

        manager.startAssetDownload(asset)

        waitForDownload(manager, asset)

        manager.deleteAssetDownload(asset, true)

        assertFalse(manager.downloadExists(asset))
        assertNull(manager.getDownloadFile(asset))
        assertFalse(manager.downloadExists(secondary))
        assertNull(manager.getDownloadFile(secondary))
    }

    @Test
    fun testDownloadWithSecondaryAndKeepSecondary() {
        val asset = TEST_DOWNLOAD_ASSET
        val secondary = asset.secondaryAssets.first()

        manager.startAssetDownload(asset)
        Thread.sleep(1000)

        waitForDownload(manager, asset)

        manager.deleteAssetDownload(asset, false)

        assertFalse(manager.downloadExists(asset))
        assertNull(manager.getDownloadFile(asset))
        assertTrue(manager.downloadExists(secondary))
        assertNotNull(manager.getDownloadFile(secondary))
    }

    @Test
    fun testDownloadCanceling() {
        val asset = TEST_DOWNLOAD_ASSET
        val secondary = asset.secondaryAssets.first()

        manager.startAssetDownload(asset)
        Thread.sleep(1000)

        assertTrue(manager.downloadRunning(asset))
        assertTrue(manager.downloadRunningWithSecondaryAssets(asset))

        manager.cancelAssetDownload(asset)

        assertFalse(manager.downloadRunning(asset))
        assertFalse(manager.downloadRunningWithSecondaryAssets(asset))

        assertFalse(manager.downloadExists(asset))
        assertFalse(manager.downloadExists(secondary))

        assertNull(manager.getDownloadFile(asset))
        assertNull(manager.getDownloadFile(secondary))
    }

    class TestDownloadAsset(url: String?, assetTitle: String, assetSize: Long, secondaries: MutableSet<DownloadAsset>) : DownloadAsset(url, assetTitle) {

        override val title = assetTitle

        override val mimeType = "video/mp4"
        override val size = assetSize

        override val secondaryAssets = secondaries
    }

}
