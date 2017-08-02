package com.cheep.firebase.model;

import android.support.annotation.Keep;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

/**
 * Created by sanjay on 21/10/16.
 */
@IgnoreExtraProperties
@Keep
public class TaskChatMessageModel {
    public String chatId = "";
    public String taskId;
    public String messageId;
    public String senderId;
    public String receiverId;
    public String message = "";
    public String messageType;
    public Double quoteAmount = 0d;
    public String mediaUrl = "";
    public String mediaThumbUrl = "";

    @Exclude
    public String localMediaUrl = "";

    public long mediaFileSize = 0;
    public long timestamp;

    public TaskChatMessageModel() {
    }

    public TaskChatMessageModel(String senderId, String receiverId, String message, String localMediaPath, String messageType, Double quoteAmount, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.mediaUrl = localMediaPath;
        this.messageType = messageType;
        this.quoteAmount = quoteAmount;
        this.timestamp = timestamp;
    }

    @Exclude
    public long getTimestampLong() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    //only used by firebase to replace the value with server's time stamp and store it in the above timestamp variable.
    public java.util.Map<String, String> getTimestamp() {
        return ServerValue.TIMESTAMP;
    }
}
