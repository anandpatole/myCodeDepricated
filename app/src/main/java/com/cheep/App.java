package com.cheep;

import android.content.Context;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.cheep.activity.ApplicationLifeCycle;
import com.cheep.utils.FreshChatHelper;
import com.cheep.utils.HotlineHelper;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.Map;

import io.fabric.sdk.android.Fabric;

import static com.cheep.BuildConfig.TWITTER_KEY;
import static com.cheep.BuildConfig.TWITTER_SECRET;

/**
 * Created by Bhavesh Patadiya on 9/14/16.
 */
public class App extends ApplicationLifeCycle {

    @Override
    public void onCreate() {
        super.onCreate();
        //Start the Intialization of varioud Operation in separated threads
//        new AsyncOnInitiateOperations(getApplicationContext()).execute();

        // For Facebook Integration
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);

       // HotlineHelper.getInstance(this);
        FreshChatHelper.getInstance(this);
        initiateAppsFlyerSDK();

        //Twitter Authentication for configuring Fabric account
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        //Allowing Strict mode policy for Nougat support
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
//        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onActivityResume() {
        super.onActivityResume();
        Log.i("APP", "Application Resume");
    }

    @Override
    protected void onActivityPaused() {
        super.onActivityPaused();
        Log.i("APP", "Application Paused");
    }

    @Override
    protected void onActivityCreated() {
        super.onActivityCreated();
        Log.i("APP", "Application Created");
    }

    @Override
    protected void onActivityDestroyed() {
        super.onActivityDestroyed();
        Log.i("APP", "Application Destroyed");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * This method would initiate AppsFlyer SDK
     */
    private void initiateAppsFlyerSDK() {
        // Collect IMEI & AndroidID and set them via Appsflyer SDK
        /*TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceIMEINumber = telephonyManager.getDeviceId();
        if (!TextUtils.isEmpty(deviceIMEINumber))
            AppsFlyerLib.getInstance().setImeiData(deviceIMEINumber);*/

        String android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        AppsFlyerLib.getInstance().setAndroidIdData(android_id);

        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {

            /* Returns the attribution data. Note - the same conversion data is returned every time per install */
            @Override
            public void onInstallConversionDataLoaded(Map<String, String> conversionData) {
                for (String attrName : conversionData.keySet()) {
                    Log.d(AppsFlyerLib.LOG_TAG, "attribute: " + attrName + " = " + conversionData.get(attrName));
                }

                /*** Ignore - used to display the install data on the screen ***/

//                final String install_type = "Install Type: " + conversionData.get("af_status") + "\n";
//                final String media_source = "Media Source: " + conversionData.get("media_source") + "\n";
//                final String install_time = "Install Time(GMT): " + conversionData.get("install_time") + "\n";
//                final String click_time = "Click Time(GMT): " + conversionData.get("click_time") + "\n";
//                final String is_first_launch = "Is First Launch: " + conversionData.get("is_first_launch") + "\n";
//                MainActivity.dataToShow += install_type + media_source + install_time + click_time + is_first_launch;

                /***************************************************************/
            }

            @Override
            public void onInstallConversionFailure(String errorMessage) {
                Log.d(AppsFlyerLib.LOG_TAG, "error getting conversion data: " + errorMessage);
            }

            /* Called only when a Deep Link is opened */
            @Override
            public void onAppOpenAttribution(Map<String, String> conversionData) {
                for (String attrName : conversionData.keySet()) {
                    Log.d(AppsFlyerLib.LOG_TAG, "attribute: " + attrName + " = " + conversionData.get(attrName));
                }
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d(AppsFlyerLib.LOG_TAG, "error onAttributionFailure : " + errorMessage);
            }
        };


        AppsFlyerLib.getInstance().init(BuildConfig.APPSFLYER_DEV_KEY , conversionListener , getApplicationContext());
        AppsFlyerLib.getInstance().startTracking(this, BuildConfig.APPSFLYER_DEV_KEY);


        /* Set to true to see the debug logs. Comment out or set to false to stop the function */

        AppsFlyerLib.getInstance().setDebugLog(true);

    }
}
