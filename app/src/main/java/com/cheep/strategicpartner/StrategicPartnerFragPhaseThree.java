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
import com.cheep.utils.ErrorLoadingHelper;

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
        if (mStrategicPartnerTaskCreationAct.getSelectedQuestions().length() != 0) {
            isTaskDescriptionVerified = true;
        } else {
            isTaskDescriptionVerified = false;
        }
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
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");

        mFragmentStrategicPartnerPhaseThreeBinding.recycleSelectedService.setLayoutManager(new LinearLayoutManager(mStrategicPartnerTaskCreationAct));
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

}
