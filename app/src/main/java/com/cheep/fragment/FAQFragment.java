package com.cheep.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.HomeActivity;
import com.cheep.adapter.FAQRecyclerViewAdapter;
import com.cheep.databinding.FragmentFaqBinding;
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
 * Created by pankaj on 9/27/16.
 */

public class FAQFragment extends BaseFragment {

    public static final String TAG = FAQFragment.class.getSimpleName();

    private DrawerLayoutInteractionListener mListener;
    private FragmentFaqBinding mFragmentFaqBinding;
    private FAQRecyclerViewAdapter.FAQItemInteractionListener mFAQInteractionListener;

    public static FAQFragment newInstance() {
        FAQFragment fragment = new FAQFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentFaqBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_faq, container, false);
        setHasOptionsMenu(true);
        return mFragmentFaqBinding.getRoot();
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
        if (context instanceof FAQRecyclerViewAdapter.FAQItemInteractionListener) {
            mFAQInteractionListener = (FAQRecyclerViewAdapter.FAQItemInteractionListener) context;
        }
        if (context instanceof DrawerLayoutInteractionListener) {
            mListener = (DrawerLayoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");
        mFAQInteractionListener = null;
        mListener = null;
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.FAQS);
        super.onDetach();
    }

    @Override
    public void initiateUI() {
        //Setting up toolbar
        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            ((AppCompatActivity) mContext).setSupportActionBar(mFragmentFaqBinding.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        }
        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mFragmentFaqBinding.toolbar);
        mFragmentFaqBinding.textTitle.setText(getString(R.string.label_faq));


        callFetchContentWS();

        /*//Setting RecyclerView Adapter
        FAQRecyclerViewAdapter chatTabRecyclerViewAdapter = new FAQRecyclerViewAdapter(BootstrapConstant.DUMMY_FAQ_LIST, mFAQInteractionListener);
        mFragmentFaqBinding.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mFragmentFaqBinding.commonRecyclerView.recyclerView.setAdapter(chatTabRecyclerViewAdapter);
        mFragmentFaqBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_0dp)));*/
    }

    @Override
    public void setListener() {
        mFragmentFaqBinding.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext instanceof HomeActivity) {
                    /*HomeActivity homeActivity= (HomeActivity) mContext;
                    homeActivity.callToCheepAdmin(mFragmentFaqBinding.getRoot());*/
                    Utility.initiateCallToCheepHelpLine(mContext);
                }
            }
        });

        //Click event of Chat button
        mFragmentFaqBinding.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HotlineHelper.getInstance(mContext).showConversation(mContext);
            }
        });
    }

    /*************************************************************************************************************
     *************************************************************************************************************
     *****************************************Webservice Integration [End]**************************************
     *************************************************************************************************************
     */


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// PageContent[START]/////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call Login WS Key Webservice
     */
    private void callFetchContentWS() {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentFaqBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressBar(true);

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        }

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.FAQS
                , mCallFetchContentWSErrorListener
                , mCallFetchContentWSResponseListener
                , mHeaderParams
                , null
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest,NetworkUtility.WS.FAQS);

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

                        ArrayList<FAQModel> faqModelArrayList = Utility.getObjectListFromJsonString(jsonObject.getJSONArray(NetworkUtility.TAGS.DATA).toString(), FAQModel[].class);

                        //Update Recyclerview now
                        updateRecyclerView(faqModelArrayList);
//                                Utility.getObjectListFromJsonString(jsonObject.getJSONArray(NetworkUtility.TAGS.DATA).toString(), FAQModel.class);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentFaqBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mFragmentFaqBinding.getRoot());
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
            showProgressBar(false);

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentFaqBinding.getRoot());
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
        mFragmentFaqBinding.progress.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mFragmentFaqBinding.commonRecyclerViewNoSwipe.recyclerView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    private void updateRecyclerView(ArrayList<FAQModel> faqModelArrayList) {
        //Setting RecyclerView Adapter
        FAQRecyclerViewAdapter chatTabRecyclerViewAdapter = new FAQRecyclerViewAdapter(faqModelArrayList, mFAQInteractionListener);
        mFragmentFaqBinding.commonRecyclerViewNoSwipe.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mFragmentFaqBinding.commonRecyclerViewNoSwipe.recyclerView.setAdapter(chatTabRecyclerViewAdapter);
//        mFragmentFaqBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_0dp)));

    }

}
