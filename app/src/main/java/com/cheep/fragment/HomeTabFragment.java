package com.cheep.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.HomeActivity;
import com.cheep.activity.SearchActivity;
import com.cheep.activity.SelectLocationActivity;
import com.cheep.adapter.HomeTabRecyclerViewAdapter;
import com.cheep.databinding.FragmentTabHomeBinding;
import com.cheep.databinding.LayoutFilterHomePopupBinding;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.interfaces.NotificationClickInteractionListener;
import com.cheep.model.BannerImageModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.LocationInfo;
import com.cheep.model.MessageEvent;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.FetchLocationInfoUtility;
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
 * Created by Bhavesh Patadiya on 9/27/16.
 */
public class HomeTabFragment extends BaseFragment {
    public static final String TAG = "HomeTabFragment";
    FragmentTabHomeBinding mFragmentTabHomeBinding;
    private DrawerLayoutInteractionListener mListener;
    private NotificationClickInteractionListener mNotificationClickInteractionListener;
    private CategoryRowInteractionListener mCategoryRowInteractionListener;
    private HomeTabRecyclerViewAdapter homeTabRecyclerViewAdapter;
    //    private String tempCityName; // this variables value comes from SelectLocationActivity and onActivityResult of this class
    private ErrorLoadingHelper errorLoadingHelper;
    //  private boolean mAlreadyLoaded = false;

    // Saving Current Location for using them later on
//    private String mLat;
//    private String mLng;

    // For storing category Cover image list
    private ArrayList<BannerImageModel> bannerImageModelArrayList;
    private String mSelectedFilterType = Utility.FILTER_TYPES.FILTER_TYPE_FEATURED;


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
        if (userDetails == null) {
            //In case of Guest User we would be having static stuff
            mFragmentTabHomeBinding.textSearch.setText(getString(R.string.hint_search_placeholder, Utility.GUEST_STATIC_INFO.USERNAME));
            return;
        }

        // Normal User so, go ahead as per earlier flow.
        // Update the name
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
        initiateUI();
        setListener();
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
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ALL_BANNER);

        if (mFragmentTabHomeBinding != null && mFragmentTabHomeBinding.commonRecyclerView != null && mFragmentTabHomeBinding.swipeRefreshLayout != null)
            mFragmentTabHomeBinding.swipeRefreshLayout.setRefreshing(false);

        super.onDetach();
    }

    @Override
    public void initiateUI() {
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

        mFragmentTabHomeBinding.textSearch.setVisibility(View.GONE);
        //Setting search placeholder
        profileUpdate();

        initSwipeToRefreshLayout();

        if (EventBus.getDefault().isRegistered(this) == false)
            EventBus.getDefault().register(this);

        // Set Height of Viewpager according to 16:9 resolution to make the things work
       /* mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.getLayoutParams();
                params.height = Utility.getHeightFromWidthForSixteenNineRatio(params.width);
                mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.setLayoutParams(params);
            }
        });*/

        // Setup Cover Image Adapter as Empty
        setupCoverViewPager(null);
        // Initiate Recyclerview
        initiateRecyclerView(null);

        // Hide Banner Image view by default
        showBannerView(false);

        // Enable Filter text
        updateFilterText();

        // Calculat Pager Height and Width
        ViewTreeObserver mViewTreeObserver = mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.getViewTreeObserver();
        mViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//                Log.d(TAG, "onGlobalLayout() called");
                mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.getMeasuredWidth();
                int height = mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.getMeasuredHeight();
//                Log.d(TAG, "onGlobalLayout() called==> " + width + "*" + height);
                ViewGroup.LayoutParams params = mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.getLayoutParams();
                params.height = Utility.getHeightFromWidthForTwoOneRatio(width);
                mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.setLayoutParams(params);
            }
        });

        /**
         * Load the contents in onResume().
         */
        if (((HomeActivity) getActivity()).isReadyToLoad) {
            Log.i(TAG, "initiateUI: isReadyToLoad: " + ((HomeActivity) getActivity()).isReadyToLoad);
            loadHomeScreenDetails();
        } else {
            Log.i(TAG, "initiateUI: isReadyToLoad: " + ((HomeActivity) getActivity()).isReadyToLoad);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.NEW_NOTIFICATION) {
            updateCounter();
        }
    }

    private void initiateRecyclerView(ArrayList<JobCategoryModel> list) {
//        Log.d(TAG, "initiateRecyclerView() called with: list = [" + list + "]");
        homeTabRecyclerViewAdapter = new HomeTabRecyclerViewAdapter(list, mCategoryRowInteractionListener);
        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setHasFixedSize(true);
//        int padding_In_px = (int) Utility.convertDpToPixel(10, mContext);
//        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setPadding(0, 0, 0, padding_In_px);
        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setAdapter(homeTabRecyclerViewAdapter);
        ViewCompat.setNestedScrollingEnabled(mFragmentTabHomeBinding.commonRecyclerView.recyclerView, false);
//        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_home_screen, (int) getResources().getDimension(R.dimen.scale_0dp)));
    }

    private void initSwipeToRefreshLayout() {
        mFragmentTabHomeBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Fetch Only Category List from server
                getCategoryListFromServer();
            }
        });
        Utility.setSwipeRefreshLayoutColors(mFragmentTabHomeBinding.swipeRefreshLayout);
    }

    @Override
    public void setListener() {
        mFragmentTabHomeBinding.textSearch.setOnClickListener(onClickListener);
        mFragmentTabHomeBinding.textLocation.setOnClickListener(onClickListener);
        mFragmentTabHomeBinding.relNotificationAction.setOnClickListener(onClickListener);
        mFragmentTabHomeBinding.layoutBannerHeader.textFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Setup Popup Window view for Filter section
                showFilterWindow();
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedElementTransitionHelper sharedElementTransitionHelper;
            switch (view.getId()) {

                case text_search:

                    if (homeTabRecyclerViewAdapter != null) {
                        sharedElementTransitionHelper = new SharedElementTransitionHelper(getActivity());
                        sharedElementTransitionHelper.put(mFragmentTabHomeBinding.textSearch, R.string.transition_text_search);
                        SearchActivity.newInstance(mContext, sharedElementTransitionHelper.getBundle(), mFragmentTabHomeBinding.textLocation.getText().toString().trim(), homeTabRecyclerViewAdapter.getmList());
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

        void onCategoryFavouriteClicked(JobCategoryModel model, int position);

        void onListCategoryListGetsEmpty();
    }

    @Override
    public void onLocationNotAvailable() {
        super.onLocationNotAvailable();
        Log.d(TAG, "onLocationNotAvailable() called");
        ((HomeActivity) getActivity()).isReadyToLoad = true;
        loadHomeScreenDetails();
    }


    @Override
    public void onLocationFetched(Location mLocation) {
        super.onLocationFetched(mLocation);
        Log.d(TAG, "onLocationFetched() called with: mLocation = [" + mLocation + "]");
        ((HomeActivity) getActivity()).isReadyToLoad = true;
        updateLatLongOnServer(String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()));
    }

    public void loadHomeScreenDetails() {
        Log.i(TAG, "loadHomeScreenDetails: ");
        //Setting Location Name
        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Checking if there is city then call the category list else fetch and send current location to server
        if (isPreviousLocationPresent()) {
            toggleErrorScreen(false);
            mFragmentTabHomeBinding.textLocation.setText(userDetails.getDisplayLocationName());
            errorLoadingHelper.showLoading();
            getBannerImageListFromServer();
        } else {
           /* toggleErrorScreen(true);
            mFragmentTabHomeBinding.textLocation.setText(getString(R.string.hint_select_location));
            //starting to choose location
            Intent intent = new Intent(mContext, SelectLocationActivity.class);
            startActivityForResult(intent, Utility.REQUEST_CODE_CHANGE_LOCATION);
            ((AppCompatActivity) mContext).overridePendingTransition(0, 0);*/

            /**
             * Changes @28th August, 2017, Bhavesh
             * In case, user is Guest user, app can look out in case any other location been saved earlier,
             * if so, we can use them and update the details accoringly.
             */
            if (PreferenceUtility.getInstance(mContext).getGuestUserDetails() != null) {
                GuestUserDetails mGuestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
                if (mGuestUserDetails.mLat != null
                        && mGuestUserDetails.mLng != null) {
                    updateLatLongSuccess(TextUtils.isEmpty(mGuestUserDetails.mLocality)
                            ? mGuestUserDetails.mCityName
                            : mGuestUserDetails.mLocality);
                    return;
                }
            }
            /**
             * We couldn't find any location so, load the default one.
             * Passing null, would do the login
             */
            updateLatLongOnServer(null, null);
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
//                mLat = data.getStringExtra(Utility.Extra.LATITUDE);
//                mLng = data.getStringExtra(Utility.Extra.LONGITUDE);
                String displayName = data.getStringExtra(Utility.Extra.CITY_NAME);
                updateLatLongSuccess(displayName);
                // We will update location only in case its logged in user otherwise directly refresh the data
                /*if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
                    // We also need to update latlong for logged in user, we wont wait for the response.
                    updateLatLongOnServer(PreferenceUtility.getInstance(mContext).getUserDetails().mLat,
                            PreferenceUtility.getInstance(mContext).getUserDetails().mLng);
                }*/
            }

        }
    }

    //====================================================
    //============== WEB INTEGRATION =====================
    //====================================================

    /**
     * Update Category list on server
     */
    private void updateLatLongOnServer(String lat, String lng) {
        Log.d(TAG, "updateLatLongOnServer() called with: lat = [" + lat + "], lng = [" + lng + "]");
        errorLoadingHelper.showLoading();
        FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                mContext,
                new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                    @Override
                    public void onLocationInfoAvailable(LocationInfo mLocationIno) {
                        Log.d(TAG, "onLocationInfoAvailable() called with: mLocationIno = [" + mLocationIno + "]");
                        updateLatLongSuccess(TextUtils.isEmpty(mLocationIno.Locality)
                                ? mLocationIno.City
                                : mLocationIno.Locality);
                    }
                },
                true
        );
        mFetchLocationInfoUtility.getLocationInfo(!TextUtils.isEmpty(lat) ? lat : Utility.STATIC_LAT, !TextUtils.isEmpty(lng) ? lng : Utility.STATIC_LNG);


        /*if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            //Its a guest user and hence we need to call google api for ourselves for Guest user.
//            TODO: Guest User we need to call Webservice for fetching the temporary location data
            FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                    mContext,
                    new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                        @Override
                        public void onLocationInfoAvailable(LocationInfo mLocationIno) {
                            Log.d(TAG, "onLocationInfoAvailable() called with: mLocationIno = [" + mLocationIno + "]");
                            updateLatLongSuccess(TextUtils.isEmpty(mLocationIno.Locality)
                                    ? mLocationIno.City
                                    : mLocationIno.Locality);
                        }
                    },
                    true
            );
            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
            mFetchLocationInfoUtility.getLocationInfo(!TextUtils.isEmpty(guestUserDetails.mLat) ? guestUserDetails.mLat : Utility.STATIC_LAT, !TextUtils.isEmpty(guestUserDetails.mLng) ? guestUserDetails.mLng : Utility.STATIC_LNG);
            return;
        } */
        //Setting RecyclerView Adapter
        /*HomeTabRecyclerViewAdapter homeTabRecyclerViewAdapter = new HomeTabRecyclerViewAdapter(BootstrapConstant.DUMMY_JOB_CATEGORY_MODELS_LIST, mCategoryRowInteractionListener);
        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setAdapter(homeTabRecyclerViewAdapter);
        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_white, (int) getResources().getDimension(R.dimen.scale_0dp)));*/


        /**
         * TODO: This needs to be called parallel to updating the home screen contents.
         * Currently We are ommting updating lat long to server, we will do it afterwords
         */
        /*//Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.LAT, mLat);
        mParams.put(NetworkUtility.TAGS.LNG, mLng);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.UPDATE_LOCATION
                , mCallUpdateLatLngWSErrorListener
                , mCallUpdateLatLngWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.UPDATE_LOCATION);*/
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
//                        Utility.showToast(mContext, "Location updated");
                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);

                        /*// Update City and Address issue and update the same information from
                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.CityID = jsonData.optString(NetworkUtility.TAGS.CITY_ID);
                        userDetails.mCityName = jsonData.optString(NetworkUtility.TAGS.CITY_NAME);
                        userDetails.mLocality = jsonData.optString(NetworkUtility.TAGS.LOCALITY);
                        PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);

                        updateLatLongSuccess(userDetails.mLocality);*/

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
//                        mFragmentTabHomeBinding.textLocation.setText(tempCityName);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
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

    private void updateLatLongSuccess(String cityName) {
        Log.d(TAG, "updateLatLongSuccess() called with: cityName = [" + cityName + "]");
        mFragmentTabHomeBinding.textLocation.setText(cityName);
        getBannerImageListFromServer();
    }

    Response.ErrorListener mCallUpdateLatLngWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentTabHomeBinding.getRoot());
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Banner Image [Start]/////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void getBannerImageListFromServer() {
        //If user is logged out already,return from here only.
        /*if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            return;
        }*/
        ((HomeActivity) getActivity()).isReadyToLoad = true;

        if (!Utility.isConnected(mContext)) {
            errorLoadingHelper.failed(getString(R.string.no_internet), 0, onRetryBtnClickListener);
            return;
        }

        errorLoadingHelper.showLoading();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        /*//Add Params
        Map<String, String> mParams = new HashMap<>();
        */

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.ALL_BANNER
                , mCallFetchBannerImageWSErrorListener
                , mCallFetchBannerImageWSResponseListener
                , mHeaderParams
                , null
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.ALL_BANNER);
    }

    Response.Listener mCallFetchBannerImageWSResponseListener = new Response.Listener() {
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
                        bannerImageModelArrayList = Utility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), BannerImageModel[].class);

                        // Call Category listing webservice.
                        getCategoryListFromServer();


                        /*ArrayList<JobCategoryModel> list;
                        list = Utility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), JobCategoryModel[].class);

                        //Setting RecyclerView Adapter
                        homeTabRecyclerViewAdapter = new HomeTabRecyclerViewAdapter(list, mCategoryRowInteractionListener);
                        ViewCompat.setNestedScrollingEnabled(mFragmentTabHomeBinding.commonRecyclerView.recyclerView, false);
                        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.setAdapter(homeTabRecyclerViewAdapter);
                        mFragmentTabHomeBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_white, (int) getResources().getDimension(R.dimen.scale_0dp)));
                        toggleErrorScreen(false);

                        if (list != null && list.size() <= 0) {
                            errorLoadingHelper.failed(null, R.drawable.img_empty_category, onRetryBtnClickListener);
                        }*/

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
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallFetchBannerImageWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCallFetchBannerImageWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            if (mContext == null) {
                return;
            }
            mFragmentTabHomeBinding.swipeRefreshLayout.setRefreshing(false);
            errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Banner Image [End]/////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Category Listing [Start]/////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onListCategoryListGetsEmpty() {
//        Log.d(TAG, "onListCategoryListGetsEmpty() called");
        errorLoadingHelper.showEmptyFavouriteCategorySection();
    }


    private void getCategoryListFromServer(boolean isShowDialog) {
//        showProgressDialog();
        errorLoadingHelper.showLoading();
        getCategoryListFromServer();
    }

    private void getCategoryListFromServer() {
        //If user is logged out already,return from here only.
        /*
          Fixed crash issue.
          @changes by @bhavesh on 25th Feb, 2017
         */
        /*if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            return;
        }*/

        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mFragmentTabHomeBinding.getRoot());
            errorLoadingHelper.failed(getString(R.string.no_internet), 0, onRetryBtnClickListener);
            return;
        }

        if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FAVOURITES)
                && PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            onListCategoryListGetsEmpty();
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mParams.put(NetworkUtility.TAGS.CITY_NAME, PreferenceUtility.getInstance(mContext).getUserDetails().mCityName);
            mParams.put(NetworkUtility.TAGS.LAT, PreferenceUtility.getInstance(mContext).getUserDetails().mLat);
            mParams.put(NetworkUtility.TAGS.LNG, PreferenceUtility.getInstance(mContext).getUserDetails().mLng);
        } else {
            mParams.put(NetworkUtility.TAGS.CITY_NAME, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mCityName);
            mParams.put(NetworkUtility.TAGS.LAT, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mLat);
            mParams.put(NetworkUtility.TAGS.LNG, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mLng);
        }

        /*if (!TextUtils.isEmpty(mLat)) {
            mParams.put(NetworkUtility.TAGS.LAT, mLat);
            mParams.put(NetworkUtility.TAGS.LNG, mLng);
        }*/

        // Sort Type Params
        mParams.put(NetworkUtility.TAGS.SORT_TYPE, mSelectedFilterType);

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.CATEGORY_LIST
                , mCallCategoryListWSErrorListener
                , mCallCategoryListWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.CATEGORY_LIST);
    }

    Response.Listener mCallCategoryListWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;
            hideProgressDialog();
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                mFragmentTabHomeBinding.swipeRefreshLayout.setRefreshing(false);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        /**
                         * Save CityID in case of Guest User
                         */
                        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
                            // We need to store provided CityID
                            GuestUserDetails mGuestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
                            mGuestUserDetails.mCityID = jsonObject.getString(NetworkUtility.TAGS.CITY_ID);
                            PreferenceUtility.getInstance(mContext).saveGuestUserDetails(mGuestUserDetails);
                        }

                        ArrayList<JobCategoryModel> list;
                        list = Utility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), JobCategoryModel[].class);

                        // SHow Banner view now
                        showBannerView(true);

                        // Setup Cover ViewPager homeTabRecyclerViewAdapter
                        addCoverImageListing(bannerImageModelArrayList);

                        // Setting RecyclerView Adapter
                        homeTabRecyclerViewAdapter.addItems(list);

                        toggleErrorScreen(false);
                        if (list.size() <= 0) {
                            if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FAVOURITES)) {
                                onListCategoryListGetsEmpty();
                            } else {
                                errorLoadingHelper.failed(null, R.drawable.img_empty_category, onRetryBtnClickListener);
                            }
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
            mFragmentTabHomeBinding.swipeRefreshLayout.setRefreshing(false);

            errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);

            hideProgressDialog();
            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentTabHomeBinding.getRoot());
        }
    };
    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            errorLoadingHelper.showLoading();
            getCategoryListFromServer();
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Category Listing [END]/////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////Banner Image Logic[Start]/////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showBannerView(boolean flag) {
        mFragmentTabHomeBinding.layoutBannerHeader.rootBannerView.setVisibility(flag ? View.VISIBLE : View.INVISIBLE);
    }

    private BannerViewPagerAdapter bannerViewPagerAdapter;
    private Handler mHandler;

    private void setupCoverViewPager(ArrayList<BannerImageModel> mBannerListModels) {

        bannerViewPagerAdapter = new BannerViewPagerAdapter(getChildFragmentManager(), mBannerListModels);
        mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.setAdapter(bannerViewPagerAdapter);

        //See if we are having only one image, do not show the indicator in that case
        showORHidePagerIndicator();

        // For Setting up view pager Indicator
        mFragmentTabHomeBinding.layoutBannerHeader.indicatorHomeBanner.setViewPager(mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages);
        bannerViewPagerAdapter.registerDataSetObserver(mFragmentTabHomeBinding.layoutBannerHeader.indicatorHomeBanner.getDataSetObserver());
        if (mHandler != null) {
            mHandler.removeCallbacks(mAutoSlideRunnable);
            mHandler = null;
            mHandler = new Handler();
        } else {
            mHandler = new Handler();
        }
        // Sliding of Viewpager image
        mHandler.postDelayed(mAutoSlideRunnable, 4000);

        mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (mHandler == null) {
                    return;
                }
                //Reset the sliding
                mHandler.removeCallbacks(mAutoSlideRunnable);
                // reset the sliding
                mHandler.postDelayed(mAutoSlideRunnable, 4000);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private Runnable mAutoSlideRunnable = new Runnable() {
        @Override
        public void run() {
            int currentPosition = mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.getCurrentItem();
            if (currentPosition == (bannerViewPagerAdapter.getCount() - 1)) {
                currentPosition = 0;
            } else {
                currentPosition = currentPosition + 1;
            }
            mFragmentTabHomeBinding.layoutBannerHeader.viewPagerBannerImages.setCurrentItem(currentPosition);
        }
    };

    private void addCoverImageListing(ArrayList<BannerImageModel> mBannerListModels) {
        bannerViewPagerAdapter.replaceData(mBannerListModels);

        //See if we are having only one image, do not show the indicator in that case
        showORHidePagerIndicator();
    }

    private void showORHidePagerIndicator() {
        if (bannerViewPagerAdapter != null && bannerViewPagerAdapter.getCount() >= 1) {
            mFragmentTabHomeBinding.layoutBannerHeader.indicatorHomeBanner.setVisibility(View.VISIBLE);
        } else {
            mFragmentTabHomeBinding.layoutBannerHeader.indicatorHomeBanner.setVisibility(View.INVISIBLE);
        }
    }

    private static class BannerViewPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<BannerImageModel> imageModelArrayList;

        BannerViewPagerAdapter(FragmentManager fragmentManager, ArrayList<BannerImageModel> modelArrayList) {
            super(fragmentManager);
//            Log.d(TAG, "BannerViewPagerAdapter() called with: fragmentManager = [" + fragmentManager + "], modelArrayList = [" + modelArrayList + "]");
            this.imageModelArrayList = modelArrayList != null ? modelArrayList : new ArrayList<BannerImageModel>();
        }

        @Override
        public Fragment getItem(int position) {
//            Log.d(TAG, "getItem() called with: position = [" + position + "]" + " Size: " + imageModelArrayList.size());
            return BannerImageFragment.getInstance(imageModelArrayList.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            if (imageModelArrayList == null) {
                return 0;
            }
            return imageModelArrayList.size();
        }

        private ArrayList<BannerImageModel> getLists() {
            return imageModelArrayList;
        }

        private void replaceData(ArrayList<BannerImageModel> modelArrayList) {
//            Log.d(TAG, "replaceData() called with: modelArrayList = [" + modelArrayList.size() + "]");
            imageModelArrayList = modelArrayList;
            notifyDataSetChanged();
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
//            super.restoreState(state, loader);
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////Banner Image Logic[End]///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Filter Section[Start]///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void updateFilterText() {
        mFragmentTabHomeBinding.layoutBannerHeader.textFilter.setSelected(true);
        if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FEATURED)) {
            mFragmentTabHomeBinding.layoutBannerHeader.textFilter.setText(getResources().getString(R.string.label_featured));
            mFragmentTabHomeBinding.layoutBannerHeader.textFilter.setCompoundDrawablesWithIntrinsicBounds(R.drawable.selector_drawable_left_filter_home_featured, 0, 0, 0);
        } else if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_POPULAR)) {
            mFragmentTabHomeBinding.layoutBannerHeader.textFilter.setText(getResources().getString(R.string.label_popular));
            mFragmentTabHomeBinding.layoutBannerHeader.textFilter.setCompoundDrawablesWithIntrinsicBounds(R.drawable.selector_drawable_left_filter_home_popular, 0, 0, 0);
        } else if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FAVOURITES)) {
            mFragmentTabHomeBinding.layoutBannerHeader.textFilter.setText(getResources().getString(R.string.label_favourites));
            mFragmentTabHomeBinding.layoutBannerHeader.textFilter.setCompoundDrawablesWithIntrinsicBounds(R.drawable.selector_drawable_left_filter_home_favourites, 0, 0, 0);
        }
    }

    /**
     * This method would setup Filter Window  for customized view
     */
    private void showFilterWindow() {
//        Log.i(TAG, "showFilterWindow: ");

        final LayoutFilterHomePopupBinding mLayoutFilterHomePopupBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.layout_filter_home_popup, mFragmentTabHomeBinding.layoutBannerHeader.rootBannerView, false);
//        View mFilterPopupWindow = View.inflate(mContext, R.layout.layout_filter_home_popup, null);

        final PopupWindow mPopupWindow = new PopupWindow(mContext);
        mPopupWindow.setContentView(mLayoutFilterHomePopupBinding.getRoot());
        mPopupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);

        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        // No animation at present
        mPopupWindow.setAnimationStyle(0);

        // Displaying the popup at the specified location, + offsets.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mPopupWindow.showAsDropDown(mFragmentTabHomeBinding.layoutBannerHeader.textFilter, 0, -mFragmentTabHomeBinding.layoutBannerHeader.textFilter.getHeight(), Gravity.NO_GRAVITY);
        } else {
            mPopupWindow.showAsDropDown(mFragmentTabHomeBinding.layoutBannerHeader.textFilter, 0, -mFragmentTabHomeBinding.layoutBannerHeader.textFilter.getHeight());
        }

        // Manage selection till now
        updateFilterSelectionInPopup(mLayoutFilterHomePopupBinding);

        mLayoutFilterHomePopupBinding.textFeatured.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FEATURED)) {
                    mPopupWindow.dismiss();
                    return;
                }
                mSelectedFilterType = Utility.FILTER_TYPES.FILTER_TYPE_FEATURED;
                updateFilterSelectionInPopup(mLayoutFilterHomePopupBinding);
                updateFilterText();
                getCategoryListFromServer(true);
                mPopupWindow.dismiss();
            }
        });

        mLayoutFilterHomePopupBinding.textPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_POPULAR)) {
                    mPopupWindow.dismiss();
                    return;
                }
                mSelectedFilterType = Utility.FILTER_TYPES.FILTER_TYPE_POPULAR;
                updateFilterSelectionInPopup(mLayoutFilterHomePopupBinding);
                updateFilterText();
                getCategoryListFromServer(true);
                mPopupWindow.dismiss();
            }
        });

        mLayoutFilterHomePopupBinding.textFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FAVOURITES)) {
                    mPopupWindow.dismiss();
                    return;
                }
                mSelectedFilterType = Utility.FILTER_TYPES.FILTER_TYPE_FAVOURITES;
                updateFilterSelectionInPopup(mLayoutFilterHomePopupBinding);
                updateFilterText();
                getCategoryListFromServer(true);
                mPopupWindow.dismiss();
            }
        });

    }

    private void updateFilterSelectionInPopup(LayoutFilterHomePopupBinding mLayoutFilterHomePopupBinding) {
        // Click events logic
        if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FEATURED)) {
            mLayoutFilterHomePopupBinding.textFeatured.setSelected(true);
            mLayoutFilterHomePopupBinding.textPopular.setSelected(false);
            mLayoutFilterHomePopupBinding.textFavourites.setSelected(false);
        } else if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_POPULAR)) {
            mLayoutFilterHomePopupBinding.textFeatured.setSelected(false);
            mLayoutFilterHomePopupBinding.textPopular.setSelected(true);
            mLayoutFilterHomePopupBinding.textFavourites.setSelected(false);
        } else if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FAVOURITES)) {
            mLayoutFilterHomePopupBinding.textFeatured.setSelected(false);
            mLayoutFilterHomePopupBinding.textPopular.setSelected(false);
            mLayoutFilterHomePopupBinding.textFavourites.setSelected(true);
        } else {
            // By Deafult make featured as selected
            mLayoutFilterHomePopupBinding.textFeatured.setSelected(true);
            mLayoutFilterHomePopupBinding.textPopular.setSelected(false);
            mLayoutFilterHomePopupBinding.textFavourites.setSelected(false);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Filter Section[End]///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Favourite Category [Start]//////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onCategoryFavouriteClicked(JobCategoryModel model, int position) {
        if (model.isFavourite.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
            favouriteCategory(model.catId, NetworkUtility.TAGS.REMOVE);
        } else {
            favouriteCategory(model.catId, NetworkUtility.TAGS.ADD);
        }
    }

    private void favouriteCategory(String catId, String req_for) {
        //If user is logged out already,return from here only.
        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            return;
        }

        if (!Utility.isConnected(mContext)) {
            Utility.showToast(mContext, getString(R.string.no_internet));
            return;
        }

//        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CAT_ID, catId);
        mParams.put(NetworkUtility.TAGS.REQ_FOR, req_for);

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.FAVOURITE_CATEGORY
                , mCallFavouriteCategoryWSErrorListener
                , mCallFavouriteCategoryWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList);
    }

    Response.Listener mCallFavouriteCategoryWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;
            hideProgressDialog();
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        String cat_id = jsonObject.getString(NetworkUtility.TAGS.CAT_ID);
                        String isFavourite = jsonObject.getString(NetworkUtility.TAGS.IS_FAVOURITE);
                        homeTabRecyclerViewAdapter.updateOnCategoryFavourited(cat_id, isFavourite, mSelectedFilterType);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallFavouriteCategoryWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCallFavouriteCategoryWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            hideProgressDialog();
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
//            errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Favourite Category [End]/////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
