package com.cheep.firebase.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

/**
 * Created by pankaj on 9/30/16.
 */

@IgnoreExtraProperties
public class TaskChatModel
{
    @Exclude
    public String categoryName="";
    @Exclude
    public String taskDesc="";

    @Exclude
    public long totalParticipants=0;

    @Exclude
    public long lastTimestamp;
    /*
    * added by sanjay
    * */
    public String chatId="";
    public String taskId="";
    public long unreadCount=0;
    public String messageId="";
    public String senderId="";
    public String message="";
    public String mediaUrl="";
    public String mediaThumbUrl="";
    public String receiverId="";
    public String messageType="";
    public long timestamp;
    /***************************/
    public boolean unreadMsg;

    @Exclude
    public String participantName="";

    @Exclude
    public String participantPhotoUrl="";

    @Exclude
    public String isSpSelected="0";

    public TaskChatModel() {
    }

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
