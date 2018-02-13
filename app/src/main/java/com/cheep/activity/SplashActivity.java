package com.cheep.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cheep.R;
import com.cheep.databinding.ActivitySplashBinding;
import com.cheep.utils.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by pankaj on 9/26/16.
 */

public class SplashActivity extends BaseAppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    //Handler for track splash activity timeout
    private Handler mHandler;

    //Splash Screen Timeout
    public static final int SPLASH_DURATION = 3000;

    private ActivitySplashBinding mActivitySplashBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: intent [ " + getIntent() + " ] + intent action [ " + getIntent().getAction() + " ]"
                + " ]  + intent data [ " + getIntent().getData());
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.bg_splash);
        if (getIntent() != null && getIntent().getExtras() != null)
            Log.d(TAG, "SPLASH DATA : " + getIntent().getExtras().toString() + "");

        mActivitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        mActivitySplashBinding.textVersion.setText(getString(R.string.label_version_x, Utility.getApplicationVersion(mContext)));
        startAnimations();

        try {
            PackageInfo info = getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sign = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("MY KEY HASH:", sign);
                //Toast.makeText(getApplicationContext(), sign, Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onGooglePlayServiceCheckedSuccessfully() {
        super.onGooglePlayServiceCheckedSuccessfully();

        //Start the Handler now
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, SPLASH_DURATION);
    }

    /**
     * Runnable class
     */
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

 /*
 * For Guest Login We would directly redirect the user to HomeScreen
 */
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(getIntent())
                    .addOnSuccessListener(SplashActivity.this, new OnSuccessListener<PendingDynamicLinkData>() {
                        @Override
                        public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                            // Get deep link from result (may be null if no link is found)
                            Uri deepLink = null;
                            if (pendingDynamicLinkData != null) {
                                deepLink = pendingDynamicLinkData.getLink();
                                HomeActivity.newInstance(mContext, deepLink);
                            } else {
                                HomeActivity.newInstance(mContext, null);
                            }

                            finish();
                            // Handle the deep link. For example, open the linked
                            // content, or apply promotional credit to the user's
                            // account.
                            // ...

                            // ...
                        }
                    })
                    .addOnFailureListener(SplashActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "getDynamicLink:onFailure", e);
                            HomeActivity.newInstance(mContext, null);
                            finish();
                        }
                    });




 /*if (!TextUtils.isEmpty(getIntent().getDataString()))
 HomeActivity.newInstance(mContext, getIntent().getData());
 else
 HomeActivity.newInstance(mContext, null);*/

 /*if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
 if (!PreferenceUtility.getInstance(mContext).getIntroScreenStatus()) {
 //Start the Introduction activity
// TODO: This would be enabled once new INTROScreen Available
// IntroScreenActivity.newInstance(mContext);


 // Temporary intiating Login Screen
 PreferenceUtility.getInstance(mContext).updateIntroScreenStatus(true);
 //Start the Login activity
 LoginActivity.newInstance(mContext);
 } else {
 //Start the Login activity
 LoginActivity.newInstance(mContext);
 }
 finish();
 } else {
 HomeActivity.newInstance(mContext);
 finish();
 }*/
        }
    };

    @Override
    protected void onDestroy() {


        /*
          To avoid memory leak, remove callback once its not necessary
         */
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }

        super.onDestroy();
    }


    @Override
    protected void initiateUI() {

    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void onLocationNotAvailable() {
        Log.i(TAG, "onLocationNotAvailable: ");
    }

    @Override
    protected void onLocationFetched(Location mLocation) {
        Log.d(TAG, "onLocationFetched() called with: mLocation = [" + mLocation + "]");
    }

    /**
     * Start the Animation
     */
    private void startAnimations() {
        Animation animationScale = AnimationUtils.loadAnimation(this, R.anim.scale);
        mActivitySplashBinding.ivLogo.startAnimation(animationScale);

        Animation animationSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        mActivitySplashBinding.ivSparrow.startAnimation(animationSlideUp);

        Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        mActivitySplashBinding.tvCopyright.startAnimation(animationFadeIn);

    }
}
