package com.cheep.cheepcarenew.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.cheepcarenew.fragments.AddressCategorySelectionFragment;
import com.cheep.fragment.BaseFragment;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;

public class AddressActivity extends BaseAppCompatActivity {

    PackageDetail packageDetail;
    public static void newInstance(Context context, PackageDetail packageDetail) {
        Intent intent = new Intent(context, AddressActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(packageDetail));
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(0, 0);
    }

    public PackageDetail getPackageDetail() {
        return packageDetail;
    }

    @Override
    protected void initiateUI() {
        if (getIntent()!=null && getIntent().hasExtra(Utility.Extra.DATA)){
            packageDetail = (PackageDetail) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA),PackageDetail.class);
        }
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
