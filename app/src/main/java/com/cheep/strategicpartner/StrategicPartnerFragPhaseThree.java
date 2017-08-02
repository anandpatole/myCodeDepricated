package com.cheep.strategicpartner;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
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
    public static final String TAG = "StrategicPartnerFragPhaseThree";
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
        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callWeb();
            }
        });
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
