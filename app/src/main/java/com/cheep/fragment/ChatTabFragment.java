package com.cheep.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.adapter.ChatTabRecyclerViewAdapter;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.FragmentTabChatBinding;
import com.cheep.fcm.MyFirebaseMessagingService;
import com.cheep.firebase.EndlessRecyclerOnScrollListener;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatServiceProviderModel;
import com.cheep.firebase.model.ChatTaskModel;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.interfaces.NotificationClickInteractionListener;
import com.cheep.model.MessageEvent;
import com.cheep.model.UserDetails;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 9/30/16.
 */

public class ChatTabFragment extends BaseFragment {
    public static final String TAG = "ChatTabFragment";

    private FragmentTabChatBinding mFragmentTabChatBinding;
    private DrawerLayoutInteractionListener mListener;
    private ChatTabRecyclerViewAdapter chatTabRecyclerViewAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ChatTabRecyclerViewAdapter.ChatItemInteractionListener chatItemInteractionListener;
    private EndlessRecyclerOnScrollListener mEndlessRecyclerOnScrollListener;
    private NotificationClickInteractionListener mNotificationClickInteractionListener;

    private ErrorLoadingHelper errorLoadingHelper;
    private String formattedSenderId = "";

    private boolean hasMoreRecord = true;

    public static ChatTabFragment newInstance(DrawerLayoutInteractionListener mListener) {
        Bundle args = new Bundle();
        ChatTabFragment fragment = new ChatTabFragment();
        fragment.setArguments(args);
        fragment.setmListener(mListener);
        return fragment;
    }

    public void setmListener(DrawerLayoutInteractionListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register Event Bus
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentTabChatBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_chat, container, false);
        return mFragmentTabChatBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initDATA();
        clearChatNotification();
        initiateUI();
        setListener();
        addMessageListener();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update Notification Counter
        updateCounter();
    }

    /**
     * Clear unread notification messages
     */
    private void clearChatNotification() {
        MyFirebaseMessagingService.clearNotification(mContext);
    }

    /**
     * Initialized data
     */
    private void initDATA() {
        UserDetails mUserDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        if (mUserDetails != null) {
            formattedSenderId = FirebaseUtils.getPrefixUserId(mUserDetails.UserID);
        }
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);

        if (context instanceof ChatTabRecyclerViewAdapter.ChatItemInteractionListener) {
            chatItemInteractionListener = (ChatTabRecyclerViewAdapter.ChatItemInteractionListener) context;
        }
        if (context instanceof DrawerLayoutInteractionListener) {
            mListener = (DrawerLayoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }

        if (context instanceof NotificationClickInteractionListener) {
            mNotificationClickInteractionListener = (NotificationClickInteractionListener) context;
        }

    }

    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");
        chatItemInteractionListener = null;
        mListener = null;
        mNotificationClickInteractionListener = null;
        super.onDetach();
    }

    @Override
    public void initiateUI() {
        errorLoadingHelper = new ErrorLoadingHelper(mFragmentTabChatBinding.commonRecyclerView.recyclerView);
        //Setting up toolbar
        ((AppCompatActivity) mContext).setSupportActionBar(mFragmentTabChatBinding.toolbar);
        ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        mFragmentTabChatBinding.textTitle.setText(getString(R.string.label_chat));
        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mFragmentTabChatBinding.toolbar);

        mFragmentTabChatBinding.commonRecyclerView.swipeRefreshLayout.setEnabled(false);

        //Setting RecyclerView Adapter
        chatTabRecyclerViewAdapter = new ChatTabRecyclerViewAdapter(mContext, chatItemInteractionListener);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mFragmentTabChatBinding.commonRecyclerView.recyclerView.setLayoutManager(mLinearLayoutManager);
        mFragmentTabChatBinding.commonRecyclerView.recyclerView.setAdapter(chatTabRecyclerViewAdapter);
        mFragmentTabChatBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_0dp)));

        // Update Notification Counter
        updateCounter();

        // Setup Click listener of Notificaiotn
        mFragmentTabChatBinding.relNotificationAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNotificationClickInteractionListener != null) {
                    mNotificationClickInteractionListener.onNotificationIconClicked();
                }
            }
        });

    }

    @Override
    public void setListener() {
        mEndlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore() {
                if (hasMoreRecord) {
                    long lastTimestamp = chatTabRecyclerViewAdapter.getLastTimestamp();
                    loadMoreData(lastTimestamp);
                }
            }
        };
        mFragmentTabChatBinding.commonRecyclerView.recyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
    }

    /**
     * used to get last 10 messages and set message update listener
     */
    public void addMessageListener() {
        mFragmentTabChatBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(true);
        final DatabaseReference databaseReference = FirebaseHelper.getRecentChatRef(formattedSenderId);
        databaseReference.limitToFirst(Utility.CHAT_PAGINATION_RECORD_LIMIT).orderByChild(FirebaseHelper.KEY_TIMESTAMP).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFragmentTabChatBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
                mFragmentTabChatBinding.commonRecyclerView.swipeRefreshLayout.setEnabled(false);
                if (dataSnapshot.getValue() != null && dataSnapshot.getChildrenCount() > 0) {
                    hasMoreRecord = true;
                    if (dataSnapshot.getChildrenCount() < Utility.CHAT_PAGINATION_RECORD_LIMIT) {
                        hasMoreRecord = false;
                    }
                    int count = 0;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.exists() && ds.getValue() != null) {
                            count += 1;
                            TaskChatModel taskChatModel = ds.getValue(TaskChatModel.class);
                            if (taskChatModel != null) {
                                if (count == dataSnapshot.getChildrenCount()) {
                                    mFragmentTabChatBinding.commonRecyclerView.recyclerView.smoothScrollToPosition(0);

                                    final Long mLastTimestamp = taskChatModel.timestamp;
                                    final DatabaseReference databaseReference = FirebaseHelper.getRecentChatRef(formattedSenderId);

                                    Query query = databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP).startAt(mLastTimestamp + 1);
                                    query.addChildEventListener(messageChildEventListener);

                                    Query query2 = databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP).startAt(chatTabRecyclerViewAdapter.getFirstTimestamp()).endAt(mLastTimestamp);
                                    query2.addChildEventListener(childEventListener);
                                }
                                loadTaskDetail(taskChatModel);
                            }
                        }
                    }
                } else {
                    errorLoadingHelper.failed(null, R.drawable.img_empty_chat, null);
                    Query query = databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP);
                    query.addChildEventListener(messageChildEventListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mFragmentTabChatBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
                mFragmentTabChatBinding.commonRecyclerView.swipeRefreshLayout.setEnabled(false);
            }
        });
    }

    private void loadTaskDetail(final TaskChatModel taskChatModel) {
        Log.d(TAG, "loadTaskDetail() called with: taskChatModel = [" + taskChatModel + "]");
        final DatabaseReference databaseReference = FirebaseHelper.getTaskRef(taskChatModel.taskId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ChatTaskModel taskModel = dataSnapshot.getValue(ChatTaskModel.class);
                    if (taskModel != null && taskChatModel != null) {
                        taskChatModel.taskDesc = taskModel.taskDesc;
                        taskChatModel.categoryName = taskModel.categoryName;
                        if (taskChatModel.chatId.equalsIgnoreCase(taskChatModel.taskId)) {
                            loadTotalTaskChatCount(taskChatModel);
                        } else {
                            loadServiceProviderDetail(taskChatModel);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadTotalTaskChatCount(final TaskChatModel taskChatModel) {
        final DatabaseReference databaseReference = FirebaseHelper.getTaskChatRef(taskChatModel.taskId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    taskChatModel.totalParticipants = dataSnapshot.getChildrenCount();
                    int unreadCount = 0;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.exists() && ds.getValue() != null) {
                            TaskChatModel tcModel = ds.getValue(TaskChatModel.class);
                            if (tcModel != null) {
                                unreadCount += tcModel.unreadCount;
                            }
                        }
                    }
                    ;
                    taskChatModel.unreadCount = unreadCount;
                    final DatabaseReference databaseReference = FirebaseHelper.getRecentChatRef(formattedSenderId);
                    databaseReference.child(taskChatModel.taskId).child(FirebaseHelper.KEY_UNREADCOUNT).setValue(unreadCount);
                }
                loadServiceProviderDetail(taskChatModel);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadServiceProviderDetail(final TaskChatModel taskChatModel) {
        String otherUserId = "";
        if (taskChatModel.senderId.equalsIgnoreCase(formattedSenderId)) {
            otherUserId = taskChatModel.receiverId;
        } else {
            otherUserId = taskChatModel.senderId;
        }
        final DatabaseReference databaseReference = FirebaseHelper.getServiceProviderRef(otherUserId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ChatServiceProviderModel spModel = dataSnapshot.getValue(ChatServiceProviderModel.class);
                    if (spModel != null && taskChatModel != null) {
                        taskChatModel.participantName = spModel.getSpName();
                        taskChatModel.participantPhotoUrl = spModel.getProfileImg();
                        chatTabRecyclerViewAdapter.updateMessage(taskChatModel);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * used to remove message listener
     */
    public void removeMessageListener() {
        DatabaseReference databaseReference = FirebaseHelper.getRecentChatRef(formattedSenderId);
        databaseReference.removeEventListener(messageChildEventListener);
        databaseReference.removeEventListener(childEventListener);
    }

    ChildEventListener messageChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (dataSnapshot.getValue() != null) {
                errorLoadingHelper.success();
                TaskChatModel model = dataSnapshot.getValue(TaskChatModel.class);
                if (model != null) {
                    loadTaskDetail(model);
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if (dataSnapshot.getValue() != null) {
                TaskChatModel model = dataSnapshot.getValue(TaskChatModel.class);
                if (model != null) {
                    loadTaskDetail(model);
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() != null) {
                final TaskChatModel model = dataSnapshot.getValue(TaskChatModel.class);
                if (model != null && !TextUtils.isEmpty(model.messageId)) {
                    chatTabRecyclerViewAdapter.deleteMessage(model);
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if (dataSnapshot.getValue() != null) {
                TaskChatModel model = dataSnapshot.getValue(TaskChatModel.class);
                if (model != null) {
                    loadTaskDetail(model);
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void loadMoreData(final long lastTimestamp) {
        if (lastTimestamp > 0) {
            chatTabRecyclerViewAdapter.showProgressBar();
            final DatabaseReference databaseReference = FirebaseHelper.getRecentChatRef(formattedSenderId);
            Query query = databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP).startAt(lastTimestamp + 1).limitToFirst(Utility.CHAT_PAGINATION_RECORD_LIMIT);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatTabRecyclerViewAdapter.hideProgressBar();
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.getChildrenCount() > 0) {
                            hasMoreRecord = true;
                            if (dataSnapshot.getChildrenCount() < Utility.CHAT_PAGINATION_RECORD_LIMIT) {
                                hasMoreRecord = false;
                            }
                            List<TaskChatModel> taskChatModelList = new ArrayList<>();
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if (ds.exists() && ds.getValue() != null) {
                                    TaskChatModel message = ds.getValue(TaskChatModel.class);
                                    if (message != null) {
                                        taskChatModelList.add(message);
                                    }
                                }
                            }
                            if (taskChatModelList.size() > 0) {
                                chatTabRecyclerViewAdapter.appendMessageList(taskChatModelList);
                            }
                        } else {
                            hasMoreRecord = false;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    chatTabRecyclerViewAdapter.hideProgressBar();
                }
            });
        }
    }

   /* *//**
     * Showing and hiding Views based on Progressbar
     *//*
    private void hideEmptyStateImage()
    {
        if(mFragmentTabChatBinding.commonRecyclerView.imgEmptyState.getVisibility()==View.VISIBLE) {
            mFragmentTabChatBinding.commonRecyclerView.recyclerView.setVisibility(View.VISIBLE);
            mFragmentTabChatBinding.commonRecyclerView.imgEmptyState.setImageResource(R.drawable.img_empty_chat);
            mFragmentTabChatBinding.commonRecyclerView.imgEmptyState.setVisibility(View.GONE);
        }
    }

    */

    /**
     * Showing and hiding Views based on Progressbar
     *//*
    private void showEmptyStateImage()
    {
        if(mFragmentTabChatBinding.commonRecyclerView.imgEmptyState.getVisibility()==View.GONE)
        {
            mFragmentTabChatBinding.commonRecyclerView.recyclerView.setVisibility(View.GONE);
            mFragmentTabChatBinding.commonRecyclerView.imgEmptyState.setImageResource(R.drawable.img_empty_chat);
            mFragmentTabChatBinding.commonRecyclerView.imgEmptyState.setVisibility(View.VISIBLE);
        }
    }
*/
    @Override
    public void onDestroy() {
        removeMessageListener();
        // Register Event Bus
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private void updateCounter() {
        if (mFragmentTabChatBinding != null) {
            //Updating counter
            int notificationCounter = PreferenceUtility.getInstance(mContext).getUnreadNotificationCounter();
            if (notificationCounter > 0) {
                mFragmentTabChatBinding.tvBadgeCount.setText(String.valueOf(notificationCounter));
                mFragmentTabChatBinding.tvBadgeCount.setVisibility(View.VISIBLE);
            } else {
                mFragmentTabChatBinding.tvBadgeCount.setVisibility(View.GONE);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.NEW_NOTIFICATION) {
            updateCounter();
        }
    }

}