package com.cheep.cheepcare.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.cheepcare.adapter.PaymentHistoryCCAdapter;
import com.cheep.databinding.FragmentProfilePaymentHistoryBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.HistoryModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kruti on 20/3/18.
 */

public class ProfilePaymentHistoryFragment extends BaseFragment {

    private FragmentProfilePaymentHistoryBinding mBinding;
    private static final String TAG = LogUtils.makeLogTag(ProfilePaymentHistoryFragment.class);
    private ErrorLoadingHelper errorLoadingHelper;
    private SuperCalendar superCalendar;
    private SuperCalendar calendarChanger;
    private PaymentHistoryCCAdapter mAdapter;

    public static ProfilePaymentHistoryFragment newInstance() {
        Bundle args = new Bundle();
        ProfilePaymentHistoryFragment fragment = new ProfilePaymentHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_payment_history, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void initiateUI() {
        errorLoadingHelper = new ErrorLoadingHelper(mBinding.commonRecyclerView.recyclerView);
        superCalendar = SuperCalendar.getInstance();

        //changing month name
        mBinding.textMonthYear.setText(superCalendar.format(SuperCalendar.SuperFormatter.MONTH_JAN
                + Utility.ONE_CHARACTER_SPACE + SuperCalendar.SuperFormatter.YEAR_4_DIGIT));

        //Setting adapter on recycler view
        mAdapter = new PaymentHistoryCCAdapter(/*mHistoryListener*/);
        mBinding.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.commonRecyclerView.recyclerView.setAdapter(mAdapter);

        errorLoadingHelper.showLoading();
        callHistoryWS(superCalendar.getTimeInMillis());
    }

    @Override
    public void setListener() {
        mBinding.iconLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                superCalendar.getCalendar().add(Calendar.MONTH, -1);
                //changing month name
                mBinding.textMonthYear.setText(superCalendar.format(SuperCalendar.SuperFormatter.MONTH_JAN
                        + Utility.ONE_CHARACTER_SPACE + SuperCalendar.SuperFormatter.YEAR_4_DIGIT));
                callHistoryWS(superCalendar.getTimeInMillis());
            }
        });
        mBinding.iconRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                superCalendar.getCalendar().add(Calendar.MONTH, 1);
                //changing month name
                mBinding.textMonthYear.setText(superCalendar.format(SuperCalendar.SuperFormatter.MONTH_JAN
                        + Utility.ONE_CHARACTER_SPACE + SuperCalendar.SuperFormatter.YEAR_4_DIGIT));
                callHistoryWS(superCalendar.getTimeInMillis());
            }
        });
    }

    /**
     * Call History WS
     *
     * @param timestamp
     */
    private void callHistoryWS(long timestamp) {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
//            mBinding.lnTotalMoneySpent.setVisibility(View.GONE);
            mBinding.clMonthSelector.setVisibility(View.GONE);
//            mBinding.layoutTitle.setVisibility(View.GONE);
//            mBinding.layoutSummary.setVisibility(View.GONE);
            errorLoadingHelper.failed(null, R.drawable.img_empty_history, null, null);
            return;
        } else {
//            mBinding.lnTotalMoneySpent.setVisibility(View.VISIBLE);
//            mBinding.rlMonthSelector.setVisibility(View.VISIBLE);
        }

        if (calendarChanger == null)
            calendarChanger = SuperCalendar.getInstance();

        calendarChanger.setLocaleTimeZone();
        calendarChanger.setTimeInMillis(timestamp);
        calendarChanger.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.MONTH_YEAR, String.valueOf(calendarChanger.format(SuperCalendar.SuperFormatter.MONTH_NUMBER + "/" + SuperCalendar.SuperFormatter.YEAR_4_DIGIT)));

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.PAYMENT_HISTORY
                , mCallHistoryWSErrorListener
                , mCallHistoryWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList, NetworkUtility.WS.PAYMENT_HISTORY);
    }

    Response.Listener mCallHistoryWSResponseListener = new Response.Listener() {
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

//                        mFragmentHistoryBinding.textPrice.setText(getString(R.string.ruppe_symbol_x, jsonObject.optString(NetworkUtility.TAGS.TOTAL_EARNED)));
                        double price = Double.parseDouble(jsonObject.optString(NetworkUtility.TAGS.TOTAL_EARNED));
                        DecimalFormat decimalFormat = new DecimalFormat("0.00");
//                        mBinding.textPrice.setText(getString(R.string.rupee_symbol_x, decimalFormat.format(price)));

//                        mBinding.monthlyEarned.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(jsonObject.optString(NetworkUtility.TAGS.MONTHLY_TOTAL))));
//                        mBinding.monthlySaved.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(jsonObject.optString(NetworkUtility.TAGS.MONTHLY_SAVED_TOTAL))));

                        ArrayList<HistoryModel> list = GsonUtility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), HistoryModel[].class);
                        mAdapter.setItems(list);

                        if (list != null && list.size() > 0) {
//                            mFragmentHistoryBinding.layoutMonthSelector.setVisibility(View.VISIBLE);
//                            mBinding.layoutTitle.setVisibility(View.VISIBLE);
//                            mBinding.layoutSummary.setVisibility(View.VISIBLE);
                            errorLoadingHelper.success();
                        } else {
//                            mFragmentHistoryBinding.layoutMonthSelector.setVisibility(View.GONE);
//                            mBinding.layoutTitle.setVisibility(View.GONE);
//                            mBinding.layoutSummary.setVisibility(View.GONE);
                            errorLoadingHelper.failed(null, R.drawable.img_empty_history, null, null);
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentHistoryBinding.getRoot());
                        errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mFragmentHistoryBinding.getRoot());
                        errorLoadingHelper.failed(error_message, 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        // Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallHistoryWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallHistoryWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());

        }
    };

    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            callHistoryWS(superCalendar.getTimeInMillis());
        }
    };
}
