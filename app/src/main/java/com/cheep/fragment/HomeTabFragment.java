package com.cheep.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Scroller;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.HomeActivity;
import com.cheep.activity.SearchActivity;
import com.cheep.activity.SelectLocationActivity;
import com.cheep.activity.TaskCreationActivity;
import com.cheep.adapter.HomeTabRecyclerViewAdapter;
import com.cheep.cheepcare.activity.LandingScreenPickPackageActivity;
import com.cheep.cheepcare.fragment.SubscriptionBannerFragment;
import com.cheep.cheepcare.model.CityDetail;
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
import com.cheep.strategicpartner.StrategicPartnerTaskCreationAct;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.FetchLocationInfoUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SharedElementTransitionHelper;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
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
    public ArrayList<CityDetail> careBannerModelArrayList;
    private String mSelectedFilterType = Utility.FILTER_TYPES.FILTER_TYPE_FEATURED;

    public String getmSelectedFilterType() {
        return mSelectedFilterType;
    }

    public static HomeTabFragment newInstance(DrawerLayoutInteractionListener mListener, Uri link) {
        Bundle args = new Bundle();
        HomeTabFragment fragment = new HomeTabFragment();
        args.putParcelable(Utility.Extra.DYNAMIC_LINK_URI, link);
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
        if (userDetails != null && TextUtils.isEmpty(userDetails.userName) == false && userDetails.userName.trim().length() > 1) {
            String name = userDetails.userName.substring(0, 1).toUpperCase() + userDetails.userName.substring(1);
            //Used to get only first word, e.g "Pankaj" from "pankaj sharma"
            mFragmentTabHomeBinding.textSearch.setText(getString(R.string.hint_search_placeholder, name));
//            mFragmentTabHomeBinding.textSearch.setText(getString(R.string.hint_search_placeholder, name.split(" ")[0]));
        } else {
            mFragmentTabHomeBinding.textSearch.setText(getString(R.string.hint_search_placeholder, userDetails.userName));
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
        errorLoadingHelper.setBecomeCheepMemberClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LandingScreenPickPackageActivity.newInstance(mContext, careBannerModelArrayList.get(0), GsonUtility.getJsonStringFromObject(careBannerModelArrayList));
            }
        });

        if (mFragmentTabHomeBinding.toolbar != null) {
            mFragmentTabHomeBinding.toolbar.setTitle(Utility.EMPTY_STRING);
        }

        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mFragmentTabHomeBinding.toolbar);

        mFragmentTabHomeBinding.textSearch.setVisibility(View.GONE);
        //Setting search placeholder
        profileUpdate();

        initSwipeToRefreshLayout();

        if (!EventBus.getDefault().isRegistered(this))
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
        setupCheepCareBannerViewPager(null);
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
        LogUtils.LOGE(TAG, "onMessageEvent() called with: event = [" + event + "]");
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.NEW_NOTIFICATION) {
            updateCounter();
        }
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_SUCCESSFULLY) {
            for (CityDetail cityDetail : cheepCareBannerViewPagerAdapter.getLists()) {
                if (cityDetail.id.equalsIgnoreCase(event.id))
                    cityDetail.isSubscribed = Utility.BOOLEAN.YES;
            }
            cheepCareBannerViewPagerAdapter.notifyDataSetChanged();
        }
    }

    private void initiateRecyclerView(ArrayList<JobCategoryModel> list) {
//        Log.d(TAG, "initiateRecyclerView() called with: list = [" + list + "]");
        homeTabRecyclerViewAdapter = new HomeTabRecyclerViewAdapter(list, mCategoryRowInteractionListener, mSelectedFilterType);
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
                getBannerImageListFromServer();
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
            if (getArguments().getParcelable(Utility.Extra.DYNAMIC_LINK_URI) != null) {
                getCategoryIdBasedOnSlug();
            }
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
                LocationInfo mLocationInfo = (LocationInfo) GsonUtility.getObjectFromJsonString(data.getStringExtra(Utility.Extra.LOCATION_INFO), LocationInfo.class);
                if (mLocationInfo != null) {
                    updateLatLongSuccess(mLocationInfo.getDisplayLocationName());
                    if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
                        updateLocationForLoggedInUser(mLocationInfo);
                    }
                } else {
                    Log.e(TAG, "onActivityResult: Location not fetched properly>>");
                }
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
        if (!Utility.isConnected(mContext)) {
            errorLoadingHelper.failed(Utility.NO_INTERNET_CONNECTION, 0, onRetryBtnClickListener);
        }
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
                        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
                            Log.v("in if condition", "update location");
                            updateLocationForLoggedInUser(mLocationIno);
                        }
                    }

                    @Override
                    public void internetConnectionNotFound() {
                        errorLoadingHelper.failed(Utility.NO_INTERNET_CONNECTION, 0, onRetryBtnClickListener);
                    }
                },
                true
        );
        mFetchLocationInfoUtility.getLocationInfo(!TextUtils.isEmpty(lat) ? lat : Utility.STATIC_LAT, !TextUtils.isEmpty(lng) ? lng : Utility.STATIC_LNG);
    }

    private void updateLocationForLoggedInUser(LocationInfo mLocationIno) {
        Log.d(TAG, "updateLocationForLoggedInUser() called with: mLocationIno = [" + mLocationIno + "]");
        /**
         * Only call webservice for update location in case user is logged in
         */
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            // Update User location
            //Add Header parameters
            Map<String, String> mHeaderParams = new HashMap<>();
            mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

            //Add Params
            Map<String, String> mParams = new HashMap<>();
            mParams.put(NetworkUtility.TAGS.LAT, mLocationIno.lat);
            mParams.put(NetworkUtility.TAGS.LNG, mLocationIno.lng);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, mLocationIno.City);
            mParams.put(NetworkUtility.TAGS.COUNTRY, mLocationIno.Country);
            mParams.put(NetworkUtility.TAGS.STATE, mLocationIno.State);
            mParams.put(NetworkUtility.TAGS.LOCALITY, TextUtils.isEmpty(mLocationIno.Locality) ? Utility.EMPTY_STRING : mLocationIno.Locality);

            VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.UPDATE_LOCATION
                    , mCallUpdateLatLngWSErrorListener
                    , mCallUpdateLatLngWSResponseListener
                    , mHeaderParams
                    , mParams
                    , null);
            Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.UPDATE_LOCATION);
        }
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

                        // Update City and Address issue and update the same information from
                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.CityID = jsonData.optString(NetworkUtility.TAGS.CITY_ID);
                        userDetails.mLat = jsonData.optString(NetworkUtility.TAGS.LAT);
                        userDetails.mLng = jsonData.optString(NetworkUtility.TAGS.LNG);
                        userDetails.mStateName = jsonData.optString(NetworkUtility.TAGS.STATE);
                        userDetails.mCityName = jsonData.optString(NetworkUtility.TAGS.CITY_NAME);
                        userDetails.mCountry = jsonData.optString(NetworkUtility.TAGS.COUNTRY);
                        userDetails.mLocality = jsonData.optString(NetworkUtility.TAGS.LOCALITY);
                        PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);

//                       Beenal - For Category listing update after the location update.
                        // Call Category listing webservice.
                        getCategoryListFromServer();
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
        if (getArguments().getParcelable(Utility.Extra.DYNAMIC_LINK_URI) != null) {
            getCategoryIdBasedOnSlug();
        }
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
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).isReadyToLoad = true;
        } else {
            return;
        }

        if (!Utility.isConnected(mContext)) {
            errorLoadingHelper.failed(Utility.NO_INTERNET_CONNECTION, 0, onRetryBtnClickListener);
            return;
        }

        errorLoadingHelper.showLoading();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

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
                        JSONObject dataObject = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                        bannerImageModelArrayList = GsonUtility.getObjectListFromJsonString(dataObject.getString(NetworkUtility.TAGS.NORMAL_BANNER), BannerImageModel[].class);
                        careBannerModelArrayList = GsonUtility.getObjectListFromJsonString(dataObject.getString(NetworkUtility.TAGS.CARE_BANNER), CityDetail[].class);
                        // Call Category listing webservice.

                        for (CityDetail cityDetail : careBannerModelArrayList) {
                            if (cityDetail.isSubscribed.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                                LogUtils.LOGE(TAG, "cityDetail: " + cityDetail.cityName);
                                mSelectedFilterType = Utility.FILTER_TYPES.FILTER_TYPE_SUBSCRIBED;
                                updateFilterText();
                                break;
                            }
                        }


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
        errorLoadingHelper.showEmptyCategorySection(mSelectedFilterType);
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
            errorLoadingHelper.failed(Utility.NO_INTERNET_CONNECTION, 0, onRetryBtnClickListener);
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
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mParams.put(NetworkUtility.TAGS.LAT, PreferenceUtility.getInstance(mContext).getUserDetails().mLat);
            mParams.put(NetworkUtility.TAGS.LNG, PreferenceUtility.getInstance(mContext).getUserDetails().mLng);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, PreferenceUtility.getInstance(mContext).getUserDetails().mCityName);
            mParams.put(NetworkUtility.TAGS.COUNTRY, PreferenceUtility.getInstance(mContext).getUserDetails().mCountry);
            mParams.put(NetworkUtility.TAGS.STATE, PreferenceUtility.getInstance(mContext).getUserDetails().mStateName);
        } else {
            mParams.put(NetworkUtility.TAGS.LAT, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mLat);
            mParams.put(NetworkUtility.TAGS.LNG, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mLng);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mCityName);
            mParams.put(NetworkUtility.TAGS.COUNTRY, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mCountryName);
            mParams.put(NetworkUtility.TAGS.STATE, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mStateName);
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

//                       Condition for changing the location icon
                        Log.v("Closest Area", jsonObject.optString(NetworkUtility.TAGS.CLOSEST_AREA));
                        String cat = jsonObject.optString(NetworkUtility.TAGS.CLOSEST_AREA);
                        if (cat.equals("{}") || cat.equals("[]") || cat.equals("null") || TextUtils.isEmpty(cat)) {
                            String Category = "";
                            Log.v("Closest Area if", Category);
                            updateLogoSuccess(Category);
                        } else {
                            String Category = "";
                            Category = jsonObject.getJSONObject(NetworkUtility.TAGS.CLOSEST_AREA).getString(NetworkUtility.TAGS.CLOSEST_CATEGORY);
                            Log.v("Closest Area else", Category);
                            updateLogoSuccess(Category);
                        }
                        ArrayList<JobCategoryModel> list;
                        list = GsonUtility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), JobCategoryModel[].class);

                        // SHow Banner view now
                        showBannerView(true);

                        // Setup Cover ViewPager homeTabRecyclerViewAdapter
                        addCoverImageListing(bannerImageModelArrayList, careBannerModelArrayList);

                        // Setting RecyclerView Adapter
                        homeTabRecyclerViewAdapter.addItems(list, mSelectedFilterType);

                        toggleErrorScreen(false);
                        if (list.size() <= 0) {
                            if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FAVOURITES) ||
                                    mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_SUBSCRIBED)) {
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
            } catch (Exception e) {
                e.printStackTrace();
                mCallCategoryListWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    private void updateLogoSuccess(String category) {
        Log.d(TAG, "updateLogoSuccess() called with: LogoCategory = [" + category + "]");

        if (category.equalsIgnoreCase("home")) {
            mFragmentTabHomeBinding.imgLocation.setImageDrawable(getResources().getDrawable(R.drawable.ab_home));
        } else if (category.equalsIgnoreCase("office")) {
            mFragmentTabHomeBinding.imgLocation.setImageDrawable(getResources().getDrawable(R.drawable.ab_office));
        } else if (category.equalsIgnoreCase("other")) {
            mFragmentTabHomeBinding.imgLocation.setImageDrawable(getResources().getDrawable(R.drawable.ab_other));
        } else {
            mFragmentTabHomeBinding.imgLocation.setImageDrawable(getResources().getDrawable(R.drawable.ab_pick_location));
        }
    }

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
    /////////////////////////////////////// Dynamic Linking getCategoryIdBasedOnSlug [Start]/////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void getCategoryIdBasedOnSlug() {
        //If user is logged out already,return from here only.
        /*if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            return;
        }*/
        if (mContext == null)
            return;
        String categorySlug = "";

        ((HomeActivity) mContext).isReadyToLoad = true;
        if (getArguments().getParcelable(Utility.Extra.DYNAMIC_LINK_URI) != null) {
            Log.d(TAG, "getCategoryIdBasedOnSlug: category slug" +
                    ((Uri) getArguments().getParcelable(Utility.Extra.DYNAMIC_LINK_URI)).getLastPathSegment());
            categorySlug = ((Uri) getArguments().getParcelable(Utility.Extra.DYNAMIC_LINK_URI)).getLastPathSegment();
            if (categorySlug.equalsIgnoreCase(Utility.DYNAMIC_LINK_CATEGORY_HOME) || categorySlug.isEmpty()) {
                return;
            }
        } else {
            return;
        }


        // if dynamic link is for home then do not call web service
        if (getArguments().getParcelable(Utility.Extra.DYNAMIC_LINK_URI) != null) {
            Log.d(TAG, "getCategoryIdBasedOnSlug: category slug" +
                    ((Uri) getArguments().getParcelable(Utility.Extra.DYNAMIC_LINK_URI)).getLastPathSegment());
            categorySlug = ((Uri) getArguments().getParcelable(Utility.Extra.DYNAMIC_LINK_URI)).getLastPathSegment();
            if (categorySlug.equalsIgnoreCase(Utility.DYNAMIC_LINK_CATEGORY_HOME) || categorySlug.isEmpty()) {
                return;
            }
        } else {
            return;
        }

        if (!Utility.isConnected(mContext)) {
            errorLoadingHelper.failed(Utility.NO_INTERNET_CONNECTION, 0, onRetryBtnClickListener);
            return;
        }

        errorLoadingHelper.showLoading();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CAT_SLUG, categorySlug);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.GET_CATEGORY_ID_BASED_ON_SLUG
                , mCallGetCategoryIdWSErrorListener
                , mCallGetCategoryIdWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).

                addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_CATEGORY_ID_BASED_ON_SLUG);
    }

    Response.Listener mCallGetCategoryIdWSResponseListener = new Response.Listener() {
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
                        String categoryType =
                                jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.CAT_TYPE);
                        if (categoryType.equalsIgnoreCase(Utility.NORMAL)) {
                            JobCategoryModel model = (JobCategoryModel) GsonUtility.getObjectFromJsonString(
                                    jsonObject.getString(NetworkUtility.TAGS.DATA), JobCategoryModel.class);
//                            TaskCreationCCActivity.getInstance(mContext, model);
                            TaskCreationActivity.getInstance(mContext, model);
                        } else {
                            BannerImageModel model = (BannerImageModel) GsonUtility.getObjectFromJsonString(
                                    jsonObject.getString(NetworkUtility.TAGS.DATA), BannerImageModel.class);
                            StrategicPartnerTaskCreationAct.getInstance(mContext, model);
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
                mCallGetCategoryIdWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCallGetCategoryIdWSErrorListener = new Response.ErrorListener() {
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
    /////////////////////////////////////// Dynamic Linking getCategoryIdBasedOnSlug [End]/////////////////////////////////
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
    private CheepCareBannerViewPagerAdapter cheepCareBannerViewPagerAdapter;
    private Handler mHandler;
    private Handler mHandlerSubscriptionBanner;

    private void setupCheepCareBannerViewPager(ArrayList<CityDetail> mBannerListModels) {

        cheepCareBannerViewPagerAdapter = new CheepCareBannerViewPagerAdapter(getChildFragmentManager(), mBannerListModels);
        mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages.setAdapter(cheepCareBannerViewPagerAdapter);

        //See if we are having only one image, do not show the indicator in that case
        showORHidePagerIndicator();

        // For Setting up view pager Indicator
        mFragmentTabHomeBinding.layoutBannerHeader.indicatorSubscriptionBanner.setViewPager(mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages);
        cheepCareBannerViewPagerAdapter.registerDataSetObserver(mFragmentTabHomeBinding.layoutBannerHeader.indicatorSubscriptionBanner.getDataSetObserver());
        if (mHandlerSubscriptionBanner != null) {
            mHandlerSubscriptionBanner.removeCallbacks(mAutoSlideRunnableSubscriptionBanner);
            mHandlerSubscriptionBanner = null;
            mHandlerSubscriptionBanner = new Handler();
        } else {
            mHandlerSubscriptionBanner = new Handler();
        }
        // Sliding of Viewpager image
        mHandlerSubscriptionBanner.postDelayed(mAutoSlideRunnableSubscriptionBanner, 4000);
        mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages.addOnPageChangeListener(new CircularViewPagerHandler(mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages));

        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages, new CustomScroller(mContext, new DecelerateInterpolator(), 700));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


//        mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int i, float v, int i1) {
//                mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages.getParent().requestDisallowInterceptTouchEvent(true);
//            }
//
//            @Override
//            public void onPageSelected(int i) {
//                if (mHandlerSubscriptionBanner == null) {
//                    return;
//                }
//                //Reset the sliding
//                mHandlerSubscriptionBanner.removeCallbacks(mAutoSlideRunnableSubscriptionBanner);
//                // reset the sliding
//                mHandlerSubscriptionBanner.postDelayed(mAutoSlideRunnableSubscriptionBanner, 4000);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int i) {
//
//            }
//        });
    }

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
    private Runnable mAutoSlideRunnableSubscriptionBanner = new Runnable() {
        @Override
        public void run() {
//            int currentPosition = mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages.getCurrentItem();
//            if (currentPosition == (cheepCareBannerViewPagerAdapter.getCount() - 1)) {
//                currentPosition = 0;
//            } else {
//                currentPosition = currentPosition + 1;
//            }

            final int lastPosition = mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages.getAdapter().getCount() - 1;
            if (mCurrentCityBannerPosition == 0) {
                mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages.setCurrentItem(mCurrentCityBannerPosition + 1, true);
            } else if (mCurrentCityBannerPosition == lastPosition) {
                mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages.setCurrentItem(0, false);
            } else {
                mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages.setCurrentItem(mCurrentCityBannerPosition + 1, true);
            }

            mFragmentTabHomeBinding.layoutBannerHeader.viewPagerSubscriptionBannerImages.setCurrentItem(mCurrentCityBannerPosition);
        }
    };

    private void addCoverImageListing(ArrayList<BannerImageModel> mBannerListModels, ArrayList<CityDetail> careBannerModelArrayList) {
        bannerViewPagerAdapter.replaceData(mBannerListModels);
        cheepCareBannerViewPagerAdapter.replaceData(careBannerModelArrayList);

        //See if we are having only one image, do not show the indicator in that case
        showORHidePagerIndicator();
    }

    private void showORHidePagerIndicator() {
        if (bannerViewPagerAdapter != null && bannerViewPagerAdapter.getCount() >= 1) {
            mFragmentTabHomeBinding.layoutBannerHeader.indicatorHomeBanner.setVisibility(View.VISIBLE);
        } else {
            mFragmentTabHomeBinding.layoutBannerHeader.indicatorHomeBanner.setVisibility(View.INVISIBLE);
        }

        if (cheepCareBannerViewPagerAdapter != null && cheepCareBannerViewPagerAdapter.getCount() >= 1) {
            mFragmentTabHomeBinding.layoutBannerHeader.indicatorSubscriptionBanner.setVisibility(View.VISIBLE);
        } else {
            mFragmentTabHomeBinding.layoutBannerHeader.indicatorSubscriptionBanner.setVisibility(View.INVISIBLE);
        }
    }

    private static class BannerViewPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<BannerImageModel> strategicPartnerBannerArrayList;

        BannerViewPagerAdapter(FragmentManager fragmentManager, ArrayList<BannerImageModel> modelArrayList) {
            super(fragmentManager);
//            Log.d(TAG, "BannerViewPagerAdapter() called with: fragmentManager = [" + fragmentManager + "], modelArrayList = [" + modelArrayList + "]");
            this.strategicPartnerBannerArrayList = modelArrayList != null ? modelArrayList : new ArrayList<BannerImageModel>();
        }

        @Override
        public Fragment getItem(int position) {
//            Log.d(TAG, "getItem() called with: position = [" + position + "]" + " Size: " + cheepCareSubscriptionBannerArrayList.size());
            return BannerImageFragment.getInstance(strategicPartnerBannerArrayList.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            if (strategicPartnerBannerArrayList == null) {
                return 0;
            }
            return strategicPartnerBannerArrayList.size();
        }

        private ArrayList<BannerImageModel> getLists() {
            return strategicPartnerBannerArrayList;
        }

        private void replaceData(ArrayList<BannerImageModel> modelArrayList) {
            strategicPartnerBannerArrayList = modelArrayList;
            notifyDataSetChanged();
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

    }

    private static class CheepCareBannerViewPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<CityDetail> cheepCareSubscriptionBannerArrayList;

        CheepCareBannerViewPagerAdapter(FragmentManager fragmentManager, ArrayList<CityDetail> modelArrayList) {
            super(fragmentManager);
//            Log.d(TAG, "BannerViewPagerAdapter() called with: fragmentManager = [" + fragmentManager + "], modelArrayList = [" + modelArrayList + "]");
            this.cheepCareSubscriptionBannerArrayList = modelArrayList != null ? modelArrayList : new ArrayList<CityDetail>();
        }

        @Override
        public Fragment getItem(int position) {
//            Log.d(TAG, "getItem() called with: position = [" + position + "]" + " Size: " + cheepCareSubscriptionBannerArrayList.size());
            return SubscriptionBannerFragment.getInstance(cheepCareSubscriptionBannerArrayList.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return cheepCareSubscriptionBannerArrayList == null ? 0 : cheepCareSubscriptionBannerArrayList.size();
        }

        private ArrayList<CityDetail> getLists() {
            return cheepCareSubscriptionBannerArrayList;
        }

        private void replaceData(ArrayList<CityDetail> modelArrayList) {
//            Log.d(TAG, "replaceData() called with: modelArrayList = [" + modelArrayList.size() + "]");
            cheepCareSubscriptionBannerArrayList = modelArrayList;
            notifyDataSetChanged();
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
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
        } else if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_SUBSCRIBED)) {
            mFragmentTabHomeBinding.layoutBannerHeader.textFilter.setText(getResources().getString(R.string.label_subscribed));
            mFragmentTabHomeBinding.layoutBannerHeader.textFilter.setCompoundDrawablesWithIntrinsicBounds(R.drawable.selector_drawable_left_filter_home_featured, 0, 0, 0);
        }
    }

    /**
     * This method would setup Filter Window  for customized view
     */
    private void showFilterWindow() {
//        Log.i(TAG, "showFilterWindow: ");

        final LayoutFilterHomePopupBinding mLayoutFilterHomePopupBinding =
                DataBindingUtil.inflate(
                        LayoutInflater.from(mContext)
                        , R.layout.layout_filter_home_popup
                        , mFragmentTabHomeBinding.layoutBannerHeader.rootBannerView
                        , false
                );
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

        mLayoutFilterHomePopupBinding.textSubscribed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_SUBSCRIBED)) {
                    mPopupWindow.dismiss();
                    return;
                }
                mSelectedFilterType = Utility.FILTER_TYPES.FILTER_TYPE_SUBSCRIBED;
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
            mLayoutFilterHomePopupBinding.textSubscribed.setSelected(false);
        } else if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_POPULAR)) {
            mLayoutFilterHomePopupBinding.textFeatured.setSelected(false);
            mLayoutFilterHomePopupBinding.textPopular.setSelected(true);
            mLayoutFilterHomePopupBinding.textFavourites.setSelected(false);
            mLayoutFilterHomePopupBinding.textSubscribed.setSelected(false);
        } else if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FAVOURITES)) {
            mLayoutFilterHomePopupBinding.textFeatured.setSelected(false);
            mLayoutFilterHomePopupBinding.textPopular.setSelected(false);
            mLayoutFilterHomePopupBinding.textFavourites.setSelected(true);
            mLayoutFilterHomePopupBinding.textSubscribed.setSelected(false);
        } else if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_SUBSCRIBED)) {
            mLayoutFilterHomePopupBinding.textFeatured.setSelected(false);
            mLayoutFilterHomePopupBinding.textPopular.setSelected(false);
            mLayoutFilterHomePopupBinding.textFavourites.setSelected(false);
            mLayoutFilterHomePopupBinding.textSubscribed.setSelected(true);
        } else {
            // By Deafult make featured as selected
            mLayoutFilterHomePopupBinding.textFeatured.setSelected(true);
            mLayoutFilterHomePopupBinding.textPopular.setSelected(false);
            mLayoutFilterHomePopupBinding.textFavourites.setSelected(false);
            mLayoutFilterHomePopupBinding.textSubscribed.setSelected(false);
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
            Utility.showToast(mContext, Utility.NO_INTERNET_CONNECTION);
            return;
        }

//        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

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


    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////            Vertical view pager smooth scrooling           [start]       /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class CustomScroller extends Scroller {

        private int mDuration;

        public CustomScroller(Context context, Interpolator interpolator, int duration) {
            super(context, interpolator);
            mDuration = duration;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }

    private int mCurrentCityBannerPosition;

    public class CircularViewPagerHandler implements ViewPager.OnPageChangeListener {
        private ViewPager mViewPager;
        private int mScrollState;

        public CircularViewPagerHandler(final ViewPager viewPager) {
            mViewPager = viewPager;
        }

        @Override
        public void onPageSelected(final int position) {
            mCurrentCityBannerPosition = position;
            if (mHandler == null) {
                return;
            }
            mHandler.removeCallbacks(mAutoSlideRunnableSubscriptionBanner);
            mHandler.postDelayed(mAutoSlideRunnableSubscriptionBanner, 4000);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            handleScrollState(state);
            mScrollState = state;
        }

        private void handleScrollState(final int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                setNextItemIfNeeded();
            }
        }

        private void setNextItemIfNeeded() {
            if (!isScrollStateSettling()) {
                handleSetNextItem();
            }
        }

        private boolean isScrollStateSettling() {
            return mScrollState == ViewPager.SCROLL_STATE_SETTLING;
        }

        private void handleSetNextItem() {
        }

        @Override
        public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////            Vertical view pager smooth scrooling           [end]         /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

}
