package com.cheep.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BootstrapConstant;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.adapter.AddressRecyclerViewAdapter;
import com.cheep.adapter.ProviderRecyclerViewAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.ActivityHireNewJobBinding;
import com.cheep.databinding.DialogFilterBinding;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatTaskModel;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SharedElementTransitionHelper;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.cheep.R.id.edit_address;
import static com.cheep.utils.Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE;
import static com.cheep.utils.Utility.getObjectFromJsonString;

/**
 * Created by pankaj on 10/6/16.
 */

public class HireNewJobActivity extends BaseAppCompatActivity implements ProviderRecyclerViewAdapter.ProviderRowInteractionListener {

    private static final String TAG = "HireNewJobActivity";

    ActivityHireNewJobBinding mActivityHireNewJobBinding;
    private ProviderRecyclerViewAdapter spRecyclerViewAdapter;
    private ErrorLoadingHelper errorLoadingHelper;

    //Create task main fields which we have to send to server for create task
//    private Calendar startDateTimeCalendar = Calendar.getInstance();
    private SuperCalendar startDateTimeSuperCalendar;
    private String mCurrentPhotoPath;
    private String addressId = "", categoryId = "";
    private boolean isDateSelected = false;
    private int pageNo = 0;

    //This model is only for hire selectedProvider

    private TaskDetailModel taskDetailModel;
    private String intentAction;
    private String taskId;
    private String providerID;
    boolean isFirstTime;


    public static void newInstance(Context context, TaskDetailModel model, boolean isFirstTimeCreate) {
        Intent intent = new Intent(context, HireNewJobActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(model));
        intent.putExtra(Utility.Extra.IS_FIRST_TIME, isFirstTimeCreate);
        intent.setAction(Utility.ACTION_HIRE_PROVIDER);
        context.startActivity(intent);
    }

    public static void newInstance(Context context, String taskId, String provideId) {
        Intent intent = new Intent(context, HireNewJobActivity.class);
        intent.putExtra(NetworkUtility.TAGS.TASK_ID, taskId);
        intent.putExtra(NetworkUtility.TAGS.SP_USER_ID, provideId);
        intent.putExtra(Utility.Extra.IS_FIRST_TIME, false);
        intent.setAction(Utility.ACTION_HIRE_PROVIDER_WITH_REFRESHING_TASK_DETAILS);
        context.startActivity(intent);
    }

    private Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        // Checking if savedinstance state is null then only init views
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        mActivityHireNewJobBinding = DataBindingUtil.setContentView(this, R.layout.activity_hire_new_job);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Setting toolbar
        startDateTimeSuperCalendar = SuperCalendar.getInstance();

        intentAction = getIntent().getAction();
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            taskId = bundle.getString(NetworkUtility.TAGS.TASK_ID);
            providerID = bundle.getString(NetworkUtility.TAGS.SP_USER_ID);
            isFirstTime = bundle.getBoolean(Utility.Extra.IS_FIRST_TIME, false);
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);
           /* if (Utility.ACTION_NEW_JOB_CREATE.equalsIgnoreCase(intentAction)) {
                jobCategoryModel = (JobCategoryModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), JobCategoryModel.class);
            } else {
                taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);
            }*/
        }

        setSupportActionBar(mActivityHireNewJobBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivityHireNewJobBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }

        if (Utility.ACTION_HIRE_PROVIDER.equalsIgnoreCase(intentAction)) {
            populateFieldsForHireProvider();
        } else if (Utility.ACTION_HIRE_PROVIDER_WITH_REFRESHING_TASK_DETAILS.equalsIgnoreCase(intentAction)) {
            callTaskDetailWS(taskId, providerID);
        }

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.UPDATE_FAVOURITE) {
            if (!TextUtils.isEmpty(event.isFav))
                spRecyclerViewAdapter.updateFavStatus(event.id, event.isFav);
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PAID) {
            finish();
            // Refresh the SP listing
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.QUOTE_REQUESTED_BY_PRO
                || event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.REQUEST_FOR_DETAIL) {

            // Only go ahead if we are in same task detail screen whose notification comes
            if (taskDetailModel.taskId.equals(event.id)) {
                // We need to refresh the SP listing.
                spRecyclerViewAdapter.enableLoadMore();
                reloadSPListWS();
            }
        }
    }


    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy() called");
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        /**
         * Cancel the request as it no longer available
         */
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SP_ADD_TO_FAV);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.EDIT_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ADD_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.DELETE_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.CREATE_TASK);

        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SP_LIST);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SP_LIST_TASK_WISE);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SP_LIST_FILTER);

        super.onDestroy();
    }

    @Override
    protected void setListeners() {

    }

   /* *//**
     * FIELDS INITIALIZATION AND DATA LOADING FOR
     * NEW JOB CREATE
     *//*
    private void populateFieldsForNewJobCreate() {

        mActivityHireNewJobBinding.iconFilter.setVisibility(View.GONE);

        errorLoadingHelper = new ErrorLoadingHelper(mActivityHireNewJobBinding.commonRecyclerView.recyclerView);

        if (jobCategoryModel != null) {

            categoryId = jobCategoryModel.catId;
//            mActivityHireNewJobBinding.textTitle.setText(getString(R.string.label_hire_a_x, model.catName));
            mActivityHireNewJobBinding.textTitle.setText(jobCategoryModel.catName);

            final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

            //Setting adapter with no lists;
            //Setting SP List recycler view adapter
            mActivityHireNewJobBinding.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            spRecyclerViewAdapter = new ProviderRecyclerViewAdapter(HireNewJobActivity.this, intentAction);
            mActivityHireNewJobBinding.commonRecyclerView.recyclerView.setAdapter(spRecyclerViewAdapter);
            //Set dividers to Recyclerview
            mActivityHireNewJobBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal));

            errorLoadingHelper.showLoading();
            spRecyclerViewAdapter.setIsLoadMoreEnabled(true, R.layout.load_more_progress, mActivityHireNewJobBinding.commonRecyclerView.recyclerView, new LoadMoreRecyclerAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    Log.d(TAG, "onLoadMore() called");
                    if (spRecyclerViewAdapter.getmList().size() > 0) {
                        callSPListWS(jobCategoryModel.catId, userDetails.CityID, null);
                    }
                }
            });

            callSPListWS(jobCategoryModel.catId, userDetails.CityID, null);
        }

        mActivityHireNewJobBinding.btnPostIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTaskOnServer();
//                HireNewJobActivity.newInstance(mContext, BootstrapConstant.DUMMY_TASK_CHAT_LIST.get(0));
//                finish();
            }
        });

        mActivityHireNewJobBinding.layoutWhen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Utility.hideKeyboard(mContext, mActivityHireNewJobBinding.editTaskDesc);
                showDateTimePickerDialog();
            }
        });
        mActivityHireNewJobBinding.layoutWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Utility.hideKeyboard(mContext, mActivityHireNewJobBinding.editTaskDesc);
                showAddressDialog();
            }
        });

        mActivityHireNewJobBinding.layoutAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onClickOfChooseFile(Utility.REQUEST_CODE_GET_FILE_ADD_PHOTO, Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_COVER);
               // Utility.hideKeyboard(mContext, mActivityHireNewJobBinding.editTaskDesc);
                showPictureChooserDialog();
            }
        });


        mActivityHireNewJobBinding.textDate.setVisibility(View.GONE);
        mActivityHireNewJobBinding.textTime.setVisibility(View.GONE);
        mActivityHireNewJobBinding.textAddress.setVisibility(View.GONE);
        mActivityHireNewJobBinding.imgAttach.setVisibility(View.GONE);

        mActivityHireNewJobBinding.textLabelAttach.setText(getString(R.string.label_attach));

        initSwipeToRefreshLayout();
    }*/

    /**
     * FIELDS INITIALIZATION AND DATA LOADING FOR
     * HIRE PROVIDER
     */
    private void populateFieldsForHireProvider() {

        errorLoadingHelper = new ErrorLoadingHelper(mActivityHireNewJobBinding.commonRecyclerView.recyclerView);

//        mActivityHireNewJobBinding.editTaskDesc.setEnabled(false);
        mActivityHireNewJobBinding.editTaskDesc.setFocusable(false);
        mActivityHireNewJobBinding.editTaskDesc.setFocusableInTouchMode(false);
        mActivityHireNewJobBinding.btnPostIt.setVisibility(View.GONE);


        mActivityHireNewJobBinding.iconFilter.setVisibility(View.VISIBLE);

        if (taskDetailModel != null) {

            mActivityHireNewJobBinding.textTitle.setText(taskDetailModel.categoryName);
            mActivityHireNewJobBinding.editTaskDesc.setText(taskDetailModel.taskDesc);

            mActivityHireNewJobBinding.textSubCategoryName.setText(taskDetailModel.subCategoryName);
            //Setting adapter with no lists;
            //Setting SP List recycler view adapter
            mActivityHireNewJobBinding.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            spRecyclerViewAdapter = new ProviderRecyclerViewAdapter(HireNewJobActivity.this, intentAction);
            mActivityHireNewJobBinding.commonRecyclerView.recyclerView.setAdapter(spRecyclerViewAdapter);
            //Set dividers to Recyclerview
            //  mActivityHireNewJobBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal));

            //Filling predefined fields from model

            SuperCalendar superCalendar = SuperCalendar.getInstance();
            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            superCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskStartdate));
            superCalendar.setLocaleTimeZone();

            /*startDateTimeCalendar = Calendar.getInstance();
            startDateTimeCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskStartdate));
            mActivityHireNewJobBinding.textDate.setText(DateHelper.getFormatedDate(startDateTimeCalendar, DateHelper.DATE_FORMAT_DD_MM_YY));
            mActivityHireNewJobBinding.textTime.setText(DateHelper.getFormatedDate(startDateTimeCalendar, DateHelper.DATE_FORMAT_HH_MM_AM));*/

            mActivityHireNewJobBinding.textDate.setText(superCalendar.format(Utility.DATE_FORMAT_DD_MMM));
            mActivityHireNewJobBinding.textTime.setText(superCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));
            mActivityHireNewJobBinding.textAddress.setText(taskDetailModel.taskAddress);
            mActivityHireNewJobBinding.editTaskDesc.setText(taskDetailModel.taskDesc);

            if (!TextUtils.isEmpty(taskDetailModel.taskImage)) {
                Utility.loadImageView(mContext, mActivityHireNewJobBinding.imgAttach, taskDetailModel.taskImage, 0);
                mActivityHireNewJobBinding.imgAttach.setVisibility(View.VISIBLE);
            } else {
                mActivityHireNewJobBinding.imgAttach.setVisibility(View.GONE);
            }


            final boolean isFirstTime = taskDetailModel.providerCount != null && "0".equalsIgnoreCase(taskDetailModel.providerCount);
            spRecyclerViewAdapter.setIsLoadMoreEnabled(true, R.layout.load_more_progress, mActivityHireNewJobBinding.commonRecyclerView.recyclerView, new LoadMoreRecyclerAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    spRecyclerViewAdapter.onLoadMoreComplete();
                    /*Log.d(TAG, "onLoadMore() called");
                    //@Bhavesh: 4thFeb2017 Do not go ahead in case filter is enabled
                    if (isFilterApplied && mDialogFilterBinding != null) {
                        spRecyclerViewAdapter.onLoadMoreComplete();
                        return;
                    }

                    if (spRecyclerViewAdapter.getmList().size() > 0) {
                        //call
                        if (isFirstTime) {
//                        if (taskDetailModel.providerCount != null && "0".equalsIgnoreCase(taskDetailModel.providerCount)) {
                            final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                            callSPListWS(taskDetailModel.categoryId, userDetails.CityID, null);
                        } else {
                            callSPListWS(null, null, taskDetailModel.taskId);
                           // callSPListWS(mCategoryId, mCityId, mTaskId);
                        }
                    }*/
                }
            });

            errorLoadingHelper.showLoading();
            pageNo = 0;
            //Checking if there are no selectedProvider who quote for this task then hide filter icons and call nearby selectedProvider service ELSE call sp list with task
            if (isFirstTime) {
//            if (taskDetailModel.providerCount != null && "0".equalsIgnoreCase(taskDetailModel.providerCount)) {
                final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                callSPListWS(taskDetailModel.categoryId, userDetails.CityID, null);
                mActivityHireNewJobBinding.iconFilter.setVisibility(View.GONE);
            } else {
                callSPListWS(null, null, taskDetailModel.taskId);
                mActivityHireNewJobBinding.iconFilter.setVisibility(View.VISIBLE);
            }
        }

        mActivityHireNewJobBinding.textDate.setVisibility(View.VISIBLE);
        mActivityHireNewJobBinding.textTime.setVisibility(View.VISIBLE);
        mActivityHireNewJobBinding.textAddress.setVisibility(View.VISIBLE);
        mActivityHireNewJobBinding.imgAttach.setVisibility(View.VISIBLE);
        mActivityHireNewJobBinding.textLabelAttach.setText(getString(R.string.label_view));
        mActivityHireNewJobBinding.iconFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog(cheepest, distance, isFav, rating);
            }
        });

        mActivityHireNewJobBinding.layoutWhen.setOnClickListener(null);
        mActivityHireNewJobBinding.layoutWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show address in Bottom sheet
                showFullDesc(getString(R.string.label_address), taskDetailModel.taskAddress);
            }
        });

        //Checking if there is image then set onclick listener else set it null
        if (!TextUtils.isEmpty(taskDetailModel.taskImage)) {

            mActivityHireNewJobBinding.imgAttach.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedElementTransitionHelper sharedElementTransitionHelper = new SharedElementTransitionHelper(HireNewJobActivity.this);
                    sharedElementTransitionHelper.put(mActivityHireNewJobBinding.imgAttach, R.string.transition_image_view);
                    ZoomImageActivity.newInstance(mContext, sharedElementTransitionHelper.getBundle(), taskDetailModel.taskImage);
                }
            });
        } else {
            mActivityHireNewJobBinding.imgAttach.setOnClickListener(null);
        }
        mActivityHireNewJobBinding.layoutAttach.setOnClickListener(null);

        initSwipeToRefreshLayout();
    }

    TextView txtMessage;
    BottomAlertDialog dialogDesc;

    private void showFullDesc(String title, String message) {
        if (dialogDesc == null) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_information, null, false);
            txtMessage = (TextView) view.findViewById(R.id.text_message);
            dialogDesc = new BottomAlertDialog(mContext);

            view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDesc.dismiss();
                }
            });

            dialogDesc.setTitle(title);
            dialogDesc.setCustomView(view);
        }
        txtMessage.setText(message);
        dialogDesc.showDialog();
    }

    int cheepest, distance, rating;
    boolean isFav;

    private void initSwipeToRefreshLayout() {
        mActivityHireNewJobBinding.commonRecyclerView.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                spRecyclerViewAdapter.enableLoadMore();
                reloadSPListWS();
            }
        });
        Utility.setSwipeRefreshLayoutColors(mActivityHireNewJobBinding.commonRecyclerView.swipeRefreshLayout);
    }


    @Override
    public void onProviderRowClicked(ProviderModel providerModel, int position) {
        /*if (Utility.TASK_STATUS.QUOTE_REQUESTED.equalsIgnoreCase(providerModel.requestType) && taskDetailModel != null) {
            JobSummaryActivity.newInstance(mContext, taskDetailModel, providerModel);
        } else {
            ProviderProfileActivity.newInstance(mContext, providerModel);
        }*/
        if (taskDetailModel != null) {
            ProviderProfileActivity.newInstance(mContext, providerModel, taskDetailModel);
        } else {
            ProviderProfileActivity.newInstance(mContext, providerModel);
        }
    }

    @Override
    public void onProviderPayClicked(ProviderModel providerModel, int position) {
        if (taskDetailModel != null) {
//            JobSummaryActivity.newInstance(mContext, taskDetailModel, providerModel);
            PaymentsStepActivity.newInstance(mContext, taskDetailModel, providerModel, 0);
        } else {
            ProviderProfileActivity.newInstance(mContext, providerModel);
        }
    }

    @Override
    public void onActionButtonClicked(ProviderModel providerModel, int position) {
        Log.d(TAG, "onActionButtonClicked() called with: providerModel = [" + providerModel + "], position = [" + position + "]");
        showDetailRequestDialog(providerModel);
    }

    @Override
    public void onChatClicked(ProviderModel providerModel, int position) {
        if (providerModel != null && taskDetailModel != null) {
            if (providerModel.request_detail_status.equalsIgnoreCase(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED)) {
                TaskChatModel taskChatModel = new TaskChatModel();
                taskChatModel.categoryName = taskDetailModel.categoryName;
                taskChatModel.taskDesc = taskDetailModel.taskDesc;
                taskChatModel.taskId = taskDetailModel.taskId;
                taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(providerModel.providerId);
                taskChatModel.participantName = providerModel.userName;
                taskChatModel.participantPhotoUrl = providerModel.profileUrl;
                ChatActivity.newInstance(HireNewJobActivity.this, taskChatModel);
                return;
            }
            callTaskDetailRequestAcceptWS(Utility.ACTION_CHAT, taskDetailModel.taskId, providerModel);
        }
    }

    @Override
    public void onCallClicked(ProviderModel providerModel, int position) {
        if (providerModel != null && !TextUtils.isEmpty(providerModel.providerId) && taskDetailModel != null) {
            if (providerModel.request_detail_status.equalsIgnoreCase(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED)) {
//                callToOtherUser(mActivityHireNewJobBinding.getRoot(), providerModel.providerId);
                Utility.openCustomerCareCallDialer(mContext, providerModel.sp_phone_number);
                return;
            }
            callTaskDetailRequestAcceptWS(Utility.ACTION_CALL, taskDetailModel.taskId, providerModel);
        }
    }

    @Override
    public void onFavClicked(ProviderModel providerModel, boolean isAddToFav, int position) {
        callAddToFavWS(providerModel.providerId, isAddToFav);
    }

    private BottomAlertDialog addressDialog;
    private AddressRecyclerViewAdapter addressRecyclerViewAdapter;

    private void showAddressDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_choose_address_new_task, null, false);
        boolean shouldOpenAddAddress = fillAddressRecyclerView((RecyclerView) view.findViewById(R.id.recycler_view));
        addressDialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_add_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddAddressDialog(null);
//                addAddressDialog.dismiss();
            }
        });
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addressRecyclerViewAdapter != null && addressRecyclerViewAdapter.getmList().isEmpty() == false) {
                    AddressModel model = addressRecyclerViewAdapter.getSelectedAddress();
                    if (model != null) {
                        mActivityHireNewJobBinding.textAddress.setText(model.address);
                        mActivityHireNewJobBinding.textAddress.setVisibility(View.VISIBLE);
                        addressId = model.address_id;
                        addressDialog.dismiss();
                    }

                    //refresh list based on address

                    pageNo = 0;
                    isFilterApplied = false;
                    errorLoadingHelper.showLoading();
                    callSPListWS(mCategoryId, mCityId, mTaskId);
                }
            }
        });
        addressDialog.setTitle(getString(R.string.label_address));
        addressDialog.setCustomView(view);
        addressDialog.setExpandedInitially(true);
        addressDialog.showDialog();

        if (shouldOpenAddAddress) {
            showAddAddressDialog(null);
        }
    }

    private int getMaxQuote() {
        try {
            if (!TextUtils.isEmpty(taskDetailModel.maxQuotePrice) && !taskDetailModel.maxQuotePrice.equalsIgnoreCase("0")) {
                return (int) Double.parseDouble(taskDetailModel.maxQuotePrice) + 1;
            }
        } catch (Exception e) {
        }
        return 20000;
    }

    private int getMaxDistance() {
        return 15;
    }

    BottomAlertDialog filterDialog;
    DialogFilterBinding mDialogFilterBinding;

    private void showFilterDialog(int cheepest, int distance, boolean isFav, int rating) {

        if (filterDialog == null) {
            mDialogFilterBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_filter, null, false);
            //Cheepest is max ₹10000
            mDialogFilterBinding.seekbarCheepest.setPrefix("₹");
//            mDialogFilterBinding.seekbarCheepest.setMin(100);
            mDialogFilterBinding.seekbarCheepest.setMax(getMaxQuote());
            mDialogFilterBinding.textLabelMaxPrice.setText(getString(R.string.ruppe_symbol_x, String.valueOf(mDialogFilterBinding.seekbarCheepest.getMax())));
            //Distance is max 50km
            mDialogFilterBinding.seekbarDistance.setSuffix("km");
//            mDialogFilterBinding.seekbarDistance.setMin(3);
            mDialogFilterBinding.seekbarDistance.setMax(getMaxDistance());
            mDialogFilterBinding.textLabelMaxDistance.setText(getMaxDistance() + "km");
            mDialogFilterBinding.imgFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialogFilterBinding.imgFav.setSelected(!mDialogFilterBinding.imgFav.isSelected());
                }
            });
            filterDialog = new BottomAlertDialog(mContext);
            filterDialog.setExpandedInitially(true);
            mDialogFilterBinding.btnShowResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Saving this field when user fix filter fields
                    HireNewJobActivity.this.cheepest = mDialogFilterBinding.seekbarCheepest.getDisplayProgress();
                    HireNewJobActivity.this.distance = mDialogFilterBinding.seekbarDistance.getDisplayProgress();
                    HireNewJobActivity.this.isFav = mDialogFilterBinding.imgFav.isSelected();
                    HireNewJobActivity.this.rating = mDialogFilterBinding.ratingBar.getProgress();
//                    Utility.showToast(mContext, "Under Development.");

                    //filter from server
                    isFilterApplied = true;
                    pageNo = 0;
                    errorLoadingHelper.showLoading();
                    callSPListWS(mCategoryId, mCityId, mTaskId);

                    filterDialog.dismiss();
                }
            });
            mDialogFilterBinding.btnReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetFilterFields(mDialogFilterBinding);
                    pageNo = 0;
                    isFilterApplied = false;
                    errorLoadingHelper.showLoading();
                    callSPListWS(mCategoryId, mCityId, mTaskId);
                    filterDialog.dismiss();
                }
            });
            filterDialog.setTitle(getString(R.string.label_filter));
            filterDialog.setCustomView(mDialogFilterBinding.getRoot());

            //reseting all the fields for first time
            resetFilterFields(mDialogFilterBinding);
        } else {
//            mDialogFilterBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_filter, null, false);
        }

        //Prefilling all fields when user filter is already applied
        if (cheepest > 0) {
            mDialogFilterBinding.seekbarCheepest.setDisplayProgress(cheepest);
        }
        if (distance > 0) {
            mDialogFilterBinding.seekbarDistance.setDisplayProgress(distance);
        }
        mDialogFilterBinding.imgFav.setSelected(isFav);
        mDialogFilterBinding.ratingBar.setRating(rating);

        filterDialog.showDialog();
    }

    boolean isFilterApplied = false;

    private void resetFilterFields(DialogFilterBinding mDialogFilterBinding) {
        mDialogFilterBinding.seekbarDistance.setProgress(getMaxDistance());
        mDialogFilterBinding.seekbarCheepest.setProgress(getMaxQuote());
        mDialogFilterBinding.imgFav.setSelected(false);
        mDialogFilterBinding.ratingBar.setRating(0);

        cheepest = mDialogFilterBinding.seekbarCheepest.getDisplayProgress();
        distance = mDialogFilterBinding.seekbarDistance.getDisplayProgress();
        isFav = mDialogFilterBinding.imgFav.isSelected();
        rating = mDialogFilterBinding.ratingBar.getProgress();

        isFilterApplied = false;
    }

    private boolean isFilterApplied() {
        return false;
    }

    private BottomAlertDialog addAddressDialog;
    private EditText edtAddress;

    private void showAddAddressDialog(final AddressModel addressModel) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_address, null, false);
        final RadioButton radioHome = (RadioButton) view.findViewById(R.id.radio_home);
        final RadioButton radioOther = (RadioButton) view.findViewById(R.id.radio_other);
//        final EditText edtName = (EditText) view.findViewById(R.id.edit_name);
        edtAddress = (EditText) view.findViewById(edit_address);

        edtAddress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    final int DRAWABLE_LEFT = 0;
                    final int DRAWABLE_TOP = 1;
                    final int DRAWABLE_RIGHT = 2;
                    final int DRAWABLE_BOTTOM = 3;

                    if (edtAddress.getTag() != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (event.getRawX() >= (edtAddress.getRight() - edtAddress.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // your action here
                            showPlacePickerDialog(false);
                            return true;
                        }
                    } else if (edtAddress.getTag() == null) {
                        showPlacePickerDialog(false);
                        return true;
                    }
                }
                return false;
            }
        });
        final Button btnAdd = (Button) view.findViewById(R.id.btn_add);

        if (addressModel != null) {
            if (NetworkUtility.TAGS.ADDRESS_TYPE.HOME.equalsIgnoreCase(addressModel.category)) {
                radioHome.setChecked(true);
//                radioHome.setSelected(true);
            } else {
                radioOther.setChecked(true);
//                radioOther.setSelected(true);
            }

            edtAddress.setTag(addressModel.getLatLng());
//            edtName.setText(addressModel.name);
            edtAddress.setText(addressModel.address);
            btnAdd.setText(getString(R.string.label_update));

        } else {
            btnAdd.setText(getString(R.string.label_add));
            radioHome.setChecked(true);
            edtAddress.setFocusable(false);
            edtAddress.setFocusableInTouchMode(false);
        }

        addAddressDialog = new BottomAlertDialog(mContext);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*if (TextUtils.isEmpty(edtName.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address_nickname));
                } else*/ if (TextUtils.isEmpty(edtAddress.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address));
                } else {
                    if (addressModel != null) {
                        callUpdateAddressWS(addressModel.address_id
                                , (radioHome.isChecked() ? NetworkUtility.TAGS.ADDRESS_TYPE.HOME : NetworkUtility.TAGS.ADDRESS_TYPE.OTHERS)
                                /*, edtName.getText().toString().trim()*/
                                , edtAddress.getText().toString().trim()
                                , (LatLng) edtAddress.getTag());
                    } else {
                        callAddAddressWS((radioHome.isChecked() ? NetworkUtility.TAGS.ADDRESS_TYPE.HOME : NetworkUtility.TAGS.ADDRESS_TYPE.OTHERS)
                                /*, edtName.getText().toString().trim()*/
                                , edtAddress.getText().toString().trim()
                                , (LatLng) edtAddress.getTag());
                    }
                }
            }
        });
        addAddressDialog.setTitle(getString(R.string.label_add_address));
        addAddressDialog.setCustomView(view);
        addAddressDialog.showDialog();
    }


    private void showPlacePickerDialog(boolean isForceShow) {

        if (isForceShow == false) {

            if (mLocationTrackService != null) {
                isPlacePickerClicked = true;
                mLocationTrackService.requestLocationUpdate();
                return;
            }
            /*if (isLocationEnabled() == false) {
                if (isGPSEnabled() == false) {
                    showGPSEnableDialog();
                    return;
                }
            }*/

            /*String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (locationProviders == null || locationProviders.equals("")) {
                //show gps disabled and enable gps dialog here
                showGPSEnableDialog();
                return;
            }

            LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //show gps disabled and enable gps dialog here
                showGPSEnableDialog();
                return;
            }*/
        }


        try {
            Utility.hideKeyboard(mContext);
            showProgressDialog();
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(HireNewJobActivity.this);
            startActivityForResult(intent, Utility.PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {

            //TODO: Adding dummy place when playservice is not there
            if (edtAddress != null) {
                edtAddress.setText("Dummy Address with " + BootstrapConstant.LAT + "," + BootstrapConstant.LNG);
                edtAddress.setFocusable(true);
                edtAddress.setFocusableInTouchMode(true);
                try {
                    edtAddress.setTag(new LatLng(Double.parseDouble(BootstrapConstant.LAT), Double.parseDouble(BootstrapConstant.LNG)));
                } catch (Exception exe) {
                    exe.printStackTrace();
                    edtAddress.setTag(new LatLng(0, 0));
                }
            }

            e.printStackTrace();
            Utility.showToast(mContext, getString(R.string.label_playservice_not_available));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        Log.d(TAG, "onSaveInstanceState() called with: outState = [" + outState + "]");

        outState.putString(Utility.Extra.ADDRESS_ID, addressId);
        outState.putSerializable(Utility.Extra.ADDRESS_TEXT, mActivityHireNewJobBinding.textAddress.getText().toString());
        outState.putSerializable(Utility.Extra.DATE_TIME, startDateTimeSuperCalendar.getCalendar());

        outState.putSerializable(Utility.Extra.SELECTED_IMAGE_PATH, mCurrentPhotoPath);


        outState.putString(Utility.Extra.CATEGORY_ID, categoryId);
        outState.putBoolean(Utility.Extra.IS_DATE_SELECTED, isDateSelected);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState() called with: savedInstanceState = [" + savedInstanceState + "]");
        addressId = savedInstanceState.getString(Utility.Extra.ADDRESS_ID);
        categoryId = savedInstanceState.getString(Utility.Extra.CATEGORY_ID);
        isDateSelected = savedInstanceState.getBoolean(Utility.Extra.IS_DATE_SELECTED);
        Calendar calendar = (Calendar) savedInstanceState.getSerializable(Utility.Extra.DATE_TIME);
        startDateTimeSuperCalendar = SuperCalendar.getInstance();
        startDateTimeSuperCalendar.setCalendar(calendar);

        mCurrentPhotoPath = savedInstanceState.getString(Utility.Extra.SELECTED_IMAGE_PATH);

        if (isDateSelected) {
            mActivityHireNewJobBinding.textDate.setText(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM));
            mActivityHireNewJobBinding.textTime.setText(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));
            mActivityHireNewJobBinding.textDate.setVisibility(View.VISIBLE);
            mActivityHireNewJobBinding.textTime.setVisibility(View.VISIBLE);
        }
        mActivityHireNewJobBinding.textAddress.setText(savedInstanceState.getString(Utility.Extra.ADDRESS_TEXT));
        mActivityHireNewJobBinding.textAddress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLocationFetched(Location mLocation) {
        super.onLocationFetched(mLocation);
    }

    boolean isPlacePickerClicked = false;

    @Override
    public void gpsEnabled() {
        super.gpsEnabled();
        if (isPlacePickerClicked == true) {
            showPlacePickerDialog(true);
        }
    }

    @Override
    public void onLocationSettingsDialogNeedToBeShow(Status locationRequest) {
        super.onLocationSettingsDialogNeedToBeShow(locationRequest);
        // Location settings are not satisfied, but this can be fixed
        // by showing the user a dialog.
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            locationRequest.startResolutionForResult(this, Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
        }
    }

    private void showGPSEnableDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle);
        /*builder.setCancelable(false);
        builder.setTitle(getString(R.string.label_force_logout));
        builder.setMessage(getString(R.string.desc_force_logout));
        builder.setPositiveButton(getString(R.string.label_Ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(android.selectedProvider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.show();
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);*/
        builder.setMessage("For best results, let your device turn on location using Google's location service.")
                .setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        showPlacePickerDialog(true);
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    // Better way to check location service status
    protected boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            /*
                Settings.Secure
                    Secure system settings, containing system preferences that applications can read
                    but are not allowed to write. These are for preferences that the user must
                    explicitly modify through the system UI or specialized APIs for those values,
                    not modified directly by applications.
            */
            /*
                public static final String LOCATION_MODE
                    The degree of location access enabled by the user.

                    When used with putInt(ContentResolver, String, int), must be one of
                    LOCATION_MODE_HIGH_ACCURACY, LOCATION_MODE_SENSORS_ONLY,
                    LOCATION_MODE_BATTERY_SAVING, or LOCATION_MODE_OFF. When used with
                    getInt(ContentResolver, String), the caller must gracefully handle additional
                    location modes that might be added in the future.

                    Note: do not rely on this value being present in settings.db or on
                    ContentObserver notifications for the corresponding Uri.
                    Use MODE_CHANGED_ACTION to receive changes in this value.

                    Constant Value: "location_mode"
            */
            /*
                public static int getInt (ContentResolver cr, String name, int def)
                    Convenience function for retrieving a single secure settings value as an integer.
                    Note that internally setting values are always stored as strings; this function
                    converts the string to an integer for you. The default value will be returned
                    if the setting is not defined or not an integer.

                Parameters
                    cr : The ContentResolver to access.
                    name : The name of the setting to retrieve.
                    def : Value to return if the setting is not defined.
                Returns
                    The setting's current value, or 'def' if it is not defined or not a valid integer.
            */
            // check location state for api version 19 or greater
            int locationMode = Settings.Secure.getInt(
                    mContext.getContentResolver(),
                    Settings.Secure.LOCATION_MODE,
                    0
            );

            /*
                public static final int LOCATION_MODE_OFF
                    Location access disabled.

                Constant Value: 0 (0x00000000)
            */
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            /*
                public static String getString (ContentResolver resolver, String name)
                    Look up a name in the database.

                Parameters
                    resolver : to access the database with
                    name : to look up in the table
                Returns
                    the corresponding value, or null if not present
            */
            /*
                public static final String LOCATION_PROVIDERS_ALLOWED
                    This constant was deprecated in API level 19.
                    use LOCATION_MODE and MODE_CHANGED_ACTION (or PROVIDERS_CHANGED_ACTION)

                    Comma-separated list of location providers that activities may access.
                    Do not rely on this value being present in settings.db or on ContentObserver
                    notifications on the corresponding Uri.

                    Constant Value: "location_providers_allowed"
            */
            String locationProviders = Settings.Secure.getString(
                    mContext.getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            );

            /*
                public static boolean isEmpty (CharSequence str)
                    Returns true if the string is null or 0-length.

                Parameters
                    str : the string to be examined
                Returns
                    true : if str is null or zero length
            */
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    // Custom method to check GPS service is enabled or disabled
    protected boolean isGPSEnabled() {
        /*
            LocationManager
                This class provides access to the system location services. These services allow
                applications to obtain periodic updates of the device's geographical location, or
                to fire an application-specified Intent when the device enters the proximity of
                a given geographical location.

                You do not instantiate this class directly; instead, retrieve it through
                Context.getSystemService(Context.LOCATION_SERVICE).
        */
        /*
            public abstract Object getSystemService (String name)
                Return the handle to a system-level service by name. The class of the returned
                object varies by the requested name.
        */
        /*
            public static final String LOCATION_SERVICE
                Use with getSystemService(Class) to retrieve a LocationManager for
                controlling location updates.

                Constant Value: "location"
        */
        LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        /*
            public boolean isProviderEnabled (String selectedProvider)
                Returns the current enabled/disabled status of the given selectedProvider.

                If the user has enabled this selectedProvider in the Settings menu, true is returned
                otherwise false is returned

                Callers should instead use LOCATION_MODE unless they depend on selectedProvider-specific
                APIs such as requestLocationUpdates(String, long, float, LocationListener).

                Before API version LOLLIPOP, this method would throw SecurityException if the
                location permissions were not sufficient to use the specified selectedProvider.

            Parameters
                selectedProvider : the name of the selectedProvider
            Returns
                true : if the selectedProvider exists and is enabled
            Throws
                IllegalArgumentException : if selectedProvider is null

        */
        boolean GPSStatus = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return GPSStatus;
    }

    // Custom method for api level 19 or higher to check location service status
    protected boolean isLocationEnabledFromAPI19() {
        int locationMode = Settings.Secure.getInt(
                mContext.getContentResolver(),
                Settings.Secure.LOCATION_MODE,
                0
        );

        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

    private void showDateTimePickerDialog() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (view.isShown()) {
                    Log.d(TAG, "onDateSet() called with: view = [" + view + "], year = [" + year + "], monthOfYear = [" + monthOfYear + "], dayOfMonth = [" + dayOfMonth + "]");
                    startDateTimeSuperCalendar.set(Calendar.YEAR, year);
                    startDateTimeSuperCalendar.set(Calendar.MONTH, monthOfYear);
                    startDateTimeSuperCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

//                startDateTimeCalendar.set(Calendar.YEAR, year);
//                startDateTimeCalendar.set(Calendar.MONTH, monthOfYear);
//                startDateTimeCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePickerDialog();
                }
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
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

//                        startDateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                        startDateTimeCalendar.set(Calendar.MINUTE, minute);

                            if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                isDateSelected = true;
//                            mActivityHireNewJobBinding.textDate.setText(DateHelper.getFormatedDate(startDateTimeCalendar, DateHelper.DATE_FORMAT_DD_MM_YY));
//                            mActivityHireNewJobBinding.textTime.setText(DateHelper.getFormatedDate(startDateTimeCalendar, DateHelper.DATE_FORMAT_HH_MM_AM));

                                mActivityHireNewJobBinding.textDate.setText(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM));
                                mActivityHireNewJobBinding.textTime.setText(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));

                                mActivityHireNewJobBinding.textDate.setVisibility(View.VISIBLE);
                                mActivityHireNewJobBinding.textTime.setVisibility(View.VISIBLE);
                            } else {
                                isDateSelected = false;
                                mActivityHireNewJobBinding.textDate.setVisibility(View.GONE);
                                mActivityHireNewJobBinding.textTime.setVisibility(View.GONE);
                                Utility.showSnackBar(getString(R.string.validate_future_date), mActivityHireNewJobBinding.getRoot());
                            }
                        }
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        timePickerDialog.show();
    }


    /**
     * Loads address in choose address dialog box in recycler view
     */
    private String TEMP_ADDRESS_ID = "";

    private boolean fillAddressRecyclerView(RecyclerView recyclerView) {
        ArrayList<AddressModel> addressList = PreferenceUtility.getInstance(mContext).getUserDetails().addressList;
        //Setting RecyclerView Adapter
        addressRecyclerViewAdapter = new AddressRecyclerViewAdapter(addressList, new AddressRecyclerViewAdapter.AddressItemInteractionListener() {
            @Override
            public void onEditClicked(AddressModel model, int position) {
                TEMP_ADDRESS_ID = model.address_id;
                showAddAddressDialog(model);
            }

            @Override
            public void onDeleteClicked(AddressModel model, int position) {
                TEMP_ADDRESS_ID = model.address_id;
                callDeleteAddressWS(model.address_id);
            }

            @Override
            public void onRowClicked(AddressModel model, int position) {

            }
        });
        addressRecyclerViewAdapter.setSelectedAddressId(addressId);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(addressRecyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_16dp)));

        //Here we are checking if address is not there then open add address dialog immediatly
        if (addressList == null || (addressList != null && addressList.isEmpty())) {
            return true;
        }
        return false;
    }

    /**
     * Image selection Code
     *
     * @param requestFileChooserCode
     * @param requestPermissionCode
     */

    public void onClickOfChooseFile(int requestFileChooserCode, int requestPermissionCode) {
        Log.i(TAG, "onClickOfChooseFile: ");
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            }
        } else {
            //Go ahead with file choosing
            startIntentFileChooser(requestFileChooserCode);
            showPictureChooserDialog();
        }
    }

    private void showPictureChooserDialog() {
        Log.d(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.choose_image)
                .setItems(R.array.choose_image_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            dispatchTakePictureIntent(REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE, Utility.REQUEST_CODE_WRITE_EXTERNAL_STORAGE_ADD_PROFILE_CAMERA);
                        } else {
                            //Select Gallery
                            // In case Choose File from Gallery
                            choosePictureFromGallery(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY, Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY);
                        }
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();

    }

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

    public void startCameraCaptureChooser(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                mCurrentPhotoPath = photoFile.getAbsolutePath();
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

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        /*File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );*/

        File photoFile = new File(new File(getFilesDir(), "CheepImages"), imageFileName);
        mCurrentPhotoPath = photoFile.getAbsolutePath();
        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = photoFile.getAbsolutePath();
        return photoFile;
    }

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

    public void startIntentFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_COVER) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_COVER);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityHireNewJobBinding.getRoot());
            }
        } else if (requestCode == Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityHireNewJobBinding.getRoot());
            }
        } else if (requestCode == Utility.REQUEST_CODE_ADD_PROFILE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                startCameraCaptureChooser(Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied Camera");
                Utility.showSnackBar(getString(R.string.permission_denied_camera), mActivityHireNewJobBinding.getRoot());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.PLACE_PICKER_REQUEST) {

            isPlacePickerClicked = false;
            if (resultCode == RESULT_OK) {
                final Place place = PlacePicker.getPlace(mContext, data);
                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                if (edtAddress != null) {
                    edtAddress.setText(address);
                    edtAddress.setFocusable(true);
                    edtAddress.setFocusableInTouchMode(true);
                    edtAddress.setTag(place.getLatLng());
                }
            }
            hideProgressDialog();
        } else if (requestCode == Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: CurrentPath" + mCurrentPhotoPath);

            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mCurrentPhotoPath = Utility.getPath(this, contentUri);
            Utility.loadImageView(mContext, mActivityHireNewJobBinding.imgAttach, mCurrentPhotoPath, 0);
            mActivityHireNewJobBinding.imgAttach.setVisibility(View.VISIBLE);
            /*Intent intent = CropImage
                    .activity(contentUri)

                    //Set Aspect ration
                    .setAspectRatio(1, 1)

                    //Set Activity Title
                    .setActivityTitle(getString(R.string.label_crop_image))
                    .setActivityMenuIconColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //AutoZoom enabled
                    .setAutoZoomEnabled(true)

                    //Allow rotation
                    .setAllowRotation(true)

                    //Check output compression quality
                    .setOutputCompressQuality(100)

                    //Whether guidelines will be shown or not
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setGuidelinesColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //Border Color
                    .setBorderLineColor(ContextCompat.getColor(mContext, R.color.white))
                    .setBorderLineThickness(getResources().getDimension(R.dimen.scale_3dp))

                    //Border Corner Color
                    .setBorderCornerColor(ContextCompat.getColor(mContext, R.color.white))

//                    Shape
                    .setCropShape(CropImageView.CropShape.OVAL)

                    .getIntent(this);

            startActivityForResult(intent, Utility.REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE);*/
        } else if (requestCode == Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: " + data.getData().toString());

            mCurrentPhotoPath = Utility.getPath(mContext, data.getData());
            Utility.loadImageView(mContext, mActivityHireNewJobBinding.imgAttach, mCurrentPhotoPath, 0);
            mActivityHireNewJobBinding.imgAttach.setVisibility(View.VISIBLE);

           /* Intent intent = CropImage
                    .activity(data.getData())

                    //Set Aspect ration
                    .setAspectRatio(1, 1)

                    //Set Activity Title
                    .setActivityTitle(getString(R.string.label_crop_image))
                    .setActivityMenuIconColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //AutoZoom enabled
                    .setAutoZoomEnabled(true)

                    //Allow rotation
                    .setAllowRotation(true)

                    //Check output compression quality
                    .setOutputCompressQuality(100)

                    //Whether guidelines will be shown or not
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setGuidelinesColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //Border Color
                    .setBorderLineColor(ContextCompat.getColor(mContext, R.color.white))
                    .setBorderLineThickness(getResources().getDimension(R.dimen.scale_3dp))

                    //Border Corner Color
                    .setBorderCornerColor(ContextCompat.getColor(mContext, R.color.white))

//                    Shape
                    .setCropShape(CropImageView.CropShape.OVAL)

                    .getIntent(this);

            startActivityForResult(intent, REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE);*/


        } /*else if (requestCode == Utility.REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                String selectedImagePath = Utility.getPath(this, resultUri);
                Log.i(TAG, "onActivityResult: Path:" + selectedImagePath);
                //Load the image from Glide
                Utility.showCircularImageView(mContext, TAG, mActivitySignupBinding.imgProfile, selectedImagePath, R.drawable.icon_profile_img, true);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.i(TAG, "onActivityResult: Crop Error" + error.toString());
            }
        }*/
    }
/*

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.i(TAG, "onActivityResult: " + data.getData().toString());
        if (requestCode == Utility.PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            final Place place = PlacePicker.getPlace(mContext, data);
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            if (edtAddress != null) {
                edtAddress.setText(address);
                edtAddress.setFocusable(true);
                edtAddress.setFocusableInTouchMode(true);
                edtAddress.setTag(place.getLatLng());
            }
        } else if (requestCode == Utility.REQUEST_CODE_GET_FILE_ADD_PHOTO && resultCode == RESULT_OK) {
            selectedImagePath = Utility.getPath(mContext, data.getData());
            Utility.loadImageView(mContext, mActivityHireNewJobBinding.imgAttach, selectedImagePath, 0);
            mActivityHireNewJobBinding.imgAttach.setVisibility(View.VISIBLE);
        }
    }*/


    /**
     * Call Add to fav
     *
     * @param providerId
     * @param isAddToFav
     */
    private void callAddToFavWS(String providerId, boolean isAddToFav) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHireNewJobBinding.getRoot());
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
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
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

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());

        }
    };

    /**
     * Calling Get SP list web service from server
     */

    private void reloadSPListWS() {
        pageNo = 0;
        callSPListWS(mCategoryId, mCityId, mTaskId);
    }

    private String mCategoryId, mCityId, mTaskId;

    private void callSPListWS(String categoryId, String cityId, String taskId) {
        this.mCategoryId = categoryId;
        this.mCityId = cityId;
        this.mTaskId = taskId;

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHireNewJobBinding.getRoot());
            return;
        }

        //Show Progress
//        showProgressDialog();


        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        if (TextUtils.isEmpty(taskId)) {
            mParams.put(NetworkUtility.TAGS.CITY_ID, cityId);
            mParams.put(NetworkUtility.TAGS.CAT_ID, categoryId);
        } else {
            mParams.put(NetworkUtility.TAGS.TASK_ID, taskId);
        }

        //for pagination
        mParams.put(NetworkUtility.TAGS.PAGE_NUM, pageNo);

        if (!TextUtils.isEmpty(addressId)) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
        }

        /*//if address id is greater then 0 then it means we need to update the existing address so sending address_id as parameter also
        if (!"0".equalsIgnoreCase(addressId)) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, String.valueOf(addressId));
        }*/

        String url;
        if (isFilterApplied && mDialogFilterBinding != null) {
            url = NetworkUtility.WS.SP_LIST_FILTER;

            if (cheepest != mDialogFilterBinding.seekbarCheepest.getMax()) {
                mParams.put(NetworkUtility.TAGS.PRICE, cheepest);
            }

            if (distance != mDialogFilterBinding.seekbarDistance.getMax()) {
                mParams.put(NetworkUtility.TAGS.DISTANCE, distance);
            }

            if (isFav)
                mParams.put(NetworkUtility.TAGS.IS_FAVOURITE, isFav ? Utility.BOOLEAN.YES : Utility.BOOLEAN.NO);

            if (rating > 0)
                mParams.put(NetworkUtility.TAGS.RATINGS, rating);

            mParams.remove(NetworkUtility.TAGS.PAGE_NUM);

        } else if (TextUtils.isEmpty(taskId))
            url = NetworkUtility.WS.SP_LIST;
        else
            url = NetworkUtility.WS.SP_LIST_TASK_WISE;


        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                , mCallSPListWSErrorListener
                , mCallSPListResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            errorLoadingHelper.showLoading();
            reloadSPListWS();
        }
    };

    Response.Listener mCallSPListResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                mActivityHireNewJobBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);

                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        ArrayList<ProviderModel> list = Utility.getObjectListFromJsonString(jsonObject.getString(NetworkUtility.TAGS.DATA), ProviderModel[].class);

                        //Setting SP List recycler view adapter
                        if (spRecyclerViewAdapter == null) {
                            spRecyclerViewAdapter = new ProviderRecyclerViewAdapter(HireNewJobActivity.this, intentAction);
                        }

                        //For Pull to refresh
                        if (pageNo == 0)
                            spRecyclerViewAdapter.setList(list);
                        else {
                            //for load more and
                            spRecyclerViewAdapter.addToList(list);
                        }

                        if (spRecyclerViewAdapter.getmList() != null && spRecyclerViewAdapter.getmList().size() > 0) {
                            errorLoadingHelper.success();
                            pageNo++;
                        } else {
                            pageNo = 0;
//                            errorLoadingHelper.failed(getString(R.string.label_no_sp_found), 0, getString(R.string.label_refresh), onRetryBtnClickListener);

                            if (Utility.ACTION_NEW_JOB_CREATE.equalsIgnoreCase(intentAction)) {
                                errorLoadingHelper.failed(getString(R.string.label_no_sp_found), 0, null, null);
                            } else if (Utility.ACTION_HIRE_PROVIDER.equalsIgnoreCase(intentAction)) {
                                errorLoadingHelper.failed(getString(R.string.label_no_quotes_available), 0, null, null);
                            }
//                            errorLoadingHelper.failed(null, R.drawable.img_empty_reviews, getString(R.string.label_refresh), onRetryBtnClickListener);
                        }
                        spRecyclerViewAdapter.onLoadMoreComplete();

                        if (list.size() == 0) {
                            spRecyclerViewAdapter.disableLoadMore();
                            mActivityHireNewJobBinding.textResponseCounter.setText(getString(R.string.label_response, "0"));
                        } else {
                            mActivityHireNewJobBinding.textResponseCounter.setText(getString(R.string.label_response, "" + list.size()));
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
                        errorLoadingHelper.failed(error_message, 0, onRetryBtnClickListener);
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
                mCallSPListWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallSPListWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            mActivityHireNewJobBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
            // Close Progressbar
//            hideProgressDialog();


            errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);


            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
        }
    };

    /**
     * Calling Update Address WS
     *
     * @param addressType
     * @param address
     */
    private void callUpdateAddressWS(String addressId, String addressType, /*String addressName,*/ String address, LatLng latLng) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHireNewJobBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CATEGORY, addressType);
//        mParams.put(NetworkUtility.TAGS.NAME, addressName);
        mParams.put(NetworkUtility.TAGS.ADDRESS, address);

        if (latLng != null) {
            mParams.put(NetworkUtility.TAGS.LAT, latLng.latitude + "");
            mParams.put(NetworkUtility.TAGS.LNG, latLng.longitude + "");
        }


        //if address id is greater then 0 then it means we need to update the existing address so sending address_id as parameter also
        if (!"0".equalsIgnoreCase(addressId)) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, String.valueOf(addressId));
        }

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest((!"0".equalsIgnoreCase(addressId) ? NetworkUtility.WS.EDIT_ADDRESS : NetworkUtility.WS.ADD_ADDRESS)
                , mCallUpdateAddressWSErrorListener
                , mCallUpdateAddressResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }


    Response.Listener mCallUpdateAddressResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                if (addAddressDialog != null) {
                    addAddressDialog.dismiss();
                }
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        AddressModel addressModel = (AddressModel) getObjectFromJsonString(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).toString(), AddressModel.class);

                        if (!TextUtils.isEmpty(TEMP_ADDRESS_ID)) {
                            if (addressRecyclerViewAdapter != null) {
                                addressRecyclerViewAdapter.updateItem(addressModel);
                            }
                        }

                        //Saving information in sharedpreference
                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.addressList = addressRecyclerViewAdapter.getmList();
                        PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);


//                        String message = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.OTP_CODE);
//                        VerificationActivity.newInstance(mContext, PreferenceUtility.getInstance(mContext).getUserDetails(), TEMP_PHONE_NUMBER, message);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
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
                mCallUpdateAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallUpdateAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
        }
    };


    /**
     * Calling Add Address WS
     *
     * @param addressType
//     * @param addressName
     * @param address
     */
    private void callAddAddressWS(String addressType, /*String addressName,*/ String address, LatLng latLng) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHireNewJobBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CATEGORY, addressType);
//        mParams.put(NetworkUtility.TAGS.NAME, addressName);
        mParams.put(NetworkUtility.TAGS.ADDRESS, address);

        if (latLng != null) {
            mParams.put(NetworkUtility.TAGS.LAT, latLng.latitude + "");
            mParams.put(NetworkUtility.TAGS.LNG, latLng.longitude + "");
        }
        Utility.hideKeyboard(mContext);
        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ADD_ADDRESS
                , mCallAddAddressWSErrorListener
                , mCallAddAddressResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }


    Response.Listener mCallAddAddressResponseListener = new Response.Listener() {
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

                        AddressModel addressModel = (AddressModel) getObjectFromJsonString(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).toString(), AddressModel.class);

                        if (addressRecyclerViewAdapter != null) {
                            addressRecyclerViewAdapter.add(addressModel);
                        }

                        //Saving information in sharedpreference
                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.addressList = addressRecyclerViewAdapter.getmList();
                        PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);

                        if (addAddressDialog != null) {
                            addAddressDialog.dismiss();
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
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
                mCallAddAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallAddAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
        }
    };

    /**
     * Calling delete address Web service
     *
     * @param addressId
     */
    private void callDeleteAddressWS(String addressId) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHireNewJobBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, String.valueOf(addressId));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.DELETE_ADDRESS
                , mCallDeleteAddressWSErrorListener
                , mCallDeleteAddressResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    Response.Listener mCallDeleteAddressResponseListener = new Response.Listener() {
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
                        if (addressRecyclerViewAdapter != null) {
                            addressRecyclerViewAdapter.delete(TEMP_ADDRESS_ID);

                            //Saving information in sharedpreference
                            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                            userDetails.addressList = addressRecyclerViewAdapter.getmList();
                            PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
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
                mCallDeleteAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallDeleteAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
        }
    };

    /**
     * Call Create Task webservice
     */
    private void createTaskOnServer() {
        //   Utility.hideKeyboard(mContext, mActivityHireNewJobBinding.editTaskDesc);
        //Validation
        if (TextUtils.isEmpty(mActivityHireNewJobBinding.editTaskDesc.getText().toString().trim())) {
            Utility.showSnackBar(getString(R.string.validate_task_desc), mActivityHireNewJobBinding.getRoot());
//            Utility.showToast(mContext, getString(R.string.validate_task_desc));
            return;
        } else if (isDateSelected == false) {
            Utility.showSnackBar(getString(R.string.validate_date), mActivityHireNewJobBinding.getRoot());
//            Utility.showToast(mContext, getString(R.string.validate_date));
            return;
        } else if (TextUtils.isEmpty(addressId)) {
            Utility.showSnackBar(getString(R.string.validate_address_new_task), mActivityHireNewJobBinding.getRoot());
//            Utility.showToast(mContext, getString(R.string.validate_address_new_task));
            return;
        }

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHireNewJobBinding.getRoot());
//            Utility.showToast(mContext, getString(R.string.no_internet));
            return;
        }

        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeInMillis(startDateTimeSuperCalendar.getTimeInMillis());
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

        // Get date-time for next 3 hours
        SuperCalendar calAfter3Hours = SuperCalendar.getInstance().getNext3HoursTime();

        if (superCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {
            Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours), mActivityHireNewJobBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        // Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        // Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_DESC, mActivityHireNewJobBinding.editTaskDesc.getText().toString().trim());
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, categoryId);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, String.valueOf(superCalendar.getTimeInMillis()));

        // Add Params
        HashMap<String, File> mFileParams = new HashMap<String, File>();
        if (!TextUtils.isEmpty(mCurrentPhotoPath) && new File(mCurrentPhotoPath).exists())
            mFileParams.put(NetworkUtility.TAGS.TASK_IMAGE, new File(mCurrentPhotoPath));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CREATE_TASK
                , mCallCreateTaskWSErrorListener
                , mCallCreateTaskWSResponseListener
                , mHeaderParams
                , mParams
                , mFileParams);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

    }

    Response.Listener mCallCreateTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        /**
                         * Below was older approach when app needs to update the same task page.
                         */
//                        TaskDetailModel taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);
//                        getIntent().putExtra(Utility.Extra.DATA, jsonObject.optString(NetworkUtility.TAGS.DATA));
//                        getIntent().putExtra(Utility.Extra.IS_FIRST_TIME, true);
//                        getIntent().setAction(Utility.ACTION_HIRE_PROVIDER);
//                        initiateUI();
//                        setListeners();


                        /**
                         * Now according to the new flow, once task created
                         * app will be redirected to MyTask Detail screen.
                         */
                        TaskDetailModel taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);
                        if (taskDetailModel != null) {
                            /*
                            * Add new task detail on firebase
                            * @Sanjay 20 Feb 2016
                            * */
                            ChatTaskModel chatTaskModel = new ChatTaskModel();
                            chatTaskModel.taskId = FirebaseUtils.getPrefixTaskId(taskDetailModel.taskId);
                            chatTaskModel.taskDesc = taskDetailModel.taskDesc;
                            chatTaskModel.categoryId = taskDetailModel.categoryId;
                            chatTaskModel.categoryName = taskDetailModel.categoryName;
                            chatTaskModel.selectedSPId = "";
                            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                            chatTaskModel.userId = FirebaseUtils.getPrefixUserId(userDetails.UserID);
                            FirebaseHelper.getTaskRef(chatTaskModel.taskId).setValue(chatTaskModel);
                        }
                        // Finish the current activity
                        finish();
                        //Sending Broadcast to the HomeScreen Screen.
                        Intent intent = new Intent(Utility.BR_ON_TASK_CREATED);
                        sendBroadcast(intent);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
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
                mCallCreateTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallCreateTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
        }
    };


    /**
     * In case Task is Getting Detail Request
     */
    BottomAlertDialog badForRequestTaskDetail;

    private void showDetailRequestDialog(final ProviderModel providerModel) {
        badForRequestTaskDetail = new BottomAlertDialog(mContext);
        badForRequestTaskDetail.setTitle(getString(R.string.label_action));
        badForRequestTaskDetail.setMessage(getString(R.string.desc_detail_action_request, providerModel.userName));
        badForRequestTaskDetail.addPositiveButton(getString(R.string.label_accept), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick() called with: view = [" + view + "]");
//                callReportSPWS(!reportAbuse);
//                dialog.dismiss();
//                providerModel.request_detail_status = Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED;
//                spRecyclerViewAdapter.updateModelForRequestDetailStatus(providerModel);

                callTaskDetailRequestAcceptRejectWS(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED, taskDetailModel.taskId, providerModel.providerId);

                badForRequestTaskDetail.dismiss();
            }
        });
        badForRequestTaskDetail.addNegativeButton(getString(R.string.label_decline), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                badForRequestTaskDetail.dismiss();

                callTaskDetailRequestAcceptRejectWS(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.REJECTED, taskDetailModel.taskId, providerModel.providerId);
            }
        });
        badForRequestTaskDetail.showDialog();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////Accept-Reject Detail Service[Start] ///////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Calling delete address Web service
     */
    private void callTaskDetailRequestAcceptRejectWS(String requestDetailStatus, String taskID, String spUserID) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHireNewJobBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.REQUEST_DETAIL_STATUS, requestDetailStatus);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskID);
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, spUserID);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ACTION_ON_DETAIL
                , mCallActionOnDetailWSErrorListener
                , mCallActionOnDetailWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    Response.Listener mCallActionOnDetailWSResponseListener = new Response.Listener() {
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
//                        providerModel.request_detail_status = Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED;
                        JSONObject jObjData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
//                        String task_id = jsonObject.getString(NetworkUtility.TAGS.TASK_ID);
                        String spUserID = jObjData.getString(NetworkUtility.TAGS.SP_USER_ID);
                        String requestDatailStatus = jObjData.getString(NetworkUtility.TAGS.REQUEST_DETAIL_STATUS);
                        if (requestDatailStatus.equalsIgnoreCase(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED)) {
                            spRecyclerViewAdapter.updateModelForRequestDetailStatus(spUserID, requestDatailStatus);
                        } else {
                            // Need to pass this details to Pending listing as well.
                            MessageEvent messageEvent = new MessageEvent();
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.DETAIL_REQUEST_REJECTED;
                            messageEvent.id = taskId;
                            EventBus.getDefault().post(messageEvent);

                            // Update recycler view
                            spRecyclerViewAdapter.removeModelForRequestDetailStatus(spUserID, requestDatailStatus);

                            // Check if listing is empty now, display message
                            if (spRecyclerViewAdapter.getmList().size() == 0) {
                                errorLoadingHelper.failed(getString(R.string.label_no_quotes_available), 0, null, null);
                            }
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
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
                mCallActionOnDetailWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallActionOnDetailWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Accept-Reject Detail Service[End] //////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Calling accept request Web service
     */
    private void callTaskDetailRequestAcceptWS(final String action, String taskID, final ProviderModel providerModel) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHireNewJobBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.REQUEST_DETAIL_STATUS, Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskID);
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ACTION_ON_DETAIL
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

                // Close Progressbar
                hideProgressDialog();

                // Show Toast
                Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
            }
        }
                , new Response.Listener() {
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
                            if (action.equalsIgnoreCase(Utility.ACTION_CHAT)) {
                                TaskChatModel taskChatModel = new TaskChatModel();
                                taskChatModel.categoryName = taskDetailModel.categoryName;
                                taskChatModel.taskDesc = taskDetailModel.taskDesc;
                                taskChatModel.taskId = taskDetailModel.taskId;
                                taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(providerModel.providerId);
                                taskChatModel.participantName = providerModel.userName;
                                taskChatModel.participantPhotoUrl = providerModel.profileUrl;
                                ChatActivity.newInstance(HireNewJobActivity.this, taskChatModel);
                            } else if (action.equalsIgnoreCase(Utility.ACTION_CALL)) {
//                                callToOtherUser(mActivityHireNewJobBinding.getRoot(), providerModel.providerId);
                                Utility.openCustomerCareCallDialer(mContext, providerModel.sp_phone_number);
                            }
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            // Show Toast
                            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                            // Show message
                            Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
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
                    mCallActionOnDetailWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
                }
                hideProgressDialog();
            }
        }
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    /**
     * Call Task Detail web service
     */
    private void callTaskDetailWS(String mTaskId, String providerId) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityHireNewJobBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerId);
        mParams.put(NetworkUtility.TAGS.TASK_ID, mTaskId);

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.TASK_DETAIL
                , mCallTaskDetailWSErrorListener
                , mCallTaskDetailWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList, Utility.getUniqueTagForNetwork(this, NetworkUtility.WS.TASK_DETAIL));
    }

    Response.Listener mCallTaskDetailWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;

                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);
                        /**
                         * NOW REFRESHES the listings
                         */
                        populateFieldsForHireProvider();

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
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
                mCallTaskDetailWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallTaskDetailWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());

        }
    };
}
