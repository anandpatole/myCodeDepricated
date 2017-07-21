package com.cheep;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.cheep.activity.ApplicationLifeCycle;
import com.cheep.utils.HotlineHelper;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

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

        HotlineHelper.getInstance(this);

        //Twitter Authentication for configuring Fabric account
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        //Allowing Strict mode policy for Nougat support
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
//        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onApplicationResume() {
        super.onApplicationResume();
        Log.i("APP", "Application Resume");
    }

    @Override
    protected void onApplicationPaused() {
        super.onApplicationPaused();
        Log.i("APP", "Application Paused");
    }

    @Override
    protected void onApplicationCreated() {
        super.onApplicationCreated();
        Log.i("APP", "Application Created");
    }

    @Override
    protected void onApplicationDestroyed() {
        super.onApplicationDestroyed();
        Log.i("APP", "Application Destroyed");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
