package de.xikolo.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("short_name")
    public String short_name;

    @SerializedName("email")
    public String email;

    @SerializedName("birthdate")
    public String birthdate;

    @SerializedName("gender")
    public String gender;

    @SerializedName("time_zone")
    public String time_zone;

    @SerializedName("company")
    public String company;

}
