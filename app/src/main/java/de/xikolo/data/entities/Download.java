package de.xikolo.data.entities;

import com.koushikdutta.async.future.Future;

import java.io.File;

public class Download {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_FAILED = 1;
    public static final int STATUS_RUNNING = 2;
    public static final int STATUS_SUCCESSFUL = 3;
    public static final int STATUS_CANCELLED = 4;

    public String title;
    public String localFilename;
    public String uri;
    public long totalSizeBytes = 1;
    public long bytesDownloadedSoFar;
    public int status = STATUS_PENDING;

    private Future<File> fileFuture;

    public Download() {}

    public Download(String title, String localFilename, String uri) {
        this.title = title;
        this.localFilename = localFilename;
        this.uri = uri;
    }

    public void setFileFuture(Future<File> fileFuture) {
        this.fileFuture = fileFuture;
    }

    public void cancel() {
        if (!fileFuture.isCancelled()) {
            fileFuture.cancel();
        }
        status = STATUS_CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Download)) return false;

        Download download = (Download) o;

        return !(localFilename != null ? !localFilename.equals(download.localFilename) : download.localFilename != null);

    }

    @Override
    public int hashCode() {
        return localFilename != null ? localFilename.hashCode() : 0;
    }
    
}
