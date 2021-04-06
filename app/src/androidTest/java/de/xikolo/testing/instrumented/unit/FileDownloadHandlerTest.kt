package de.xikolo.testing.instrumented.unit

import de.xikolo.download.DownloadCategory
import de.xikolo.download.filedownload.FileDownloadHandler
import de.xikolo.download.filedownload.FileDownloadIdentifier
import de.xikolo.download.filedownload.FileDownloadRequest
import de.xikolo.testing.instrumented.mocking.SampleMockData
import de.xikolo.utils.extensions.preferredStorage
import io.mockk.every
import io.mockk.spyk

class FileDownloadHandlerTest : DownloadHandlerTest<FileDownloadHandler,
    FileDownloadIdentifier, FileDownloadRequest>() {

    override var downloadHandler = spyk(FileDownloadHandler, recordPrivateCalls = true) {
        every { this@spyk getProperty "context" } answers { context }
    }

    override var successfulTestRequest = FileDownloadRequest(
        SampleMockData.mockVideoStreamSdUrl,
        createTempFile(directory = context.preferredStorage.file),
        "File 1",
        true,
        DownloadCategory.Other
    )
    override var successfulTestRequest2 = FileDownloadRequest(
        SampleMockData.mockVideoStreamThumbnailUrl,
        createTempFile(directory = context.preferredStorage.file),
        "File 2",
        true,
        DownloadCategory.Other
    )
    override var failingTestRequest = FileDownloadRequest(
        "https://www.example.com/notfoundfilehwqnqkdrzn42r.mp4",
        createTempFile(directory = context.preferredStorage.file),
        "Failing File",
        true,
        DownloadCategory.Other
    )
}
