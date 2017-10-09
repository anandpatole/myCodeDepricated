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
        //Provide callback to activity to link drawer layout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mfragmentReferAndEarnBinding.toolbar);

        mfragmentReferAndEarnBinding.textTitle.setText(getString(R.string.label_refer_and_earn));

        mfragmentReferAndEarnBinding.tvReferralCode.setText(PreferenceUtility.getInstance(mContext).getUserDetails().refer_code);
        callGetReferBalance();

        // set clickable spannable for know more button
        SpannableStringBuilder know_more = new SpannableStringBuilder(getString(R.string.label_when_they_use_cheep_get_50));
        know_more.setSpan(new RelativeSizeSpan(.6f), know_more.length() - 9, know_more.length(), 0);
        know_more.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.splash_gradient_end)), know_more.length() - 9, know_more.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(View view) {
                // open model window on click of know more
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
        // on click of invite friends
        // open all sharing intent for messaging
        mfragmentReferAndEarnBinding.tvInviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.label_share_subject));
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.label_refer_and_earn_share_body, BuildConfig.REFER_AND_SHARE_APP_URL, PreferenceUtility.getInstance(mContext).getUserDetails().refer_code));
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.label_share_via)));
                }

            }
        });
    }

    /**
     * Get user refer balance and refer counting and earning
     */
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

            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            hideProgressDialog();
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        String referralBalance = (jsonObject.optJSONObject(NetworkUtility.TAGS.DATA)).optString(NetworkUtility.TAGS.WALLET_BALANCE);
                        String referralCount = (jsonObject.optJSONObject(NetworkUtility.TAGS.DATA)).optString(NetworkUtility.TAGS.REFER_COUNT);
                        String maxDiscountAmount = (jsonObject.optJSONObject(NetworkUtility.TAGS.DATA)).optString(NetworkUtility.TAGS.MAX_REFER_DISCOUNT);
                        if (Double.parseDouble(referralBalance) > 0) {
                            mfragmentReferAndEarnBinding.tvRefereBalanceAndCount.setVisibility(View.VISIBLE);
                            mfragmentReferAndEarnBinding.tvUserBalance.setVisibility(View.VISIBLE);
                            double totalEarning = 0;
                            try {
                                totalEarning = Double.parseDouble(referralCount) * Double.parseDouble(maxDiscountAmount);
                            } catch (NumberFormatException ignored) {
                            }
                            if (Integer.parseInt(referralCount) == 1 || Integer.parseInt(referralCount) == 0) {
                                mfragmentReferAndEarnBinding.tvRefereBalanceAndCount.setText(getString(R.string.label_you_have_earned, totalEarning + "", referralCount, "referral"));
                            } else {
                                mfragmentReferAndEarnBinding.tvRefereBalanceAndCount.setText(getString(R.string.label_you_have_earned, totalEarning + "", referralCount, "referrals"));
                            }
                            mfragmentReferAndEarnBinding.tvUserBalance.setText(getString(R.string.label_your_current_balance, referralBalance));
                        }
                        Log.e(TAG, referralBalance);
                        Log.e(TAG, referralCount);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mfragmentReferAndEarnBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mfragmentReferAndEarnBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallGetReferBalanceErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }

    };
    Response.ErrorListener mCallGetReferBalanceErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
        }
    };

}

