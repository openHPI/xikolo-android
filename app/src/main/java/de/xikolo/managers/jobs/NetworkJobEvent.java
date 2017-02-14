package de.xikolo.managers.jobs;

public abstract class NetworkJobEvent {

    public enum State {
        SUCCESS, WARNING, ERROR
    }

    private State state;

    public State getState() {
        return state;
    }

    public enum Code {
        NO_NETWORK, NO_RESULT, NO_AUTH, CANCELED
    }

    private Code code;

    public Code getCode() {
        return code;
    }

    private String id;

    public String getId() {
        return id;
    }

    public NetworkJobEvent(State state, Code code, String id) {
        this.state = state;
        this.code = code;
        this.id = id;
    }

    public String getMessage() {
        String message = this.getClass().getSimpleName() + " " + getState().name();
        if (code != null) {
            message += " " + code;
        }
        if (id != null) {
            message += " ID: " + id;
        }
        return message;
    }

}
