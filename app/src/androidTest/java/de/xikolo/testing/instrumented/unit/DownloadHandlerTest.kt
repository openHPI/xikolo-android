package de.xikolo.testing.instrumented.unit

import androidx.test.rule.ActivityTestRule
import de.xikolo.controllers.main.MainActivity
import de.xikolo.download.DownloadHandler
import de.xikolo.download.DownloadIdentifier
import de.xikolo.download.DownloadRequest
import de.xikolo.download.DownloadStatus
import de.xikolo.testing.instrumented.mocking.base.BaseTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

abstract class DownloadHandlerTest<T : DownloadHandler<I, R>,
    I : DownloadIdentifier, R : DownloadRequest> : BaseTest() {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(MainActivity::class.java, false, true)

    abstract var downloadHandler: T

    abstract var successfulTestRequest: R
    abstract var successfulTestRequest2: R
    abstract var failingTestRequest: R
    abstract var invalidIdentifier: I

    @Test
    fun testDownloadIdentifier() {
        val identifier = downloadHandler.download(successfulTestRequest)
        Assert.assertTrue(identifier.toString().isNotBlank())
    }

    @Test
    fun testInvalidIdentifier() {
        downloadHandler.status(invalidIdentifier) {
            Assert.assertNull(it)
        }
        downloadHandler.cancel(invalidIdentifier) {
            Assert.assertFalse(it)
        }
    }

    @Test
    fun testDownloadStatusAfterStart() {
        downloadHandler.download(successfulTestRequest, null) { identifier ->
            identifier!!
            downloadHandler.status(identifier) { status ->
                status!!
                Assert.assertNotEquals(DownloadStatus.State.FAILED, status.state)
                Assert.assertNotEquals(DownloadStatus.State.SUCCESSFUL, status.state)
                if (status.totalBytes >= 0) {
                    Assert.assertTrue(
                        status.downloadedBytes <= status.totalBytes
                    )
                }

                downloadHandler.cancel(identifier)
            }
        }
    }

    @Test
    fun testDownloadStatusAfterCancel() {
        var called = false
        downloadHandler.download(
            successfulTestRequest,
            { status ->
                if (status?.state == DownloadStatus.State.CANCELLED) {
                    called = true
                }
            },
            { identifier ->
                identifier!!

                downloadHandler.cancel(identifier) { success ->
                    Assert.assertTrue(success)
                }
            }
        )

        waitWhile({ !called })
    }

    @Test
    fun testDownloadStatusAfterSuccess() {
        var called = false
        var id: I? = null

        downloadHandler.download(
            successfulTestRequest,
            { status ->
                if (status?.state == DownloadStatus.State.SUCCESSFUL) {
                    called = true
                }
            },
            { identifier ->
                identifier!!
                id = identifier
            }
        )

        waitWhile({ !called })

        downloadHandler.status(id!!) {
            it!!
            Assert.assertEquals(DownloadStatus.State.SUCCESSFUL, it.state)
            Assert.assertEquals(it.downloadedBytes, it.totalBytes)
        }
    }

    @Test
    fun testParallelDownloading() {
        downloadHandler.download(
            successfulTestRequest,
            null,
            { identifier ->
                identifier!!
                downloadHandler.status(identifier) {
                    it!!
                    Assert.assertNotEquals(
                        DownloadStatus.State.FAILED,
                        it
                    )
                    Assert.assertNotEquals(
                        DownloadStatus.State.SUCCESSFUL,
                        it
                    )
                    downloadHandler.cancel(identifier)
                }
            }
        )

        downloadHandler.download(
            successfulTestRequest2,
            null,
            { identifier ->
                identifier!!
                downloadHandler.status(identifier) {
                    it!!
                    Assert.assertNotEquals(
                        DownloadStatus.State.FAILED,
                        it
                    )
                    Assert.assertNotEquals(
                        DownloadStatus.State.SUCCESSFUL,
                        it
                    )
                    downloadHandler.cancel(identifier)
                }
            }
        )
    }

    @Test
    fun testDownloadStatusAfterFailure() {
        var called = false
        var id: I? = null

        downloadHandler.download(
            failingTestRequest,
            { status ->
                if (status?.state == DownloadStatus.State.FAILED) {
                    called = true
                }
            },
            { identifier ->
                identifier!!
                id = identifier
            }
        )

        waitWhile({ !called })

        downloadHandler.status(id!!) {
            it!!
            Assert.assertEquals(DownloadStatus.State.FAILED, it.state)
            Assert.assertNotEquals(it.downloadedBytes, it.totalBytes)
        }
    }

    private fun waitWhile(condition: () -> Boolean, timeout: Long = 300000) {
        var waited = 0
        while (condition()) {
            Thread.sleep(100)
            waited += 100
            if (waited > timeout) {
                throw Exception()
            }
        }
    }
}
