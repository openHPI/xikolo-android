package de.xikolo.model.events;

import de.xikolo.data.entities.Download;

public class DownloadCompletedEvent extends Event {

    private Download dl;

    public DownloadCompletedEvent(Download download) {
        super(DownloadCompletedEvent.class.getSimpleName() + ": id = " + download.id + ", uri = " + download.uri);
        this.dl = download;
    }

    public Download getDownload() {
        return dl;
    }
    
}
