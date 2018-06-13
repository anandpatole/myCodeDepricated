package com.cheep.cheepcarenew.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.cheepcarenew.activities.PaymentSummaryActivityCheepCare;
import com.cheep.model.ComparisionChartModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ComparisionChartFragmentDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = ComparisionChartFragmentDialog.class.getSimpleName();
    private ArrayList<ComparisionChartModel> comparisionChartsList;
    private ComparisionChatAdapter adapter;
    private RecyclerView recyclerView;
    private RelativeLayout rootLayout;
    private ProgressDialog mProgressDialog;
    private TextView tvBookKnowPremium,tvBookKnowCare;

    public static ComparisionChartFragmentDialog newInstance(String param1, String param2) {
        ComparisionChartFragmentDialog fragment = new ComparisionChartFragmentDialog();
        Bundle args = new Bundle();
       /* args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*/
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
        View view = inflater.inflate(R.layout.fragment_comparsion_chart_fragment_dialog, container, false);
        initView(view);
        setListener();

        callGetPackageFeatureListDetailWS();

        if (comparisionChartsList == null) {
            comparisionChartsList = new ArrayList<>();
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

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
    private void initView(View view){
        rootLayout = view.findViewById(R.id.rootLayout);
        recyclerView = view.findViewById(R.id.recycler_view);

        tvBookKnowPremium = view.findViewById(R.id.tv_book_know_premimu);
        tvBookKnowCare = view.findViewById(R.id.tv_book_know_care);

    }
    private void setListener(){
        tvBookKnowPremium.setOnClickListener(this);
        tvBookKnowCare.setOnClickListener(this);
    }

    private void loadData() {
        recyclerView.setHasFixedSize(true);
        adapter = new ComparisionChatAdapter();
        recyclerView.setAdapter(adapter);
    }

    //View.OnClickListener
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_book_know_premimu:
                PaymentSummaryActivityCheepCare.newInstance(getContext());
                break;
            case R.id.tv_book_know_care:
                PaymentSummaryActivityCheepCare.newInstance(getContext());
                break;
        }
    }

    public class ComparisionChatAdapter extends RecyclerView.Adapter<ComparisionChatAdapter.ComparisionChatViewHolder> {

        @NonNull
        @Override
        public ComparisionChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.cell_comparision_chart_dialog, parent, false);
            return new ComparisionChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ComparisionChatViewHolder holder, int position) {
            final ComparisionChartModel model = comparisionChartsList.get(position);
            holder.tvBenefitsFeatures.setText(model.feature);
            holder.tvCheepCarePackage.setText(model.normal);
            if (position % 2 == 1) {
                holder.linear.setBackgroundColor(Color.parseColor("#06FFCC01"));
            } else {
                holder.linear.setBackgroundColor(Color.parseColor("#20646460"));
            }

            if (model.premium.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                holder.imagePremium.setVisibility(View.VISIBLE);
                holder.tvPremium.setVisibility(View.GONE);
                holder.imagePremium.setBackgroundResource(R.drawable.verified_icon);
            } else if (model.premium.equalsIgnoreCase(Utility.BOOLEAN.NO)) {
                holder.imagePremium.setVisibility(View.VISIBLE);
                holder.tvPremium.setVisibility(View.GONE);
                holder.imagePremium.setBackgroundResource(R.drawable.cancelled);
            } else {
                holder.tvPremium.setVisibility(View.VISIBLE);
                holder.imagePremium.setVisibility(View.GONE);
                holder.tvPremium.setText(model.premium);

            }
        }

        @Override
        public int getItemCount() {

            return comparisionChartsList.size();

        }

        class ComparisionChatViewHolder extends RecyclerView.ViewHolder {


            private TextView tvBenefitsFeatures;
            private TextView tvCheepCarePackage;
            private LinearLayout linear;
            private ImageView imagePremium;
            private TextView tvPremium;


            ComparisionChatViewHolder(View itemView) {
                super(itemView);
                tvBenefitsFeatures = itemView.findViewById(R.id.tv_benefits_features);
                tvCheepCarePackage = itemView.findViewById(R.id.tv_cheep_care_package);
                linear = itemView.findViewById(R.id.linear);
                imagePremium = itemView.findViewById(R.id.imagePremium);
                tvPremium = itemView.findViewById(R.id.tvPremium);

            }
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

    private void callGetPackageFeatureListDetailWS() {
        if (!Utility.isConnected(getContext())) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, rootLayout);
            return;
        }

        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(getContext()).getXAPIKey());

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.GET_PACKAGE_FEATURE_LIST
                , mCallGetCityCareDetailsWSErrorListener
                , mCallGetCityCareDetailsWSResponseListener
                , mHeaderParams
                , null
                , null);

        Volley.getInstance(getContext()).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.GET_PACKAGE_FEATURE_LIST);
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
                            ComparisionChartModel cityModel = new ComparisionChartModel(obj);
                            comparisionChartsList.add(cityModel);
                        }
                        loadData();

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
