package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.adapter.TaskQuotesRecyclerViewAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.custom_view.GridImageView;
import com.cheep.databinding.DialogFilterBinding;
import com.cheep.model.ProviderModel;
import com.cheep.model.QuoteListResponse;
import com.cheep.model.TaskDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.rating;

public class TaskQuotesActivity extends BaseAppCompatActivity implements TaskQuotesRecyclerViewAdapter.OnInteractionListener {

    public static void newInstance(Context context, TaskDetailModel model, boolean isFirstTimeCreate) {
        Intent intent = new Intent(context, TaskQuotesActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(model));
        intent.putExtra(Utility.Extra.IS_FIRST_TIME, isFirstTimeCreate);
        intent.setAction(Utility.ACTION_HIRE_PROVIDER);
        context.startActivity(intent);
    }

    private Toolbar mToolbar;
    private TextView tvTitle;
    private TextView tvTaskTitle;
    private TextView tvTaskDescription;
    private TextView tvTaskBookingTime;
    private TextView tvTaskStartsIn;

    private GridImageView mGridImageView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TaskQuotesRecyclerViewAdapter mAdapter;
    private ErrorLoadingHelper mErrorLoadingHelper;


    private BottomAlertDialog mFilterDialog;
    private DialogFilterBinding mDialogFilterBinding;

    private List<ProviderModel> mQuotesList;
    //private String mIntentAction;
    private TaskDetailModel mTaskDetailModel;
    private Gson mGson;
    private Filter mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_quotes);

        initiateUI();

        callSPListWS();
    }

    @Override
    protected void initiateUI() {
        mGson = new Gson();
        mQuotesList = new ArrayList<>();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.text_title);
        tvTaskTitle = (TextView) findViewById(R.id.tvTaskTitle);
        tvTaskDescription = (TextView) findViewById(R.id.tvTaskDescription);
        tvTaskBookingTime = (TextView) findViewById(R.id.tvTaskBookingTime);
        tvTaskStartsIn = (TextView) findViewById(R.id.tvTaskStartsIn);
        mGridImageView = (GridImageView) findViewById(R.id.gridImageView);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mErrorLoadingHelper = new ErrorLoadingHelper(mRecyclerView);

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TaskQuotesRecyclerViewAdapter(this, mQuotesList, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal));
        setupActionbar();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callSPListWS();
            }
        });
        Utility.setSwipeRefreshLayoutColors(mSwipeRefreshLayout);

        //mIntentAction = getIntent().getAction();
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            //mIsFirstTime = bundle.getBoolean(Utility.Extra.IS_FIRST_TIME, false);
            mTaskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);

            populateData();
        }
    }

    @Override
    protected void setListeners() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_quotes_filter, menu);
        return true;
    }

    private void setupActionbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }

    private void callSPListWS() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), null);
            return;
        }

        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setEnabled(false);
        } else {
            mErrorLoadingHelper.showLoading();
        }


        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, mTaskDetailModel.taskId);

        String url;
        if (mFilter != null) {
            url = NetworkUtility.WS.SP_LIST_FILTER;
            if (mFilter.cheapest != mDialogFilterBinding.seekbarCheepest.getMax()) {
                mParams.put(NetworkUtility.TAGS.PRICE, mFilter.cheapest);
            }

            if (mFilter.distance != mDialogFilterBinding.seekbarDistance.getMax()) {
                mParams.put(NetworkUtility.TAGS.DISTANCE, mFilter.distance);
            }

            mParams.put(NetworkUtility.TAGS.IS_FAVOURITE, mFilter.isFav ? Utility.BOOLEAN.YES : Utility.BOOLEAN.NO);

            if (rating > 0) {
                mParams.put(NetworkUtility.TAGS.RATINGS, mFilter.rating);
            }
        } else {
            url = NetworkUtility.WS.SP_LIST_TASK_WISE;
        }


        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                , mSPListErrorResponseListener
                , mSPListResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    private void populateData() {
        if (mTaskDetailModel != null) {
            tvTitle.setText(checkNonNullAndSet(mTaskDetailModel.categoryName));
            tvTaskTitle.setText(checkNonNullAndSet(mTaskDetailModel.subCategoryName));
            tvTaskDescription.setText(checkNonNullAndSet(mTaskDetailModel.taskDesc));

            SuperCalendar superCalendar = SuperCalendar.getInstance();
            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            superCalendar.setTimeInMillis(Long.parseLong(mTaskDetailModel.taskStartdate));
            superCalendar.setLocaleTimeZone();

            tvTaskBookingTime.setText(getString(R.string.label_task_booking_time, superCalendar.format(Utility.DATE_FORMAT_DD_MMM), superCalendar.format(Utility.DATE_FORMAT_HH_MM_AM)));
            tvTaskStartsIn.setText(getString(R.string.label_task_starts_in, "XX"));
        }
    }

    private void populateGridImageView() {
        mGridImageView.clear();
        List<Uri> uriList = new ArrayList<>();
        for (ProviderModel model : mQuotesList) {
            if (model.profileUrl != null) {
                uriList.add(Uri.parse(model.profileUrl));
            } else {
                uriList.add(Uri.parse("www.dummycheepurl.k"));
            }
        }
        mGridImageView.createWithUrls(uriList);
    }

    private void showFilterDialog() {

        if (mFilterDialog == null) {
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
            mFilterDialog = new BottomAlertDialog(mContext);
            mFilterDialog.setExpandedInitially(true);
            mDialogFilterBinding.btnShowResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Saving this field when user fix filter fields
                    mFilter = new Filter();
                    mFilter.cheapest = mDialogFilterBinding.seekbarCheepest.getDisplayProgress();
                    mFilter.distance = mDialogFilterBinding.seekbarDistance.getDisplayProgress();
                    mFilter.rating = mDialogFilterBinding.ratingBar.getProgress();
                    mFilter.isFav = mDialogFilterBinding.imgFav.isSelected();

                    mFilterDialog.dismiss();

                    callSPListWS();
                }
            });

            mDialogFilterBinding.btnReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetFilterFields(mDialogFilterBinding);
                    mFilterDialog.dismiss();

                    mErrorLoadingHelper.showLoading();
                    callSPListWS();
                }
            });
            mFilterDialog.setTitle(getString(R.string.label_filter));
            mFilterDialog.setCustomView(mDialogFilterBinding.getRoot());

            //reseting all the fields for first time
            resetFilterFields(mDialogFilterBinding);
        }

        //Prefilling all fields when user filter is already applied
        if (mFilter != null) {
            if (mFilter.cheapest > 0) {
                mDialogFilterBinding.seekbarCheepest.setDisplayProgress(mFilter.cheapest);
            }
            if (mFilter.distance > 0) {
                mDialogFilterBinding.seekbarDistance.setDisplayProgress(mFilter.distance);
            }
            mDialogFilterBinding.imgFav.setSelected(mFilter.isFav);
            mDialogFilterBinding.ratingBar.setRating(mFilter.rating);
        }

        mFilterDialog.showDialog();
    }

    private void resetFilterFields(DialogFilterBinding mDialogFilterBinding) {
        mDialogFilterBinding.seekbarDistance.setProgress(getMaxDistance());
        mDialogFilterBinding.seekbarCheepest.setProgress(getMaxQuote());
        mDialogFilterBinding.imgFav.setSelected(false);
        mDialogFilterBinding.ratingBar.setRating(0);

        mFilter = null;
    }

    private int getMaxQuote() {
        try {
            if (!TextUtils.isEmpty(mTaskDetailModel.maxQuotePrice) && !mTaskDetailModel.maxQuotePrice.equalsIgnoreCase("0")) {
                return (int) Double.parseDouble(mTaskDetailModel.maxQuotePrice) + 1;
            }
        } catch (Exception e) {
        }
        return 20000;
    }

    private int getMaxDistance() {
        return 15;
    }

    private String checkNonNullAndSet(String text) {
        return text != null ? text.trim() : "";
    }

    private Response.Listener mSPListResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object rawResponse) {
            QuoteListResponse response = mGson.fromJson((String) rawResponse, QuoteListResponse.class);
            switch (response.statusCode) {
                case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                    //clear the data as there is no pagination login
                    mQuotesList.clear();

                    if (response.quoteList != null && response.quoteList.size() > 0) {
                        //repopulate the data
                        mQuotesList.addAll(response.quoteList);
                        mAdapter.notifyDataSetChanged();

                        mErrorLoadingHelper.success();
                    } else {
                        mErrorLoadingHelper.failed(getString(R.string.label_no_quotes_available), 0, null, null);
                    }

                    populateGridImageView();
                    break;
                case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                    mErrorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                    break;
                case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                    mErrorLoadingHelper.failed(response.message, 0, onRetryBtnClickListener);
                    break;
                case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                    //Logout and finish the current activity
                    Utility.logout(mContext, true, response.statusCode);
                    finish();
                    break;
            }
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };

    private Response.ErrorListener mSPListErrorResponseListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setRefreshing(false);
            mErrorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
        }
    };

    @Override
    public void onBookClick(ProviderModel provider) {
        PaymentsStepActivity.newInstance(mContext, mTaskDetailModel, provider, 0);
    }

    @Override
    public void onItemClick(ProviderModel provider) {
        ProviderProfileActivity.newInstance(mContext, provider, mTaskDetailModel);
    }


    private View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            callSPListWS();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_filter:
                showFilterDialog();
                return true;
            default:
                return false;
        }
    }

    public static class Filter {
        private int cheapest;
        private int distance;
        private int rating;
        private boolean isFav;
    }
}
