package com.cheep.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

/**
 * Created by pankaj on 9/14/16.
 */
@SuppressLint("Registered")
public class ApplicationLifeCycle extends MultiDexApplication{
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    @Override
    public void onTerminate() {
        unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        super.onTerminate();
    }

    boolean isApplicationResumed;
    int isApplicationCreated = 0;
    ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            if (isApplicationCreated == 0) {
                onApplicationCreated();
            }
            isApplicationCreated++;
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (isApplicationResumed == false) {
                onApplicationResume();
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
            if (isApplicationResumed == true) {
                onApplicationPaused();
            }
            isApplicationResumed = false;
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            isApplicationCreated--;
            if (isApplicationCreated <= 0) {
                onApplicationDestroyed();
            }
        }
    };

    /**
     * Called when application is paused whether by back pressing or direct by home button click
     */
    protected void onApplicationResume() {

    }

    /**
     * Called when application is paused whether by back pressing or direct by home button click
     */
    protected void onApplicationPaused() {

    }

    /**
     * Called when application is created
     */
    protected void onApplicationCreated() {

    }

    /**
     * Called when application is Destroyed
     * Note:Not necessary get called when application is force closed by User.
     */
    protected void onApplicationDestroyed() {

    }
}
