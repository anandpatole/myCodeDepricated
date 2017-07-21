
package com.cheep.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cheep.R;
import com.cheep.activity.LoginActivity;
import com.cheep.utils.PreferenceUtility;

public class IntroImageFragment extends BaseFragment
{
    private static final String TAG = "CoverImageFragment";
    private ImageView img_intro;
    private ImageView img_get_started;
    private int introImage;

    public IntroImageFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void initiateUI()
    {
        img_intro.setImageResource(introImage);
        img_get_started.setVisibility(View.GONE);
        if(introImage==R.drawable.img_intro_5)
        {
            img_get_started.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setListener()
    {
        img_get_started.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                PreferenceUtility.getInstance(mContext).updateIntroScreenStatus(true);
                //Start the Login activity
                LoginActivity.newInstance(mContext);
                getActivity().finish();
            }
        });
    }

    @SuppressLint("ValidFragment")
    private IntroImageFragment(int introImage)
    {
        // Required empty public constructor
        this.introImage = introImage;
    }

    public static IntroImageFragment getInstance(int introImage) {
        return new IntroImageFragment(introImage);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.fragment_intro_image, container, false);
        img_intro = (ImageView) view.findViewById(R.id.img_intro);
        img_get_started= (ImageView) view.findViewById(R.id.img_get_started);
        initiateUI();
        setListener();
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach() called");
    }
}