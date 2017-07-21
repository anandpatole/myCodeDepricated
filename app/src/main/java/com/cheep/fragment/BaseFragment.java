package com.cheep.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.support.v4.app.Fragment;

import com.cheep.R;
import com.google.android.gms.common.api.Status;

public abstract class BaseFragment extends Fragment {
    protected Context mContext;

    /**
     * Empty Fragment
     */
    public BaseFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public abstract void initiateUI();

    public abstract void setListener();

    public boolean onBackPressed() {
        return false;
    }

    ProgressDialog mProgressDialog;

    /**
     * Show Progress Dialog
     */
    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(getString(R.string.label_please_wait));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressDialog.show();
    }

    /**
     * Close Progress Dialog
     */
    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }

    public void onBindLocationTrackService() {

    }

    public void onLocationNotAvailable() {

    }

    public void onLocationFetched(Location mLocation) {

    }

    public void onLocationSettingsDialogNeedToBeShow(Status locationRequest) {

    }

    public void gpsEnabled() {

    }


}
