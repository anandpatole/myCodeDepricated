package com.cheep.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

/**
 * Created by Bhavesh V Patadiya on 9/14/16.
 * This class would track activity creation and lifecycle callbacks.
 */
@SuppressLint("Registered")
public class ApplicationLifeCycle extends MultiDexApplication {

    /**
     * This field would manage whether application is Resumed or not.
     */
    boolean isApplicationResumed;

    /**
     * This field would manage whether count of how many @{@link Activity} is created by the application.
     * Note: This is important if we need to know whether at any moment application is running or not.
     */
    int numberOfActivityCreated = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        // Register @ActivityLifecycleCallbacks
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    @Override
    public void onTerminate() {
        //Unregister @ActivityLifecycleCallbacks
        unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        super.onTerminate();
    }


    /**
     * Managing Callback of @{@link android.app.Application.ActivityLifecycleCallbacks} interface
     */
    ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            // Manage @numberOfActivityCreated counter here.
            if (numberOfActivityCreated == 0) {
                ApplicationLifeCycle.this.onActivityCreated();
            }
            numberOfActivityCreated++;
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (!isApplicationResumed) {
                onActivityResume();
            }
            isApplicationResumed = true;
        }

        @Override
        public void onActivityResumed(Activity activity) {
            isApplicationResumed = false;
        }

        @Override
        public void onActivityPaused(Activity activity) {
            isApplicationResumed = true;
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (isApplicationResumed) {
                ApplicationLifeCycle.this.onActivityPaused();
            }
            isApplicationResumed = false;
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            numberOfActivityCreated--;
            if (numberOfActivityCreated <= 0) {
                ApplicationLifeCycle.this.onActivityDestroyed();
            }
        }
    };

    /**
     * Called when @{@link Activity} is Resume whether by back pressing or direct by home button click
     */
    protected void onActivityResume() {

    }

    /**
     * Called when @{@link Activity} is paused whether by back pressing or direct by home button click
     */
    protected void onActivityPaused() {

    }

    /**
     * Called when @{@link Activity} is Created whether by back pressing or direct by home button click
     */
    protected void onActivityCreated() {

    }

    /**
     * Called when @{@link Activity} is destroyed by back pressing or direct by home button click.
     * Important note: Not necessary get called when application is force closed by User.
     * i.e.: From recent app listing.
     */
    protected void onActivityDestroyed() {

    }
}
