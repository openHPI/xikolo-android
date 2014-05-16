package de.xikolo.openhpi.model;

import com.google.gson.annotations.SerializedName;

public class Course {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("course_code")
    public String course_code;

    @SerializedName("lecturer")
    public String lecturer;

    @SerializedName("language")
    public String language;

    @SerializedName("duration")
    public String duration;

    @SerializedName("url")
    public String url;

    @SerializedName("visual_url")
    public String visual_url;

    @SerializedName("available_from")
    public String available_from;

    @SerializedName("available_to")
    public String available_to;

    @SerializedName("locked")
    public boolean locked;

    @SerializedName("exam_available_from")
    public String exam_available_from;

    @SerializedName("exam_available_to")
    public String exam_available_to;

}
