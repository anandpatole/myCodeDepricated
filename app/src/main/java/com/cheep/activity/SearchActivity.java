package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.cheep.R;
import com.cheep.adapter.SearchAdapter;
import com.cheep.databinding.ActivitySearchBinding;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.UserDetails;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SharedElementTransitionHelper;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/28/16.
 */

public class SearchActivity extends BaseAppCompatActivity implements SearchAdapter.CategoryRowInteractionListener {

    ActivitySearchBinding mActivitySearchBinding;

    public static void newInstance(Context context, Bundle bnd, String cityName, ArrayList<JobCategoryModel> list) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(list));
        intent.putExtra(Utility.Extra.CITY_NAME, cityName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            context.startActivity(intent, bnd);
        } else {
            context.startActivity(intent);
        }
//        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Sets intermediate background of activity when activity starts
        SharedElementTransitionHelper.enableTransition(this);
        mActivitySearchBinding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        //Setting Toolbar
        setSupportActionBar(mActivitySearchBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivitySearchBinding.toolbar.setNavigationIcon(R.drawable.ic_arrow_white);
            mActivitySearchBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext, mActivitySearchBinding.editSearch);
                    onBackPressed();
                }
            });
        }

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Setting search placeholder
        if (userDetails != null && TextUtils.isEmpty(userDetails.UserName) == false && userDetails.UserName.trim().length() > 1) {
            String name = userDetails.UserName.substring(0, 1).toUpperCase() + userDetails.UserName.substring(1);
            //Used to get only first word, e.g "Pankaj" from "pankaj sharma"
            mActivitySearchBinding.editSearch.setHint(getString(R.string.hint_search_placeholder, name));
//            mActivitySearchBinding.editSearch.setHint(getString(R.string.hint_search_placeholder, name.split(" ")[0]));
        } else {
            mActivitySearchBinding.editSearch.setHint(getString(R.string.hint_search_placeholder, Utility.GUEST_STATIC_INFO.USERNAME));
        }


//        mActivitySearchBinding.editSearch.setHint(getString(R.string.hint_search_placeholder, userDetails.UserName));

        mActivitySearchBinding.editSearch.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utility.showKeyboard(mContext, mActivitySearchBinding.editSearch);
            }
        }, 50);

        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            //Setting RecyclerView Adapter

            mActivitySearchBinding.textLocation.setText(getIntent().getStringExtra(Utility.Extra.CITY_NAME));

            final ArrayList<JobCategoryModel> list = Utility.getObjectListFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), JobCategoryModel[].class);
            final SearchAdapter adapter = new SearchAdapter(this);
            mActivitySearchBinding.commonRecyclerViewNoSwipe.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            mActivitySearchBinding.commonRecyclerViewNoSwipe.recyclerView.setAdapter(adapter);
            mActivitySearchBinding.commonRecyclerViewNoSwipe.recyclerView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));


            //Adding Searching textlistener to search edittext
            mActivitySearchBinding.editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    adapter.getUserFilter(list).performFiltering(charSequence);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        }
    }

    @Override
    protected void setListeners() {

    }


    @Override
    public void onCategoryRowClicked(JobCategoryModel model, int position) {
//        HireNewJobActivity.newInstance(mContext, model);
        TaskCreationActivity.getInstance(mContext, model);
    }
}
