package com.cheep.strategicpartner;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.databinding.ActivityMediaViewStrategicPartnerBinding;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giteeka on 20/7/17.
 * This activity is Specifically for Strategic partner feature
 * This includes 3 Step
 * Phase 1 - service selection
 * Phase 2 - Questionnary
 * Phase 3 - Payment summary
 * logic to update status of step number in header
 * location services for address
 */
public class StrategicPartnerMediaViewActiivty extends BaseAppCompatActivity {


    private MediaViewPagerAdapter mMediaViewPagerAdapter;
    private ActivityMediaViewStrategicPartnerBinding mActivityMediaViewStrategicPartnerBinding;

    public static void getInstance(Context mContext, ArrayList<MediaModel> modelArrayList) {
        Intent intent = new Intent(mContext, StrategicPartnerMediaViewActiivty.class);
        intent.putExtra("list", modelArrayList);
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityMediaViewStrategicPartnerBinding = DataBindingUtil.setContentView(this, R.layout.activity_media_view_strategic_partner);
        initiateUI();
    }

    @Override
    protected void initiateUI() {
        setupViewPager(mActivityMediaViewStrategicPartnerBinding.viewpager);
        mActivityMediaViewStrategicPartnerBinding.toolbar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.black_translucent)));

        setSupportActionBar(mActivityMediaViewStrategicPartnerBinding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mActivityMediaViewStrategicPartnerBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    @Override
    protected void setListeners() {

    }

    ArrayList<MediaModel> mediaModels;

    /**
     * This will setup the viewpager and tabs as well
     *
     * @param pager view pager for 3 steps
     */
    private void setupViewPager(ViewPager pager) {
        if (getIntent() != null && getIntent().hasExtra("list"))
            mediaModels = (ArrayList<MediaModel>) getIntent().getSerializableExtra("list");
        mMediaViewPagerAdapter = new MediaViewPagerAdapter(getSupportFragmentManager());
        if (mediaModels != null && !mediaModels.isEmpty()) {
            mMediaViewPagerAdapter.setMediaModelList(mediaModels);
            pager.setAdapter(mMediaViewPagerAdapter);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public class MediaViewPagerAdapter extends FragmentPagerAdapter {
        private List<MediaModel> mMediaModelList = new ArrayList<>();

        public void setMediaModelList(List<MediaModel> mediaModelList) {
            mMediaModelList = mediaModelList;
        }

        public MediaViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return MediaViewFragment.newInstance(mMediaModelList.get(position));
        }

        @Override
        public int getCount() {
            return mMediaModelList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Media_" + position;
        }

    }

}
