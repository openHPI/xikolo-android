package de.xikolo.managers.jobs;

public abstract class NetworkJobEvent {

    public enum State {
        SUCCESS, CANCEL, ERROR, NO_NETWORK, NO_AUTH
    }

    private State state;

    public State getState() {
        return state;
    }

    public enum Code {

    }

    private String id;

    public String getId() {
        return id;
    }

    public NetworkJobEvent(State state, String id) {
        this.state = state;
        this.id = id;
    }

    public String getMessage() {
        String message = this.getClass().getSimpleName() + " " + getState().name();
        if (id != null) {
            message += " ID: " + id;
        }
        return message;
    }

}
