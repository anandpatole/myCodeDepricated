package com.cheep.firebase.model;

import android.support.annotation.Keep;
import android.text.TextUtils;

/**
 * Created by sanjay on 10/2/17.
 */
@Keep
public class ChatServiceProviderModel
{
    private String spId;
    private String spName;
    private String profileImg;

    public ChatServiceProviderModel()
    {
    }

    public String getSpId()
    {
        if(TextUtils.isEmpty(spId))
            return "";
        return spId;
    }

    public void setSpId(String spId) {
        this.spId = spId;
    }

    public String getSpName()
    {
        if(TextUtils.isEmpty(spName))
            return "";
        return spName;
    }

    public void setSpName(String spName) {
        this.spName = spName;
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
