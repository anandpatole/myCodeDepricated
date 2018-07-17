package com.cheep.cheepcarenew.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcarenew.adapters.ManageSubscriptionAddressAdapter;
import com.cheep.cheepcarenew.model.ManagesSubscriptionModel;
import com.cheep.cheepcarenew.model.UserCityCareDetail;
import com.cheep.cheepcarenew.model.UserPackageDataModel;
import com.cheep.cheepcarenew.model.UserPackageDetailsModel;
import com.cheep.cheepcarenew.model.UserRenewSubscriptionModel;
import com.cheep.databinding.FragmentManageSubscriptionBinding;
import com.cheep.model.MessageEvent;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.tags.MyLinearLayoutManager;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageSubscriptionActivity extends BaseAppCompatActivity implements
        ManageSubscriptionAddressAdapter.AddressItemClickListener,
        View.OnClickListener {

    public static final String TAG = "ManageSubscriptionActivity";
    private ManageSubscriptionAddressAdapter adapter;
    private FragmentManageSubscriptionBinding mBinding;
    ArrayList<ManagesSubscriptionModel> managesSubscriptionList=null;
    int dateDifference = 0;
    // model classes
    private UserRenewSubscriptionModel userRenewSubscriptionModel;
    private ManagesSubscriptionModel managesSubscriptionModel;

    public static void newInstance(Context context) {

        Intent intent = new Intent(context, ManageSubscriptionActivity.class);
        context.startActivity(intent);
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.fragment_manage_subscription);

        // add event bus listener
        EventBus.getDefault().register(this);

        initiateUI();
        setListeners();
        callManageSubscriptionWS();

    }
    @Nullable
    @Override
    public void initiateUI() {

    }

    @Override
    protected void setListeners() {
        mBinding.back.setOnClickListener(this);
        mBinding.notification.setOnClickListener(this);
        mBinding.addressDropDowns.setOnClickListener(this);
        mBinding.addressDropUp.setOnClickListener(this);
        mBinding.renewBtn.setOnClickListener(this);
        mBinding.autoRenewalToggle.setOnClickListener(this);
        mBinding.addressDropDownsSingle.setOnClickListener(this);
        mBinding.addressDropDowns.setOnClickListener(this);
        mBinding.back.setOnClickListener(this);
    }

    private void setAllAddressOnView() {
        mBinding.onlyOneAddressLayout.setVisibility(View.INVISIBLE);
        mBinding.allAddressLayout.setVisibility(View.VISIBLE);
        mBinding.addressDropDowns.setVisibility(View.VISIBLE);
        mBinding.addressDropUp.setVisibility(View.GONE);


        MyLinearLayoutManager layoutManager= new MyLinearLayoutManager(mContext);
        mBinding.subscriptionRecyclerView.setLayoutManager(layoutManager);
        mBinding.subscriptionRecyclerView.setNestedScrollingEnabled(false);
        mBinding.subscriptionRecyclerView.setHasFixedSize(false);
        adapter = new ManageSubscriptionAddressAdapter(managesSubscriptionList, ManageSubscriptionActivity.this);
        mBinding.subscriptionRecyclerView.setAdapter(adapter);

    }

    private void setOnlyOneAddressOnView(ManagesSubscriptionModel managesSubscriptionModel) {
        mBinding.onlyOneAddressLayout.setVisibility(View.VISIBLE);
        mBinding.allAddressLayout.setVisibility(View.GONE);
        mBinding.textFullAddress.setText(managesSubscriptionModel.userPackageData.address);
        if (managesSubscriptionModel.userPackageData.packageType.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.PREMIUM)) {
            mBinding.tvCheepCarePackage.setText(Utility.CHEEP_CARE_PREMIUM_PACKAGE);
        } else {
            mBinding.tvCheepCarePackage.setText(Utility.CHEEP_CARE_PACKAGE);
        }
        mBinding.textSubscriptionDuration.setText(managesSubscriptionModel.userPackageData.packageDuration + " " + Utility.MONTH);
        mBinding.textAddressCategory.setText(Utility.getAddressCategoryString(managesSubscriptionModel.userPackageData.category));
        mBinding.textAddressCategory.setCompoundDrawablesWithIntrinsicBounds(Utility.getAddressCategoryBlueIcon(managesSubscriptionModel.userPackageData.category), 0, 0, 0);

        mBinding.textAmountPaid.setText(mContext.getString(R.string.rupee_symbol_x, managesSubscriptionModel.userPackageData.paidAmount));

        if (Utility.PAYMENT_TYPE_IS_PAYU.equalsIgnoreCase(managesSubscriptionModel.userPackageData.paymentType)) {
            mBinding.textPaymentMethod.setText(Utility.HDFC);
        } else {
            mBinding.textPaymentMethod.setText(managesSubscriptionModel.userPackageData.paymentType);
        }

        mBinding.textSubscribedOn.setText(Utility.getDate(managesSubscriptionModel.userPackageData.startDate)); // grt date like 23th jun 208
        mBinding.textSubscriptionEndDate.setText(Utility.getDate(managesSubscriptionModel.userPackageData.endDate));
        if (managesSubscriptionModel.userPackageData.isRenew.equalsIgnoreCase(Utility.YES)) {
            mBinding.relativeAutoRenewal.setVisibility(View.VISIBLE);
            mBinding.renewLl.setVisibility(View.GONE);
        } else {
            mBinding.relativeAutoRenewal.setVisibility(View.GONE);
            mBinding.renewLl.setVisibility(View.VISIBLE);
        }
        // calculate how many date to left to from end date
        /* if day is more than 10 then RENEW BUTTON is clickable and color will be blue
         * other wise color will be gray and not clickable*/

        dateDifference = Utility.getDifferenceBetweenTwoDate(Utility.getCurrentDate(), managesSubscriptionModel.userPackageData.endDate); //2018-12-20
        if (dateDifference <= Integer.valueOf(managesSubscriptionModel.PRIOR_PACKAGE_RENEW_NOTIFICATION_DAY)) {
            mBinding.linearRenew.setVisibility(View.VISIBLE);
        } else {
            mBinding.linearRenew.setVisibility(View.VISIBLE);
        }

        collectDataForRenewSubscription(managesSubscriptionModel);
    }
    // this method is used to collect data for renew subscription
    private void collectDataForRenewSubscription(ManagesSubscriptionModel model){
        userRenewSubscriptionModel = new UserRenewSubscriptionModel();

        //user_package_data
        userRenewSubscriptionModel.userPackageId = model.userPackageData.userPackageId;
        userRenewSubscriptionModel.packageId = model.userPackageData.packageId;
        userRenewSubscriptionModel.packageType = model.userPackageData.packageType;
        userRenewSubscriptionModel.packageDuration = model.userPackageData.packageDuration;
        userRenewSubscriptionModel.startDate = model.userPackageData.startDate;
        userRenewSubscriptionModel.endDate = model.userPackageData.endDate;
        userRenewSubscriptionModel.addressId = model.userPackageData.addressId;
        userRenewSubscriptionModel.isRenew = model.userPackageData.isRenew;
        userRenewSubscriptionModel.name = model.userPackageData.name;
        userRenewSubscriptionModel.address = model.userPackageData.address;
        userRenewSubscriptionModel.pincode = model.userPackageData.pincode;
        userRenewSubscriptionModel.category = model.userPackageData.category;
        userRenewSubscriptionModel.paidAmount = model.userPackageData.paidAmount;
        userRenewSubscriptionModel.assetTypeId = model.userPackageData.assetTypeId;

        //package_detail
        userRenewSubscriptionModel.title = model.userPackageDetail.title;
        userRenewSubscriptionModel.subtitle = model.userPackageDetail.subtitle;
        userRenewSubscriptionModel.type = model.userPackageDetail.type;
        userRenewSubscriptionModel.old_price = model.userPackageDetail.old_price;
        userRenewSubscriptionModel.new_price = model.userPackageDetail.new_price;

        //city_care_detail
        userRenewSubscriptionModel.id = model.userCityCareDetail.id;
        userRenewSubscriptionModel.cityTitle = model.userCityCareDetail.title;
        userRenewSubscriptionModel.citySubtitle = model.userCityCareDetail.subtitle;
        userRenewSubscriptionModel.citySlug = model.userCityCareDetail.citySlug;
        userRenewSubscriptionModel.cityName = model.userCityCareDetail.cityName;


        Log.e(TAG, "USER PACKAGE ID = [" + userRenewSubscriptionModel.userPackageId + "]");
        Log.e(TAG, "USER PACKAGE TITILE = [" + userRenewSubscriptionModel.title + "]");
    }

    // ManageSubscriptionAddressAdapter.AddressItemClickListener
    @Override
    public void onClickItem(ManagesSubscriptionModel model)
    {

        setOnlyOneAddressOnView(model);

    }

    //View.OnClickListener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.address_drop_downs_single:
                setAllAddressOnView();
                break;
            case R.id.address_drop_downs:
                mBinding.onlyOneAddressLayout.setVisibility(View.VISIBLE);
                mBinding.allAddressLayout.setVisibility(View.GONE);
                break;
            case R.id.back:
                onBackPressed();
                //getSupportFragmentManager().beginTransaction().replace(R.id.content, ProfileTabFragment.newInstance(), ProfileTabFragment.TAG).commitAllowingStateLoss();
                break;
            case R.id.renew_btn:
                PaymentSummaryCheepCareActivity.newInstance(getApplicationContext(),userRenewSubscriptionModel,Utility.MANAGE_SUBSCRIPTION);
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Category Manage Subscription [END]///////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void callManageSubscriptionWS() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.MANAGE_SUBSCRIPTION
                , mCallGetCityCareDetailsWSErrorListener
                , mCallGetCityCareDetailsWSResponseListener
                , mHeaderParams
                , null
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.MANAGE_SUBSCRIPTION);
    }


    private Response.Listener mCallGetCityCareDetailsWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            hideProgressDialog();
            Log.e(TAG, "onResponse() called with: response = [" + response + "]");
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.e(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        JSONArray jsonArray = jsonObject.optJSONArray(NetworkUtility.TAGS.DATA);
                        if(managesSubscriptionList == null){
                            managesSubscriptionList = new ArrayList<>();
                        }
                        for(int n = 0; n < jsonArray.length(); n++)
                        {
                            JSONObject jsonData = jsonArray.getJSONObject(n);

                            JSONObject userPackageData = jsonData.getJSONObject(NetworkUtility.TAGS.MANAGE_SUBSCRIPTION_USER_PACKAGE_DATA);
                            JSONObject packageDetails = jsonData.getJSONObject(NetworkUtility.TAGS.MANAGE_SUBSCRIPTION_USER_PACKAGE_DETAILS);
                            JSONObject cityCareDetail = jsonData.getJSONObject(NetworkUtility.TAGS.MANAGE_SUBSCRIPTION_USER_CITY_CARE_DETAIL);
                            managesSubscriptionModel = new ManagesSubscriptionModel();
                            managesSubscriptionModel.userPackageData = (UserPackageDataModel) GsonUtility.getObjectFromJsonString(userPackageData.toString(), UserPackageDataModel.class);
                            managesSubscriptionModel.userPackageDetail = (UserPackageDetailsModel) GsonUtility.getObjectFromJsonString(packageDetails.toString(), UserPackageDetailsModel.class);
                            managesSubscriptionModel.userCityCareDetail = (UserCityCareDetail) GsonUtility.getObjectFromJsonString(cityCareDetail.toString(), UserCityCareDetail.class);
                            JSONObject item = jsonArray.getJSONObject(n);
                            managesSubscriptionModel.PRIOR_PACKAGE_RENEW_NOTIFICATION_DAY = item.getString(NetworkUtility.TAGS.PRIOR_PACKAGE_RENEW_NOTIFICATION_DAY);

                            managesSubscriptionList.add(managesSubscriptionModel);
                        }

                        setOnlyOneAddressOnView(managesSubscriptionList.get(0));

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);

                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                mCallGetCityCareDetailsWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };
    private Response.ErrorListener mCallGetCityCareDetailsWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            hideProgressDialog();
            Log.e(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    /**
     * Event Bus Callbacks
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_RENEW_SUCCESSFULLY:
                finish();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}