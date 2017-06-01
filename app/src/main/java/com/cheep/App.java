package com.cheep;

import android.content.Context;
import android.os.AsyncTask;
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
 * Created by pankaj on 9/14/16.
 */
public class App extends ApplicationLifeCycle {

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        //Start the Intialization of varioud Operation in separated threads
//        new AsyncOnInitiateOperations(getApplicationContext()).execute();

        // For Facebook Integration
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);

        HotlineHelper.getInstance(this);

        //Twitter Authentication for configuring Fabric account
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

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

    private static class AsyncOnInitiateOperations extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        AsyncOnInitiateOperations(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected Void doInBackground(Void... params) {


            return null;
        }
    }
}
