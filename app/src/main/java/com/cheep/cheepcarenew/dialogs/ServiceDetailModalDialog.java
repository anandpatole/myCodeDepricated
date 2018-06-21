package com.cheep.cheepcarenew.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.TaskCreationActivity;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ServiceDetailModalDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = ServiceDetailModalDialog.class.getSimpleName();
    private RelativeLayout rootLayout;
    private ProgressDialog mProgressDialog;
    static Context mContexts;
    private TextView tvData, tvSoundsGood;
    UserDetails userDetails;

    static JobCategoryModel models;

    public ServiceDetailModalDialog() {
        // Required empty public constructor
    }

    public static ServiceDetailModalDialog newInstance(Context mContext, JobCategoryModel model) {
        ServiceDetailModalDialog fragment = new ServiceDetailModalDialog();

        Bundle args = new Bundle();
        mContexts = mContext;
        models = model;
//        args.putString(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(packageDetail));
//        args.putString(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(cityDetail));
//        args.putString(Utility.Extra.DATA_3, GsonUtility.getJsonStringFromObject(comparisionChartModel));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimationZoomInOut;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_service_detail, container, false);
        initView(view);
        //callGetPackageDetailModelDataWS();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        userDetails = PreferenceUtility.getInstance(getContext()).getUserDetails();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setCanceledOnTouchOutside(true);
        this.setCancelable(true);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.AlertAnimation;
        return dialog;

    }

    private void initView(View view) {
        tvData = view.findViewById(R.id.tv_data);
        rootLayout = view.findViewById(R.id.rootLayout);
        tvSoundsGood = view.findViewById(R.id.tv_sounds_good);
        tvSoundsGood.setOnClickListener(this);
    }

    //View.OnClickListener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_sounds_good:
                TaskCreationActivity.getInstance(mContexts, models);
                dismiss();
                //PaymentSummaryCheepCareActivity.newInstance(getContext());
//                if (getArguments() != null) {
//                    PackageDetail packageDetail = (PackageDetail) GsonUtility.getObjectFromJsonString(getArguments().getString(Utility.Extra.DATA), PackageDetail.class);
//                    CareCityDetail careCityDetail = (CareCityDetail) GsonUtility.getObjectFromJsonString(getArguments().getString(Utility.Extra.DATA_2), CareCityDetail.class);
//                    ComparisionChartModel comparisionChartModel = (ComparisionChartModel) GsonUtility.getObjectFromJsonString(getArguments().getString(Utility.Extra.DATA_3), ComparisionChartModel.class);
//                    if (packageDetail != null) {
//                        AddressActivity.newInstance(getContext(), packageDetail, careCityDetail, comparisionChartModel);
//                        dismiss();
//                    }
//                }

                break;
        }

    }


    /************************************************************************************************
     **********************************Calling Webservice ********************************************
     ************************************************************************************************/

    protected void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressDialog.show();
    }

    /**
     * Show Progress Dialog
     */
    public void showProgressDialog() {
        showProgressDialog(getString(R.string.label_please_wait));
    }

    /**
     * Close Progress Dialog
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }

    private void callGetPackageDetailModelDataWS() {
        if (!Utility.isConnected(getContext())) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, rootLayout);
            return;
        }

        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(getContext()).getXAPIKey());

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.GET_BADGE_MESSAGE
                , mCallGetCityCareDetailsWSErrorListener
                , mCallGetCityCareDetailsWSResponseListener
                , mHeaderParams
                , null
                , null);

        Volley.getInstance(getContext()).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.GET_BADGE_MESSAGE);
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
                        JSONArray data = jsonObject.getJSONArray(NetworkUtility.TAGS.DATA);
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            tvData.setText(Html.fromHtml(obj.getString(NetworkUtility.TAGS.TEXT)));
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), rootLayout);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, rootLayout);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(getContext(), true, statusCode);

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
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), rootLayout);
        }
    };

}
