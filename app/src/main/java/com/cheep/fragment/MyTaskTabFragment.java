package com.cheep.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.adapter.MyTaskTabPagerAdapter;
import com.cheep.databinding.FragmentTabMyTaskTabBinding;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.interfaces.NotificationClickInteractionListener;
import com.cheep.model.MessageEvent;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by pankaj on 9/30/16.
 */

public class MyTaskTabFragment extends BaseFragment {
    public static final String TAG = "MyTaskTabFragment";

    FragmentTabMyTaskTabBinding mFragmentTabMyTaskTabBinding;

    DrawerLayoutInteractionListener mListener;
    private NotificationClickInteractionListener mNotificationClickInteractionListener;

    public static MyTaskTabFragment newInstance(DrawerLayoutInteractionListener mListener) {
        Bundle args = new Bundle();
        MyTaskTabFragment fragment = new MyTaskTabFragment();
        fragment.setArguments(args);
        fragment.setmListener(mListener);
        return fragment;
    }

    /*private static MyTaskTabFragment fragment;

    public static MyTaskTabFragment singleInstance(DrawerLayoutInteractionListener mListener) {
        Bundle args = new Bundle();
        if (fragment == null) {
            fragment = new MyTaskTabFragment();
            fragment.setArguments(args);
            fragment.setmListener(mListener);
        }
        return fragment;
    }*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register Event Bus
        EventBus.getDefault().register(this);
    }

    public void setmListener(DrawerLayoutInteractionListener mListener) {
        this.mListener = mListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentTabMyTaskTabBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_my_task_tab, container, false);
        return mFragmentTabMyTaskTabBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);
        if (context instanceof DrawerLayoutInteractionListener) {
            mListener = (DrawerLayoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }

        if (context instanceof NotificationClickInteractionListener) {
            mNotificationClickInteractionListener = (NotificationClickInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");
        mListener = null;
        mNotificationClickInteractionListener = null;
        super.onDetach();
    }

    @Override
    public void initiateUI() {
        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            //Setting up toolbar
            ((AppCompatActivity) mContext).setSupportActionBar(mFragmentTabMyTaskTabBinding.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        }
        mFragmentTabMyTaskTabBinding.textTitle.setText(getString(R.string.label_my_task));

        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mFragmentTabMyTaskTabBinding.toolbar);

        //Setting viewpager
        setupHomeViewPager(mFragmentTabMyTaskTabBinding.viewpager);

        // Update Notification Counter
        updateCounter();

        // Setup Click listener of Notification
        mFragmentTabMyTaskTabBinding.relNotificationAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNotificationClickInteractionListener != null) {
                    mNotificationClickInteractionListener.onNotificationIconClicked();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        // Update Notification Counter
        updateCounter();
    }

    @Override
    public void setListener() {
    }

    /**
     * This will setup the viewpager and tabs as well
     *
     * @param pager
     */
    private void setupHomeViewPager(ViewPager pager) {
        MyTaskTabPagerAdapter mHomeFragmentPagerAdater = new MyTaskTabPagerAdapter(getChildFragmentManager());
        mHomeFragmentPagerAdater.addFragment(getString(R.string.label_upcoming_tasks));
        mHomeFragmentPagerAdater.addFragment(getString(R.string.label_past_tasks));
        pager.setAdapter(mHomeFragmentPagerAdater);
        mFragmentTabMyTaskTabBinding.tabs.setupWithViewPager(pager, true);
    }

    private void updateCounter() {
        if (mFragmentTabMyTaskTabBinding != null) {
            //Updating counter
            int notificationCounter = PreferenceUtility.getInstance(mContext).getUnreadNotificationCounter();
            if (notificationCounter > 0) {
                mFragmentTabMyTaskTabBinding.tvBadgeCount.setText(String.valueOf(notificationCounter));
                mFragmentTabMyTaskTabBinding.tvBadgeCount.setVisibility(View.VISIBLE);
            } else {
                mFragmentTabMyTaskTabBinding.tvBadgeCount.setVisibility(View.GONE);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.NEW_NOTIFICATION) {
            updateCounter();
        }
    }

    @Override
    public void onDestroy() {
        // Register Event Bus
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }
}
