/*
  Copyright 2015 Google Inc. All Rights Reserved.
  <p/>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p/>
  http://www.apache.org/licenses/LICENSE-2.0
  <p/>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.cheep.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.cheep.R;
import com.cheep.activity.ChatActivity;
import com.cheep.activity.HomeActivity;
import com.cheep.activity.NotificationActivity;
import com.cheep.firebase.model.ChatNotification;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.model.MessageEvent;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.freshdesk.hotline.Hotline;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "onMessageReceived() called with: remoteMessage = [" + remoteMessage + "]");
        //Not taking any action if user is not logged in simply return
        if (PreferenceUtility.getInstance(getApplicationContext()).getUserDetails() == null) {
            return;
        }

        // Need to check if notification coming is for Hoteline SDK
        if (Hotline.isHotlineNotification(remoteMessage)) {
            Hotline.getInstance(this).handleFcmMessage(remoteMessage);
            return;
        }

        Map<String, String> map = remoteMessage.getData();
        if (map != null) {
            String notificationType = map.get(NetworkUtility.TAGS.TYPE);
            if (Utility.NOTIFICATION_TYPE.CHAT_MESSAGE.equalsIgnoreCase(notificationType)) {
                JsonElement jsonElement = new Gson().toJsonTree(map);
                ChatNotification chatNotification = new Gson().fromJson(jsonElement, ChatNotification.class);
                if (chatNotification != null) {
                    generateNotification(chatNotification);
                }
                return;
            } /*else if (Utility.NOTIFICATION_TYPE.TASK_CREATE.equalsIgnoreCase(notificationType)) {
                // Do nothing go ahead
            } else {
                // return from here as we couldnt get any NotificationType
                return;
            }*/
        }

        String from = remoteMessage.getFrom();
        String message = map.get("message");
        String title = map.get("title");


        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Title: " + title);

        /*
          In case Message is Empty DON'T Go ahead as it might be Dummy Notification sent by
         */
        if (TextUtils.isEmpty(message)) {
            return;
        }

        Bundle bnd = new Bundle();
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Log.d(TAG, entry.getKey() + ": " + entry.getValue());
            bnd.putString("" + entry.getKey(), "" + entry.getValue());
        }

        /*
          In some cases it may be useful to show a notification indicating to the user
          that a message was received.
         */
        int notificationId = 0;
        try {
            notificationId = Integer.parseInt(bnd.getString(NetworkUtility.TAGS.TASK_ID));
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendNotification(notificationId, title, message, bnd);
        PreferenceUtility.getInstance(getApplicationContext()).incrementUnreadNotificationCounter();

        // Broadcast NEW Notification action
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.NEW_NOTIFICATION;
        EventBus.getDefault().post(messageEvent);

        // Broadcast Notification for QUOTE_REQUEST type
        if (bnd.getString(NetworkUtility.TAGS.TYPE).equalsIgnoreCase(Utility.NOTIFICATION_TYPE.QUOTE_REQUEST)) {
            messageEvent = new MessageEvent();
            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.QUOTE_REQUESTED_BY_PRO;
            messageEvent.max_quote_price = bnd.getString(NetworkUtility.TAGS.MAX_QUOTE_PRICE);
            messageEvent.sp_counts = bnd.getString(NetworkUtility.TAGS.SP_COUNTS);
            messageEvent.quoted_sp_image_url = bnd.getString(NetworkUtility.TAGS.QUOTED_SP_IMAGE_URL);
            messageEvent.id = String.valueOf(notificationId);
            EventBus.getDefault().post(messageEvent);
        }
        // Broadcast Notification for REQUEST_FOR_DETAIL type
        else if (bnd.getString(NetworkUtility.TAGS.TYPE).equalsIgnoreCase(Utility.NOTIFICATION_TYPE.REQUEST_FOR_DETAIL)) {
            messageEvent = new MessageEvent();
            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.REQUEST_FOR_DETAIL;
            messageEvent.id = String.valueOf(notificationId);
            messageEvent.sp_counts = bnd.getString(NetworkUtility.TAGS.SP_COUNTS);
            messageEvent.quoted_sp_image_url = bnd.getString(NetworkUtility.TAGS.QUOTED_SP_IMAGE_URL);
            messageEvent.request_detail_status = bnd.getString(NetworkUtility.TAGS.REQUEST_DETAIL_STATUS);
            EventBus.getDefault().post(messageEvent);
        }
        // Broadcast Notification when task status got changed
        else if (bnd.getString(NetworkUtility.TAGS.TYPE).equalsIgnoreCase(Utility.NOTIFICATION_TYPE.TASK_STATUS_CHANGE)) {
            messageEvent = new MessageEvent();
            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_STATUS_CHANGE;
            messageEvent.id = String.valueOf(notificationId);
            if (TextUtils.isEmpty(bnd.getString(NetworkUtility.TAGS.TASK_STATUS))) {
                return;
            }
            messageEvent.taskStatus = bnd.getString(NetworkUtility.TAGS.TASK_STATUS);
            EventBus.getDefault().post(messageEvent);
        }
        // Broadcast when additional payment requested
        else if (bnd.getString(NetworkUtility.TAGS.TYPE).equalsIgnoreCase(Utility.NOTIFICATION_TYPE.ADDITIONAL_PAYMENT_REQUESTED)) {
            messageEvent = new MessageEvent();
            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.ADDITIONAL_PAYMENT_REQUESTED;
            messageEvent.id = String.valueOf(notificationId);
            if (TextUtils.isEmpty(bnd.getString(NetworkUtility.TAGS.TASK_STATUS))) {
                return;
            }
            messageEvent.taskStatus = bnd.getString(NetworkUtility.TAGS.TASK_STATUS);
            messageEvent.additional_quote_amount = bnd.getString(NetworkUtility.TAGS.ADDITIONAL_QUOTE_AMOUNT);

            EventBus.getDefault().post(messageEvent);
        }
        // Broadcast when Task Create ALERT Event
        else if (bnd.getString(NetworkUtility.TAGS.TYPE).equalsIgnoreCase(Utility.NOTIFICATION_TYPE.TASK_START_ALERT)) {
            messageEvent = new MessageEvent();
            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_START_ALERT;
            messageEvent.id = String.valueOf(notificationId);
            messageEvent.total_ongoing_task = bnd.getString(NetworkUtility.TAGS.TOTAL_ONGOING_TASK);
            EventBus.getDefault().post(messageEvent);
        }

    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(int notificationId, String title, String message, Bundle bnd) {
        /*//Start landing page screen
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bnd);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId *//* Request code *//*, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_app_icon)
                .setContentTitle(title)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.splash_gradient_end))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId *//* ID of notification *//*, notificationBuilder.build());*/


        /*
          if type is WEB_CUSTOM_NOTIFICATION we need to redirect the user to Notification Screen,
          else we need to redirect the user to HomeActivity.
         */
        if (Utility.NOTIFICATION_TYPE.WEB_CUSTOM_NOTIFICATION.equalsIgnoreCase(bnd.getString(NetworkUtility.TAGS.TYPE))) {
            //Start  Notification Screen
            Intent intent = new Intent(this, NotificationActivity.class);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addParentStack(NotificationActivity.class);
            taskStackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0/* Pass 0 to make the WebNotification ID Unique*/, PendingIntent.FLAG_UPDATE_CURRENT);

//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            intent.putExtras(bnd);
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0/* Pass 0 to make the WebNotification ID Unique*/, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.notification_app_icon)
                    .setContentTitle(title)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.splash_gradient_end))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0/* Pass 0 to make the WebNotification ID Unique*/, notificationBuilder.build());

        } else {
            //Start landing page screen
            Intent intent = new Intent(this, HomeActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtras(bnd);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.notification_app_icon)
                    .setContentTitle(title)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.splash_gradient_end))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build());
        }


    }

    public static Map<String, ArrayList<String>> mapMessages = new HashMap<>();
    public static ArrayList<String> privateMessages = new ArrayList<>();
    /*
     * Chat message notification id
     * */
    public static int MESSAGE_NOTIFICATION_ID = 1;

    /**
     * Used to generate notification
     */
    private void generateNotification(ChatNotification chatNotification) {
        if (!TextUtils.isEmpty(chatNotification.chatId) && !chatNotification.chatId.equalsIgnoreCase(Utility.CURRENT_CHAT_ID)) {
            generateChatNotification(this, chatNotification);
        }
    }

    /**
     * CHAT NOTIFICATIONS
     */
    public void generateChatNotification(Context context, ChatNotification chatNotification) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (mapMessages == null) {
            mapMessages = new HashMap<>();
        }
        if (privateMessages == null) {
            privateMessages = new ArrayList<>();
        }

        privateMessages.add(String.format("%s,%s", chatNotification.title, chatNotification.message));
        mapMessages.put(chatNotification.chatId, privateMessages);

        NotificationCompat.Builder builder = createMessageNotificationBuilder(context, chatNotification);

        builder.setDefaults(Notification.DEFAULT_LIGHTS
                | Notification.DEFAULT_VIBRATE);
        builder.setSound(soundUri);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.splash_gradient_end));

        builder.setNumber(privateMessages.size());
        //Look up the notification manager service.
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        //Pass the notification to the NotificationManager.
        nm.notify(MESSAGE_NOTIFICATION_ID, builder.build());
    }

    private static NotificationCompat.Builder createMessageNotificationBuilder(Context context, ChatNotification chatNotification) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if (mapMessages.size() > 1) {
            String formatedText = String.format(Locale.US, "%d" + context.getString(R.string.new_)+ " %s", getActiveMessageCount(), getActiveMessageCount() > 1
                    ? context.getString(R.string.messages) : context.getString(R.string.message));
            builder.setContentTitle(formatedText);
        } else {
            builder.setContentTitle(chatNotification.title);
        }
        if (getActiveMessageCount() > 1) {
            builder.setContentText(privateMessages.size()
                    + context.getString(R.string.messages_from) + mapMessages.size()
                    + context.getString(R.string.conversations));
        } else {
            SpannableString ss2 = new SpannableString(" " + chatNotification.message);
            ss2.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorSemiGray)), 0, ss2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setContentText(ss2);
        }

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        builder.setSmallIcon(R.drawable.notification_app_icon);
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());

        if (mapMessages.size() > 1) {
            if (Build.VERSION.SDK_INT >= 19) // KITKAT
            {
                PendingIntent pi = createNotificationMessageActivityPendingIntent(context, chatNotification);
                if (pi != null) {
                    pi.cancel();
                }
            }

            PendingIntent msgPendingIntent = createNotificationMessageActivityPendingIntent(context, chatNotification);
            builder.setContentIntent(msgPendingIntent);
        } else {
            if (Build.VERSION.SDK_INT >= 19) // KITKAT
            {
                PendingIntent pi = createChatActivityPendingIntent(context, chatNotification);
                if (pi != null) {
                    pi.cancel();
                }
            }
            PendingIntent msgPendingIntent = createChatActivityPendingIntent(context, chatNotification);
            builder.setContentIntent(msgPendingIntent);
        }

        android.support.v7.app.NotificationCompat.InboxStyle inboxStyle = new android.support.v7.app.NotificationCompat.InboxStyle();
        if ((mapMessages != null) && (mapMessages.size() > 0)) {
            for (int i = privateMessages.size() - 1; i >= 0; i--) {
                if (!TextUtils.isEmpty(privateMessages.get(i))) {
                    String[] arr = privateMessages.get(i).split(",");
                    SpannableStringBuilder sb = new SpannableStringBuilder();
                    if (arr.length > 1) {
                        if (mapMessages.size() > 1) {
                            SpannableString ss1 = new SpannableString(arr[0]);
                            ss1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorGray)), 0, ss1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            sb.append(ss1);
                        }
                        SpannableString ss2 = new SpannableString(" " + arr[1]);
                        ss2.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorSemiGray)), 0, ss2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        sb.append(ss2);
                    }
                    inboxStyle.addLine(sb);
                }
            }
        }

        if ((mapMessages != null) && (getActiveMessageCount() >= 1)) {
            if (getActiveMessageCount() > 1)
                inboxStyle.setSummaryText(privateMessages.size()
                        + context.getString(R.string.messages_from) + mapMessages.size()
                        + context.getString(R.string.conversations));
            builder.setStyle(inboxStyle);
        }
        return builder;
    }

    /**
     * Used to get count of active messages
     *
     * @return
     */
    private static int getActiveMessageCount() {
        int cnt = 0;
        if (privateMessages != null) {
            cnt = privateMessages.size();
        }
        return cnt;
    }

    //IT WILL OPEN SINGLE CHAT ACTIVITY
    private static PendingIntent createChatActivityPendingIntent(Context context, ChatNotification chatNotification) {
        //Create an intent to start DalChatActivity.
        Intent msgIntent = null;
        msgIntent = new Intent(context, ChatActivity.class);
        msgIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (chatNotification != null) {
            TaskChatModel taskChatModel = new TaskChatModel();
            taskChatModel.participantName = chatNotification.title;
            taskChatModel.chatId = chatNotification.chatId;
            taskChatModel.taskId = chatNotification.taskId;
            taskChatModel.messageId = chatNotification.messageId;
            taskChatModel.senderId = chatNotification.senderId;
            taskChatModel.receiverId = chatNotification.receiverId;
            taskChatModel.message = chatNotification.message;
            taskChatModel.messageType = chatNotification.messageType;
            taskChatModel.isSpSelected = chatNotification.isSpSelected;
            taskChatModel.timestamp = chatNotification.timestamp;
            msgIntent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskChatModel));
        }
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //Add the back stack to the stack builder. This method also adds flags
        //that start the stack in a fresh task.
        stackBuilder.addParentStack(ChatActivity.class);

        //Add the Intent that starts the Activity from the notification.
        stackBuilder.addNextIntent(msgIntent);

        //Get a PendingIntent containing the entire back stack.
        PendingIntent msgPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return msgPendingIntent;
    }

    //IT WILL OPEN RECENT CHAT SCREEN.
    private static PendingIntent createNotificationMessageActivityPendingIntent(Context context, ChatNotification chatNotification) {
        //Start DalMainActivity in the message threads position.
        Intent msgIntent = new Intent(context, HomeActivity.class);
        msgIntent.putExtra(Utility.Extra.CHAT_NOTIFICATION_DATA, chatNotification);
        msgIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //Add the back stack to the stack builder. This method also adds flags
        //that start the stack in a fresh task.
        stackBuilder.addParentStack(HomeActivity.class);

        //Add the Intent that starts the Activity from the notification.
        stackBuilder.addNextIntent(msgIntent);

        //Get a PendingIntent containing the entire back stack.
        PendingIntent msgPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return msgPendingIntent;
    }

    public static void clearNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(MESSAGE_NOTIFICATION_ID);
        if (mapMessages != null)
            mapMessages.clear();
        if (privateMessages != null)
            privateMessages.clear();
    }
}
