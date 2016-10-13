package de.xikolo.events;

import de.xikolo.models.Item;

public class DownloadDeletedEvent extends Event {

    private Item item;

    public DownloadDeletedEvent(Item item) {
        super(DownloadDeletedEvent.class.getSimpleName() + ": item.id = " + item.id);
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
    
}
