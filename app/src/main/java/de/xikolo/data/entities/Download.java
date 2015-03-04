package de.xikolo.data.entities;

public class Download {
    
    public long id;
    
    public String title;
    
    public String description;

    public String localFilename;

    public String localUri;

    public String uri;

    public int status;

    public int reason;
    
    public long totalSizeBytes;
    
    public long bytesDownloadedSoFar;
    
    public long lastModifiedTimestamp;
    
    public String mediaproviderUri;
    
    public String mediaType;

    public Download() {};
    
    public Download(long id, String title, String description, String localFilename, String localUri, String uri, int status, int reason) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.localFilename = localFilename;
        this.localUri = localUri;
        this.uri = uri;
        this.status = status;
        this.reason = reason;
    }

    public Download(long id, String title, String description, String localFilename, String localUri, String uri, int status, int reason, long totalSizeBytes, long bytesDownloadedSoFar, long lastModifiedTimestamp, String mediaproviderUri, String mediaType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.localFilename = localFilename;
        this.localUri = localUri;
        this.uri = uri;
        this.status = status;
        this.reason = reason;
        this.totalSizeBytes = totalSizeBytes;
        this.bytesDownloadedSoFar = bytesDownloadedSoFar;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
        this.mediaproviderUri = mediaproviderUri;
        this.mediaType = mediaType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Download)) return false;

        Download download = (Download) o;

        if (localFilename != null ? !localFilename.equals(download.localFilename) : download.localFilename != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return localFilename != null ? localFilename.hashCode() : 0;
    }
    
}
