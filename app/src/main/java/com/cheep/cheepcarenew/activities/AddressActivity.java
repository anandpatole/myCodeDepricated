package com.cheep.cheepcarenew.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcarenew.fragments.AddressCategorySelectionFragment;
import com.cheep.fragment.BaseFragment;

public class AddressActivity extends BaseAppCompatActivity {

    public static void newInstance(Context context) {
        context.startActivity(new Intent(context, AddressActivity.class));
        ((Activity) context).overridePendingTransition(0, 0);
    }

    @Override
    protected void initiateUI() {
        loadFragment(AddressCategorySelectionFragment.TAG, AddressCategorySelectionFragment.newInstance());

    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        initiateUI();
    }

    public void loadFragment(String tag, BaseFragment baseFragment) {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out)
                .replace(R.id.content, baseFragment, tag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    private static final String TAG = "AddressActivity";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
            Log.e(TAG, "onBackPressed:  " + getSupportFragmentManager().getBackStackEntryCount());
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finish();
        }
    }


}
