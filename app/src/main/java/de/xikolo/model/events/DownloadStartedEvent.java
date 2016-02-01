package de.xikolo.model.events;

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
