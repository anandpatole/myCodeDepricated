package com.cheep.cheepcarenew.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcarenew.fragments.AddressCategorySelectionFragment;
import com.cheep.cheepcarenew.fragments.AddressSizeForHomeOfficeFragment;
import com.cheep.cheepcarenew.model.CareCityDetail;
import com.cheep.cheepcarenew.model.PackageDetail;
import com.cheep.databinding.ActivityAddressBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.ComparisionChart.ComparisionChartModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AddressActivity extends BaseAppCompatActivity {

    private PackageDetail packageDetail;
    private CareCityDetail careCityDetail;
    private ComparisionChartModel comparisionChartModel;
    private ActivityAddressBinding mBinding;
    private String comingFrom=Utility.EMPTY_STRING;

    public static void newInstance(Context context, PackageDetail packageDetail, CareCityDetail careCityDetail, String comingFrom) {
        Intent intent = new Intent(context, AddressActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(packageDetail));
        intent.putExtra(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(careCityDetail));
        intent.putExtra(Utility.Extra.COMING_FROM,comingFrom);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(0, 0);
    }

    public PackageDetail getPackageDetail() {
        return packageDetail;
    }

    @Override
    protected void initiateUI() {
        if (getIntent() != null && getIntent().hasExtra(Utility.Extra.DATA)) {
            packageDetail = (PackageDetail) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), PackageDetail.class);
        }
        if (getIntent() != null && getIntent().hasExtra(Utility.Extra.DATA_2)) {
            careCityDetail = (CareCityDetail) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), CareCityDetail.class);
        }
        if (getIntent() != null && getIntent().hasExtra(Utility.Extra.COMING_FROM)) {
            comingFrom = (String) getIntent().getStringExtra(Utility.Extra.COMING_FROM);
        }
        loadFragment(AddressCategorySelectionFragment.TAG, AddressCategorySelectionFragment.newInstance(comingFrom));
        registerReceiver(mBR_OnLoginSuccess, new IntentFilter(Utility.BR_ON_LOGIN_SUCCESS));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBR_OnLoginSuccess);
        EventBus.getDefault().unregister(this);
    }

    /**
     * Event Bus Callbacks
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_SUCCESSFULLY:
                finish();
                break;
        }

    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_address);
        initiateUI();
        EventBus.getDefault().register(this);
    }

    public void loadFragment(String tag, BaseFragment baseFragment) {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out)
                .replace(R.id.content, baseFragment, tag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    private static final String TAG = "AddressActivity";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e(TAG, "onBackPressed:  " + getSupportFragmentManager().getBackStackEntryCount());
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finish();
        }
    }

    /**
     * verfiy selected address is correct for selected city, and also if user has already purchased subscription
     *
     * @param model address model
     */
    public void verifyAddressForCity(final AddressModel model) {

        //Add Header parameters

        showProgressDialog();

        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).

                getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).

                getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).

                    getUserDetails().userID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        int addressId;
        try {
            addressId = Integer.parseInt(model.address_id);
        } catch (Exception e) {
            addressId = 0;
        }
        if (addressId <= 0) {
            // In case its nagative then provide other address information
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, Utility.ZERO_STRING);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, model.cityName);
        } else {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, model.address_id);
            mParams.put(NetworkUtility.TAGS.CARE_CITY_ID, careCityDetail.id);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.CARE_PACKAGE_ID, packageDetail.id);
            mParams.put(NetworkUtility.TAGS.PACKAGE_TYPE, packageDetail.type);
        }

        Utility.hideKeyboard(mContext);
        @SuppressWarnings("unchecked")
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.VERIFY_ADDRESS_CHEEP_CARE
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
                // Close Progressbar
                hideProgressDialog();
                // Show Toast
                Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
            }
        }
                , new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                hideProgressDialog();
                Log.i(TAG, "onResponse: " + response);

                String strResponse = (String) response;
                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    Log.i(TAG, "onResponse: " + jsonObject.toString());
                    int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                    String error_message;

                    switch (statusCode) {
                        case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                            JSONObject jsonData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                            // strategic partner pro id for given location
                            String city_id = jsonData.optString(NetworkUtility.TAGS.CITY_ID);
                            String isPurchased = jsonData.optString(NetworkUtility.TAGS.IS_PURCHASED);
                            if (careCityDetail.id.equalsIgnoreCase(city_id)) {
                                if (isPurchased.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                                    Utility.showToast(AddressActivity.this, getString(R.string.validation_message_already_purchased_package_for_this_address));
                                } else {
                                    String isSamePackageType = jsonData.optString(NetworkUtility.TAGS.IS_SAME_PACKAGE_TYPE);
                                    if (isSamePackageType.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                                        Utility.showToast(AddressActivity.this, getString(R.string.validation_message_same_address_for_same_group_of_care));
                                    } else {
                                        // correct address
                                        loadFragment(AddressSizeForHomeOfficeFragment.TAG, AddressSizeForHomeOfficeFragment.newInstance(model,packageDetail,careCityDetail));
                                    }
                                }
                            } else {
                                Utility.showToast(AddressActivity.this, getString(R.string.validation_message_cheep_care_address, careCityDetail.cityName));
                            }


                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            Utility.showToast(AddressActivity.this, getString(R.string.label_something_went_wrong));
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                            // Show message
                            Utility.showToast(AddressActivity.this, error_message);
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                        case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                            //Logout and finish the current activity
                            Utility.logout(mContext, true, statusCode);
                            AddressActivity.this.finish();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mCallGetCarePackageDetailsSingWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
                }
                hideProgressDialog();
            }
        }
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).

                addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.VERIFY_ADDRESS_CHEEP_CARE);

    }

    private Response.ErrorListener mCallGetCarePackageDetailsSingWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            hideProgressDialog();
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    /**
     * BroadCast that would restart the screen once login has been done.
     */
    private BroadcastReceiver mBR_OnLoginSuccess = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utility.hideKeyboard(mContext);
            UserDetails mUserDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
            if (mUserDetails != null) {
                if (mUserDetails.addressList.isEmpty()) {
                    mUserDetails.addressList = new ArrayList<>();
                }
                // Add additional selected address model here.
                AddressSizeForHomeOfficeFragment addressSizeForHomeOfficeFragment = (AddressSizeForHomeOfficeFragment) getSupportFragmentManager().findFragmentByTag(AddressSizeForHomeOfficeFragment.TAG);
                if (addressSizeForHomeOfficeFragment != null) {
                    mUserDetails.addressList.add(addressSizeForHomeOfficeFragment.addressModel);
                    PaymentSummaryCheepCareActivity.newInstance(mContext, careCityDetail,packageDetail, addressSizeForHomeOfficeFragment.addressModel);
                }
                // Save the user now.
                PreferenceUtility.getInstance(mContext).saveUserDetails(mUserDetails);
            }
        }
    };
}
