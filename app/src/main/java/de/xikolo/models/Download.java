package de.xikolo.models;

public class Download {

    public int id;

    public String title;

    public String url;

    public String filePath;

    public long totalBytes;

    public long bytesWritten;

    public State state = State.PENDING;

    public enum State {
        PENDING,
        RUNNING,
        SUCCESSFUL,
        FAILURE,
    }

}
