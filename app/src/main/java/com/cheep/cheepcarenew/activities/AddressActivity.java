package com.cheep.cheepcarenew.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cheep.R;
import com.cheep.cheepcarenew.fragments.AddressCategorySelectionFragment;
import com.cheep.fragment.BaseFragment;

public class AddressActivity extends AppCompatActivity {

    public static void newInstance(Context context) {
        context.startActivity(new Intent(context, AddressActivity.class));
        ((Activity) context).overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        loadFragment(AddressCategorySelectionFragment.TAG, AddressCategorySelectionFragment.newInstance());
    }

    public void loadFragment(String tag, BaseFragment baseFragment) {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out).replace(R.id.content, baseFragment, tag).addToBackStack(null).commitAllowingStateLoss();
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
