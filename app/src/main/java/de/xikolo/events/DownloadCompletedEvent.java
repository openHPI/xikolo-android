package de.xikolo.events;

import de.xikolo.events.base.Event;

public class DownloadCompletedEvent extends Event {

    public String url;

    public DownloadCompletedEvent(String url) {
        super(DownloadCompletedEvent.class.getSimpleName() + ": url = " + url);
        this.url = url;
    }

}
