package com.cheep.strategicpartner;

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
    String mediaName = "";

    @SerializedName("media_thumb_name")
    String mediaThumbName = "";

    @SerializedName("media_type")
    String mediaType;


    String localFilePath;


    class MediaType {
        public static final String TYPE_IMAGE = "image";
        public static final String TYPE_VIDEO = "video";
    }

    MediaModel() {

    }

}
