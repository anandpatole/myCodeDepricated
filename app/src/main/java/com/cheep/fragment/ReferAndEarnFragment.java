package com.cheep.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.FragmentReferAndEarnBinding;
import com.cheep.dialogs.AcknowledgementDialogWithoutProfilePic;
import com.cheep.dialogs.ReferAndEarnDialogKnowMore;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by meet on 7/9/17.
 */

public class ReferAndEarnFragment extends BaseFragment {

    public static final String TAG = "ReferAndEarnFragment";

    private DrawerLayoutInteractionListener mListener;
    private FragmentReferAndEarnBinding mfragmentReferAndEarnBinding;
    private UserDetails mUserDetails;


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
        mfragmentReferAndEarnBinding.tvReferralCode.setText(PreferenceUtility.getInstance(mContext).getUserDetails().refer_code);
        callGetReferBalance();

        SpannableStringBuilder know_more = new SpannableStringBuilder(getString(R.string.label_when_they_use_cheep_get_100));
        know_more.setSpan(new RelativeSizeSpan(.6f), know_more.length() - 9, know_more.length(), 0);
        know_more.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.splash_gradient_end)), know_more.length() - 9, know_more.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View view) {
                // Add next activity code here
                ReferAndEarnDialogKnowMore mReferAndEarnDialogKnowMore = new ReferAndEarnDialogKnowMore();
                mReferAndEarnDialogKnowMore.show(getChildFragmentManager(), AcknowledgementDialogWithoutProfilePic.TAG);

            }
        };
        know_more.setSpan(clickableSpan, know_more.length() - 9, know_more.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mfragmentReferAndEarnBinding.tvUserGetMoney.setText(know_more, TextView.BufferType.SPANNABLE);
        mfragmentReferAndEarnBinding.tvUserGetMoney.setMovementMethod(LinkMovementMethod.getInstance());


    }

    @Override
    public void setListener() {
        mfragmentReferAndEarnBinding.tvInviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
                    //callValidateReferCode();
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.label_share_subject));
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.label_refer_and_earn_share_body, BuildConfig.REFER_AND_SHARE_APP_URL, PreferenceUtility.getInstance(mContext).getUserDetails().refer_code));
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.label_share_via)));
                }

            }
        });
    }

    private void callGetReferBalance() {
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
        Map<String, String> mParams = new HashMap<>();
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.REFER_BALANCE
                , mCallGetReferBalanceErrorListener
                , mCallGetReferBalanceWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.REFER_BALANCE);

    }

    Response.Listener mCallGetReferBalanceWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                String REFERRAL_BALANCE = (jsonObject.optJSONObject(NetworkUtility.TAGS.DATA)).optString(NetworkUtility.TAGS.WALLET_BALANCE);
                String REFERRAL_COUNT = (jsonObject.optJSONObject(NetworkUtility.TAGS.DATA)).optString(NetworkUtility.TAGS.REFER_COUNT);
                if (Double.parseDouble(REFERRAL_BALANCE) > 0) {
                    mfragmentReferAndEarnBinding.refereBalanceAndCount.setVisibility(View.VISIBLE);
                    if (Integer.parseInt(REFERRAL_COUNT) == 1 || Integer.parseInt(REFERRAL_COUNT) == 0) {
                        mfragmentReferAndEarnBinding.refereBalanceAndCount.setText(getString(R.string.label_you_have_earned, REFERRAL_BALANCE, REFERRAL_COUNT, "referral"));
                    } else
                        mfragmentReferAndEarnBinding.refereBalanceAndCount.setText(getString(R.string.label_you_have_earned, REFERRAL_BALANCE, REFERRAL_COUNT, "referrals"));
                }
                Log.e(TAG, REFERRAL_BALANCE);
                Log.e(TAG, REFERRAL_COUNT);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                mCallGetReferBalanceWSResponseListener.onResponse(new VolleyError(e.getMessage()));
            }
        }
    };
    Response.ErrorListener mCallGetReferBalanceErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
        }
    };


/*
    private void callValidateReferCode() {
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        Map<String,String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.VALID_REFER_CODE,PreferenceUtility.getInstance(mContext).getUserDetails().refer_code);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.VALIDATE_REFER_CODE
                , mCallValidateReferCodeWSErrorListener
                , mCallValidateReferCodeWSResponseListener
                , mHeaderParams
                , mParams
                , null);
    }

    Response.Listener mCallValidateReferCodeWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
        }
            catch (JSONException e) {
                e.printStackTrace();
                mCallValidateReferCodeWSResponseListener.onResponse(new VolleyError(e.getMessage()));
            }
    }
    };

     Response.ErrorListener mCallValidateReferCodeWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
        }
    };*/

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

