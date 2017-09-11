package com.cheep.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.HomeActivity;
import com.cheep.adapter.FAQRecyclerViewAdapter;
import com.cheep.databinding.FragmentFaqBinding;
import com.cheep.databinding.FragmentReferAndEarnBinding;
import com.cheep.dialogs.AcknowledgementDialogWithoutProfilePic;
import com.cheep.dialogs.ReferAndEarnDialogKnowMore;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.model.FAQModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.HotlineHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by meet on 7/9/17.
 */

public class ReferAndEarnFragment extends BaseFragment {

    public static final String TAG = "ReferAndEarnFragment";

    private DrawerLayoutInteractionListener mListener;
    private FragmentReferAndEarnBinding mfragmentReferAndEarnBinding;

    public static ReferAndEarnFragment newInstance() {
        ReferAndEarnFragment fragment = new ReferAndEarnFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mfragmentReferAndEarnBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_refer_and_earn, container, false);
        setHasOptionsMenu(true);
        return mfragmentReferAndEarnBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);
        if (context instanceof DrawerLayoutInteractionListener) {
            mListener = (DrawerLayoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");
        super.onDetach();
    }

    @Override
    public void initiateUI() {
        //Setting up toolbar
        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            ((AppCompatActivity) mContext).setSupportActionBar(mfragmentReferAndEarnBinding.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        }
        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mfragmentReferAndEarnBinding.toolbar);
        mfragmentReferAndEarnBinding.textTitle.setText(getString(R.string.label_refer_and_earn));


        SpannableStringBuilder know_more = new SpannableStringBuilder(getString(R.string.label_when_they_use_cheep_get_100));
        know_more.setSpan(new RelativeSizeSpan(.7f), know_more.length()-9, know_more.length(), 0);
        know_more.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.splash_gradient_end)), know_more.length()-9, know_more.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // Add next activity code here
                ReferAndEarnDialogKnowMore mReferAndEarnDialogKnowMore = new ReferAndEarnDialogKnowMore();
                mReferAndEarnDialogKnowMore.show(getChildFragmentManager(), AcknowledgementDialogWithoutProfilePic.TAG);

            }
        };
        know_more.setSpan(clickableSpan, know_more.length()-9, know_more.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mfragmentReferAndEarnBinding.tvUserGetMoney.setText(know_more, TextView.BufferType.SPANNABLE);
        mfragmentReferAndEarnBinding.tvUserGetMoney.setMovementMethod(LinkMovementMethod.getInstance());


    }

    @Override
    public void setListener() {

    }

    /*************************************************************************************************************
     *************************************************************************************************************
     *****************************************Webservice Integration [End]**************************************
     *************************************************************************************************************
     */


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// PageContent[START]/////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////




    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// PageContent[End]/////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**************************************************************************************************************
     * *************************************************************************************************************
     * *****************************************Webservice Integration [End]**************************************
     * *************************************************************************************************************
     ************************************************************************************************************/


}

