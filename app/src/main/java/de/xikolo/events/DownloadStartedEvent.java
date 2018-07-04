package de.xikolo.events;

import de.xikolo.events.base.Event;
import de.xikolo.utils.DownloadUtil;

public class DownloadStartedEvent extends Event {

    public DownloadUtil.AssetDownload download;

    public DownloadStartedEvent(DownloadUtil.AssetDownload download) {
        super(DownloadCompletedEvent.class.getSimpleName() + ": filePath = " + download.getFilePath() + ", type = " + download.getAssetType().toString());
        this.download = download;
    }
}
