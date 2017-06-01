package com.cheep.utils;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.transition.ChangeBounds;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;

/**
 * Shared Element Transition
 * Must be placed on styles.xml(theme)  ==> <item name="android:windowContentTransitions">true</item>
 * Created by root on 20/4/16.
 */
public class SharedElementTransitionHelper {
    ArrayList<Pair<View, String>> list;
    Activity context;

    public SharedElementTransitionHelper(Activity context) {
        list = new ArrayList<>();
        this.context = context;
    }

    public void put(View view, String transitionName) {
        list.add(Pair.create(view, transitionName));
//        list.add(Pair.create(view.findViewById(R.id.txt_drive_score), transitionName));
    }

    public void put(View view, int res) {
        list.add(Pair.create(view, context.getString(res)));
    }

    /**
     * Sending this bundle to intent on launching activity
     *
     * @return
     */
    public Bundle getBundle() {
        Pair[] array = list.toArray(new Pair[list.size()]);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, array);
        return options.toBundle();
    }

    /**
     * Must be called before setContentView
     *
     * @param activity
     */
    public static void enableTransition(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            activity.getWindow().setSharedElementEnterTransition(new ChangeBounds());
            activity.getWindow().setSharedElementExitTransition(new ChangeBounds());
        }
    }
}
