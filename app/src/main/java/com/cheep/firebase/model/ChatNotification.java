package com.cheep.firebase.model;

import java.io.Serializable;

/**
 * Created by sanjay on 11/3/17.
 */

public class ChatNotification implements Serializable
{
    public String title="";
    public String chatId="";
    public String taskId="";
    public String messageId="";
    public String senderId="";
    public String receiverId="";
    public String message="";
    public String messageType="";
    public String profileImg="";
    public long timestamp;
    public String isSpSelected="";

    public ChatNotification()
    {
    }
}
