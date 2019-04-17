package com.dc.exoplayersample.video;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VideoResponse {
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("sources")
    @Expose
    private String sources;
    @SerializedName("subtitle")
    @Expose
    private String subtitle;
    @SerializedName("thumb")
    @Expose
    private String thumb;
    @SerializedName("title")
    @Expose
    private String title;

    public String getDescription() {
        return description;
    }

    public String getSources() {
        return sources;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getThumb() {
        return thumb;
    }

    public String getTitle() {
        return title;
    }
}
