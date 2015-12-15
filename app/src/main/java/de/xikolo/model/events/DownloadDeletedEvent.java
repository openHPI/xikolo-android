package de.xikolo.model.events;

import de.xikolo.data.entities.Item;

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
