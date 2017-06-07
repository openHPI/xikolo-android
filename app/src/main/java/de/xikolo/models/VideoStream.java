package de.xikolo.models;

import io.realm.RealmObject;

public class VideoStream extends RealmObject {

    public String hdUrl;

    public String sdUrl;

    public String hlsUrl;

    public int hdSize;

    public int sdSize;

    public String poster;

}
