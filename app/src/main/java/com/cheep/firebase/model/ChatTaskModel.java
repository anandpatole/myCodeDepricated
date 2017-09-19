package com.cheep.firebase.model;

import android.support.annotation.Keep;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

/**
 * Created by sanjay on 10/2/17.
 */
@Keep
public class ChatTaskModel
{
    public String userId;
    public String taskId;
    public String taskDesc;
    public String categoryId;
    public String categoryName;
    public String selectedSPId;
    public Long timestamp;


    @Exclude
    public long getTimestampLong() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    //only used by firebase to replace the value with server's time stamp and store it in the above timestamp variable.
    public java.util.Map<String, String> getTimestamp()
    {
        return ServerValue.TIMESTAMP;
    }
}
