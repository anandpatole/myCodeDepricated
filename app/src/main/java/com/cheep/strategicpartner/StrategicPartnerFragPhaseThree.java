package com.cheep.strategicpartner;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.databinding.FragmentStrategicPartnerPhaseThreeBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bhavesh on 28/4/17.
 */
public class StrategicPartnerFragPhaseThree extends BaseFragment {
    public static final String TAG = "StrategicPartnerFragPha";
    private FragmentStrategicPartnerPhaseThreeBinding mFragmentStrategicPartnerPhaseThreeBinding;
    ErrorLoadingHelper errorLoadingHelper;
    private StrategicPartnerTaskCreationAct mStrategicPartnerTaskCreationAct;
    private boolean isVerified = false;
    private boolean isTaskDescriptionVerified;
    private String total;
    String addressId = "";
    private String payableAmount;

    @SuppressWarnings("unused")
    public static StrategicPartnerFragPhaseThree newInstance() {
        return new StrategicPartnerFragPhaseThree();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentStrategicPartnerPhaseThreeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_strategic_partner_phase_three, container, false);
        return mFragmentStrategicPartnerPhaseThreeBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
        if (!isVisibleToUser || mStrategicPartnerTaskCreationAct == null) {
            return;
        }

        // Task Description
        if (mStrategicPartnerTaskCreationAct.isAllQuestionAnswer) {
            isTaskDescriptionVerified = true;
        } else {
            isTaskDescriptionVerified = false;
        }
        mFragmentStrategicPartnerPhaseThreeBinding.txtdesc.setSelected(true);
        mFragmentStrategicPartnerPhaseThreeBinding.scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFragmentStrategicPartnerPhaseThreeBinding.scrollView.fullScroll(View.FOCUS_UP);
            }
        }, 5);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getSpannableString("Your order with ", ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString(mStrategicPartnerTaskCreationAct.mBannerImageModel.name, ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString(mStrategicPartnerTaskCreationAct.date + ", " + mStrategicPartnerTaskCreationAct.time
                , ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString(mStrategicPartnerTaskCreationAct.address, ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end), true));

        mFragmentStrategicPartnerPhaseThreeBinding.txtdesc.setText(spannableStringBuilder);

        if (mStrategicPartnerTaskCreationAct.getSelectedSubService() != null)
            mFragmentStrategicPartnerPhaseThreeBinding.recycleSelectedService.setAdapter(new StrategicPartnerPaymentAdapter(mStrategicPartnerTaskCreationAct, mStrategicPartnerTaskCreationAct.getSelectedSubService()));
        int totalAmount = 0;

        ArrayList<QueAnsModel> mList = mStrategicPartnerTaskCreationAct.getSelectedQuestions();
        for (int i = 0; i < mList.size(); i++) {
            QueAnsModel queAnsModel = mList.get(i);
            if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER))
                start_datetime = queAnsModel.answer;
            if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION))
                addressId = queAnsModel.answer;
        }
        for (StrategicPartnerSubCategoryModel model : mStrategicPartnerTaskCreationAct.getSelectedSubService()) {
            List<StrategicPartnerSubCategoryModel.AllSubSubCat> allSubSubCats = model.allSubSubCats;
            for (StrategicPartnerSubCategoryModel.AllSubSubCat allSubSubCat : allSubSubCats) {
                try {
                    totalAmount += Integer.parseInt(allSubSubCat.price);
                } catch (NumberFormatException e) {
                    totalAmount += 0;
                }
            }
        }
        total = String.valueOf(totalAmount);
        mFragmentStrategicPartnerPhaseThreeBinding.txttotal.setText(getString(R.string.ruppe_symbol_x, String.valueOf(total)));
        mFragmentStrategicPartnerPhaseThreeBinding.txtsubtotal.setText(getString(R.string.ruppe_symbol_x, String.valueOf(total)));
        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setText("Pay " + getString(R.string.ruppe_symbol_x, String.valueOf(total)));
        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callWeb();
                Utility.showToast(mStrategicPartnerTaskCreationAct, "Work in progress!");
            }
        });
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end));
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));

        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setEnabled(true);
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCheepCodeDialog();
            }
        });
        mFragmentStrategicPartnerPhaseThreeBinding.imgCheepCodeClose.setVisibility(View.GONE);
        mFragmentStrategicPartnerPhaseThreeBinding.txtpromocode.setText(getString(R.string.ruppe_symbol_x, "" + 0.0));
        mFragmentStrategicPartnerPhaseThreeBinding.imgCheepCodeClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setEnabled(true);
                cheepCode = null;
//                if (TextUtils.isEmpty(actualQuotePrice) == false) {
//                    providerModel.quotePrice = actualQuotePrice;
//                }
//                actualQuotePrice = null;
                mFragmentStrategicPartnerPhaseThreeBinding.imgCheepCodeClose.setVisibility(View.GONE);
                resetPromocodeValue();
            }
        });

    }

    public void resetPromocodeValue() {
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end));
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));
        mFragmentStrategicPartnerPhaseThreeBinding.txtsubtotal.setText(getString(R.string.ruppe_symbol_x, "" + total));
        mFragmentStrategicPartnerPhaseThreeBinding.txttotal.setText(getString(R.string.ruppe_symbol_x, "" + total));
        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setText(getString(R.string.label_pay_fee_v1, "" + total));
        mFragmentStrategicPartnerPhaseThreeBinding.txtpromocode.setText(getString(R.string.ruppe_symbol_x, "" + 0.0));

        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCheepCodeDialog();
            }
        });
    }


    EditText edtCheepcode;
    BottomAlertDialog cheepCodeDialog;

    private void showCheepCodeDialog() {

        View view = View.inflate(mContext, R.layout.dialog_add_promocode, null);
        edtCheepcode = view.findViewById(R.id.edit_cheepcode);
        cheepCodeDialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtCheepcode.getText().toString())) {
                    Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                    return;
                }
                validateCheepCode(edtCheepcode.getText().toString());
            }
        });
        cheepCodeDialog.setTitle(getString(R.string.label_cheepcode));
        cheepCodeDialog.setCustomView(view);
        cheepCodeDialog.showDialog();
    }

    private String cheepCode;

    private void validateCheepCode(String s) {
        cheepCode = s;
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
            return;
        }
        showProgressDialog();
        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();


        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, total);
        mParams.put(NetworkUtility.TAGS.CHEEPCODE, s);
        mParams.put(NetworkUtility.TAGS.CAT_ID, mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.CHECK_CHEEPCODE_FOR_STRATEGIC_PARTNER
                , mCallValidateCheepCodeWSErrorListener
                , mCallValidateCheepCodeWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    Response.Listener mCallValidateCheepCodeWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        if (edtCheepcode != null) {
                            cheepCode = edtCheepcode.getText().toString().trim();
                            cheepCodeDialog.dismiss();

                            total = jsonObject.optString(NetworkUtility.TAGS.QUOTE_AMOUNT);

                            String discount = jsonObject.optString(NetworkUtility.TAGS.DISCOUNT_AMOUNT);
                            String payable = jsonObject.optString(NetworkUtility.TAGS.PAYABLE_AMOUNT);
                            updatePaymentBtn(total, discount, payable);

                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        Utility.showToast(mContext, error_message);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        mStrategicPartnerTaskCreationAct.finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallValidateCheepCodeWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };
    Response.ErrorListener mCallValidateCheepCodeWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();

//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));

        }

    };

    private void updatePaymentBtn(String total, String discount, String payable) {
        this.total = total;
        payableAmount = payable;
        mFragmentStrategicPartnerPhaseThreeBinding.txtpromocode.setText(getString(R.string.ruppe_symbol_x, "" + discount));
        mFragmentStrategicPartnerPhaseThreeBinding.txttotal.setText(getString(R.string.ruppe_symbol_x, "" + payable));
        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setText(getString(R.string.label_pay_fee_v1, payable));
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setEnabled(false);
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setText(cheepCode);
        mFragmentStrategicPartnerPhaseThreeBinding.imgCheepCodeClose.setVisibility(View.VISIBLE);
    }

    public SpannableStringBuilder getSpannableString(String fullstring, int color, boolean isBold) {
        SpannableStringBuilder text = new SpannableStringBuilder(fullstring);
        text.setSpan(new ForegroundColorSpan(color), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            text.setSpan(new StyleSpan(Typeface.BOLD), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");

        mFragmentStrategicPartnerPhaseThreeBinding.recycleSelectedService.setLayoutManager(new LinearLayoutManager(mStrategicPartnerTaskCreationAct));
        if (mStrategicPartnerTaskCreationAct.getSelectedSubService() != null)
            mFragmentStrategicPartnerPhaseThreeBinding.recycleSelectedService.setAdapter(new StrategicPartnerPaymentAdapter(mStrategicPartnerTaskCreationAct, mStrategicPartnerTaskCreationAct.getSelectedSubService()));
    }

    @Override
    public void setListener() {
        Log.d(TAG, "setListener() called");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof StrategicPartnerTaskCreationAct) {
            mStrategicPartnerTaskCreationAct = (StrategicPartnerTaskCreationAct) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.FETCH_SUB_SERVICE_LIST);
    }

    /**
     * This method would return whether the stage is verified or not
     *
     * @return
     */

    public boolean isVerified() {
        return isVerified;
    }

    String start_datetime = "";

    private void callWeb() {
        JsonArray selectedServiceArray = new JsonArray();
        ArrayList<StrategicPartnerSubCategoryModel> list = mStrategicPartnerTaskCreationAct.getSelectedSubService();
        for (int i = 0; i < list.size(); i++) {
            StrategicPartnerSubCategoryModel model = list.get(i);
            for (int j = 0; j < model.allSubSubCats.size(); j++) {
                StrategicPartnerSubCategoryModel.AllSubSubCat allSubSubCat = model.allSubSubCats.get(j);
                if (allSubSubCat.isSelected) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("subcategory_id", model.catId);
                    obj.addProperty("sub_sub_cat_id", allSubSubCat.subSubCatId);
                    obj.addProperty("price", allSubSubCat.price);
                    selectedServiceArray.add(obj);
                }
            }
        }
        String sub_category_detail = selectedServiceArray.toString();

        ArrayList<QueAnsModel> mList = mStrategicPartnerTaskCreationAct.getSelectedQuestions();
        for (int i = 0; i < mList.size(); i++) {
            QueAnsModel queAnsModel = mList.get(i);
            if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER))
                start_datetime = queAnsModel.answer;
            if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION))
                addressId = queAnsModel.answer;
        }
        JsonArray quesArray = new JsonArray();
        for (int i = 0; i < mList.size(); i++) {
            QueAnsModel queAnsModel = mList.get(i);
            if (!queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_DATE_PICKER)
                    && !queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER)
                    && !queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION)
                    && !queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("question_id", queAnsModel.questionId);
                jsonObject.addProperty("answer", queAnsModel.answer);
                quesArray.add(jsonObject);
            }
        }

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        String question_detail = quesArray.toString();
        Log.e(TAG, " addressId >>" + addressId);
        Log.e(TAG, " city_id >>" + userDetails.CityID);
        Log.e(TAG, " cat_id >>" + mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);
        Log.e(TAG, " sub_category_detail >>" + sub_category_detail);
        Log.e(TAG, " question_detail >>" + question_detail);
        Log.e(TAG, " start_datetime >>" + start_datetime);


        // Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        String txnid = System.currentTimeMillis() + "";
        // Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, start_datetime);
        mParams.put(NetworkUtility.TAGS.SUB_CATEGORY_DETAIL, sub_category_detail);
        mParams.put(NetworkUtility.TAGS.QUESTION_DETAIL, question_detail);
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, total + "");
        mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, payableAmount);
        mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, txnid);
        Log.e(TAG, mParams + "");

    }

}
