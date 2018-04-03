package com.cheep.cheepcare.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.cheepcare.adapter.RateAndReviewAdapter;
import com.cheep.databinding.FragmentReviewsOfMeBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.RateAndReviewModel;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kruti on 19/3/18.
 */

public class ReviewsOfMeFragment extends BaseFragment {

    private static final String TAG = "ReviewsOfMeFragment";
    private FragmentReviewsOfMeBinding mBinding;
    private RateAndReviewAdapter rateAndReviewAdapter;
  //  private ErrorLoadingHelper errorLoadingHelper;
    private String nextPageId = "0";

    public static ReviewsOfMeFragment newInstance() {
        Bundle args = new Bundle();
        ReviewsOfMeFragment fragment = new ReviewsOfMeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_reviews_of_me, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        initiateUI();
        setListener();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void initiateUI() {
        List<RateAndReviewModel.ReviewData> rateAndReviewByMeModelList = new ArrayList<>() ;
        rateAndReviewAdapter = new RateAndReviewAdapter(rateAndReviewByMeModelList,true);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.recyclerView.setAdapter(rateAndReviewAdapter);
    //    errorLoadingHelper = new ErrorLoadingHelper(mBinding/*.commonRecyclerView*/.recyclerView);
        mBinding.recyclerView.setNestedScrollingEnabled(false);
        initSwipeToRefreshLayout();
        callWsRateAndReview();

    }

    private void initSwipeToRefreshLayout() {
        mBinding/*.commonRecyclerView*/.srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                rateAndReviewAdapter.enableLoadMore();
                reloadRateAndReview();
            }
        });

        Utility.setSwipeRefreshLayoutColors(mBinding/*.commonRecyclerView*/.srl);

    }

    private void reloadRateAndReview() {
        nextPageId = "0";
        callWsRateAndReview();
    }

    private void callWsRateAndReview() {
        mBinding.srl.setRefreshing(false);
        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mBinding.getRoot());
     //       errorLoadingHelper.failed(Utility.NO_INTERNET_CONNECTION, 0, onRetryBtnClickListener);
            return;
        }
        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            mBinding/*.commonRecyclerView*/.srl.setRefreshing(false);
       //     errorLoadingHelper.failed(null, R.string.cb_error, null);
            return;
        }

        WebCallClass.getUserReviewList(mContext,
                nextPageId,
                PreferenceUtility.getInstance(mContext).getUserDetails().userID,
                Utility.ReviewByMe.NO,
                mCommonResponseListener,
                mGetUserReviewList
                );

    }

    WebCallClass.GetUserReviewListListener mGetUserReviewList = new WebCallClass.GetUserReviewListListener() {
        @Override
        public void getUserReviewList(RateAndReviewModel model, String pageNumber) {
            mBinding/*.commonRecyclerView*/.srl.setRefreshing(false);

            //Setting RecyclerView Adapter
            if (TextUtils.isEmpty(nextPageId) || nextPageId.equals("0")) {
                rateAndReviewAdapter.setItem(model.reviewData);
                setHeaderData(model.reviewSummary);
            } else {
                rateAndReviewAdapter.addItem(model.reviewData);
            }
            nextPageId = pageNumber;
         //   errorLoadingHelper.success();
            rateAndReviewAdapter.onLoadMoreComplete();
            if (model.reviewData.size() == 0) {
                rateAndReviewAdapter.disableLoadMore();
            }

            if (rateAndReviewAdapter.getmList().size() <= 0) {
          //      errorLoadingHelper.failed(null, R.drawable.img_empty_notifications, null);
            }
        }
    };

    private void setHeaderData(RateAndReviewModel.ReviewSummaryData reviewSummary) {
        if(reviewSummary!=null){
            mBinding.reviewOfMeHeader.ratingAndReview.setText(getString(R.string.label_rating_and_review_number,reviewSummary.ratingCount,reviewSummary.reviewCommentCount));
            mBinding.reviewOfMeHeader.averageRating.setText(getString(R.string.label_rating_number,String.format("%.1f",Float.parseFloat(reviewSummary.avgRatings))));
            mBinding.reviewOfMeHeader.progress1Star.setProgress((Integer.parseInt(reviewSummary.star1))/(Integer.parseInt(reviewSummary.ratingCount))*100);
            mBinding.reviewOfMeHeader.progress2Star.setProgress((Integer.parseInt(reviewSummary.star2))/(Integer.parseInt(reviewSummary.ratingCount))*100);
            mBinding.reviewOfMeHeader.progress3Star.setProgress((Integer.parseInt(reviewSummary.star3))/(Integer.parseInt(reviewSummary.ratingCount))*100);
            mBinding.reviewOfMeHeader.progress4Star.setProgress((Integer.parseInt(reviewSummary.star4))/(Integer.parseInt(reviewSummary.ratingCount))*100);
            mBinding.reviewOfMeHeader.progress5Star.setProgress((Integer.parseInt(reviewSummary.star5))/(Integer.parseInt(reviewSummary.ratingCount))*100);

        }
        else {
            mBinding.reviewOfMeHeader.ratingAndReview.setText(getString(R.string.label_rating_and_review_number,"0","0"));
            mBinding.reviewOfMeHeader.averageRating.setText(getString(R.string.label_rating_number,"0.0"));

        }
    }

    private final WebCallClass.CommonResponseListener mCommonResponseListener =
            new WebCallClass.CommonResponseListener() {
                @Override
                public void volleyError(VolleyError error) {
                    Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                    mBinding/*.commonRecyclerView*/.srl.setRefreshing(false);
          //          errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                }

                @Override
                public void showSpecificMessage(String message) {
                    mBinding/*.commonRecyclerView*/.srl.setRefreshing(false);
            //        errorLoadingHelper.failed(message, 0, onRetryBtnClickListener);
                }

                @Override
                public void forceLogout() {
                    mBinding/*.commonRecyclerView*/.srl.setRefreshing(false);
                    //Logout and finish the current activity
                    if (getActivity() != null)
                        getActivity().finish();
                }
            };

    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            reloadRateAndReview();
        }
    };

    @Override
    public void setListener() {
        rateAndReviewAdapter.setIsLoadMoreEnabled(true, R.layout.load_more_progress
                , mBinding.recyclerView, new LoadMoreRecyclerAdapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        if(TextUtils.isEmpty(nextPageId)){
                            rateAndReviewAdapter.disableLoadMore();
                            return;
                        }
                        callWsRateAndReview();
                    }
                });
    }
}
