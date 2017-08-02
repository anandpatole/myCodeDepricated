package com.cheep.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pankaj on 10/10/16.
 */
@Keep
public class CommentsModel {

    @SerializedName("comment_id")
    public String comment_id;
    @SerializedName("commenter_user_id")
    public String commenter_user_id;
    @SerializedName("commenter_name")
    public String commenter_name;
    @SerializedName("commenter_profile_image")
    public String commenter_profile_image;
    @SerializedName("comment")
    public String comment;
    @SerializedName("comment_date")
    public String comment_date;

    /*
    public String name;
    public long timestamp;
    public String message;
    public String photoUrl;

    //Testing fields
    public int photoUrlRes;
    public String date;

    public CommentsModel(String name, long timestamp, String message, String photoUrl, int photoUrlRes, String date) {
        this.name = name;
        this.timestamp = timestamp;
        this.message = message;
        this.photoUrl = photoUrl;
        this.photoUrlRes = photoUrlRes;
        this.date = date;


    }*/
}
