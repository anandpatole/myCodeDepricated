package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.cheep.R;
import com.cheep.adapter.ChatTabRecyclerViewAdapter;
import com.cheep.adapter.NotificationRecyclerViewAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.ActivityChatIndividualListBinding;
import com.cheep.firebase.EndlessRecyclerOnScrollListener;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatServiceProviderModel;
import com.cheep.firebase.model.ChatTaskModel;
import com.cheep.model.NotificationModel;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.model.UserDetails;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 10/5/16.
 */

public class ChatIndividualListActivity extends BaseAppCompatActivity implements NotificationRecyclerViewAdapter.NotificationItemInteractionListener, ChatTabRecyclerViewAdapter.ChatItemInteractionListener {

    private static final String TAG = "ChatIndividualListActivity";

    private ChatTabRecyclerViewAdapter chatTabRecyclerViewAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private TaskChatModel taskChatModel;
    private ActivityChatIndividualListBinding mActivityChatIndividualListBinding;

    private EndlessRecyclerOnScrollListener mEndlessRecyclerOnScrollListener;
    private UserDetails mUserDetails;
    private String formattedSenderId = "";
    private String formattedTaskId = "";

    private boolean hasMoreRecord = true;

    public static void newInstance(Context context, TaskChatModel taskChatModel) {
        Intent intent = new Intent(context, ChatIndividualListActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskChatModel));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityChatIndividualListBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat_individual_list);
        initDATA();
        initiateUI();
        setListeners();
        addMessageListener();
    }

    /**
     * Initialized data
     */
    private void initDATA() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey(Utility.Extra.DATA) && !TextUtils.isEmpty(bundle.getString(Utility.Extra.DATA))) {
            taskChatModel = (TaskChatModel) Utility.getObjectFromJsonString(bundle.getString(Utility.Extra.DATA), TaskChatModel.class);
            if (taskChatModel != null) {
                formattedTaskId = taskChatModel.taskId;
            }
        }
        UserDetails mUserDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        if (mUserDetails != null) {
            formattedSenderId = FirebaseUtils.getPrefixUserId(mUserDetails.UserID);
        }
    }

    @Override
    protected void initiateUI() {
        setSupportActionBar(mActivityChatIndividualListBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mActivityChatIndividualListBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (taskChatModel != null && !TextUtils.isEmpty(taskChatModel.categoryName)) {
            mActivityChatIndividualListBinding.textTitle.setText(taskChatModel.categoryName);
        } else
            mActivityChatIndividualListBinding.textTitle.setText(Utility.EMPTY_STRING);

        mActivityChatIndividualListBinding.commonRecyclerView.swipeRefreshLayout.setEnabled(false);

        //Setting RecyclerView Adapter
        chatTabRecyclerViewAdapter = new ChatTabRecyclerViewAdapter(ChatIndividualListActivity.this, this);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mActivityChatIndividualListBinding.commonRecyclerView.recyclerView.setLayoutManager(mLinearLayoutManager);
        mActivityChatIndividualListBinding.commonRecyclerView.recyclerView.setAdapter(chatTabRecyclerViewAdapter);
        mActivityChatIndividualListBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_0dp)));
    }

    @Override
    protected void setListeners() {
        mEndlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore() {
                if (hasMoreRecord) {
                    long lastTimestamp = chatTabRecyclerViewAdapter.getLastTimestamp();
                    loadMoreData(lastTimestamp);
                }
            }
        };
        mActivityChatIndividualListBinding.commonRecyclerView.recyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
    }

    @Override
    public void onNotificationRowClicked(NotificationModel model, int position) {
        View view = View.inflate(mContext, R.layout.dialog_information, null);
        ((TextView) view.findViewById(R.id.text_message)).setText(getString(R.string.label_reschedule_inform, model.message));
        final BottomAlertDialog dialog = new BottomAlertDialog(mContext);

        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setTitle(model.notificationType);
        dialog.setCustomView(view);
        dialog.showDialog();
    }

    @Override
    public void onChatItemClicked(TaskChatModel model, int position) {
        //Opening chat activity
        ChatActivity.newInstance(mContext, model);
    }

    /**
     * used to get last 10 messages and set message update listener
     */
    public void addMessageListener() {
        mActivityChatIndividualListBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(true);
        final DatabaseReference databaseReference = FirebaseHelper.getTaskChatRef(formattedTaskId);

        databaseReference.limitToFirst(Utility.CHAT_PAGINATION_RECORD_LIMIT).orderByChild(FirebaseHelper.KEY_TIMESTAMP).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mActivityChatIndividualListBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
                mActivityChatIndividualListBinding.commonRecyclerView.swipeRefreshLayout.setEnabled(false);
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
                                    mActivityChatIndividualListBinding.commonRecyclerView.recyclerView.smoothScrollToPosition(0);
                                    final Long mLastTimestamp = taskChatModel.timestamp;

                                    final DatabaseReference databaseReference = FirebaseHelper.getTaskChatRef(formattedTaskId);

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
                    Query query = databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP);
                    query.addChildEventListener(messageChildEventListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mActivityChatIndividualListBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
                mActivityChatIndividualListBinding.commonRecyclerView.swipeRefreshLayout.setEnabled(false);
            }
        });
    }

    private void loadTaskDetail(final TaskChatModel taskChatModel) {
        final DatabaseReference databaseReference = FirebaseHelper.getTaskRef(taskChatModel.taskId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ChatTaskModel taskModel = dataSnapshot.getValue(ChatTaskModel.class);
                    if (taskModel != null && taskChatModel != null) {
                        taskChatModel.taskDesc = taskModel.taskDesc;
                        taskChatModel.categoryName = taskModel.categoryName;
                        loadServiceProviderDetail(taskChatModel);
                    }
                }
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
            final DatabaseReference databaseReference = FirebaseHelper.getTaskChatRef(formattedTaskId);
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

    @Override
    public void onDestroy() {
        removeMessageListener();
        super.onDestroy();
    }
}
