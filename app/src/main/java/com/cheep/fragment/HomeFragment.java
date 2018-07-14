package com.cheep.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.TaskQuotesActivity;
import com.cheep.cheepcarenew.activities.RateAndReviewActivity;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.databinding.FragmentHomeBinding;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cheep.utils.Utility.getSpannableString;

/**
 * Created by pankaj on 9/27/16.
 */
public class HomeFragment extends BaseFragment {
    public static final String TAG = HomeFragment.class.getSimpleName();

    public static final String TAB_HOME = HomeTabFragment.TAG;
    public static final String TAB_MY_TASK = MyTaskTabFragment.TAG;
    public static final String TAB_ME = ProfileDetailsFragment.TAG;
    public static final String TAB_CHAT = ChatTabFragment.TAG;
    public static final String TAB_EMERGENCY = "EMERGENCY";

    private DrawerLayoutInteractionListener mListener;
    private TextView textLastSelectedTab = null;
    private String lastSelectedTab;

    private FragmentHomeBinding mBinding;

    private HashMap<String, BaseFragment> mFragments;
    private ArrayList<String> mFragmentsStackTags;

    private String formattedSenderId = "";
    private List<String> unreadCountIds = new ArrayList<>();

    public static HomeFragment newInstance(Uri link) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(Utility.Extra.DYNAMIC_LINK_URI, link);
        fragment.setArguments(args);
        return fragment;
    }

    public static HomeFragment newInstance(String type) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(Utility.Extra.DATA, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext.registerReceiver(mBR_OnTaskCreated, new IntentFilter(Utility.BR_ON_TASK_CREATED));
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initDATA();
        initiateUI();
        setListener();
        addUnreadCountListener();
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);
        if (context instanceof DrawerLayoutInteractionListener) {
            mListener = (DrawerLayoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }

        // Check for the onGoing Task Counter
        checkOngoingTaskCounter();
    }

    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");

        mListener = null;
        removeUnreadCountListener();

        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.CHECK_PROCESSING_TASK);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.EMERGENCY_ALERT);
        super.onDetach();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.TASK_START_ALERT:
                checkOngoingTaskCounter();
//            mTaskDetailModel.taskStatus = event.taskStatus;
//            setUpTaskDetails(mTaskDetailModel);
                break;
            case Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN:
            case Utility.BROADCAST_TYPE.TASK_PAID_FOR_INSTA_BOOKING:
                // Need to rediretct the user to MyTask Screen
                setCurrentTab(TAB_MY_TASK);
                break;

            case Utility.BROADCAST_TYPE.SUBSCRIBED_TASK_CREATE_SUCCESSFULLY:
                // Need to rediretct the user to MyTask Screen
                setCurrentTab(TAB_MY_TASK);
                break;
            case Utility.BROADCAST_TYPE.TASK_RATED:
                mBinding.flRateTask.setVisibility(View.GONE);
                break;
            case Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_RENEW_SUCCESSFULLY:
                Log.d(TAG, "MAJID RENEW PACKAGE = [" + "RENEW" + "]" );
                setCurrentTab(TAB_HOME);
                break;


        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister the broadcast receiver in case fragment gets destroyed

        mContext.unregisterReceiver(mBR_OnTaskCreated);

        EventBus.getDefault().unregister(this);
    }

    private void initDATA() {
        UserDetails mUserDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        if (mUserDetails != null) {
            formattedSenderId = FirebaseUtils.getPrefixUserId(mUserDetails.userID);
        }
    }

    @Override
    public void initiateUI() {

        mFragments = new HashMap<>();
        mFragmentsStackTags = new ArrayList<>();


        //Setting image tint of all the tabs
        resetAllTabs();

        if (Utility.IS_FROM_NOTIFICATION) {
            Utility.IS_FROM_NOTIFICATION = false;
            //Setting First Fragment ie. HomeTabFragment;
            setCurrentTab(TAB_CHAT);
        } else if (getArguments() != null && !TextUtils.isEmpty(getArguments().getString(Utility.Extra.DATA))) {
            setCurrentTab(TAB_HOME);
            setCurrentTab(TAB_MY_TASK);
        } else {
            //Setting First Fragment ie. HomeTabFragment;
            setCurrentTab(TAB_HOME);
        }
    }

    @Override
    public void setListener() {

        //Setting listener to tabs
        mBinding.textTabHome.setOnClickListener(tabClickListener);
        mBinding.textTabMyTask.setOnClickListener(tabClickListener);
//        mBinding.textTabMe.setOnClickListener(tabClickListener);
        mBinding.textTabChat.setOnClickListener(tabClickListener);
        mBinding.textTabEmergency.setOnClickListener(tabClickListener);

    }

    public void addUnreadCountListener() {
        Log.d(TAG, "addUnreadCountListener() called");
        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            return;
        }

        final DatabaseReference databaseReference = FirebaseHelper.getRecentChatRef(formattedSenderId);
        databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP).addChildEventListener(mChildEventListener);
        databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * used to remove unread count listener
     */
    public void removeUnreadCountListener() {
        Log.d(TAG, "removeUnreadCountListener() called");
        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            return;
        }

        DatabaseReference databaseReference = FirebaseHelper.getRecentChatRef(formattedSenderId);
        databaseReference.removeEventListener(mChildEventListener);
    }

    ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            updateUnreadCount(dataSnapshot, false);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            updateUnreadCount(dataSnapshot, false);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            updateUnreadCount(dataSnapshot, true);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void updateUnreadCount(DataSnapshot dataSnapshot, boolean isDelete) {
        Log.d(TAG, "updateUnreadCount() called with: dataSnapshot = [" + dataSnapshot + "], isDelete = [" + isDelete + "]");
        if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
            try {
                TaskChatModel taskChatModel = dataSnapshot.getValue(TaskChatModel.class);
                if (taskChatModel != null) {
                    if (isDelete || unreadCountIds.contains(taskChatModel.chatId)) {
                        if (taskChatModel.unreadCount == 0 || isDelete) {
                            unreadCountIds.remove(taskChatModel.chatId);
                            if (unreadCountIds.size() <= 0) {
                                mBinding.tvChatUnreadCount.setVisibility(View.GONE);
                            } else {
                                mBinding.tvChatUnreadCount.setVisibility(View.VISIBLE);
                                mBinding.tvChatUnreadCount.setText(String.valueOf(unreadCountIds.size()));
                            }
                            Log.e(TAG, "Updated Unread Count :" + unreadCountIds.size());
                        }
                    } else if (taskChatModel.unreadCount > 0) {
                        unreadCountIds.add(taskChatModel.chatId);
                        if (unreadCountIds.size() <= 0) {
                            mBinding.tvChatUnreadCount.setVisibility(View.GONE);
                        } else {
                            mBinding.tvChatUnreadCount.setVisibility(View.VISIBLE);
                            mBinding.tvChatUnreadCount.setText(String.valueOf(unreadCountIds.size()));
                        }
                        Log.e(TAG, "Updated Unread Count :" + unreadCountIds.size());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    View.OnClickListener tabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.text_tab_home:
                    setCurrentTab(TAB_HOME);
                    break;
                case R.id.text_tab_my_task:
                    setCurrentTab(TAB_MY_TASK);
                    break;
               /* case R.id.text_tab_me:
                    setCurrentTab(TAB_ME);
                    break;*/
                case R.id.text_tab_chat:
                    setCurrentTab(TAB_CHAT);
                    break;
                case R.id.text_tab_emergency:
                    setCurrentTab(TAB_EMERGENCY);
                    break;
            }
        }
    };

    /**
     * Resetting all tabs image tint color
     */
    private void resetAllTabs() {

        setSelected(mBinding.textTabHome, false);
        setSelected(mBinding.textTabMyTask, false);
//        setSelected(mBinding.textTabMe, false);
        setSelected(mBinding.textTabChat, false);
        setSelected(mBinding.textTabEmergency, false);

    }

    //Sets the background and font color based on isSelected Flag, background and text color are specified in Selector drawable file
    private void setSelected(TextView textTab, boolean isSelected) {
        textTab.setSelected(isSelected);
        //Changing Image Color
//        Drawable drawable = textTab.getCompoundDrawables()[1];
//        drawable = DrawableCompat.wrap(drawable);
//        DrawableCompat.setTint(drawable.mutate(), ContextCompat.getColor(mContext, isSelected ? R.color.white : R.color.splash_gradient_end));
    }


    //Loads the fragment in inner_content container
    private void loadFragment(String tab, BaseFragment fragmentToCommit) {
        if (textLastSelectedTab != null) {
            //Checking if last selected tab is same which current pressed then no need to load again
            setSelected(textLastSelectedTab, false);
        }
        lastSelectedTab = tab;
        switch (tab) {
            case TAB_HOME:
                setSelected(mBinding.textTabHome, true);
                textLastSelectedTab = mBinding.textTabHome;
                //enable Home stripe
                mBinding.stripHome.setVisibility(View.VISIBLE);
                mBinding.stripMyTask.setVisibility(View.GONE);
//                mBinding.stripMyMe.setVisibility(View.GONE);
                mBinding.stripMyChat.setVisibility(View.GONE);


                break;
            case TAB_MY_TASK:
                setSelected(mBinding.textTabMyTask, true);
                textLastSelectedTab = mBinding.textTabMyTask;

                //enable My Task stripe
                mBinding.stripHome.setVisibility(View.GONE);
                mBinding.stripMyTask.setVisibility(View.VISIBLE);
//                mBinding.stripMyMe.setVisibility(View.GONE);
                mBinding.stripMyChat.setVisibility(View.GONE);


                break;
            /*case TAB_ME:
                setSelected(mBinding.textTabMe, true);
                textLastSelectedTab = mBinding.textTabMe;

                //enable MEstripe
                mBinding.stripHome.setVisibility(View.GONE);
                mBinding.stripMyTask.setVisibility(View.GONE);
                mBinding.stripMyMe.setVisibility(View.VISIBLE);
                mBinding.stripMyChat.setVisibility(View.GONE);
                break;*/
            case TAB_CHAT:
                setSelected(mBinding.textTabChat, true);
                textLastSelectedTab = mBinding.textTabChat;

                //enable Chat stripe
                mBinding.stripHome.setVisibility(View.GONE);
                mBinding.stripMyTask.setVisibility(View.GONE);
//                mBinding.stripMyMe.setVisibility(View.GONE);
                mBinding.stripMyChat.setVisibility(View.VISIBLE);


                break;

        }
        getChildFragmentManager().beginTransaction().replace(R.id.inner_content, fragmentToCommit, tab).commitAllowingStateLoss();
    }

    /**
     * Sets the Current Tab.
     */
    public void setCurrentTab(String tab) {

        if (tab.equalsIgnoreCase(lastSelectedTab)) {
            return;
        }
        BaseFragment fragmentToCommit;
        switch (tab) {
            case TAB_HOME:
                if (mFragmentsStackTags.contains(HomeTabFragment.TAG) && mFragments.containsKey(HomeTabFragment.TAG)) {
                    fragmentToCommit = mFragments.get(HomeTabFragment.TAG);
                    mFragmentsStackTags.remove(HomeTabFragment.TAG);
                    mFragmentsStackTags.add(HomeTabFragment.TAG);
                } else {
                    //For Dynamic Link
                    if (getArguments().getParcelable(Utility.Extra.DYNAMIC_LINK_URI) != null) {
                        fragmentToCommit = HomeTabFragment.newInstance(mListener
                                , (Uri) getArguments().getParcelable(Utility.Extra.DYNAMIC_LINK_URI));
                    } else {
                        fragmentToCommit = HomeTabFragment.newInstance(mListener, null);
                    }
                    if (!mFragmentsStackTags.contains(HomeTabFragment.TAG)) {
                        mFragmentsStackTags.add(HomeTabFragment.TAG);
                    }
                    mFragments.put(HomeTabFragment.TAG, fragmentToCommit);
                }
                loadFragment(HomeTabFragment.TAG, fragmentToCommit);
                break;
            case TAB_MY_TASK:
                if (mFragmentsStackTags.contains(MyTaskTabFragment.TAG) && mFragments.containsKey(MyTaskTabFragment.TAG)) {
                    fragmentToCommit = mFragments.get(MyTaskTabFragment.TAG);
                    mFragmentsStackTags.remove(MyTaskTabFragment.TAG);
                    mFragmentsStackTags.add(MyTaskTabFragment.TAG);
                } else {
                    fragmentToCommit = MyTaskTabFragment.newInstance(mListener);
                    if (!mFragmentsStackTags.contains(MyTaskTabFragment.TAG)) {
                        mFragmentsStackTags.add(MyTaskTabFragment.TAG);
                    }
                    mFragments.put(MyTaskTabFragment.TAG, fragmentToCommit);
                }
                loadFragment(MyTaskTabFragment.TAG, fragmentToCommit);
                break;
            /*case TAB_ME:
                if (mFragmentsStackTags.contains(ProfileDetailsFragment.TAG) && mFragments.containsKey(ProfileDetailsFragment.TAG)) {
                    fragmentToCommit = mFragments.get(ProfileDetailsFragment.TAG);
                    mFragmentsStackTags.remove(ProfileDetailsFragment.TAG);
                    mFragmentsStackTags.add(ProfileDetailsFragment.TAG);
                } else {

                    fragmentToCommit = ProfileDetailsFragment.newInstance(mListener);
                    if (!mFragmentsStackTags.contains(ProfileDetailsFragment.TAG)) {
                        mFragmentsStackTags.add(ProfileDetailsFragment.TAG);
                    }
                    mFragments.put(ProfileDetailsFragment.TAG, fragmentToCommit);
                }
                loadFragment(ProfileDetailsFragment.TAG, fragmentToCommit);
                break;*/
            case TAB_CHAT:
                if (mFragmentsStackTags.contains(ChatTabFragment.TAG) && mFragments.containsKey(ChatTabFragment.TAG)) {
                    fragmentToCommit = mFragments.get(ChatTabFragment.TAG);
                    mFragmentsStackTags.remove(ChatTabFragment.TAG);
                    mFragmentsStackTags.add(ChatTabFragment.TAG);
                } else {
                    fragmentToCommit = ChatTabFragment.newInstance(mListener);
                    if (!mFragmentsStackTags.contains(ChatTabFragment.TAG)) {
                        mFragmentsStackTags.add(ChatTabFragment.TAG);
                    }
                    mFragments.put(ChatTabFragment.TAG, fragmentToCommit);
                }
                loadFragment(ChatTabFragment.TAG, fragmentToCommit);
                break;
            case TAB_EMERGENCY:
                if (mBinding.textTabEmergency.isSelected()) {
                    // Alert is active so show the alert dialog
                    showAlertDialog();
                } else {
//                    Utility.showToast(mContext, getString(R.string.label_alert_not_active));
                    showFullDesc(getString(R.string.app_name).toUpperCase(), getString(R.string.label_alert_not_active));
                }
                break;
        }
    }


    @Override
    public boolean onBackPressed() {
        if (mFragmentsStackTags.size() > mFragmentsStackTags.size() - 1) {
            if (mFragmentsStackTags.size() - 1 >= 0)
                mFragmentsStackTags.remove(mFragmentsStackTags.size() - 1);
            if (mFragmentsStackTags.size() > 0) {
                String tag = mFragmentsStackTags.get(mFragmentsStackTags.size() - 1);
                loadFragment(tag, mFragments.get(tag));
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public void onLocationSettingsDialogNeedToBeShow(Status locationRequest) {
        super.onLocationSettingsDialogNeedToBeShow(locationRequest);
        Log.d(TAG, "onLocationSettingsDialogNeedToBeShow() called with: locationRequest = [" + locationRequest + "]");
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.inner_content);
        if (fragment != null && fragment instanceof BaseFragment) {
            ((BaseFragment) fragment).onLocationSettingsDialogNeedToBeShow(locationRequest);
        }
    }

    @Override
    public void gpsEnabled() {
        super.gpsEnabled();
        Log.d(TAG, "gpsEnabled() called");
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.inner_content);
        if (fragment != null && fragment instanceof BaseFragment) {
            ((BaseFragment) fragment).gpsEnabled();
        }
    }

    @Override
    public void onLocationNotAvailable() {
        super.onLocationNotAvailable();
        Log.d(TAG, "onLocationNotAvailable() called");
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.inner_content);
        if (fragment != null && fragment instanceof BaseFragment) {
            ((BaseFragment) fragment).onLocationNotAvailable();
            if (!(fragment instanceof HomeTabFragment)) {
                if (mFragments.get(TAB_HOME) != null) {
                    mFragments.get(TAB_HOME).onLocationNotAvailable();
                }
            }
        }
    }

    public void onLoadHomeScreenWithEarlierSavedAddress() {
        Log.d(TAG, "onLoadHomeScreenWithEarlierSavedAddress() called");
        onLocationNotAvailable();
    }

    @Override
    public void onLocationFetched(Location mLocation) {
        super.onLocationFetched(mLocation);
        Log.d(TAG, "onLocationFetched() called with: mLocation = [" + mLocation + "]");
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.inner_content);
        if (fragment != null && fragment instanceof BaseFragment) {
            ((BaseFragment) fragment).onLocationFetched(mLocation);
            if (!(fragment instanceof HomeTabFragment)) {
                if (mFragments.get(TAB_HOME) != null) {
                    mFragments.get(TAB_HOME).onLocationFetched(mLocation);
                }
            }
        }
    }

    public void openCreateNewTask() {
        mBinding.textTabHome.performClick();
    }


    /**
     * Broadcast Listener that would listen up when task is created.
     */
    private BroadcastReceiver mBR_OnTaskCreated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
            /*
              Redirect the user to MYTask Screen
             */

            // By giteeka on create task re direct screen to pre fed quotes activity sept 13

            if (intent.hasExtra(Utility.Extra.IS_INSTA_BOOKING_TASK))
                if (!intent.getBooleanExtra(Utility.Extra.IS_INSTA_BOOKING_TASK, false)) {
                    TaskDetailModel mTaskDetailModel = (TaskDetailModel) GsonUtility.getObjectFromJsonString(intent.getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);
                    TaskQuotesActivity.newInstance(mContext, mTaskDetailModel, false);
                }

            setCurrentTab(TAB_MY_TASK);
        }
    };


    /**
     * Alert Message Management
     */
    //////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////WebService [Starts]//////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    //showAlertDialog for Last Tab
    private void showAlertDialog() {
        final BottomAlertDialog dialog = new BottomAlertDialog(mContext);
        dialog.setTitle(getString(R.string.tab_emergency));
        dialog.setMessage(getString(R.string.label_alert_msg));
        dialog.addPositiveButton(getString(R.string.label_yes), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(mContext, "Yes", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                callEmergencyAlert();

            }
        });
        dialog.addNegativeButton(getString(R.string.label_no), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(mContext, "No", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.showDialog();
    }

    private void callEmergencyAlert() {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        /*
        //Add Params
        Map<String, String> mParams = new HashMap<>();
        if (!TextUtils.isEmpty(lat)) {
            mParams.put(NetworkUtility.TAGS.LAT, lat);
            mParams.put(NetworkUtility.TAGS.LNG, lng);
        }*/

        showProgressDialog();

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.EMERGENCY_ALERT
                , mCallEmergencyAlertWSErrorListener
                , mCallEmergencyAlertWSResponseListener
                , mHeaderParams
                , null
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.EMERGENCY_ALERT);
    }

    Response.Listener mCallEmergencyAlertWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        Utility.showSnackBar(jsonObject.getString(NetworkUtility.TAGS.MESSAGE), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
//                        Utility.showSnackBar(error_message, mFragmentTabHomeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);

                        /*if (getActivity() != null)
                            getActivity().finish();*/
                        break;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                mCallEmergencyAlertWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };


    Response.ErrorListener mCallEmergencyAlertWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            hideProgressDialog();
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };


    /*================================================================================================================
    ======================================Alert Icon Counter========================================================
    ================================================================================================================*/

    private void checkOngoingTaskCounter() {
        Log.d(TAG, "checkOngoingTaskCounter() called");
        /**
         * In case of Guest User we need to return from here.
         */
        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            return;
        }

        if (!Utility.isConnected(mContext)) {
            if (mBinding != null)
                Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

       /* //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.LAT, lat);*/


//        showProgressDialog();

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.CHECK_PROCESSING_TASK
                , mCheckProcessingTaskWSErrorListener
                , mCheckProcessingTaskWSResponseListener
                , mHeaderParams
                , null
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.CHECK_PROCESSING_TASK);
    }

    Response.Listener mCheckProcessingTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
//                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        String counter = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.TOTAL_ONGOING_TASK);
                        if (TextUtils.isDigitsOnly(counter) && Integer.parseInt(counter) > 0) {
                            enableAlert(true);
                        } else {
                            enableAlert(false);
                        }
//                        Utility.showSnackBar(jsonObject.getString(NetworkUtility.TAGS.MESSAGE), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
//                        Utility.showSnackBar(error_message, mFragmentTabHomeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
//                        Utility.logout(mContext, true, statusCode);

                        /*if (getActivity() != null)
                            getActivity().finish();*/
                        break;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                mCheckProcessingTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCheckProcessingTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
//            hideProgressDialog();
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    /*================================================================================================================
    ========================================Alert Icon Counter========================================================
    ================================================================================================================*/

    /**
     * This method would enable or disable the alert button
     *
     * @param flag
     */
    private void enableAlert(boolean flag) {
        Log.d(TAG, "enableAlert() called with: flag = [" + flag + "]");
        mBinding.textTabEmergency.setSelected(flag);
    }

    /**
     * Show Alert Dialog in case Alert is not active at this time.
     */
    private BottomAlertDialog dialogDesc;
    private TextView txtMessage;

    private void showFullDesc(String title, String message) {
        if (dialogDesc == null) {
            View view = View.inflate(mContext, R.layout.dialog_information, null);
            txtMessage = (TextView) view.findViewById(R.id.text_message);
            dialogDesc = new BottomAlertDialog(mContext);
            view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDesc.dismiss();
                }
            });
            dialogDesc.setCustomView(view);
        }
        dialogDesc.setTitle(title);
        txtMessage.setText(message);
        dialogDesc.showDialog();
    }

    public void showRateSection(final String taskId, final String catName, final ProviderModel providerModel) {
        mBinding.flRateTask.setVisibility(View.VISIBLE);

        GlideUtility.showCircularImageViewWithColorBorder(mContext, TAG, mBinding.imgProfilePic, providerModel.profileUrl
                , R.drawable.ic_cheep_circular_icon, R.color.white, true);

        mBinding.imgFav.setSelected(providerModel.isFavourite.equalsIgnoreCase(getString(R.string.label_yes)));

        mBinding.tvProCategory.setText(catName);

        mBinding.tvProStatus.setVisibility(providerModel.isVerified.equalsIgnoreCase(getString(R.string.label_yes))
                ? View.VISIBLE : View.GONE);

        SpannableStringBuilder rateDesc = new SpannableStringBuilder();
        rateDesc.append(getSpannableString(providerModel.userName, ContextCompat.getColor(mContext, R.color.black), true));
        rateDesc.append(getSpannableString(getString(R.string.msg_rate_desc_home_1st_part), ContextCompat.getColor(mContext, R.color.black), false));
        rateDesc.append(getSpannableString(catName, ContextCompat.getColor(mContext, R.color.black), true));
        rateDesc.append(getSpannableString(getString(R.string.msg_rate_desc_home_2nd_part), ContextCompat.getColor(mContext, R.color.black), false));

        mBinding.tvRateDescription.setText(rateDesc);

        mBinding.flRateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RateAndReviewActivity.newInstance(mContext, taskId, catName, providerModel);
            }
        });
    }
}