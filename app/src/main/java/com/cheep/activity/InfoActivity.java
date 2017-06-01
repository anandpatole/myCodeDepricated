package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.cheep.R;
import com.cheep.databinding.ActivityInfoBinding;
import com.cheep.fragment.InfoFragment;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.utils.Utility;

/**
 * Created by pankaj on 10/26/16.
 */

public class InfoActivity extends BaseAppCompatActivity implements DrawerLayoutInteractionListener {

    ActivityInfoBinding mActivityInfoBinding;

    public static void newInstance(Context context, String type) {
        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(Utility.Extra.INFO_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityInfoBinding = DataBindingUtil.setContentView(this, R.layout.activity_info);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {
        getSupportFragmentManager().beginTransaction().replace(R.id.info_container, InfoFragment.newInstance(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))).commit(); //NetworkUtility.TAGS.PAGEID_TYPE.TERMS

    }

    @Override
    protected void setListeners() {

    }

    @Override
    public void setUpDrawerLayoutWithToolBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void profileUpdated() {

    }
}
