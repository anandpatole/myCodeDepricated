package com.cheep.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.SearchActivity;
import com.cheep.activity.SelectLocationActivity;
import com.cheep.adapter.HomeTabRecyclerViewAdapter;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.FragmentTabHomeBinding;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.interfaces.NotificationClickInteractionListener;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SharedElementTransitionHelper;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.R.id.text_search;

/**
 * Created by pankaj on 9/27/16.
 */

public class HomeTabFragment extends BaseFragment {
    public static final String TAG = "HomeTabFragment";

    FragmentTabHomeBinding mFragmentTabHomeBinding;

    private DrawerLayoutInteractionListener mListener;
    private NotificationClickInteractionListener mNotificationClickInteractionListener;
    private CategoryRowInteractionListener mCategoryRowInteractionListener;
    private HomeTabRecyclerViewAdapter adapter;
    private String tempCityName; // this variables value comes from SelectLocationActivity and onActivityResult of this class

    private ErrorLoadingHelper errorLoadingHelper;
    private boolean mAlreadyLoaded = false;

    public static HomeTabFragment newInstance(DrawerLayoutInteractionListener mListener) {
        Bundle args = new Bundle();
        HomeTabFragment fragment = new HomeTabFragment();
        fragment.setArguments(args);
        fragment.setmListener(mListener);
        return fragment;
    }

//    private static HomeTabFragment fragment;
//
//    public static HomeTabFragment singleInstance(DrawerLayoutInteractionListener mListener) {
//        Bundle args = new Bundle();
//        if (fragment == null) {
//            fragment = new HomeTabFragment();
//            fragment.setArguments(args);
//            fragment.setmListener(mListener);
//        }
//        return fragment;
//    }

    public void setmListener(DrawerLayoutInteractionListener mListener) {
        this.mListener = mListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mFragmentTabHomeBinding == null) {
            mFragmentTabHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_home, container, false);
        }
        return mFragmentTabHomeBinding.getRoot();
    }

    public void profileUpdate() {
        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Update the name
        if (userDetails != null && TextUtils.isEmpty(userDetails.UserName) == false && userDetails.UserName.trim().length() > 1) {
            String name = userDetails.UserName.substring(0, 1).toUpperCase() + userDetails.UserName.substring(1);
            //Used to get only first word, e.g "Pankaj" from "pankaj sharma"
            mFragmentTabHomeBinding.textSearch.setText(getString(R.string.hint_search_placeholder, name));
//            mFragmentTabHomeBinding.textSearch.setText(getString(R.string.hint_search_placeholder, name.split(" ")[0]));
        } else {
            mFragmentTabHomeBinding.textSearch.setText(getString(R.string.hint_search_placeholder, userDetails.UserName));
        }
        updateCounter();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCounter();
    }

    private void updateCounter() {
        if (mFragmentTabHomeBinding != null) {
            //Updating counter
            int notificationCounter = PreferenceUtility.getInstance(mContext).getUnreadNotificationCounter();
            if (notificationCounter > 0) {
                mFragmentTabHomeBinding.tvBadgeCount.setText(String.valueOf(notificationCounter));
                mFragmentTabHomeBinding.tvBadgeCount.setVisibility(View.VISIBLE);
            } else {
                mFragmentTabHomeBinding.tvBadgeCount.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null && !mAlreadyLoaded) {
            mAlreadyLoaded = true;
            // Do this code only first time, not after rotation or reuse fragment from backstack
            initiateUI();
            setListener();
        } else {
            profileUpdate();
        }
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);
        if (context instanceof CategoryRowInteractionListener) {
            mCategoryRowInteractionListener = (CategoryRowInteractionListener) context;
        }
        if (context instanceof NotificationClickInteractionListener) {
            mNotificationClickInteractionListener = (NotificationClickInteractionListener) context;
        }
        if (context instanceof DrawerLayoutInteractionListener) {
            mListener = (DrawerLayoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");

        if (EventBus.getDefault().isRegistered(this) == true)
            EventBus.getDefault().unregister(this);

        mListener = null;
        mCategoryRowInteractionListener = null;
        mNotificationClickInteractionListener = null;

        /*
          Cancel the request as it no longer available
         */
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.CATEGORY_LIST);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.UPDATE_LOCATION);

        if (mFragmentTabHomeBinding != null && mFragmentTabHomeBinding.commonRecyclerView != null && mFragmentTabHomeBinding.commonRecyclerView.swipeRefreshLayout != null)
            mFragmentTabHomeBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);

        super.onDetach();
    }

    @Override
    void initiateUI() {
        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            //Setting up toolbar
            ((AppCompatActivity) mContext).setSupportActionBar(mFragmentTabHomeBinding.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        }

        errorLoadingHelper = new ErrorLoadingHelper(mFragmentTabHomeBinding.commonRecyclerView.recyclerView);

        if (mFragmentTabHomeBinding.toolbar != null) {
            mFragmentTabHomeBinding.toolbar.setTitle(Utility.EMPTY_STRING);
        }

        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mFragmentTabHomeBinding.toolbar);

        //Seting Location Name
        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Checking if there is city then call the category list else fetch and send current location to server
        if (isPreviousLocationPresent()) {
            toggleErrorScreen(false);
            mFragmentTabHomeBinding.textLocation.setText(userDetails.getLocality());
            errorLoadingHelper.showLoading();
            getCategoryListFromServer("", "");
        } else {

//            TODO: This needs to be changed afetr
           /* toggleErrorScreen(true);
            mFragmentTabHomeBinding.textLocation.setText(getString(R.string.hint_select_location));
            //starting to choose location
            Intent intent = new Intent(mContext, SelectLocationActivity.class);
            startActivityForResult(intent, Utility.REQUEST_CODE_CHANGE_LOCATION);
            ((AppCompatActivity) mContext).overridePendingTransition(0, 0);*/
            updateLatLongOnServer("19.1363246", "72.82766");
        }

        mFragmentTabHomeBinding.textSearch.setVisibility(View.GONE);
        //Setting search placeholder
        profileUpdate();

        initSwipeToRefreshLayout();

        if (EventBus.getDefault().isRegistered(this) == false)
            EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.NEW_NOTIFICATION) {
            updateCounter();
        }
    }

    private void initSwipeToRefreshLayout() {
        mFragmentTabHomeBinding.commonRecyclerView.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadCategoryListFromServer();
            }
        });
        Utility.setSwipeRefreshLayoutColors(mFragmentTabHomeBinding.commonRecyclerView.swipeRefreshLayout);
    }

    @Override
    void setListener() {
        mFragmentTabHomeBinding.textSearch.setOnClickListener(onClickListener);
        mFragmentTabHomeBinding.textLocation.setOnClickListener(onClickListener);
        mFragmentTabHomeBinding.relNotificationAction.setOnClickListener(onClickListener);
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedElementTransitionHelper sharedElementTransitionHelper;
            switch (view.getId()) {

                case text_search:

                    if (adapter != null) {
                        sharedElementTransitionHelper = new SharedElementTransitionHelper(getActivity());
                        sharedElementTransitionHelper.put(mFragmentTabHomeBinding.textSearch, R.string.transition_text_search);
                        SearchActivity.newInstance(mContext, sharedElementTransitionHelper.getBundle(), mFragmentTabHomeBinding.textLocation.getText().toString().trim(), adapter.getmList());
                    }

                    break;
                case R.id.text_location:

                    Intent intent = new Intent(mContext, SelectLocationActivity.class);
                    startActivityForResult(intent, Utility.REQUEST_CODE_CHANGE_LOCATION);
                    ((AppCompatActivity) mContext).overridePendingTransition(0, 0);

                    break;
                case R.id.rel_notification_action:
                    if (mNotificationClickInteractionListener != null) {
                        mNotificationClickInteractionListener.onNotificationIconClicked();
                    }
                    break;
            }
        }
    };

    public interface CategoryRowInteractionListener {
        void onCategoryRowClicked(JobCategoryModel model, int position);
    }

    @Override
    public void onLocationNotAvailable() {
        super.onLocationNotAvailable();
        Log.d(TAG, "onLocationNotAvailable() called");
        if (!isPreviousLocationPresent()) {
            // Show Error Screen
            toggleErrorScreen(true);
        }
    }

    @Override
    public void onLocationFetched(Location mLocation) {
        super.onLocationFetched(mLocation);
        Log.d(TAG, "onLocationFetched() called with: mLocation = [" + mLocation + "]");
        if (!isPreviousLocationPresent()) {
            errorLoadingHelper.showLoading();
            getCategoryListFromServer(String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()));
//            getCategoryListFromServer("", "");
        }
    }

    private void toggleErrorScreen(boolean showErrorScreen) {
        if (showErrorScreen) {
            //Show Error Screen
            mFragmentTabHomeBinding.textSearch.setVisibility(View.GONE);
//            errorLoadingHelper.failed(getString(R.string.label_no_category_found_in_location), R.drawable.img_empty_category, null);
            errorLoadingHelper.failed(null, R.drawable.img_empty_category, null);
            mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setVisibility(View.GONE);
        } else {
            //Hide Error Screen
            mFragmentTabHomeBinding.textSearch.setVisibility(View.VISIBLE);
            errorLoadingHelper.success();
            mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private boolean isPreviousLocationPresent() {
        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        return userDetails != null && !"-1".equalsIgnoreCase(userDetails.CityID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.REQUEST_CODE_CHANGE_LOCATION && resultCode == AppCompatActivity.RESULT_OK) {

            if (data != null) {
                String latitude = data.getStringExtra(Utility.Extra.LATITUDE);
                String longitude = data.getStringExtra(Utility.Extra.LONGITUDE);
                tempCityName = data.getStringExtra(Utility.Extra.CITY_NAME);
                updateLatLongOnServer(latitude, longitude);

            }

        }
    }

    //====================================================
    //============== WEB INTEGRATION =====================
    //====================================================


    /**
     * Update Category list on server
     *
     * @param lat
     * @param lng
     */
    private void updateLatLongOnServer(String lat, String lng) {
        this.lat = lat;
        this.lng = lng;
        //Setting RecyclerView Adapter
        /*HomeTabRecyclerViewAdapter adapter = new HomeTabRecyclerViewAdapter(BootstrapConstant.DUMMY_JOB_CATEGORY_MODELS_LIST, mCategoryRowInteractionListener);
        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setAdapter(adapter);
        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_white, (int) getResources().getDimension(R.dimen.scale_0dp)));*/

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.LAT, lat);
        mParams.put(NetworkUtility.TAGS.LNG, lng);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.UPDATE_LOCATION
                , mCallUpdateLatLngWSErrorListener
                , mCallUpdateLatLngWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest,NetworkUtility.WS.UPDATE_LOCATION);
    }

    Response.Listener mCallUpdateLatLngWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        Utility.showToast(mContext, "Location updated");
                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);

                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.CityID = jsonData.optString(NetworkUtility.TAGS.CITY_ID);
                        userDetails.CityName = jsonData.optString(NetworkUtility.TAGS.CITY_NAME);
                        userDetails.locality = jsonData.optString(NetworkUtility.TAGS.LOCALITY);
                        PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);
                        mFragmentTabHomeBinding.textLocation.setText(userDetails.getLocality());

                        getCategoryListFromServer(lat, lng);
                        // Show message
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentTabHomeBinding.getRoot());
                        mFragmentTabHomeBinding.textLocation.setText(tempCityName);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mFragmentTabHomeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        ;
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallUpdateLatLngWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallUpdateLatLngWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentTabHomeBinding.getRoot());
        }
    };

    /**
     * Get Category list from server
     */

    private void reloadCategoryListFromServer() {
        /*if (mVolleyNetworkRequestForCategoryList != null) {
            Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList);
        } else {
            mFragmentTabHomeBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
        }*/
        getCategoryListFromServer(lat, lng);
    }

    String lat, lng;

    private void getCategoryListFromServer(final String lat, final String lng) {

        //If user is logged out already,return from here only.
        /*
          Fixed crash issue.
          @changes by @bhavesh on 25th Feb2017
         */
        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            return;
        }

        this.lat = lat;
        this.lng = lng;

        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mFragmentTabHomeBinding.getRoot());
            errorLoadingHelper.failed(getString(R.string.no_internet), 0, onRetryBtnClickListener);
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        if (!TextUtils.isEmpty(lat)) {
            mParams.put(NetworkUtility.TAGS.LAT, lat);
            mParams.put(NetworkUtility.TAGS.LNG, lng);
        }

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.CATEGORY_LIST
                , mCallCategoryListWSErrorListener
                , mCallCategoryListWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList,NetworkUtility.WS.CATEGORY_LIST);
    }

    Response.Listener mCallCategoryListWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                mFragmentTabHomeBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        ArrayList<JobCategoryModel> list;
                        list = Utility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), JobCategoryModel[].class);

                        //Setting RecyclerView Adapter
                        adapter = new HomeTabRecyclerViewAdapter(list, mCategoryRowInteractionListener);
                        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setAdapter(adapter);
                        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_white, (int) getResources().getDimension(R.dimen.scale_0dp)));
                        toggleErrorScreen(false);

                        if (list != null && list.size() <= 0) {
                            errorLoadingHelper.failed(null, R.drawable.img_empty_category, onRetryBtnClickListener);
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentTabHomeBinding.getRoot());
                        errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        toggleErrorScreen(true);
                        // Show message
//                        Utility.showSnackBar(error_message, mFragmentTabHomeBinding.getRoot());
                        errorLoadingHelper.failed(error_message, 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        ;
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallCategoryListWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCallCategoryListWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            mFragmentTabHomeBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);

            errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentTabHomeBinding.getRoot());
        }
    };
    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            errorLoadingHelper.showLoading();
            reloadCategoryListFromServer();
        }
    };
}
