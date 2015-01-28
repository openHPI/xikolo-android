package de.xikolo.model.events;

public class NetworkStateEvent extends Event {

    private boolean online;

    public NetworkStateEvent(boolean isOnline) {
        super(NetworkStateEvent.class.getSimpleName() +  ": isOnline = " + isOnline);
        this.online = isOnline;
    }

    public boolean isOnline() {
        return online;
    }

}
