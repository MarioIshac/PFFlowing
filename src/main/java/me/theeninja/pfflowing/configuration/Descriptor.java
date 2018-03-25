package me.theeninja.pfflowing.configuration;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Descriptor {
    @Expose
    @SerializedName("group")
    private final String group;

    @Expose
    @SerializedName("name")
    private final String name;

    @Expose
    @SerializedName("description")
    private final String description;

    Descriptor(String group, String name, String description) {
        this.group = group;
        this.name = name;
        this.description = description;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
