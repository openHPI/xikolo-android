package de.xikolo.model.events;


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
