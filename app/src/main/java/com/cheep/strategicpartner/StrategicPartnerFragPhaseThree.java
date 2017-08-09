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

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.databinding.FragmentStrategicPartnerPhaseThreeBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.UserDetails;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

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
        int total = 0;

        for (StrategicPartnerSubCategoryModel model : mStrategicPartnerTaskCreationAct.getSelectedSubService()) {
            List<StrategicPartnerSubCategoryModel.AllSubSubCat> allSubSubCats = model.allSubSubCats;
            for (StrategicPartnerSubCategoryModel.AllSubSubCat allSubSubCat : allSubSubCats) {
                try {
                    total += Integer.parseInt(allSubSubCat.price);
                } catch (NumberFormatException e) {
                    total += 0;
                }
            }
        }
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
//        if (!TextUtils.isEmpty(providerModel.quotePrice)) {
//            double taskPaidAmount = getQuotePriceInInteger(providerModel.quotePrice);
//            double additionalCharges = 0;
//            double promocodeValue = 0;
//            double additionalPaidAmount = 0;
//            if (!TextUtils.isEmpty(taskDetailModel.additionalQuoteAmount)) {
//                additionalCharges = getQuotePriceInInteger(taskDetailModel.additionalQuoteAmount);
//            }
//
//            if (!TextUtils.isEmpty(taskDetailModel.task_total_amount)) {
//                double task_total_amount = 0;
//                double taskPaidAmountTotal = 0;
//                if (!TextUtils.isEmpty(taskDetailModel.taskPaidAmount)) {
//                    taskPaidAmountTotal = getQuotePriceInInteger(taskDetailModel.taskPaidAmount);
//                }
//                task_total_amount = getQuotePriceInInteger(taskDetailModel.task_total_amount);
//                promocodeValue = task_total_amount - taskPaidAmountTotal;
//
//            }
//
//            double subTotal = (taskPaidAmount + additionalCharges);
//            double totalPayment = (taskPaidAmount + additionalCharges) - promocodeValue;
//            mFragmentStrategicPartnerPhaseThreeBinding.txtprofee.setText(getString(R.string.ruppe_symbol_x, "" + taskPaidAmount));
//            mFragmentStrategicPartnerPhaseThreeBinding.txtsubtotal.setText(getString(R.string.ruppe_symbol_x, "" + subTotal));
//            mFragmentStrategicPartnerPhaseThreeBinding.txttotal.setText(getString(R.string.ruppe_symbol_x, "" + totalPayment));
//            mFragmentStrategicPartnerPhaseThreeBinding.textPay.setText(getString(R.string.label_pay_fee_v1, "" + totalPayment));
//            mFragmentStrategicPartnerPhaseThreeBinding.txtpromocode.setText(getString(R.string.ruppe_symbol_x, "" + promocodeValue));
//
//            mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    showCheepCodeDialog();
//                }
//            });
//        }
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
        if (edtCheepcode != null) {
            cheepCode = edtCheepcode.getText().toString().trim();


            cheepCodeDialog.dismiss();

            String total = "yyyyyy";
            String discount = "zzzz";
            String payable = "sdsdad";
            updatePaymentBtn(total, discount, payable);

        }
    }

    private void updatePaymentBtn(String total, String discount, String payable) {
        // setting payable amount as quote price to pay.
//        providerModel.quotePrice = payable;
//        mActivityJobSummaryBinding.btnPay.setText(getString(R.string.label_pay_X_X_X, total, discount, payable));
//        @change only need to show payable amount
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

    private void callWeb() {
        final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        String city_id = userDetails.CityID;
        String cat_id = mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id;
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

        String address_id = "";
        String start_datetime = "";
        ArrayList<QueAnsModel> mList = mStrategicPartnerTaskCreationAct.getSelectedQuestions();
        for (int i = 0; i < mList.size(); i++) {
            QueAnsModel queAnsModel = mList.get(i);
            if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER))
                start_datetime = queAnsModel.answer;
            if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION))
                address_id = queAnsModel.answer;
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

        String question_detail = quesArray.toString();
        Log.e(TAG, " address_id >>" + address_id);
        Log.e(TAG, " city_id >>" + city_id);
        Log.e(TAG, " cat_id >>" + cat_id);
        Log.e(TAG, " sub_category_detail >>" + sub_category_detail);
        Log.e(TAG, " question_detail >>" + question_detail);
        Log.e(TAG, " start_datetime >>" + start_datetime);
    }

}
