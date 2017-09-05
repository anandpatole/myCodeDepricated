package com.cheep.strategicpartner.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by giteeka on 31/7/17.
 * Media model for Type image and video
 */

@Keep
public class MediaModel implements Serializable {

    @SerializedName("media_name")
    public String mediaName = "";

    @SerializedName("media_thumb_name")
    public String mediaThumbName = "";

    @SerializedName("media_type")
    public String mediaType;


    public String localFilePath;


    public class MediaType {
        public static final String TYPE_IMAGE = "image";
        public static final String TYPE_VIDEO = "video";
    }

    public MediaModel() {

    }

}
