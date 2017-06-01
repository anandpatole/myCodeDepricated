package com.cheep.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pankaj on 10/5/16.
 */

public class NotificationModel {

    @SerializedName("type")
    public String notificationType;

    @SerializedName("sp_user_id")
    public long sp_user_id;

    @SerializedName("sp_user_name")
    public String sp_user_name;


    @SerializedName("sp_profile_image")
    public String sp_profile_image;

    @SerializedName("message")
    public String message;


    @SerializedName("task_id")
    public String task_id;

    @SerializedName("datetime")
    public String datetime;

}
