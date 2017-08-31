package com.cheep.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.adapter.ChatTabRecyclerViewAdapter;
import com.cheep.adapter.FAQRecyclerViewAdapter;
import com.cheep.adapter.FavouriteRecyclerViewAdapter;
import com.cheep.adapter.HistoryRecyclerViewAdapter;
import com.cheep.adapter.SlideMenuAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.databinding.ActivityHomeBinding;
import com.cheep.databinding.NavHeaderHomeBinding;
import com.cheep.databinding.RowUpcomingTaskBinding;
import com.cheep.firebase.FierbaseChatService;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.fragment.BaseFragment;
import com.cheep.fragment.FAQFragment;
import com.cheep.fragment.FavouriteFragment;
import com.cheep.fragment.HistoryFragment;
import com.cheep.fragment.HomeFragment;
import com.cheep.fragment.HomeTabFragment;
import com.cheep.fragment.InfoFragment;
import com.cheep.fragment.ProfileTabFragment;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.interfaces.NotificationClickInteractionListener;
import com.cheep.interfaces.TaskRowDataInteractionListener;
import com.cheep.model.FAQModel;
import com.cheep.model.HistoryModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.SlideMenuListModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.strategicpartner.TaskSummaryStrategicPartnerActivity;
import com.cheep.utils.HotlineHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.google.android.gms.common.api.Status;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.cheep.network.NetworkUtility.TAGS.TASK_ID;

/**
 * Created by pankaj on 9/27/16.
 */
public class HomeActivity extends BaseAppCompatActivity
        implements SlideMenuAdapter.SlideMenuListItemInterface,
        DrawerLayoutInteractionListener, TaskRowDataInteractionListener,
        NotificationClickInteractionListener, HomeTabFragment.CategoryRowInteractionListener,
        FAQRecyclerViewAdapter.FAQItemInteractionListener,
        FavouriteRecyclerViewAdapter.FavouriteRowInteractionListener,
        HistoryRecyclerViewAdapter.HistoryItemInteractionListener,
        ChatTabRecyclerViewAdapter.ChatItemInteractionListener {

    private static final String TAG = "HomeActivity";
    private ActivityHomeBinding mActivityHomeBinding;
    private NavHeaderHomeBinding navHeaderHomeBinding;

    /**
     * We will only load the listing once we have proper lat-long or Location.
     * So, once we get the location from Service we can manage this flow via
     * mentioned boolean.
     * This would be used by @{@link HomeTabFragment}
     */
    public boolean isReadyToLoad = false;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent() called with: intent = [" + intent + "]");
        manageNotificationRedirection(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
          TODO: It would get enabled once Localization feature enabled
         */
//        onLanguageChangedSuccessFully();

        initDATA();
        getWindow().setBackgroundDrawable(null);

        /*
        Earlier, we were not allowing the user to Login
        //finishing activity because no user registered
        if (PreferenceUtility.getInstance(getApplicationContext()).getUserDetails() == null) {
            finish();
        }*/

        mActivityHomeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        //For managing notification redirect to job summary
        onNewIntent(getIntent());
        initiateUI();
        setListeners();

        //Register BroadCast
        registerReceiver(mBR_OnLoginSuccess, new IntentFilter(Utility.BR_ON_LOGIN_SUCCESS));

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check Application version
        checkVersionOfApp();

    }

    private void manageNotificationRedirection(Intent intent) {
        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
//            LoginActivity.newInstance(mContext);
//            finish();
            return;
        }

        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();

            // Check if Type its request for Detail.
            if (bundle.getString(NetworkUtility.TAGS.TYPE) != null
                    && (bundle.getString(NetworkUtility.TAGS.TYPE).equalsIgnoreCase(Utility.NOTIFICATION_TYPE.REQUEST_FOR_DETAIL)
                    || bundle.getString(NetworkUtility.TAGS.TYPE).equalsIgnoreCase(Utility.NOTIFICATION_TYPE.QUOTE_REQUEST))
                    ) {
                String taskID = bundle.getString(NetworkUtility.TAGS.TASK_ID);
                String spUserId = bundle.getString(NetworkUtility.TAGS.SP_USER_ID);
                ProviderProfileActivity.newInstance(mContext, taskID, spUserId);
            } else if (bundle.getString(NetworkUtility.TAGS.TYPE).equalsIgnoreCase(Utility.NOTIFICATION_TYPE.QUOTE_REQUEST)) {
                String taskID = bundle.getString(NetworkUtility.TAGS.TASK_ID);
                String spUserId = bundle.getString(NetworkUtility.TAGS.SP_USER_ID);
                // Redirecting the user to Quote Screen
                TaskSummaryActivity.getInstance(mContext, bundle.getString(TASK_ID));
//                HireNewJobActivity.newInstance(mContext, taskID, spUserId);
            } else if (bundle.getString(NetworkUtility.TAGS.TYPE).equalsIgnoreCase(Utility.NOTIFICATION_TYPE.WEB_CUSTOM_NOTIFICATION)
                    || bundle.getString(NetworkUtility.TAGS.TYPE).equalsIgnoreCase(Utility.NOTIFICATION_TYPE.TASK_CREATE)) {
                // Do Nothing as we just need to redirect the user to Home screen
            } else {
               /* TaskDetailModel taskDetailModel = new TaskDetailModel();
                taskDetailModel.taskId = bundle.getString(TASK_ID);
                ProviderModel providerModel = new ProviderModel();
                providerModel.providerId = bundle.getString(NetworkUtility.TAGS.SP_USER_ID);
                JobSummaryActivity.newInstance(mContext, taskDetailModel, providerModel);*/
                TaskSummaryActivity.getInstance(mContext, bundle.getString(TASK_ID));
            }
            // Changed due to the fact that we should allow user goto detail screen in each of the
            // case when notification comes.
           /* switch (bundle.getString(NetworkUtility.TAGS.TYPE)) {

                case Utility.NOTIFICATION_TYPE.TASK_STATUS_CHANGE:
                case Utility.NOTIFICATION_TYPE.QUOTE_REQUEST:

                    TaskDetailModel taskDetailModel = new TaskDetailModel();
                    taskDetailModel.taskId = bundle.getString(TASK_ID);

                    ProviderModel providerModel = new ProviderModel();
                    providerModel.providerId = bundle.getString(NetworkUtility.TAGS.SP_USER_ID);

                    JobSummaryActivity.newInstance(mContext, taskDetailModel, providerModel);
                    break;
            }*/
        }
    }

    /*
  * @Sanjay
  * */
    private void initDATA() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(Utility.Extra.CHAT_NOTIFICATION_DATA)) {
            Utility.IS_FROM_NOTIFICATION = true;
        }
    }

    @Override
    protected void initiateUI() {
        // Setup Initial Fragment Change

//        FirebaseCrash.report(new Exception("My first Android non-fatal error"));

        Fragment mFragment = HomeFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.content, mFragment, HomeFragment.TAG).commit();

        //Inflate header view
        navHeaderHomeBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.nav_header_home, mActivityHomeBinding.slideListview, false);


        /*// Setting width of listview
        mActivityHomeBinding.slideListview.getLayoutParams().width = (int) (Utility.getDeviceWidthHeight(HomeActivity.this)[0] * 0.75);*/

        // InitUI for SlideMenu
        new setUpSlideMenuListAsync(mContext, mActivityHomeBinding.slideListview, navHeaderHomeBinding).execute();

    }


    @Override
    protected void setListeners() {

    }

    /**
     * This will create SlideMenu list which will be used in Navigation Slidemenu Drawer
     *
     * @return list of @SlideMenuListModel which will be passed to #slideListview.setAdapter
     */
    private static ArrayList<SlideMenuListModel> getSlideMenuData(Context mContext) {
        ArrayList<SlideMenuListModel> list = new ArrayList<>();
        list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.label_home), R.drawable.icon_side_home_blue, true, false));
        list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.label_favourites), R.drawable.icon_favourite, false, false));
        list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.tab_me), R.drawable.icon_logout, false, false));
        list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.label_history), R.drawable.icon_history, false, false));
//        list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.label_share_with_friend), R.drawable.icon_refer, false, false));
        list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.label_help), R.drawable.icon_help, false, true));
        list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.label_faq), R.drawable.icon_faq, false, false));
        list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.label_rate_this_app), R.drawable.icon_rate, false, false));
        list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.label_terms), R.drawable.icon_privacy, false, false));
//        list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.label_privacy_policy), R.drawable.icon_privacy, false, false));
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.label_logout), R.drawable.icon_logout, false, true));
        } else {
            list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.label_login), R.drawable.icon_logout, false, true));
        }
//        list.add(new SlideMenuListModel(mContext.getResources().getString(R.string.tab_alert), R.drawable.icon_logout, false, true));

        return list;
    }

    @Override
    public void onSlideMenuListItemClicked(SlideMenuListModel slideMenuListModel, int position) {
        Log.d(TAG, "onSlideMenuListItemClicked() called with: holder = [" + slideMenuListModel.title + "]");

        if (slideMenuListModel.title.equals(getString(R.string.label_home))) {
            Fragment mFragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
            if (mFragment == null) {
                loadFragment(HomeFragment.TAG, HomeFragment.newInstance());
            } else {
                Log.i(TAG, "onSlideMenuListItemClicked: " + slideMenuListModel.title + " is there");
                //We need to enable enquiry fragment, in case its not there
                HomeFragment homeFragment = (HomeFragment) mFragment;
                homeFragment.setCurrentTab(HomeFragment.TAB_HOME);
            }
        } else if (slideMenuListModel.title.equals(getString(R.string.label_favourites))) {
            Fragment mFragment = getSupportFragmentManager().findFragmentByTag(FavouriteFragment.TAG);
            if (mFragment == null) {
                loadFragment(FavouriteFragment.TAG, FavouriteFragment.newInstance());
            } else {
                Log.i(TAG, "onSlideMenuListItemClicked: " + slideMenuListModel.title + " is there");
            }
        } else if (slideMenuListModel.title.equals(getString(R.string.label_history))) {
            Fragment mFragment = getSupportFragmentManager().findFragmentByTag(HistoryFragment.TAG);
            if (mFragment == null) {
                loadFragment(HistoryFragment.TAG, HistoryFragment.newInstance());
            } else {
                Log.i(TAG, "onSlideMenuListItemClicked: " + slideMenuListModel.title + " is there");
            }
        } else if (slideMenuListModel.title.equals(getString(R.string.label_share_with_friend))) {
            Utility.showToast(mContext, "Under Development.");
        } else if (slideMenuListModel.title.equals(getString(R.string.label_help))) {
            showContactDialog();
            //We are returning here so side menu will not close at end of this method
            return;
        }
        //========
        else if (slideMenuListModel.title.equals(getString(R.string.label_faq))) {
            Fragment mFragment = getSupportFragmentManager().findFragmentByTag(FAQFragment.TAG);
            if (mFragment == null) {
                loadFragment(FAQFragment.TAG, FAQFragment.newInstance());
            } else {
                Log.i(TAG, "onSlideMenuListItemClicked: " + slideMenuListModel.title + " is there");
            }
        } else if (slideMenuListModel.title.equals(getString(R.string.label_rate_this_app))) {
//               Utility.showToast(mContext, "Under Development.");
            Utility.redirectUserToPlaystore(mContext);
        } else if (slideMenuListModel.title.equals(getString(R.string.label_terms))) {
            Fragment mFragment = getSupportFragmentManager().findFragmentByTag(InfoFragment.TAG + "_" + NetworkUtility.TAGS.PAGEID_TYPE.TERMS);
            if (mFragment == null) {
                loadFragment(InfoFragment.TAG + "_" + NetworkUtility.TAGS.PAGEID_TYPE.TERMS, InfoFragment.newInstance(NetworkUtility.TAGS.PAGEID_TYPE.TERMS));
            } else {
                Log.i(TAG, "onSlideMenuListItemClicked: " + slideMenuListModel.title + " is there");
            }
        } else if (slideMenuListModel.title.equals(getString(R.string.label_privacy_policy))) {
            Fragment mFragment = getSupportFragmentManager().findFragmentByTag(InfoFragment.TAG + "_" + NetworkUtility.TAGS.PAGEID_TYPE.PRIVACY);
            if (mFragment == null) {
                loadFragment(InfoFragment.TAG + "_" + NetworkUtility.TAGS.PAGEID_TYPE.PRIVACY, InfoFragment.newInstance(NetworkUtility.TAGS.PAGEID_TYPE.PRIVACY));
            } else {
                Log.i(TAG, "onSlideMenuListItemClicked: " + slideMenuListModel.title + " is there");
            }
        } else if (slideMenuListModel.title.equals(getString(R.string.label_logout))) {
            showLogoutConfirmationDialog();
//            LoginActivity.newInstance(mContext);
//            finish();
            // We are returning here so side menu will not close at end of this method
            return;
        } else if (slideMenuListModel.title.equals(getString(R.string.label_login))) {
            LoginActivity.newInstance(mContext);
        }/*else if (slideMenuListModel.title.equals(getString(R.string.tab_alert))) {
            showAlertDialog();
//            LoginActivity.newInstance(mContext);
//            finish();
            // We are returning here so side menu will not close at end of this method
//            return;
        }*/ else if (slideMenuListModel.title.equals(getString(R.string.tab_me))) {
            Fragment mFragment = getSupportFragmentManager().findFragmentByTag(ProfileTabFragment.TAG);
            if (mFragment == null) {
                loadFragment(ProfileTabFragment.TAG, ProfileTabFragment.newInstance());
            } else {
                Log.i(TAG, "onSlideMenuListItemClicked: " + slideMenuListModel.title + " is there");
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mActivityHomeBinding.drawerLayout.closeDrawer(GravityCompat.START, true);
            }
        }, 300);
    }


    private void loadFragment(String tag, BaseFragment baseFragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, baseFragment, tag).commit();
    }

    @Override
    public void setUpDrawerLayoutWithToolBar(Toolbar toolbar) {
        //Setting up drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void profileUpdated() {
        //Update Header view
        final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        if (userDetails != null && userDetails.ProfileImg != null && (!TextUtils.isEmpty(userDetails.ProfileImg)))
            Utility.showCircularImageView(mContext, TAG, navHeaderHomeBinding.imgProfile, userDetails.ProfileImg, R.drawable.icon_profile_img, true);

        //Update the name
        if (userDetails != null && TextUtils.isEmpty(userDetails.UserName) == false && userDetails.UserName.trim().length() > 1) {
            String name = userDetails.UserName.substring(0, 1).toUpperCase() + userDetails.UserName.substring(1);
            //Used to get only first word, e.g "Pankaj" from "pankaj sharma"
            navHeaderHomeBinding.textName.setText(name);
        }
    }

    /**
     * This methods are from Task Tab (TaskRecyclerViewAdapter.java)
     *
     * @param where
     * @param taskDetailModel
     */
    @Override
    public void onTaskRowFragListItemClicked(int where, TaskDetailModel taskDetailModel) {
//        Toast.makeText(mContext, "Row Clicked" + taskChatModel.categoryName, Toast.LENGTH_SHORT).show();
        //Checking if participant is not there then open HireNewActivity screen user can select Service Provider else open JobSummary Screen
        if (taskDetailModel.selectedProvider == null) {
            if (Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(taskDetailModel.taskStatus)
                    || Utility.TASK_STATUS.CANCELLED_SP.equalsIgnoreCase(taskDetailModel.taskStatus)
                    || Utility.TASK_STATUS.DISPUTED.equalsIgnoreCase(taskDetailModel.taskStatus)
                    || Utility.TASK_STATUS.ELAPSED.equalsIgnoreCase(taskDetailModel.taskStatus)) {
            } else {
//                HireNewJobActivity.newInstance(mContext, taskDetailModel, false);
//                TaskSummaryActivity.getInstance(mContext, taskDetailModel);
                TaskSummaryActivity.getInstance(mContext, taskDetailModel.taskId);
            }
        } else {
//            JobSummaryActivity.newInstance(mContext, taskDetailModel, taskDetailModel.selectedProvider);
            if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC))
                TaskSummaryStrategicPartnerActivity.getInstance(mContext, taskDetailModel.taskId);
            else
                TaskSummaryActivity.getInstance(mContext, taskDetailModel.taskId);
        }
    }


    @Override
    public void onFavClicked(TaskDetailModel providerModel, boolean isAddToFav, int position) {
        callAddToFavWS(providerModel.selectedProvider.providerId, isAddToFav);
    }

    @Override
    public void onMigrateTaskFromPendingToPast(TaskDetailModel model) {
        //Send Broadcast to Past task listing for adding the task, as it would get add
        //to there Need to send the broadcast to Enquiry Listing screen.
        Intent intent = new Intent(Utility.BR_NEW_TASK_ADDED);
        intent.putExtra(Utility.Extra.TASK_DETAIL, Utility.getJsonStringFromObject(model));
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onCallClicked(TaskDetailModel providerModel) {
        if (providerModel != null && providerModel.selectedProvider != null) {
//            callToOtherUser(mActivityHomeBinding.getRoot(), providerModel.selectedProvider.providerId);
            Utility.openCustomerCareCallDialer(mContext, providerModel.selectedProvider.sp_phone_number);
        }
    }

    @Override
    public void onCreateNewTask() {
        //Checking if there is already a fragment then
        Fragment mFragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
        if (mFragment == null) {
            loadFragment(HomeFragment.TAG, HomeFragment.newInstance());
        } else {
            ((HomeFragment) mFragment).openCreateNewTask();
        }
    }

    BottomAlertDialog cancelTaskDialog;

    @Override
    public void onTaskDelete(int which, final TaskDetailModel exploreDataModel, RowUpcomingTaskBinding mRowUpcomingTaskBinding) {
        cancelTaskDialog = new BottomAlertDialog(mContext);
        cancelTaskDialog.setExpandedInitially(true);
        final View view = View.inflate(mContext, R.layout.dialog_cancel_task, null);
        final RadioGroup radioGroupReasons = (RadioGroup) view.findViewById(R.id.radio_group_reasons);
        final EditText edtReason = (EditText) view.findViewById(R.id.edit_reason);
        edtReason.setEnabled(false);
        ((RadioButton) view.findViewById(R.id.radio_not_need_anymore)).setChecked(true);

        // Enabling or disabling EditText Other reason based on other reason radio button selection
        final RadioButton radioOther = (RadioButton) view.findViewById(R.id.radio_other_reason);
        radioOther.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                edtReason.setEnabled(b);
            }
        });
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioOther.isChecked() && TextUtils.isEmpty(edtReason.getText().toString())) {
                    Utility.showToast(mContext, getString(R.string.validate_reason));
                    return;
                }

                showProgressDialog();
                // Checking if other reason radio btn is selected then send reason written on edittext else send the text on radio button as reason
                RadioButton radioBtnReason = (RadioButton) view.findViewById(radioGroupReasons.getCheckedRadioButtonId());
                callCancelTaskWS(exploreDataModel.taskId, radioOther.isChecked() ? edtReason.getText().toString().trim() : radioBtnReason.getText().toString().trim());
            }
        });
        cancelTaskDialog.setTitle(getString(R.string.label_cancel_task));
        cancelTaskDialog.setCustomView(view);
        cancelTaskDialog.showDialog();
    }

    /**
     * Call Delete(Cancel)task webservice
     */
    private void callCancelTaskWS(String taskId, String reason) {

        this.taskId = taskId;
        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHomeBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(TASK_ID, taskId);
        mParams.put(NetworkUtility.TAGS.REASON, reason);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CANCEL_TASK
                , mCallCancelTaskWSErrorListener
                , mCallCancelTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

    }

    Response.Listener mCallCancelTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        Utility.showSnackBar(getString(R.string.msg_task_cancelled), mActivityHomeBinding.getRoot());
                        MessageEvent messageEvent = new MessageEvent();
                        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_CANCELED;
                        messageEvent.id = taskId;
                        messageEvent.taskStatus = jsonObject.optString(NetworkUtility.TAGS.TASK_STATUS);

                        EventBus.getDefault().post(messageEvent);
                        if (cancelTaskDialog != null)
                            cancelTaskDialog.dismiss();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityHomeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallCancelTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            taskId = null;
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallCancelTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());
        }
    };


    @Override
    public void onTaskReschedule(int which, TaskDetailModel exploreDataModel, RowUpcomingTaskBinding mRowUpcomingTaskBinding) {
//        Toast.makeText(mContext, "Reschedule Task" + exploreDataModel.categoryName, Toast.LENGTH_SHORT).show();

//        Utility.showToast(mContext, "Under Development.");
        showDateTimePickerDialog(which, exploreDataModel, mRowUpcomingTaskBinding);

        /*View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_information, null, false);
        ((TextView) view.findViewById(R.id.text_message)).setText(getString(R.string.label_reschedule_inform, exploreDataModel.selectedProvider.userName));
        final BottomAlertDialog dialog = new BottomAlertDialog(mContext);

        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setTitle("");
        dialog.setCustomView(view);
        dialog.showDialog();*/

    }


    @Override
    public void onRateClick(int which, TaskDetailModel exploreDataModel, RowUpcomingTaskBinding mRowUpcomingTaskBinding) {
        showRateDialog(exploreDataModel.selectedProvider.userName, exploreDataModel.taskId, exploreDataModel.selectedProvider.providerId);
    }

    @Override
    public void onViewQuotesClick(int which, TaskDetailModel exploreDataModel) {
        Log.d(TAG, "onViewQuotesClick() called with: which = [" + which + "], exploreDataModel = [" + exploreDataModel + "]");

        TaskQuotesActivity.newInstance(mContext, exploreDataModel, false);
    }

    BottomAlertDialog rateDialog;

    private void showRateDialog(String userName, final String taskId, final String providerId) {

        View view = View.inflate(mContext, R.layout.dialog_rate, null);

        final RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        final EditText edtMessage = (EditText) view.findViewById(R.id.edit_message);
        final TextView txtLabel = (TextView) view.findViewById(R.id.text_label);
        txtLabel.setText(getString(R.string.label_write_a_review, userName));

        rateDialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(edtMessage.getText().toString().trim())) {
                    callAddReviewWS(ratingBar.getProgress(), edtMessage.getText().toString().trim(), taskId, providerId);
                } else {
                    Utility.showToast(mContext, getString(R.string.validate_review));
                }
            }
        });
        rateDialog.setTitle(getString(R.string.label_rate));
        rateDialog.setCustomView(view);
        rateDialog.showDialog();
    }

    private String taskId;

    /**
     * Call Create Task webservice
     */
    private void callAddReviewWS(int rating, String message, String taskId, String providerId) {

        this.taskId = taskId;
        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHomeBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerId);
        mParams.put(TASK_ID, taskId);
        mParams.put(NetworkUtility.TAGS.RATINGS, String.valueOf(rating));
        mParams.put(NetworkUtility.TAGS.MESSAGE, message);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ADD_REVIEW
                , mCallAddReviewWSErrorListener
                , mCallAddReviewWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

    }

    Response.Listener mCallAddReviewWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        Utility.showSnackBar(getString(R.string.msg_thanks_for_rating), mActivityHomeBinding.getRoot());
                        MessageEvent messageEvent = new MessageEvent();
                        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_RATED;
                        messageEvent.id = taskId;
                        EventBus.getDefault().post(messageEvent);

                        if (rateDialog != null)
                            rateDialog.dismiss();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityHomeBinding.getRoot());
                        Utility.showToast(mContext, error_message);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallAddReviewWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            taskId = null;
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallAddReviewWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
        }
    };

    /**
     * ShowAlertDialog for Last Tab
     */
    private void showContactDialog() {
        final BottomAlertDialog dialog = new BottomAlertDialog(mContext);
        dialog.setTitle(getString(R.string.label_help));
        dialog.setMessage(getString(R.string.label_we_love_talking_msg));
        dialog.addPositiveButton(getString(R.string.label_call), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //callToCheepAdmin(mActivityHomeBinding.getRoot());
                Utility.initiateCallToCheepHelpLine(mContext);
            }
        });
        dialog.addNegativeButton(getString(R.string.label_chat), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HotlineHelper.getInstance(mContext).showConversation(mContext);
                dialog.dismiss();
            }
        });

        //Hiding chat dialog as it is not in current phase
        dialog.hideNegativeButton(false);

        dialog.showDialog();
    }

    public void add() {

    }

    /**
     * This is Called from HomeTabFragment.java when notification icon is clicked
     */
    @Override
    public void onNotificationIconClicked() {
        NotificationActivity.newInstance(mContext);
    }

    /**
     * Called from HomeTabRecyclerViewAdapter when category is clicked
     *
     * @param model
     * @param position
     */
    @Override
    public void onCategoryRowClicked(JobCategoryModel model, int position) {
        // Changes on 27thApril,2017
//        HireNewJobActivity.newInstance(mContext, model);
        TaskCreationActivity.getInstance(mContext, model);
    }

    @Override
    public void onCategoryFavouriteClicked(JobCategoryModel model, int position) {
        Log.d(TAG, "onCategoryFavouriteClicked() called with: model = [" + model + "], position = [" + position + "]");
        Fragment mHomeFragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
        if (mHomeFragment != null) {
            Fragment mHomeTabFragment = mHomeFragment.getChildFragmentManager().findFragmentByTag(HomeFragment.TAB_HOME);
            if (mHomeTabFragment != null) {
                Log.i(TAG, "onCategoryFavouriteClicked: Called for HomeTab");
                ((HomeTabFragment) mHomeTabFragment).onCategoryFavouriteClicked(model, position);
            }
        }
    }

    @Override
    public void onListCategoryListGetsEmpty() {
        Fragment mHomeFragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
        if (mHomeFragment != null) {
            Fragment mHomeTabFragment = mHomeFragment.getChildFragmentManager().findFragmentByTag(HomeFragment.TAB_HOME);
            if (mHomeTabFragment != null) {
                Log.i(TAG, "onCategoryFavouriteClicked: Called for HomeTab");
                ((HomeTabFragment) mHomeTabFragment).onListCategoryListGetsEmpty();
            }
        }
    }

    /**
     * Called from FAQRecyclerViewAdapter
     *
     * @param model
     */
    @Override
    public void onFAQRowClicked(FAQModel model) {
        FAQDescActivity.newInstance(mContext, model);
    }

    @Override
    public void onBackPressed() {
        if (mActivityHomeBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mActivityHomeBinding.drawerLayout.closeDrawer(GravityCompat.START, true);
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
            if (fragment != null) {
                if (!((BaseFragment) fragment).onBackPressed()) {
                    super.onBackPressed();
                }
            } else {
                Fragment mFragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
                if (mFragment == null) {
                    loadFragment(HomeFragment.TAG, HomeFragment.newInstance());
                } else {
                    super.onBackPressed();
                }
            }
        }
    }


    @Override
    public void onFavouriteRowClicked(ProviderModel providerModel, int position) {
        ProviderProfileActivity.newInstance(mContext, providerModel, true);
    }

    @Override
    public void onFavClicked(ProviderModel providerModel, boolean isAddToFav, int position) {
        callAddToFavWS(providerModel.providerId, isAddToFav);
    }

    @Override
    public void onHistoryRowClicked(HistoryModel model) {
        //Open Provider info but we need to pass ProviderModel so convert HistoryModel to ProviderModel
//        ProviderProfileActivity.newInstance(mContext, providerModel);
    }

    @Override
    public void onChatItemClicked(TaskChatModel model, int position) {
        if (model.chatId.equalsIgnoreCase(model.taskId)) {
            ChatIndividualListActivity.newInstance(mContext, model);
        } else {
            //Opening chat activity
            ChatActivity.newInstance(mContext, model);
        }
    }


    private class setUpSlideMenuListAsync extends AsyncTask<Void, Void, Void> {
        private ListView slideListview;
        private Context mContext;
        NavHeaderHomeBinding navHeaderHomeBinding;
        private SlideMenuAdapter slideMenuAdapter;

        public setUpSlideMenuListAsync(Context mContext, ListView slideListview, NavHeaderHomeBinding navHeaderHomeBinding) {
            this.slideListview = slideListview;
            this.mContext = mContext;
            this.navHeaderHomeBinding = navHeaderHomeBinding;
        }

        @Override
        protected Void doInBackground(Void... params) {

            //Initialize SlideMenuadapter
            slideMenuAdapter = new SlideMenuAdapter(getSlideMenuData(mContext), (SlideMenuAdapter.SlideMenuListItemInterface) mContext);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //Set up Slidemenu listview content
            if (slideListview.getHeaderViewsCount() == 0) {

                //Update Header view
                final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                if (userDetails != null && userDetails.ProfileImg != null && (!TextUtils.isEmpty(userDetails.ProfileImg)))
                    Utility.showCircularImageView(mContext, TAG, navHeaderHomeBinding.imgProfile, userDetails.ProfileImg, R.drawable.icon_profile_img, true);

                //Update the name
                if (userDetails != null && !TextUtils.isEmpty(userDetails.UserName) && userDetails.UserName.trim().length() > 1) {
                    navHeaderHomeBinding.textName.setText(userDetails.UserName.substring(0, 1).toUpperCase() + userDetails.UserName.substring(1));
                } else {
                    if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
                        navHeaderHomeBinding.textName.setText(Utility.GUEST_STATIC_INFO.USERNAME);
                    }
                }

//                navHeaderHomeBinding.textName.setText(userDetails.UserName);


                navHeaderHomeBinding.radioHindi.setVisibility(View.GONE);
                navHeaderHomeBinding.radioEnglish.setChecked(true);
                /*
                  TODO: This would be enabled when translation is ready to use
                 */
                //Check which language is chooseen by user
                /*if (userDetails.language.equals(NetworkUtility.TAGS.LANGUAGE_TYPE.ENGLISH)) {
                    navHeaderHomeBinding.radioEnglish.setChecked(true);
                    navHeaderHomeBinding.radioHindi.setChecked(false);
                } else {
                    navHeaderHomeBinding.radioEnglish.setChecked(false);
                    navHeaderHomeBinding.radioHindi.setChecked(true);
                }

                navHeaderHomeBinding.radioHindi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            Log.d(TAG, "onCheckedChanged() called with: buttonView HINDI = [" + buttonView + "], isChecked = [" + isChecked + "]");
                            HomeActivity.this.callUpdateLanguage(NetworkUtility.TAGS.LANGUAGE_TYPE.HINDI);
                        }
                    }
                });

                navHeaderHomeBinding.radioEnglish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            Log.d(TAG, "onCheckedChanged() called with: buttonView ENGLISH = [" + buttonView + "], isChecked = [" + isChecked + "]");
                            HomeActivity.this.callUpdateLanguage(NetworkUtility.TAGS.LANGUAGE_TYPE.ENGLISH);
                        }
                    }
                });*/

                //Add HeaderView now
                slideListview.addHeaderView(navHeaderHomeBinding.getRoot());
                slideListview.setAdapter(slideMenuAdapter);


            }
        }
    }

    /**************************************************************************************************************
     * *************************************************************************************************************
     * *****************************************Webservice Integration [End]**************************************
     * *************************************************************************************************************
     ************************************************************************************************************/

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// LOGOUT[START]/////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() called");
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SP_ADD_TO_FAV);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.LOGOUT);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ADD_REVIEW);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.CANCEL_TASK);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.CHECK_APP_VERSION);
        try {
            unregisterReceiver(mBR_OnLoginSuccess);
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    /**
     * Call Add to fav
     *
     * @param providerId
     * @param isAddToFav
     */
    private void callAddToFavWS(String providerId, boolean isAddToFav) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHomeBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerId);
        mParams.put(NetworkUtility.TAGS.REQ_FOR, isAddToFav ? "add" : "remove");

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.SP_ADD_TO_FAV
                , mCallAddSPToFavWSErrorListener
                , mCallAddSPToFavWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    Response.Listener mCallAddSPToFavWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;

                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityHomeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallAddSPToFavWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallAddSPToFavWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();


            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());

        }
    };

    /**
     * Logout Confirmation Dialog
     */
    private void showLogoutConfirmationDialog() {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle(getString(R.string.label_logout));
        builder.setMessage(getString(R.string.confirmation_logout));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick() called with: dialogInterface = [" + dialogInterface + "], i = [" + i + "]");
                callLogoutWS();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();*/

        final BottomAlertDialog dialog = new BottomAlertDialog(mContext);
        dialog.setTitle(getString(R.string.label_logout));
        dialog.setMessage(getString(R.string.confirmation_logout));
        dialog.addPositiveButton("Yes", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callLogoutWS();
                dialog.dismiss();
            }
        });
        dialog.addNegativeButton("No", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.showDialog();
    }

    /**
     * Call Login WS Key Webservice
     */
    private void callLogoutWS() {

        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mActivityHomeBinding.getRoot());
            // Clear the Users Preference Information
            PreferenceUtility.getInstance(mContext).onUserLogout();

            //Finish the current activity
            finish();

            // Redirect user to Home Screen
//          LoginActivity.newInstance(mContext);
            HomeActivity.newInstance(mContext);
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.PLATFORM, NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);
        mParams.put(NetworkUtility.TAGS.DEVICE_TOKEN, PreferenceUtility.getInstance(mContext).getFCMRegID());

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.LOGOUT
                , mCallLogoutWSErrorListener
                , mCallLogoutWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(this).addToRequestQueue(mVolleyNetworkRequest);

    }

    /**
     * Listeners for tracking Webservice calls
     */
    Response.Listener mCallLogoutWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            hideProgressDialog();

            //logout whenever it called
            // Clear the Users Preference Information
            PreferenceUtility.getInstance(mContext).onUserLogout();

            /*
            * Stop fierbase chat service
            * @Sanjay 20 Feb 2016
            * */
            stopService(new Intent(HomeActivity.this, FierbaseChatService.class));

            //Finish the current activity
            finish();

            //Redirect user to Home Screen
//            LoginActivity.newInstance(mContext);
            HomeActivity.newInstance(mContext);

            /*String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        // Clear the Users Preference Information
                        PreferenceUtility.getInstance(mContext).onUserLogout();

                        //Finish the current activity
                        finish();

                        //Redirect user to Home Screen
                        LoginActivity.newInstance(mContext);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityHomeBinding.getRoot());
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }*/

        }
    };

    Response.ErrorListener mCallLogoutWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            hideProgressDialog();

            // Clear the Users Preference Information
            PreferenceUtility.getInstance(mContext).onUserLogout();

            //Finish the current activity
            finish();

            //Redirect user to Home Screen
//            LoginActivity.newInstance(mContext);
            HomeActivity.newInstance(mContext);

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());
        }
    };
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// LOGOUT[End]/////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**************************************************************************************************************
     * *************************************************************************************************************
     * *****************************************Webservice Integration [End]**************************************
     * *************************************************************************************************************
     ************************************************************************************************************/


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////Reschedule Related Feature[Start]/////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * For Selecting Date & Time
     */
    private SuperCalendar startDateTimeSuperCalendar;

    private void showDateTimePickerDialog(final int which, final TaskDetailModel exploreDataModel, final RowUpcomingTaskBinding mRowUpcomingTaskBinding) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        startDateTimeSuperCalendar = SuperCalendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (view.isShown()) {
                    Log.d(TAG, "onDateSet() called with: view = [" + view + "], year = [" + year + "], monthOfYear = [" + monthOfYear + "], dayOfMonth = [" + dayOfMonth + "]");
                    startDateTimeSuperCalendar.set(Calendar.YEAR, year);
                    startDateTimeSuperCalendar.set(Calendar.MONTH, monthOfYear);
                    startDateTimeSuperCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    showTimePickerDialog(which, exploreDataModel, mRowUpcomingTaskBinding);
                }
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        //Show date picker dialog
        datePickerDialog.show();
    }

    private void showTimePickerDialog(final int which, final TaskDetailModel exploreDataModel, final RowUpcomingTaskBinding mRowUpcomingTaskBinding) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (view.isShown()) {
                            Log.d(TAG, "onTimeSet() called with: view = [" + view + "], hourOfDay = [" + hourOfDay + "], minute = [" + minute + "]");

                            startDateTimeSuperCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            startDateTimeSuperCalendar.set(Calendar.MINUTE, minute);

                            if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                Log.i(TAG, "onTimeSet: Date: " + startDateTimeSuperCalendar.getTimeInMillis());
                                Log.i(TAG, "onTimeSet: Date: " + startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM));
                                Log.i(TAG, "onTimeSet: Time: " + startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));

                                Calendar calRescEnd = Calendar.getInstance();
                                calRescEnd.setTimeInMillis(Long.valueOf(exploreDataModel.taskStartdate));
                                calRescEnd.add(Calendar.MONTH, 1);
                                Log.i(TAG, "onTimeSet: " + calRescEnd.getTimeInMillis());
                                if (startDateTimeSuperCalendar.getTimeInMillis() > calRescEnd.getTimeInMillis()) {
                                    Utility.showSnackBar(getString(R.string.reschedule_task_must_within_one_month), mActivityHomeBinding.getRoot());
                                    return;
                                }
                                // Call Webservice now
                                callRescheduleTaskWS(exploreDataModel.taskId, String.valueOf(startDateTimeSuperCalendar.getTimeInMillis()));


//                                mActivityHireNewJobBinding.textDate.setText(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM));
//                                mActivityHireNewJobBinding.textTime.setText(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));
                            } else {
//                                isDateSelected = false;
//                                mActivityHireNewJobBinding.textDate.setVisibility(View.GONE);
//                                mActivityHireNewJobBinding.textTime.setVisibility(View.GONE);
                                Utility.showSnackBar(getString(R.string.validate_future_date), mActivityHomeBinding.getRoot());
                            }
                        }
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        timePickerDialog.show();
    }

    /**
     * Call Delete(Cancel)task webservice
     */
    private void callRescheduleTaskWS(String taskId, String startDateTimeTimeStamp) {

        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHomeBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(TASK_ID, taskId);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, startDateTimeTimeStamp);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.RESCHEDULE_TASK
                , mCallRescheduleTaskWSErrorListener
                , mCallRescheduleTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

    }

    Response.Listener mCallRescheduleTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        JSONObject jData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
//                        Utility.showSnackBar(getString(R.string.msg_task_cancelled), mActivityHomeBinding.getRoot());
                        MessageEvent messageEvent = new MessageEvent();
                        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_RESCHEDULED;
                        messageEvent.id = jData.getString(NetworkUtility.TAGS.TASK_ID);
//                        messageEvent.taskStartdate = jData.getString(NetworkUtility.TAGS.TASK_STARTDATE);
                        EventBus.getDefault().post(messageEvent);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityHomeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallRescheduleTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallRescheduleTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////Reschedule Related Feature[End]///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////// Update Language[Start]/////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call Login WS Key Webservice
     */
    @SuppressWarnings("unchecked")
    public void callUpdateLanguage(String language) {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHomeBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.LANGUAGE, language);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.UPDATE_LANGUAGE
                , mCallUpdateLanguageWSErrorListener
                , mCallUpdateLanguageWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(this).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.UPDATE_LANGUAGE);

    }

    /**
     * Listeners for tracking Webservice calls
     */
    private final Response.Listener mCallUpdateLanguageWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        String language = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.LANGUAGE);

                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.language = language;
                        PreferenceUtility.getInstance(mContext).saveUserDetails(new JSONObject(Utility.getJsonStringFromObject(userDetails)));

                        onLanguageChangedSuccessFully();

                        //Finish the current activity
                        finish();

                        // restart this activity
                        HomeActivity.newInstance(mContext);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
//                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        Utility.showSnackBar(error_message, mActivityHomeBinding.getRoot());
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            hideProgressDialog();
        }
    };

    private void onLanguageChangedSuccessFully() {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        Log.d(TAG, "onLanguageChangedSuccessFully() called: " + PreferenceUtility.getInstance(mContext).getUserDetails().language);
        conf.locale = new Locale(PreferenceUtility.getInstance(mContext).getUserDetails().language.equals(NetworkUtility.TAGS.LANGUAGE_TYPE.HINDI)
                ? Utility.LOCALE_FOR_HINDI
                : Utility.LOCALE_FOR_ENGLISH);
        res.updateConfiguration(conf, dm);

    }

    private final Response.ErrorListener mCallUpdateLanguageWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHomeBinding.getRoot());

        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////// Update Language[END]///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This would be used for redirecting the user to HomeScreen tab, when clicks on
     * POST A TASK button from empty screen.
     */
    public void redirectToHomeTab() {
        Fragment mFragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
        if (mFragment != null) {
            HomeFragment homeFragment = (HomeFragment) mFragment;
            homeFragment.setCurrentTab(HomeFragment.TAB_HOME);
        }
    }


    /**
     * Check Version number of application
     */
    //////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////Check API [Starts]//////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    private void checkVersionOfApp() {

        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mActivityHomeBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.vVERSION, BuildConfig.VERSION_NAME);
        mParams.put(NetworkUtility.TAGS.eUSERTYPE, BuildConfig.USER_TYPE);
        mParams.put(NetworkUtility.TAGS.ePLATFORM, NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.CHECK_APP_VERSION
                , mCheckVersionNumberWSErrorListener
                , mCheckVersionNumberWSResponseListener
                , null
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList);
    }

    Response.Listener mCheckVersionNumberWSResponseListener = new Response.Listener() {
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
                        JSONObject jObjData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                        int vVersionType = Integer.parseInt(jObjData.optString(NetworkUtility.TAGS.vVERSION_TYPE));
                        String message = jObjData.optString(NetworkUtility.TAGS.VERSION_DESC);
                        switch (vVersionType) {
                            case NetworkUtility.TAGS.VERSION_CHANGE_TYPE.NORMAL:
                                // Do nothing as user is using latest update.
                                break;
                            case NetworkUtility.TAGS.VERSION_CHANGE_TYPE.RECOMMENDED_TO_UPGRADE:
                                // We need to recommend user to update the app, however not forcefully as its not compulsary
                                break;
                            case NetworkUtility.TAGS.VERSION_CHANGE_TYPE.FORCE_UPGARDE_REQUIRED:
                                // We need to forcefully ask user to update the application.
                                showForceUpgradeAppDialog(message);
                                break;

                        }
//                        Utility.showSnackBar(jsonObject.getString(NetworkUtility.TAGS.MESSAGE), mActivityHomeBinding.getRoot());
                        break;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                mCheckVersionNumberWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    private void showForceUpgradeAppDialog(String message) {
        Log.d(TAG, "showForceUpgradeAppDialog() called with: message = [" + message + "]");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setMessage(message)
                .setNegativeButton(R.string.label_update, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Utility.redirectUserToPlaystore(mContext);
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();

        builder.show();
    }

    Response.ErrorListener mCheckVersionNumberWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
        }
    };

    //////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////WebService [Ends]//////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Location [Start]
     */
    @Override
    public void onBindLocationTrackService() {
        super.onBindLocationTrackService();
        Log.d(TAG, "onBindLocationTrackService() called");
         /*
          Check if Location service is enabled or not, if not ask for user to accept it and stop the ongoing service
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Let the activity know that location permission not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Utility.REQUEST_CODE_PERMISSION_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Utility.REQUEST_CODE_PERMISSION_LOCATION);
            }
        } else {
            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
            // In case user is logged in and we have stored CityID
            if (userDetails != null && !"-1".equalsIgnoreCase(userDetails.CityID)) {
                loadHomeScreenWithEarlierSavedAddress();
            } else {
                // In case Guest user details is there
                if (PreferenceUtility.getInstance(mContext).getGuestUserDetails() != null) {
                    loadHomeScreenWithEarlierSavedAddress();
                } else {
                    requestLocationUpdateFromService();
                }
            }
        }

        /*if (mLocationTrackService.mLocation != null) {
            double latitude = mLocationTrackService.mLocation.getLatitude();
            double longitude = mLocationTrackService.mLocation.getLongitude();

            Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.content);
            if (mFragment != null && mFragment instanceof BaseFragment) {
                ((BaseFragment) mFragment).onLocationFetched(mLocationTrackService.mLocation);
            }

        } else {
            Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.content);
            if (mFragment != null && mFragment instanceof BaseFragment) {
                ((BaseFragment) mFragment).onLocationNotAvailable();
            }
            mLocationTrackService.requestLocationUpdate();
        }*/
    }

    private void loadHomeScreenWithEarlierSavedAddress() {
        // Do Nothing as app would load the earlier saved Guest locations
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.content);
        if (mFragment != null && mFragment instanceof HomeFragment) {
            ((HomeFragment) mFragment).onLoadHomeScreenWithEarlierSavedAddress();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utility.REQUEST_CODE_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                //So, ask service to fetch the location now
                requestLocationUpdateFromService();
                /**
                 * Showing Progress Dialog that, fetching your location
                 */
                showProgressDialog(getString(R.string.fetching_location));
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
//                Snackbar.make(mActivityHomeBinding.getRoot(), getString(R.string.permission_denied_location), 3000).show();
                onLocationNotAvailable();
            }
        }
    }

    @Override
    protected void onLocationNotAvailable() {
        super.onLocationNotAvailable();
        Log.d(TAG, "onLocationNotAvailable() called");
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.content);
        if (mFragment != null && mFragment instanceof BaseFragment) {
            ((BaseFragment) mFragment).onLocationNotAvailable();
        }
    }

    @Override
    protected void onLocationFetched(Location mLocation) {
        super.onLocationFetched(mLocation);
        Log.d(TAG, "onLocationFetched() called with: mLocation = [" + mLocation + "]");
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.content);
        if (mFragment != null && mFragment instanceof BaseFragment) {
            ((BaseFragment) mFragment).onLocationFetched(mLocation);
        }
        // Now There is no need to start the service so stop it.

    }

    @Override
    public void onLocationSettingsDialogNeedToBeShow(Status status) {
        super.onLocationSettingsDialogNeedToBeShow(status);
        // Location settings are not satisfied, but this can be fixed
        // by showing the user a dialog.
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            status.startResolutionForResult(this, Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
        }
    }

    @Override
    public void gpsEnabled() {
        super.gpsEnabled();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
        if (fragment != null) {
            ((BaseFragment) fragment).gpsEnabled();
        }

        fragment = getSupportFragmentManager().findFragmentByTag(ProfileTabFragment.TAG);
        if (fragment != null) {
            ((BaseFragment) fragment).gpsEnabled();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS) {
            if (resultCode == RESULT_OK) {
                onBindLocationTrackService();
            } else {
                /*
                 * Feature: Guest Login.
                 * @Changes by: Bhavesh, on 28th Aug, 2017
                 *If user doesn't want to enable location from notification pannel, we
                 * can see whether there are any earlier saved address is and if yes, we can load the
                 * data accordingly.
                 */
                onLocationNotAvailable();
            }
        }
    }


    /**
     * Location [END]
     */

    /**
     * BroadCast that would restart the screen once login has been done.
     */
    private BroadcastReceiver mBR_OnLoginSuccess = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Do nothing, just restart the activity
            Utility.hideKeyboard(mContext);
//            HomeActivity.newInstance(mContext);
            finish();
        }
    };


}
