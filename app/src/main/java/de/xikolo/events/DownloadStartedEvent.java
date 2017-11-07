package de.xikolo.events;

import de.xikolo.events.base.Event;
import de.xikolo.utils.DownloadUtil;

public class DownloadStartedEvent extends Event {

    public String itemId;

    public DownloadUtil.VideoAssetType type;

    public DownloadStartedEvent(String itemId, DownloadUtil.VideoAssetType type) {
        super(DownloadCompletedEvent.class.getSimpleName() + ": itemId = " + itemId + ", type = " + type.name());
        this.itemId = itemId;
        this.type = type;
    }
    
}
