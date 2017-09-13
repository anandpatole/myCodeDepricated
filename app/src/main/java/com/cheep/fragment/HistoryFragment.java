package com.cheep.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.adapter.HistoryRecyclerViewAdapter;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.FragmentHistoryBinding;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.model.HistoryModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
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
 * Created by pankaj on 9/7/16.
 */
public class HistoryFragment extends BaseFragment {

    public static final String TAG = "HistoryFragment";

    private DrawerLayoutInteractionListener mListener;
    private FragmentHistoryBinding mFragmentHistoryBinding;
    private HistoryRecyclerViewAdapter.HistoryItemInteractionListener mHistoryListener;
    private SuperCalendar superCalendar;
    HistoryRecyclerViewAdapter favouriteRecyclerViewAdapter;
    ErrorLoadingHelper errorLoadingHelper;

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentHistoryBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false);
        setHasOptionsMenu(true);
        return mFragmentHistoryBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);
        if (context instanceof HistoryRecyclerViewAdapter.HistoryItemInteractionListener) {
            mHistoryListener = (HistoryRecyclerViewAdapter.HistoryItemInteractionListener) context;
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
        mHistoryListener = null;
        mListener = null;
        super.onDetach();

        // Cancel the asynctask so it won't crash in case fragment is getting destroyed
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.PAYMENT_HISTORY);
    }

    @Override
    public void initiateUI() {

        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            //Setting up toolbar
            ((AppCompatActivity) mContext).setSupportActionBar(mFragmentHistoryBinding.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        }

        errorLoadingHelper = new ErrorLoadingHelper(mFragmentHistoryBinding.commonRecyclerView.recyclerView);

        mFragmentHistoryBinding.textTitle.setText(getString(R.string.label_favourites));
        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mFragmentHistoryBinding.toolbar);

        mFragmentHistoryBinding.commonRecyclerView.swipeRefreshLayout.setEnabled(false);

        mFragmentHistoryBinding.textTitle.setText(getString(R.string.label_payment_history));

        mFragmentHistoryBinding.textPrice.setText(getString(R.string.rupee_symbol_x_space, Utility.EMPTY_STRING));

        //Setting adapter on recycler view
        favouriteRecyclerViewAdapter = new HistoryRecyclerViewAdapter(mHistoryListener);
        mFragmentHistoryBinding.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mFragmentHistoryBinding.commonRecyclerView.recyclerView.setAdapter(favouriteRecyclerViewAdapter);

        mFragmentHistoryBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_20dp)));

        superCalendar = SuperCalendar.getInstance();

        //changing month name
        mFragmentHistoryBinding.textMonth.setText(superCalendar.format(SuperCalendar.SuperFormatter.MONTH_JAN));
        mFragmentHistoryBinding.textYear.setText(superCalendar.format(SuperCalendar.SuperFormatter.YEAR_4_DIGIT));


//        mFragmentHistoryBinding.layoutMonthSelector.setVisibility(View.GONE);
        mFragmentHistoryBinding.layoutTitle.setVisibility(View.GONE);
        mFragmentHistoryBinding.layoutSummary.setVisibility(View.GONE);

        errorLoadingHelper.showLoading();

        callHistoryWS(superCalendar.getTimeInMillis());
    }

    @Override
    public void setListener() {
        mFragmentHistoryBinding.iconLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                superCalendar.getCalendar().add(Calendar.MONTH, -1);
                //changing month name
                mFragmentHistoryBinding.textMonth.setText(superCalendar.format(SuperCalendar.SuperFormatter.MONTH_JAN));
                mFragmentHistoryBinding.textYear.setText(superCalendar.format(SuperCalendar.SuperFormatter.YEAR_4_DIGIT));
                callHistoryWS(superCalendar.getTimeInMillis());
            }
        });
        mFragmentHistoryBinding.iconRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                superCalendar.getCalendar().add(Calendar.MONTH, 1);
                //changing month name
                mFragmentHistoryBinding.textMonth.setText(superCalendar.format(SuperCalendar.SuperFormatter.MONTH_JAN));
                mFragmentHistoryBinding.textYear.setText(superCalendar.format(SuperCalendar.SuperFormatter.YEAR_4_DIGIT));
                callHistoryWS(superCalendar.getTimeInMillis());
            }
        });
    }

    private void updateUI() {

    }

    SuperCalendar calendarChanger;

    /**
     * Call History WS
     *
     * @param timestamp
     */
    private void callHistoryWS(long timestamp) {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentHistoryBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            mFragmentHistoryBinding.lnTotalMoneySpent.setVisibility(View.GONE);
            mFragmentHistoryBinding.rlMonthSelector.setVisibility(View.GONE);
            mFragmentHistoryBinding.layoutTitle.setVisibility(View.GONE);
            mFragmentHistoryBinding.layoutSummary.setVisibility(View.GONE);
            errorLoadingHelper.failed(null, R.drawable.img_empty_history, null, null);
            return;
        } else {
            mFragmentHistoryBinding.lnTotalMoneySpent.setVisibility(View.VISIBLE);
            mFragmentHistoryBinding.rlMonthSelector.setVisibility(View.VISIBLE);
        }

        if (calendarChanger == null)
            calendarChanger = SuperCalendar.getInstance();

        calendarChanger.setLocaleTimeZone();
        calendarChanger.setTimeInMillis(timestamp);
        calendarChanger.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

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
                        mFragmentHistoryBinding.textPrice.setText(getString(R.string.rupee_symbol_x, decimalFormat.format(price)));

                        mFragmentHistoryBinding.monthlyEarned.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(jsonObject.optString(NetworkUtility.TAGS.MONTHLY_TOTAL))));
                        mFragmentHistoryBinding.monthlySaved.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(jsonObject.optString(NetworkUtility.TAGS.MONTHLY_SAVED_TOTAL))));

                        ArrayList<HistoryModel> list = Utility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), HistoryModel[].class);
                        favouriteRecyclerViewAdapter.setItems(list);

                        if (list != null && list.size() > 0) {
//                            mFragmentHistoryBinding.layoutMonthSelector.setVisibility(View.VISIBLE);
                            mFragmentHistoryBinding.layoutTitle.setVisibility(View.VISIBLE);
                            mFragmentHistoryBinding.layoutSummary.setVisibility(View.VISIBLE);
                            errorLoadingHelper.success();
                        } else {
//                            mFragmentHistoryBinding.layoutMonthSelector.setVisibility(View.GONE);
                            mFragmentHistoryBinding.layoutTitle.setVisibility(View.GONE);
                            mFragmentHistoryBinding.layoutSummary.setVisibility(View.GONE);
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


            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentHistoryBinding.getRoot());

        }
    };

    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            callHistoryWS(superCalendar.getTimeInMillis());
        }
    };

}