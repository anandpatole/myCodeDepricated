package com.cheep.adapter;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.cheep.fragment.CoverImageFragment;
import com.cheep.model.CoverImageModel;

import java.util.ArrayList;

//Banner Image View Pager Adapter
public class CoverViewPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = CoverViewPagerAdapter.class.getSimpleName();
    private ArrayList<CoverImageModel> imageModelArrayList;

    public CoverViewPagerAdapter(FragmentManager fragmentManager, ArrayList<CoverImageModel> modelArrayList) {
        super(fragmentManager);
        Log.d(TAG, "BannerViewPagerAdapter() called with: fragmentManager = [" + fragmentManager + "], modelArrayList = [" + modelArrayList + "]");
        this.imageModelArrayList = modelArrayList;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem() called with: position = [" + position + "]" + " Size: " + imageModelArrayList.size());
        return CoverImageFragment.getInstance(imageModelArrayList.get(position));
    }

    @Override
    public int getCount() {
        return imageModelArrayList.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    private ArrayList<CoverImageModel> getLists() {
        return imageModelArrayList;
    }

    private void replaceData(ArrayList<CoverImageModel> modelArrayList) {
        imageModelArrayList = modelArrayList;
        notifyDataSetChanged();
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
//            super.restoreState(state, loader);
    }
}