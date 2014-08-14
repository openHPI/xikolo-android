package de.xikolo.entities;

import com.google.gson.annotations.SerializedName;

public class Progression {

    @SerializedName("item_id")
    public String item_id;

    @SerializedName("visited")
    public boolean visited;

    @SerializedName("completed")
    public boolean completed;

}
