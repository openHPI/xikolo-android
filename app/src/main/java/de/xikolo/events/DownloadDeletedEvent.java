package de.xikolo.events;

import de.xikolo.events.base.Event;
import de.xikolo.utils.DownloadUtil;

public class DownloadDeletedEvent extends Event {

    public DownloadUtil.AssetDownload download;

    public DownloadDeletedEvent(DownloadUtil.AssetDownload download) {
        super(DownloadCompletedEvent.class.getSimpleName() + ": filePath = " + download.getFilePath() + ", type = " + download.getAssetType().toString());
        this.download = download;
    }

}
