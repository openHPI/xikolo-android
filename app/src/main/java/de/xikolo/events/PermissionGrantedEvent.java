package de.xikolo.events;


import de.xikolo.events.base.Event;

public class PermissionGrantedEvent extends Event {

    private int rC;


    public PermissionGrantedEvent(int requestCode) {
        super(PermissionGrantedEvent.class.getSimpleName() + ": id = " + requestCode);
        this.rC = requestCode;
    }


    public int getRequestCode() {
        return rC;
    }

}
