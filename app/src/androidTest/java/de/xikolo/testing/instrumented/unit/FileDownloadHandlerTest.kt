package de.xikolo.testing.instrumented.unit

import de.xikolo.download.filedownload.FileDownloadHandler
import de.xikolo.download.filedownload.FileDownloadIdentifier
import de.xikolo.download.filedownload.FileDownloadRequest
import de.xikolo.testing.instrumented.mocking.SampleMockData
import de.xikolo.utils.extensions.preferredStorage

class FileDownloadHandlerTest : DownloadHandlerTest<FileDownloadHandler,
    FileDownloadIdentifier, FileDownloadRequest>() {

    override var downloadHandler = FileDownloadHandler
    override var successfulTestRequest = FileDownloadRequest(
        SampleMockData.mockVideoStreamSdUrl,
        createTempFile(directory = context.preferredStorage.file),
        "TITLE",
        true
    )
    override var successfulTestRequest2 = FileDownloadRequest(
        SampleMockData.mockVideoStreamThumbnailUrl,
        createTempFile(directory = context.preferredStorage.file),
        "TITLE",
        true
    )
    override var failingTestRequest = FileDownloadRequest(
        "https://www.example.com/notfoundfilehwqnqkdrzn42r.mp4",
        createTempFile(directory = context.preferredStorage.file),
        "TITLE",
        true
    )
    override var invalidIdentifier = FileDownloadIdentifier(-1)
}
