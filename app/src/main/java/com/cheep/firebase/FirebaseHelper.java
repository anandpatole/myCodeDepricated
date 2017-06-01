package com.cheep.firebase;

import android.content.Context;
import android.text.TextUtils;

import com.cheep.BuildConfig;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper
{

    /*
    * Change this variable when app is in debug/live mode according to your need.
    * */
    public static final String CHAT_NODE = BuildConfig.CHAT_NODE;

    /*
    * Firebase root nodes
    * */
    public static final String NODE_USERS = "users";
    public static final String NODE_SERVICEPROVIDER = "serviceProvider";
    public static final String NODE_TASKS = "tasks";
    public static final String NODE_RECENTCHATS="recentChats";
    public static final String NODE_TASKCHATS = "taskChats";
    public static final String NODE_MESSAGES = "messages";
    public static final String NODE_QUEUE = "queue";
    private static final String NODE_SERVER_TIMESTAMP = "serverTimestamp";

    /*
    * Firebase node keys
    * */
    public static final String KEY_BLOCKSPS="blockSPs";
    public static final String KEY_TIMESTAMP="timestamp";
    public static final String KEY_UNREADCOUNT="unreadCount";
    public static final String KEY_SELECTEDSPID="selectedSPId";
    public static final String KEY_TASK_STATUS="taskStatus";

    /*
    * Database reference
    * */
    private static DatabaseReference firebase;
    public static DatabaseReference getBaseRef()
    {
        if (firebase == null)
            firebase = FirebaseDatabase.getInstance().getReference();
        return firebase;
    }

    /*
    * Get user node reference
    * */
    public static DatabaseReference getUsersRef(String userId)
    {
        return getBaseRef().child(CHAT_NODE).child(NODE_USERS).child(userId);
    }

    /**
     * get service selectedProvider node reference
     */
    public static DatabaseReference getServiceProviderRef(String spId)
    {
        return getBaseRef().child(CHAT_NODE).child(NODE_SERVICEPROVIDER).child(spId);
    }

    /*
     * get the task node reference
     */
    public static DatabaseReference getTaskRef(String taskId)
    {
        return getBaseRef().child(CHAT_NODE).child(NODE_TASKS).child(taskId);
    }

    /*
     * get the recent chat node reference
     */
    public static DatabaseReference getRecentChatRef(String userId)
    {
        return getBaseRef().child(CHAT_NODE).child(NODE_RECENTCHATS).child(userId);
    }

    /*
     * get the task chat node reference
     */
    public static DatabaseReference getTaskChatRef(String taskId)
    {
        return getBaseRef().child(CHAT_NODE).child(NODE_TASKCHATS).child(taskId);
    }

    /**
     * get the message node reference
     */
    public static DatabaseReference getMessagesRef(String chatId)
    {
        return getBaseRef().child(CHAT_NODE).child(NODE_MESSAGES).child(chatId);
    }

    /**
     * get the message queue node reference
     * @return
     */
    public static DatabaseReference getMessageQueueRef()
    {
        return getBaseRef().child(CHAT_NODE).child(NODE_QUEUE).child(NODE_TASKS);
    }

    /*********************************************************************************************************************
                                                Utility Methods
    **********************************************************************************************************************/
    public static void updateTaskStatus(String taskId,String taskStatus)
    {
        if(!TextUtils.isEmpty(taskId) && !TextUtils.isEmpty(taskStatus))
        {
            String formattedTaskId = FirebaseUtils.getPrefixTaskId(taskId);
            getTaskRef(formattedTaskId).child(KEY_TASK_STATUS).setValue(taskStatus);
        }
    }

}
