package de.xikolo.openhpi.model;

import com.google.gson.annotations.SerializedName;

public class Course {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("url")
    public String url;

    @SerializedName("start_date")
    public String startDate;

    @SerializedName("end_date")
    public String endDate;

    @SerializedName("teacher")
    public String teacher;

    @SerializedName("cade")
    public String code;

    @SerializedName("abstract")
    public String shortAbstract;

    @SerializedName("description")
    public String description;

    @SerializedName("image")
    public String image;

    @SerializedName("language")
    public String language;

    @SerializedName("status")
    public String status;

}
