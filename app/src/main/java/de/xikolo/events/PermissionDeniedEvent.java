package de.xikolo.events;


import de.xikolo.events.base.Event;

public class PermissionDeniedEvent extends Event {

    private int rC;


    public PermissionDeniedEvent(int requestCode) {
        super(PermissionDeniedEvent.class.getSimpleName() + ": id = " + requestCode);
        this.rC = requestCode;
    }


    public int getRequestCode() {
        return rC;
    }

}
