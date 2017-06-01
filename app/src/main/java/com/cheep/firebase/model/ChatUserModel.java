package com.cheep.firebase.model;

import android.text.TextUtils;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by sanjay on 27/7/16.
 */
@IgnoreExtraProperties
public class ChatUserModel implements Serializable
{
    private String userId;
    private String userName;
    private String profileImg;

    public ChatUserModel()
    {

    }

    public String getUserId()
    {
        if(TextUtils.isEmpty(userId))
            return "";
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName()
    {
        if(TextUtils.isEmpty(userName))
            return "";
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileImg()
    {
        if(TextUtils.isEmpty(profileImg))
            return "";
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }
}
