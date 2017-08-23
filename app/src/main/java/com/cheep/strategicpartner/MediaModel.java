package com.cheep.strategicpartner;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

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

    class MediaType {
        public static final String TYPE_IMAGE = "image";
        public static final String TYPE_VIDEO = "video";
    }

    private MediaModel() {

    }

    MediaModel(String path, String type) {
        this.mediaName = path;
        this.mediaType = type;
    }


    public static ArrayList<MediaModel> getDummyData() {
        ArrayList<MediaModel> dummyData = new ArrayList<>();
        MediaModel mediaModel = new MediaModel();
        mediaModel.mediaName = "https://allthingslearning.files.wordpress.com/2012/06/learning-dummy.png";
        mediaModel.mediaThumbName = "https://s3.ap-south-1.amazonaws.com/cheepapp/task_image/original/1503037211_5996871b479bb_image.jpg";
        mediaModel.mediaType = MediaType.TYPE_IMAGE;
        dummyData.add(mediaModel);

        mediaModel = new MediaModel();
        mediaModel.mediaName = "http://phayse.com/wp-content/uploads/2017/07/360-demo-407x229.jpg";
        mediaModel.mediaThumbName = "https://s3.ap-south-1.amazonaws.com/cheepapp/task_image/original/1503037211_5996871b2c944_image.jpg";
        mediaModel.mediaType = MediaType.TYPE_IMAGE;
        dummyData.add(mediaModel);


        mediaModel = new MediaModel();
        mediaModel.mediaName = "https://s3.ap-south-1.amazonaws.com/cheepapp/task_image/original/1503037210_5996871ab98be_image.mp4";
        mediaModel.mediaThumbName = "http://farm5.staticflickr.com/4115/4821890462_a0bce6010c.jpg";
        mediaModel.mediaType = MediaType.TYPE_VIDEO;
        dummyData.add(mediaModel);


        return dummyData;
    }

}
