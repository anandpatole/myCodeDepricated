package com.cheep.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.adapter.FavouriteRecyclerViewAdapter;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.FragmentFavouriteBinding;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pankaj on 9/7/16.
 */
public class FavouriteFragment extends BaseFragment {

    public static final String TAG = FavouriteFragment.class.getSimpleName();

    private DrawerLayoutInteractionListener mListener;
    private FragmentFavouriteBinding mFragmentFavouriteFragment;
    private FavouriteRecyclerViewAdapter.FavouriteRowInteractionListener mFavouriteListener;
    private FavouriteRecyclerViewAdapter favouriteRecyclerViewAdapter;

    private ErrorLoadingHelper errorLoadingHelper;

    public static FavouriteFragment newInstance() {
        FavouriteFragment fragment = new FavouriteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentFavouriteFragment = DataBindingUtil.inflate(inflater, R.layout.fragment_favourite, container, false);
        setHasOptionsMenu(true);
        return mFragmentFavouriteFragment.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);
        if (context instanceof FavouriteRecyclerViewAdapter.FavouriteRowInteractionListener) {
            mFavouriteListener = (FavouriteRecyclerViewAdapter.FavouriteRowInteractionListener) context;
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
        if (EventBus.getDefault().isRegistered(this) == true)
            EventBus.getDefault().unregister(this);
        mListener = null;
        mFavouriteListener = null;
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.FAV_SP_LIST);
        super.onDetach();
    }

    @Override
    public void initiateUI() {

        errorLoadingHelper = new ErrorLoadingHelper(mFragmentFavouriteFragment.commonRecyclerView.recyclerView);

        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            //Setting up toolbar
            ((AppCompatActivity) mContext).setSupportActionBar(mFragmentFavouriteFragment.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        }

        mFragmentFavouriteFragment.textTitle.setText(getString(R.string.label_favourites));
        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mFragmentFavouriteFragment.toolbar);

        mFragmentFavouriteFragment.textTitle.setText(getString(R.string.label_favourites));

        //Setting adapter on recycler view
        favouriteRecyclerViewAdapter = new FavouriteRecyclerViewAdapter(new FavouriteRecyclerViewAdapter.FavouriteRowInteractionListener() {
            @Override
            public void onFavouriteRowClicked(ProviderModel providerModel, int position) {
                mFavouriteListener.onFavouriteRowClicked(providerModel, position);
            }

            @Override
            public void onFavClicked(ProviderModel providerModel, boolean isAddToFav, int position) {
                mFavouriteListener.onFavClicked(providerModel, isAddToFav, position);
                if (favouriteRecyclerViewAdapter.getItemCount() < 1) {
                    errorLoadingHelper.failed(null, R.drawable.img_empty_favourite, null);
                }
            }
        });
        mFragmentFavouriteFragment.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mFragmentFavouriteFragment.commonRecyclerView.recyclerView.setAdapter(favouriteRecyclerViewAdapter);

        mFragmentFavouriteFragment.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_0dp)));

        errorLoadingHelper.showLoading();
        initSwipeToRefreshLayout();
        callFavSPList();
        if (EventBus.getDefault().isRegistered(this) == false)
            EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.UPDATE_FAVOURITE) {
            favouriteRecyclerViewAdapter.updateFavStatus(event.id, event.isFav);
            if (favouriteRecyclerViewAdapter.getItemCount() < 1) {
                errorLoadingHelper.failed(null, R.drawable.img_empty_favourite, null);
            }
        }
    }

    private void initSwipeToRefreshLayout() {
        mFragmentFavouriteFragment.commonRecyclerView.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                favouriteRecyclerViewAdapter.enableLoadMore();
//                errorLoadingHelper.showLoading();
                reloadFavSPListFromServer();
            }
        });
        Utility.setSwipeRefreshLayoutColors(mFragmentFavouriteFragment.commonRecyclerView.swipeRefreshLayout);
    }


    VolleyNetworkRequest mVolleyNetworkRequestForFavSP;

    private void reloadFavSPListFromServer() {
        if (!Utility.isConnected(mContext)) {
            errorLoadingHelper.failed(Utility.NO_INTERNET_CONNECTION, 0, onRetryBtnClickListener);
            mFragmentFavouriteFragment.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
            return;
        }

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForFavSP);
    }

    private void callFavSPList() {

        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mFragmentFavouriteFragment.getRoot());
            errorLoadingHelper.failed(Utility.NO_INTERNET_CONNECTION, 0, onRetryBtnClickListener);
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            /**
             * As user is guest user, we need to show only Empty screen.
             */
            mFragmentFavouriteFragment.commonRecyclerView.swipeRefreshLayout.setEnabled(false);
            mFragmentFavouriteFragment.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
            errorLoadingHelper.failed(null, R.drawable.img_empty_favourite, null);

        }


        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
//        if (!TextUtils.isEmpty(nextPageId)) {
//            mParams.put(NetworkUtility.TAGS.TIMESTAMP, nextPageId);
//        } else {
//            errorLoadingHelper.showLoading();
//        }

        mVolleyNetworkRequestForFavSP = new VolleyNetworkRequest(NetworkUtility.WS.FAV_SP_LIST
                , mCallPendingTaskWSErrorListener
                , mCallPendingTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForFavSP, NetworkUtility.WS.FAV_SP_LIST);
    }

    @Override
    public void setListener() {

    }

    Response.Listener mCallPendingTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;

            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                mFragmentFavouriteFragment.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        ArrayList<ProviderModel> list = GsonUtility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), ProviderModel[].class);
                        ProviderModel temp=new ProviderModel();
//                        temp.userName="Anand Patole";
//                        temp.isVerified="yes";
//                        temp.distance="2 km away";
//                        temp.isFavourite="yes";
//                        temp.sp_locality="Juhu";
//                        list.add(temp);
                        favouriteRecyclerViewAdapter.setItem(list);
                        errorLoadingHelper.success();
                        if (favouriteRecyclerViewAdapter.getmList().size() <= 0) {
//                            errorLoadingHelper.failed(getString(R.string.hint_no_favourite_provider), 0, onRetryBtnClickListener);
//                            errorLoadingHelper.failed(getString(R.string.hint_no_favourite_provider), 0, null);
                            errorLoadingHelper.failed(null, R.drawable.img_empty_favourite, null);
                        }

                       /* //Setting RecyclerView Adapter
                        if (TextUtils.isEmpty(nextPageId)) {
                            taskRecyclerViewAdapter.setItem(list);
                        } else {
                            taskRecyclerViewAdapter.addItem(list);
                        }
                        nextPageId = jsonObject.optString(NetworkUtility.TAGS.TIMESTAMP);

                        errorLoadingHelper.success();

//                        taskRecyclerViewAdapter.onLoadMoreComplete();
                        if (list.size() == 0) {
                            taskRecyclerViewAdapter.disableLoadMore();
                        }

                        if (favouriteRecyclerViewAdapter.getmList().size() <= 0) {
                            errorLoadingHelper.failed(getString(R.string.hint_no_pending_task), 0, onRetryBtnClickListener);
                        }*/

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentTabHomeBinding.getRoot());
                        errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mFragmentTabHomeBinding.getRoot());
                        errorLoadingHelper.failed(error_message, 0, onRetryBtnClickListener);
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
                mCallPendingTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };


    Response.ErrorListener mCallPendingTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            mFragmentFavouriteFragment.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);

            errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentTabHomeBinding.getRoot());
        }
    };

    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            reloadFavSPListFromServer();
        }
    };

}