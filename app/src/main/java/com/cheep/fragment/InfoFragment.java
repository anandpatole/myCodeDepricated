package com.cheep.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.databinding.FragmentInfoBinding;
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
 * Created by pankaj on 9/27/16.
 */

public class InfoFragment extends BaseFragment {

    public static final String TAG = "InfoFragment";

    private DrawerLayoutInteractionListener mListener;
    private FragmentInfoBinding mFragmentInfoBinding;
    private String pageIDType;

    public static InfoFragment newInstance(String type) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(Utility.Extra.INFO_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentInfoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_info, container, false);
        setHasOptionsMenu(true);
        return mFragmentInfoBinding.getRoot();
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
        mListener = null;
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.PAGE_CONTENT);
        super.onDetach();
    }

    @Override
    void initiateUI() {
        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            //Setting up toolbar
            ((AppCompatActivity) mContext).setSupportActionBar(mFragmentInfoBinding.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        }

        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mFragmentInfoBinding.toolbar);

        if (getArguments().getString(Utility.Extra.INFO_TYPE, NetworkUtility.TAGS.PAGEID_TYPE.TERMS).equals(NetworkUtility.TAGS.PAGEID_TYPE.TERMS)) {
            pageIDType = NetworkUtility.TAGS.PAGEID_TYPE.TERMS;
            mFragmentInfoBinding.textTitle.setText(getString(R.string.label_terms));
        } else {
            pageIDType = NetworkUtility.TAGS.PAGEID_TYPE.PRIVACY;
            mFragmentInfoBinding.textTitle.setText(getString(R.string.label_privacy_policy));
        }

        mFragmentInfoBinding.progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(mContext,
                        R.color.splash_gradient_end), PorterDuff.Mode.SRC_IN);

        //Call Webservice for getting information
        callFetchContentWS();

    }

    @Override
    void setListener() {

    }

    /**************************************************************************************************************
     * *************************************************************************************************************
     * *****************************************Webservice Integration [End]**************************************
     * *************************************************************************************************************
     ************************************************************************************************************/


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// PageContent[START]/////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call Login WS Key Webservice
     */
    private void callFetchContentWS() {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentInfoBinding.getRoot());
            showErrorMessage(getString(R.string.no_internet));
            return;
        }

        //Show Progress
        showProgressBar(true);

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();

        //Checking if user is logged in then send userid because this fragment also opens from SignupActivity->(InfoActivity)
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
        }

        //Checking if new XAPI Key is not null then send that else send default apk key from BuildConfig
        if (PreferenceUtility.getInstance(mContext).getXAPIKey() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        } else {
//            mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, BuildConfig.X_API_KEY);
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.PAGE_ID, pageIDType);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.PAGE_CONTENT
                , mCallFetchContentWSErrorListener
                , mCallFetchContentWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

    }

    /**
     * Listeners for tracking Webservice calls
     */
    Response.Listener mCallFetchContentWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        String content = jsonObject.getString(NetworkUtility.TAGS.DATA);
                        /*Spanned result;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            result = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT);
                        } else {
                            result = Html.fromHtml(content);
                        }
                        mFragmentInfoBinding.textInfo.setText(result.toString());*/

                        mFragmentInfoBinding.webview.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentInfoBinding.getRoot());
                        showErrorMessage(getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mFragmentInfoBinding.getRoot());
                        showErrorMessage(getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        ;
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallFetchContentWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            showProgressBar(false);
        }
    };

    Response.ErrorListener mCallFetchContentWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            showProgressBar(false);
            showErrorMessage(getString(R.string.label_something_went_wrong));

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentInfoBinding.getRoot());
        }
    };
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// PageContent[End]/////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**************************************************************************************************************
     * *************************************************************************************************************
     * *****************************************Webservice Integration [End]**************************************
     * *************************************************************************************************************
     ************************************************************************************************************/

    private void showProgressBar(boolean isVisible) {
        mFragmentInfoBinding.progressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mFragmentInfoBinding.webview.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        mFragmentInfoBinding.tvErrorMessage.setVisibility(View.GONE);
    }

    /**
     * Showing and hiding Views based on Progressbar
     *
     * @param message: Message that we want to display to user
     */
    private void showErrorMessage(String message) {
        mFragmentInfoBinding.progressBar.setVisibility(View.GONE);
        mFragmentInfoBinding.tvErrorMessage.setVisibility(View.VISIBLE);
        mFragmentInfoBinding.tvErrorMessage.setText(message);
        mFragmentInfoBinding.webview.setVisibility(View.GONE);
    }
}
