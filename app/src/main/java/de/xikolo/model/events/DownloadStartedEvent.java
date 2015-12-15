package de.xikolo.model.events;

import de.xikolo.data.entities.Download;

public class DownloadStartedEvent extends Event {

    private String url;

    public DownloadStartedEvent(String url) {
        super(DownloadStartedEvent.class.getSimpleName() + ": url = " + url);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
    
}
