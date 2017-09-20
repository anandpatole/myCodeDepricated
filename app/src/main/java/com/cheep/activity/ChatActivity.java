package com.cheep.activity;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.adapter.ChatMessageRecyclerViewAdapter;
import com.cheep.databinding.ActivityChatBinding;
import com.cheep.fcm.MyFirebaseMessagingService;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatImageModel;
import com.cheep.firebase.model.TaskChatMessageModel;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cheep.utils.LogUtils.LOGI;
import static com.cheep.utils.LogUtils.makeLogTag;

/**
 * Created by Bhavesh Patadiya on 25/5/15.
 * Activity related to Chatting screen and its features.
 */
public class ChatActivity extends BaseAppCompatActivity implements View.OnClickListener {

    // Constants
    private static final String TAG = makeLogTag(ChatActivity.class);
    public static final String CHAT_STATUS_YES = "yes";
    public static final String CHAT_STATUS_NO = "no";

    private ActivityChatBinding mActivityChatBinding;
    private LinearLayoutManager mLinearLayoutManager;
    private ChatMessageRecyclerViewAdapter chatMessageRecyclerViewAdapter;
    private TaskChatModel taskChatModel;
    private UserDetails mUserDetails;

    private String mChatId = "";
    private String formattedTaskId = "";
    private String formattedSenderId = "";
    private String formattedReceiverId = "";
    private String mCurrentChatStatus = CHAT_STATUS_YES;
    private String mSelectedMediaPath = "";

    private boolean isSendClick = false;
    private boolean isLoaded = false;
    private boolean hasMoreRecord = true;
    private boolean isBackPressed = false;

    /**
     * This would start the @{@link ChatActivity}
     *
     * @param context       context to start start the activity
     * @param taskChatModel @{@link TaskChatModel} so that activity can be initialized accordingly.
     */
    public static void newInstance(Context context, TaskChatModel taskChatModel) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskChatModel));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityChatBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        initDATA();
        initiateUI();
        setListeners();
        addMessageListener();
    }


    @Override
    protected void onPause() {
        // We will only update unread counter incase user backpressed via back button.
        if (!isBackPressed) {
            updateUnreadCount();
        }
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        /**
         * Click event of buttons from the screens.
         */
        switch (view.getId()) {
            case R.id.img_media:
                // Show Media choosed dialog that would let the user choose the image.
                showMediaChooserDialog();
                break;
            case R.id.icon_filter:
                break;
            case R.id.text_send:
                if (!TextUtils.isEmpty(mActivityChatBinding.editMessage.getText().toString().trim())) {
                    sendMessage(Utility.CHAT_TYPE_MESSAGE);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * Manage several Request code and act accordingly.
         */
        switch (requestCode) {
            case Utility.REQUEST_CODE_IMAGE_CAPTURE_CHAT_MEDIA:
                if (resultCode == RESULT_OK)
                    displayAndUploadFile();
                break;
            case Utility.REQUEST_CODE_GET_FILE_CHAT_MEDIA_GALLERY:
                if (resultCode == RESULT_OK) {
                    mSelectedMediaPath = Utility.getPath(ChatActivity.this, data.getData());
                    displayAndUploadFile();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /**
         * Manage Request permission code and act accordingly.
         */
        switch (requestCode) {
            case Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_CHAT_MEDIA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_CHAT_MEDIA);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityChatBinding.getRoot());
                }
                break;
            case Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_CHAT_MEDIA_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_CHAT_MEDIA_GALLERY);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityChatBinding.getRoot());
                }
                break;
            case Utility.REQUEST_CODE_CHAT_MEDIA_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraCaptureChooser(Utility.REQUEST_CODE_IMAGE_CAPTURE_CHAT_MEDIA);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Utility.showSnackBar(getString(R.string.permission_denied_camera), mActivityChatBinding.getRoot());
                }
                break;
        }
    }


    @Override
    public void onBackPressed() {
        // Reset the values to default when activity is about to destroy.
        Utility.CURRENT_CHAT_ID = "";
        isBackPressed = true;
        updateUnreadCount();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        // unregister all receivers
        removeMessageListener();

        // Cancel all running network calls in case any of them is running even after android.app.Activity destroyed
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.IMAGE_UPLOAD_FOR_CHAT);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.CHECK_TASK_STATUS);

        // Call Super method now.
        super.onDestroy();
    }

    @Override
    protected void initiateUI() {
        // Setting background image at runtime in order to override background set by @android.content.res.Resources.Theme
        getWindow().setBackgroundDrawableResource(R.drawable.ic_chat_back);

        // Setup actionbar
        setSupportActionBar(mActivityChatBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Manage event when back arrow from @android.support.v7.widget.Toolbar pressed.
        mActivityChatBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call default back pressed event
                onBackPressed();
            }
        });

        // Update UI
        if (taskChatModel != null) {
            mActivityChatBinding.textTitle.setText(taskChatModel.participantName);
        }

        //Setting data in @RecyclerView
        chatMessageRecyclerViewAdapter = new ChatMessageRecyclerViewAdapter(ChatActivity.this);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mLinearLayoutManager.setStackFromEnd(true);
        mActivityChatBinding.recyclerView.setLayoutManager(mLinearLayoutManager);
        mActivityChatBinding.recyclerView.setAdapter(chatMessageRecyclerViewAdapter);
        mActivityChatBinding.recyclerView.scrollToPosition(chatMessageRecyclerViewAdapter.getItemCount());

        Utility.showCircularImageView(mContext, TAG, mActivityChatBinding.imgProfile, taskChatModel.participantPhotoUrl, Utility.DEFAULT_CHEEP_LOGO);

        // By Default its YES, so message would be EMPTY STRING.
        enableChatAccess(mCurrentChatStatus, Utility.EMPTY_STRING);

        /**
         * We need to check whether user would be allow to chat with other person now.
         * In case Task is @{@link com.cheep.utils.Utility.TASK_STATUS}"completed/cancelled/.." scenarios we need to show proper message
         * and do not allow the user.
         */
        checkTaskStatus(FirebaseUtils.removePrefixTaskId(taskChatModel.taskId), FirebaseUtils.removePrefixSpId(formattedReceiverId));
    }

    @Override
    protected void setListeners() {
        /**
         * set @RecyclerView.OnScrollListener to {@link RecyclerView} to manage loading more content on
         * scrolling the chat upwords
         */
        mActivityChatBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstItem = mLinearLayoutManager.findFirstVisibleItemPosition();
                if (firstItem == 0 && !isLoaded && hasMoreRecord) {
                    long timestamp = chatMessageRecyclerViewAdapter.getFirstTimestamp();
                    isLoaded = true;
                    loadMoreData(timestamp);
                }
            }
        });
    }


    /**
     * Initiate the firebase session using @{@link TaskChatModel} from intent extras.
     */
    private void initDATA() {
        mUserDetails = PreferenceUtility.getInstance(ChatActivity.this).getUserDetails();
        taskChatModel = (TaskChatModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskChatModel.class);

        if (taskChatModel != null && mUserDetails != null) {
            formattedTaskId = FirebaseUtils.getPrefixTaskId(taskChatModel.taskId);
            formattedSenderId = FirebaseUtils.getPrefixUserId(mUserDetails.UserID);

            if (taskChatModel.senderId.startsWith("SP")) {
                formattedReceiverId = FirebaseUtils.getPrefixSPId(taskChatModel.senderId);
            } else {
                formattedReceiverId = FirebaseUtils.getPrefixSPId(taskChatModel.receiverId);
            }

            mChatId = FirebaseUtils.get_T_SP_U_FormattedId(formattedTaskId, formattedReceiverId, formattedSenderId);
            Utility.CURRENT_CHAT_ID = mChatId;
            sendMessageViewBroadcast(mChatId);
        }
    }


    /**
     * This method would update the unread count and update the UI accordingly.
     */
    private void updateUnreadCount() {
        // Check whether either of them @formattedSenderId or @mChatId is null or not, if any of them
        // is null then return, else go ahead.
        if (!TextUtils.isEmpty(formattedSenderId)
                && !TextUtils.isEmpty(mChatId)) {

            // Set Listener for receiving any unreadchat count badge.
            FirebaseHelper.getRecentChatRef(formattedSenderId).child(mChatId).child(FirebaseHelper.KEY_UNREADCOUNT).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    /**
                     * Check if received data is proper and not Null or empty.
                     */
                    if (dataSnapshot.exists()) {
                        long unreadCount = (long) dataSnapshot.getValue();
                        if (unreadCount > 0) {
                            FirebaseHelper.getRecentChatRef(formattedSenderId).child(mChatId).child(FirebaseHelper.KEY_UNREADCOUNT).setValue(0);
                        }
                    } else if (!TextUtils.isEmpty(formattedTaskId)) {
                        FirebaseHelper.getTaskChatRef(formattedTaskId).child(mChatId).child(FirebaseHelper.KEY_UNREADCOUNT).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    long unreadCount = (long) dataSnapshot.getValue();
                                    if (unreadCount > 0) {
                                        FirebaseHelper.getTaskChatRef(formattedTaskId).child(mChatId).child(FirebaseHelper.KEY_UNREADCOUNT).setValue(0);
                                        updateRecentChatTaskUnreadCount(formattedTaskId);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing here.
                }
            });
        }
    }

    /**
     * This would update recent chat listing unread count.
     *
     * @param taskId TaskId of the task.
     */
    private void updateRecentChatTaskUnreadCount(final String taskId) {
        final DatabaseReference databaseReference = FirebaseHelper.getTaskChatRef(taskId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    int unreadCount = 0;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.exists() && ds.getValue() != null) {
                            TaskChatModel tcModel = ds.getValue(TaskChatModel.class);
                            if (tcModel != null) {
                                unreadCount += tcModel.unreadCount;
                            }
                        }
                    }
                    final DatabaseReference databaseReference = FirebaseHelper.getRecentChatRef(formattedSenderId);
                    databaseReference.child(taskId).child(FirebaseHelper.KEY_UNREADCOUNT).setValue(unreadCount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * It would broadcast the information to @{@link MyFirebaseMessagingService} in order to manage notification
     * mapping.
     *
     * @param mChatId unique id of conversation, we are using @mChatId
     */
    private void sendMessageViewBroadcast(String mChatId) {
        if (!TextUtils.isEmpty(mChatId)) {
            if (MyFirebaseMessagingService.mapMessages != null && MyFirebaseMessagingService.mapMessages.size() > 0) {
                MyFirebaseMessagingService.mapMessages.remove(mChatId);
                MyFirebaseMessagingService.privateMessages = null;
            }
        }
    }


    /**
     * This method is used to get last 20 messages and set message.
     * It would also update listener.
     */
    public void addMessageListener() {
        mActivityChatBinding.srlMessages.setRefreshing(true);

        final DatabaseReference databaseReference = FirebaseHelper.getMessagesRef(mChatId);

        databaseReference
                .limitToLast(Utility.CHAT_PAGINATION_RECORD_LIMIT)
                .orderByChild(FirebaseHelper.KEY_TIMESTAMP)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        isLoaded = false;
                        mActivityChatBinding.srlMessages.setRefreshing(false);
                        mActivityChatBinding.srlMessages.setEnabled(false);

                        if (dataSnapshot.getValue() != null && dataSnapshot.getChildrenCount() > 0) {
                            hasMoreRecord = dataSnapshot.getChildrenCount() >= Utility.CHAT_PAGINATION_RECORD_LIMIT;

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if (ds.exists() && ds.getValue() != null) {
                                    TaskChatMessageModel message = ds.getValue(TaskChatMessageModel.class);
                                    if (message != null) {
                                        //tvNoData.setVisibility(View.GONE);
                                        chatMessageRecyclerViewAdapter.appendNewMessage(message);
                                        mActivityChatBinding.recyclerView.smoothScrollToPosition(chatMessageRecyclerViewAdapter.getItemCount() - 1);
                                    }
                                }
                            }

                            final Long mLastTimestamp = chatMessageRecyclerViewAdapter.getLastTimestamp();
                            Query query = databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP).startAt(mLastTimestamp + 1);
                            query.addChildEventListener(messageChildEventListener);
                        } else {
                            Query query = databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP);
                            query.addChildEventListener(messageChildEventListener);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mActivityChatBinding.srlMessages.setRefreshing(false);
                        mActivityChatBinding.srlMessages.setEnabled(true);
                    }
                });
    }

    /**
     * This would remove the earlier set Message Listener
     */
    public void removeMessageListener() {
        DatabaseReference databaseReference = FirebaseHelper.getMessagesRef(mChatId);
        databaseReference.removeEventListener(messageChildEventListener);
    }

    /**
     * Implementation of {@link ChildEventListener} which would provide callback on specific events
     * This would also update the UI Accordingly.
     */
    ChildEventListener messageChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // Check whether its null or not.
            if (dataSnapshot.getValue() != null) {

                TaskChatMessageModel message = dataSnapshot.getValue(TaskChatMessageModel.class);
                if (message != null) {
                    chatMessageRecyclerViewAdapter.appendNewMessage(message);
                    if (isSendClick) {
                        mActivityChatBinding.recyclerView.smoothScrollToPosition(chatMessageRecyclerViewAdapter.getItemCount() - 1);
                        isSendClick = false;
                    } else if (mLinearLayoutManager.findLastVisibleItemPosition() == mLinearLayoutManager.getItemCount() - 2) {
                        mActivityChatBinding.recyclerView.smoothScrollToPosition(chatMessageRecyclerViewAdapter.getItemCount() - 1);
                    }
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if (dataSnapshot.getValue() != null) {
                TaskChatMessageModel message = dataSnapshot.getValue(TaskChatMessageModel.class);
                if (message != null) {
                    chatMessageRecyclerViewAdapter.updateMessage(message);
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                final TaskChatMessageModel message = dataSnapshot.getValue(TaskChatMessageModel.class);
                if (message != null && !TextUtils.isEmpty(message.messageId)) {
                    chatMessageRecyclerViewAdapter.deleteMessage(message);
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            // Do nothing.
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Do nothing.
        }
    };

    /**
     * Load mode data to @{@link RecyclerView} when it scrolled upwards.
     *
     * @param lastTimestamp time stamp of last fetched chat.
     *                      it would fetch earlier chat before mentioned timestamp.
     */
    private void loadMoreData(final long lastTimestamp) {
        if (lastTimestamp > 0) {
            chatMessageRecyclerViewAdapter.showProgressBar();
            final DatabaseReference databaseReference = FirebaseHelper.getMessagesRef(mChatId);
            Query query = databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP).endAt(lastTimestamp - 1).limitToLast(Utility.CHAT_PAGINATION_RECORD_LIMIT);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatMessageRecyclerViewAdapter.hideProgressBar();
                    isLoaded = false;
                    if (dataSnapshot.getValue() != null && dataSnapshot.getChildrenCount() > 0) {
                        hasMoreRecord = dataSnapshot.getChildrenCount() >= Utility.CHAT_PAGINATION_RECORD_LIMIT;
                        List<TaskChatMessageModel> messageModelList = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.exists() && ds.getValue() != null) {
                                TaskChatMessageModel message = ds.getValue(TaskChatMessageModel.class);
                                if (message != null) {
                                    messageModelList.add(message);
                                }
                            }
                        }
                        if (messageModelList.size() > 0) {
                            Collections.reverse(messageModelList);
                            chatMessageRecyclerViewAdapter.appendNewMessageOnTop(messageModelList);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    chatMessageRecyclerViewAdapter.hideProgressBar();
                }
            });
        }
    }

    /**
     * Sent Message to Firebase(only for text)
     *
     * @param chatType Input text
     */
    public void sendMessage(String chatType) {
        // Sent message with only text and Empty string for others.
        sendMessage(chatType, Utility.EMPTY_STRING, Utility.EMPTY_STRING);
    }

    /**
     * Sent message to Firebase ( for Media(images) )
     *
     * @param chatType      Type of chat
     * @param mediaUrl      Media Path
     * @param mediaThumbUrl Thumb image path of media
     */
    public void sendMessage(String chatType, String mediaUrl, String mediaThumbUrl) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityChatBinding.getRoot());
            if (chatType.equalsIgnoreCase(Utility.CHAT_TYPE_MEDIA)) {
                mSelectedMediaPath = "";
            }
            return;
        }

        // Manage @TaskChatMessageModel in order to provide the same to Firebase
        final TaskChatMessageModel chatMessageModel = new TaskChatMessageModel();
        chatMessageModel.chatId = mChatId;
        chatMessageModel.taskId = formattedTaskId;
        chatMessageModel.messageType = chatType;
        chatMessageModel.receiverId = formattedReceiverId;
        chatMessageModel.senderId = formattedSenderId;
        /**
         * We will set message in case @chatType is @{@link Utility.CHAT_TYPE_MESSAGE}
         * and will set media & thumb url in case @chatType is @{@link Utility.CHAT_TYPE_MEDIA}
         */
        if (chatType.equalsIgnoreCase(Utility.CHAT_TYPE_MESSAGE)) {
            chatMessageModel.message = mActivityChatBinding.editMessage.getText().toString().replaceAll(Utility.MOBILE_REGREX,Utility.EMPTY_STRING).trim();
        } else if (chatType.equalsIgnoreCase(Utility.CHAT_TYPE_MEDIA)) {
            chatMessageModel.mediaUrl = mediaUrl;
            chatMessageModel.mediaThumbUrl = mediaThumbUrl;
        }

        String key = FirebaseHelper.getMessagesRef(chatMessageModel.chatId).push().getKey();
        chatMessageModel.messageId = key;

        FirebaseHelper.getMessagesRef(chatMessageModel.chatId).child(key).setValue(chatMessageModel);
        FirebaseHelper.getMessageQueueRef().push().setValue(chatMessageModel);


        // Manage @TaskChatModel
        final TaskChatModel taskChatModel = new TaskChatModel();
        taskChatModel.chatId = chatMessageModel.chatId;
        taskChatModel.taskId = chatMessageModel.taskId;
        taskChatModel.messageId = chatMessageModel.messageId;
        taskChatModel.senderId = chatMessageModel.senderId;
        taskChatModel.receiverId = chatMessageModel.receiverId;
        taskChatModel.messageType = chatMessageModel.messageType;
        taskChatModel.unreadCount = 0;
        /**
         * We will set message in case @chatType is @{@link Utility.CHAT_TYPE_MESSAGE}
         * and will set media & thumb url in case @chatType is @{@link Utility.CHAT_TYPE_MEDIA}
         */
        if (chatType.equalsIgnoreCase(Utility.CHAT_TYPE_MESSAGE)) {
            taskChatModel.message = chatMessageModel.message;
        } else if (chatType.equalsIgnoreCase(Utility.CHAT_TYPE_MEDIA)) {
            taskChatModel.mediaUrl = mediaUrl;
            taskChatModel.mediaThumbUrl = mediaThumbUrl;
        }
        FirebaseHelper.getRecentChatRef(formattedReceiverId).child(chatMessageModel.chatId).child(FirebaseHelper.KEY_UNREADCOUNT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long undreadCount = dataSnapshot.getValue(Long.class);
                    taskChatModel.unreadCount = undreadCount + 1;
                } else {
                    taskChatModel.unreadCount = 1;
                }
                FirebaseHelper.getRecentChatRef(taskChatModel.receiverId).child(taskChatModel.chatId).setValue(taskChatModel);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseHelper.getTaskRef(formattedTaskId).child(FirebaseHelper.KEY_SELECTEDSPID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String spId = dataSnapshot.getValue(String.class);
                    if (!TextUtils.isEmpty(spId)) {
                        taskChatModel.unreadCount = 0;
                        FirebaseHelper.getRecentChatRef(taskChatModel.senderId).child(taskChatModel.chatId).setValue(taskChatModel);
                    } else {
                        taskChatModel.unreadCount = 0;
                        taskChatModel.chatId = formattedTaskId;
                        FirebaseHelper.getRecentChatRef(taskChatModel.senderId).child(formattedTaskId).setValue(taskChatModel);
                        taskChatModel.chatId = mChatId;
                        FirebaseHelper.getTaskChatRef(formattedTaskId).child(taskChatModel.chatId).setValue(taskChatModel);
                    }
                } else {
                    taskChatModel.unreadCount = 0;
                    taskChatModel.chatId = formattedTaskId;
                    FirebaseHelper.getRecentChatRef(taskChatModel.senderId).child(formattedTaskId).setValue(taskChatModel);
                    taskChatModel.chatId = mChatId;
                    FirebaseHelper.getTaskChatRef(formattedTaskId).child(taskChatModel.chatId).setValue(taskChatModel);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Set up edit lable based on @chatType
        if (chatType.equalsIgnoreCase(Utility.CHAT_TYPE_MESSAGE)) {
            mActivityChatBinding.editMessage.setText(Utility.EMPTY_STRING);
        } else if (chatType.equalsIgnoreCase(Utility.CHAT_TYPE_MEDIA)) {
            mSelectedMediaPath = Utility.EMPTY_STRING;
        }
    }

    /**
     * Used to display selected media file on adapter
     */
    public void displayAndUploadFile() {
        if (!TextUtils.isEmpty(mSelectedMediaPath)) {
            uploadImage(mSelectedMediaPath);
        }
    }

    /**
     * This would show @{@link AlertDialog} to the user asking him selecting whether media needs to be
     * fetched from Camera or Gallery
     */
    private void showMediaChooserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.choose_image)
                .setItems(R.array.choose_image_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position of the selected item
                        if (which == 0) {
                            dispatchTakePictureIntent(Utility.REQUEST_CODE_IMAGE_CAPTURE_CHAT_MEDIA, Utility.REQUEST_CODE_CHAT_MEDIA_CAMERA);
                        } else {
                            //Select Gallery In case Choose File from Gallery
                            choosePictureFromGallery(Utility.REQUEST_CODE_GET_FILE_CHAT_MEDIA_GALLERY, Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_CHAT_MEDIA_GALLERY);
                        }
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();
    }

    /**
     * This would check whether permission is granted or not.
     * If permission is granted, app would ask abour camera or gallery dialog.
     *
     * @param requestCode           RequestCode whether user started go for camera or gallery
     * @param requestPermissionCode RequestCode for particular permission that needs to be checked.
     */
    private void dispatchTakePictureIntent(int requestCode, int requestPermissionCode) {
        //Go ahead with Camera capturing
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestPermissionCode);
            }
        } else {
            //Go ahead with Camera capturing
            startCameraCaptureChooser(requestCode);
        }
    }

    /**
     * Start capturing image from Camera
     *
     * @param requestCode requestCode so that we can track the same in @onActivityResult when needed.
     */
    public void startCameraCaptureChooser(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                mSelectedMediaPath = photoFile.getAbsolutePath();
                if (photoFile.exists()) {
                    photoFile.delete();
                } else {
                    photoFile.getParentFile().mkdirs();
                }

            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.FILE_PROVIDER_URL,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Grant URI permission START
                // Enableing the permission at runtime
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip =
                            ClipData.newUri(getContentResolver(), "A photo", photoURI);
                    takePictureIntent.setClipData(clip);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    List<ResolveInfo> resInfoList =
                            getPackageManager()
                                    .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, photoURI,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                }
                //Grant URI permission END
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    /**
     * Creating a file
     *
     * @return File object that can be used later on.
     * @throws IOException
     */
    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss"/*, Locale.US*/).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File photoFile = new File(new File(getFilesDir(), "CheepImages"), imageFileName);
        mSelectedMediaPath = photoFile.getAbsolutePath();
        return photoFile;
    }

    /**
     * This would check whether permission is granted or not.
     * If permission is granted, app would ask abour camera or gallery dialog.
     *
     * @param requestFileChooserCode RequestCode whether user started go for camera or gallery
     * @param requestPermissionCode  RequestCode for particular permission that needs to be checked.
     */
    public void choosePictureFromGallery(int requestFileChooserCode, int requestPermissionCode) {
        Log.d(TAG, "choosePictureFromGallery() called with: requestFileChooserCode = [" + requestFileChooserCode + "], requestPermissionCode = [" + requestPermissionCode + "]");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            }
        } else {
            //Go ahead with file choosing
            startIntentFileChooser(requestFileChooserCode);
        }
    }

    /**
     * redirecting the user to gallery to let him/her selects the picture
     *
     * @param requestCode RequestCode whether user started go for camera or gallery
     */
    public void startIntentFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        }
    }

    /***********************************************************************************************
     ***********************************************************************************************
     ************************[Image Upload for Chat ] [Start]***************************************
     ***********************************************************************************************
     ***********************************************************************************************/
    /**
     * Once image is selected(Camera or Gallery) app would call this method in order to
     * upload the image.
     *
     * @param imagePath Absolute path of selected media file.
     */
    @SuppressWarnings("unchecked")
    public void uploadImage(String imagePath) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityChatBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        HashMap<String, File> mFileParams = new HashMap<>();
        mFileParams.put(NetworkUtility.TAGS.CHAT_IMG, new File(imagePath));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.IMAGE_UPLOAD_FOR_CHAT
                , mCallChatImageUploadErrorListener
                , mCallChatImageUploadWSResponseListener
                , mHeaderParams
                , null
                , mFileParams);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.IMAGE_UPLOAD_FOR_CHAT);

    }

    /**
     * Implementing Error callback interface
     */
    private final Response.ErrorListener mCallChatImageUploadErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            LOGI(TAG, "onErrorResponse() called with: error = [" + error + "]");
            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityChatBinding.getRoot());
        }
    };

    /**
     * Implementing success callback interface
     */
    private final Response.Listener mCallChatImageUploadWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        JSONObject jsonData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                        if (jsonData != null) {
                            ChatImageModel chatImageModel = (ChatImageModel) Utility.getObjectFromJsonString(jsonData.toString(), ChatImageModel.class);
                            if (chatImageModel != null) {
                                sendMessage(Utility.CHAT_TYPE_MEDIA, chatImageModel.original, chatImageModel.thumb);
                            }
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityChatBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityChatBinding.getRoot());
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            hideProgressDialog();
        }
    };
    /***********************************************************************************************
     ***********************************************************************************************
     ************************[Image Upload for Chat ] [End]*****************************************
     ***********************************************************************************************
     ***********************************************************************************************/

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////  Check Task Status [Start] //////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Web Call in order to check current status of Task.
     * Based on that app would going to change UI.
     *
     * @param taskID   TaskId for which chat is going on.
     * @param spUserID PRO userId with whom user is chatting with.
     */
    public void checkTaskStatus(String taskID, String spUserID) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityChatBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        HashMap<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskID);
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, spUserID);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CHECK_TASK_STATUS
                , mCheckTaskStatusErrorListener
                , mCheckTaskStatusWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.CHECK_TASK_STATUS);

    }

    private final Response.ErrorListener mCheckTaskStatusErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityChatBinding.getRoot());
        }
    };

    private final Response.Listener mCheckTaskStatusWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        mCurrentChatStatus = jsonObject.getString(NetworkUtility.TAGS.IS_CHAT);
                        final String mTaskStatus = jsonObject.getString(NetworkUtility.TAGS.TASK_STATUS);
                        enableChatAccess(mCurrentChatStatus, mTaskStatus);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityChatBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityChatBinding.getRoot());
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            hideProgressDialog();
        }
    };

    private void enableChatAccess(String mCurrentChatStatus, String currentTaskStatus) {
        if (CHAT_STATUS_YES.equalsIgnoreCase(mCurrentChatStatus)) {
            mActivityChatBinding.imgMedia.setOnClickListener(this);
            mActivityChatBinding.iconFilter.setOnClickListener(this);
            mActivityChatBinding.textSend.setOnClickListener(this);
            mActivityChatBinding.editMessage.setEnabled(true);
            mActivityChatBinding.lnChatFooter.setAlpha(1.0f);
            mActivityChatBinding.lnChatFooter.setVisibility(View.VISIBLE);
            mActivityChatBinding.textChatDisableMessage.setVisibility(View.GONE);
        } else {
            mActivityChatBinding.imgMedia.setOnClickListener(null);
            mActivityChatBinding.iconFilter.setOnClickListener(null);
            mActivityChatBinding.textSend.setOnClickListener(null);
            mActivityChatBinding.editMessage.setEnabled(false);
            mActivityChatBinding.lnChatFooter.setAlpha(0.5f);
            mActivityChatBinding.lnChatFooter.setVisibility(View.GONE);

            // Enable chat disable message and place the text
            mActivityChatBinding.textChatDisableMessage.setVisibility(View.VISIBLE);

            // Incase Task Lapsed
            if (Utility.TASK_STATUS.ELAPSED.equalsIgnoreCase(currentTaskStatus)) {
                SpannableStringBuilder ssb = new SpannableStringBuilder(getString(R.string.label_chat_ended_due_to_task_lapsed));
                ssb.setSpan(new ImageSpan(mContext, R.drawable.ic_locked_50_60), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                mActivityChatBinding.textChatDisableMessage.setText(ssb, TextView.BufferType.SPANNABLE);
            }
            // Incase Task Cancelled,Reschedule Rejected
            else if (Utility.TASK_STATUS.CANCELLED_SP.equalsIgnoreCase(currentTaskStatus)
                    || Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(currentTaskStatus)
                    || Utility.TASK_STATUS.RESCHEDULE_REQUEST_REJECTED.equalsIgnoreCase(currentTaskStatus)
                    ) {
                SpannableStringBuilder ssb = new SpannableStringBuilder(getString(R.string.label_chat_ended_due_to_task_cancelled));
                ssb.setSpan(new ImageSpan(mContext, R.drawable.ic_locked_50_60), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                mActivityChatBinding.textChatDisableMessage.setText(ssb, TextView.BufferType.SPANNABLE);
            }

            // Incase Task Completed
            else if (Utility.TASK_STATUS.COMPLETION_CONFIRM.equalsIgnoreCase(currentTaskStatus)) {
                SpannableStringBuilder ssb = new SpannableStringBuilder(getString(R.string.label_chat_ended_due_to_task_completed));
                ssb.setSpan(new ImageSpan(mContext, R.drawable.ic_locked_50_60), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                mActivityChatBinding.textChatDisableMessage.setText(ssb, TextView.BufferType.SPANNABLE);
            }

            // Any other reason like Task has been allocated to other PRO.
            else {
                SpannableStringBuilder ssb = new SpannableStringBuilder(getString(R.string.label_chat_ended_due_to_assigned_to_another_pro));
                ssb.setSpan(new ImageSpan(mContext, R.drawable.ic_locked_50_60), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                mActivityChatBinding.textChatDisableMessage.setText(ssb, TextView.BufferType.SPANNABLE);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////  Check Task Status [End]  ///////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
