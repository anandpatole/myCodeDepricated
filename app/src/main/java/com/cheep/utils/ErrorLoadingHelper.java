package com.cheep.utils;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cheep.R;

/**
 * Created by pankaj on 11/10/16.
 */
public class ErrorLoadingHelper {

    private RecyclerView recyclerView;
    private ImageView imgError;
    private TextView textError;
    private ImageView imgPostATask;
    private TextView textErrorAction;
    private ProgressBar progressBar;
    private LinearLayout mEmptyFavouriteLayout;
    private LinearLayout mEmptySubscribedLayout;

    public ErrorLoadingHelper(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        View view = (View) recyclerView.getParent().getParent();
        progressBar = (ProgressBar) view.findViewById(R.id.progress_load);
        imgError = (ImageView) view.findViewById(R.id.img_error);
        imgPostATask = (ImageView) view.findViewById(R.id.img_post_a_task);
        textError = (TextView) view.findViewById(R.id.text_error);
        textErrorAction = (TextView) view.findViewById(R.id.text_error_action);
        mEmptyFavouriteLayout = (LinearLayout) view.findViewById(R.id.ln_empty_favourite_list);
        mEmptySubscribedLayout = (LinearLayout) view.findViewById(R.id.ln_empty_subscribed_list);
    }

    public void showLoading() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        imgError.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);
        if (imgPostATask != null)
            imgPostATask.setVisibility(View.GONE);
        if (textErrorAction != null)
            textErrorAction.setVisibility(View.GONE);

        // Hide Favourite section view
        if (mEmptyFavouriteLayout != null)
            mEmptyFavouriteLayout.setVisibility(View.GONE);
        if (mEmptySubscribedLayout != null)
            mEmptySubscribedLayout.setVisibility(View.GONE);
    }

    public void success() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        imgError.setVisibility(View.GONE);
        if (imgPostATask != null)
            imgPostATask.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);
        textErrorAction.setVisibility(View.GONE);

        // Hide Favourite section view
        if (mEmptyFavouriteLayout != null)
            mEmptyFavouriteLayout.setVisibility(View.GONE);
        if (mEmptySubscribedLayout != null)
            mEmptySubscribedLayout.setVisibility(View.GONE);

    }

    public void failed(String errorMessage, int errorRes, View.OnClickListener clickListener) {
        failed(errorMessage, errorRes, Utility.EMPTY_STRING/*"Retry"*/, clickListener);
    }

    public void failed(String errorMessage, int errorRes, String btnText, View.OnClickListener clickListener) {
        failed(errorMessage, errorRes, btnText, clickListener, null);
    }

    public void showEmptyCategorySection(String mSelectedFilterType) {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);
        imgError.setVisibility(View.GONE);

        if (!mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FAVOURITES)) {
            if (mEmptyFavouriteLayout != null)
                mEmptyFavouriteLayout.setVisibility(View.VISIBLE);
            if (mEmptySubscribedLayout != null)
                mEmptySubscribedLayout.setVisibility(View.GONE);
        } else /*if (mSelectedFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_SUBSCRIBED)) */ {
            if (mEmptyFavouriteLayout != null)
                mEmptyFavouriteLayout.setVisibility(View.GONE);
            if (mEmptySubscribedLayout != null)
                mEmptySubscribedLayout.setVisibility(View.VISIBLE);
        }
    }

    public void failed(String errorMessage, int errorRes, String btnText, View.OnClickListener clickListener, View.OnClickListener postaTaskClickListener) {

        // Hide Favourite section view
        if (mEmptyFavouriteLayout != null)
            mEmptyFavouriteLayout.setVisibility(View.GONE);
        if (mEmptySubscribedLayout != null)
            mEmptySubscribedLayout.setVisibility(View.GONE);

        // Changing it to null so retry button is not visible
        clickListener = null;

        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(errorMessage)) {
            textError.setText(errorMessage);
            textError.setVisibility(View.VISIBLE);
        } else {
            textError.setVisibility(View.GONE);
        }

        if (errorRes != 0) {
            imgError.setImageResource(errorRes);
            imgError.setVisibility(View.VISIBLE);
        } else {
            imgError.setVisibility(View.GONE);
        }

        if (clickListener != null) {
            textErrorAction.setText(btnText);
            textErrorAction.setOnClickListener(clickListener);
            textErrorAction.setVisibility(View.VISIBLE);
        } else {
            textErrorAction.setVisibility(View.GONE);
        }

        if (postaTaskClickListener != null && imgPostATask != null) {
            imgPostATask.setVisibility(View.VISIBLE);
            imgPostATask.setOnClickListener(postaTaskClickListener);
        }
    }
}
