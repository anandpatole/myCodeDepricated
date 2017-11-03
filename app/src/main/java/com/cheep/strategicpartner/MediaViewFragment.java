package com.cheep.strategicpartner;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cheep.R;
import com.cheep.databinding.FragmentMediaViewBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.strategicpartner.model.MediaModel;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

/**
 * Created by Giteeka on 18/8/17.
 * This Fragment is first step of Strategic partner screen.
 * Expandable list view of services
 * Single or multiple sub services selection according to specific partners features
 */

public class MediaViewFragment extends BaseFragment {
    FragmentMediaViewBinding mFragmentMediaViewBinding;
    private static final String TAG = MediaViewFragment.class.getSimpleName();
    private MediaModel mMediaModel;


    @SuppressWarnings("unused")
    public static MediaViewFragment newInstance(MediaModel mediaModel) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Utility.Extra.MODEL, mediaModel);
        MediaViewFragment mediaViewFragment = new MediaViewFragment();
        mediaViewFragment.setArguments(bundle);
        return mediaViewFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentMediaViewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_media_view, container, false);
        return mFragmentMediaViewBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        LogUtils.LOGD(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
    }


    @Override
    public void initiateUI() {
        LogUtils.LOGD(TAG, "initiateUI() called");
        if (getArguments() != null && getArguments().getSerializable(Utility.Extra.MODEL) != null) {
            mMediaModel = (MediaModel) getArguments().getSerializable(Utility.Extra.MODEL);

            String url = mMediaModel.mediaType.equalsIgnoreCase(MediaModel.MediaType.TYPE_VIDEO) ? mMediaModel.mediaThumbName : mMediaModel.mediaName;
            Glide.with(mContext)
                    .load(url)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            mFragmentMediaViewBinding.progress.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mFragmentMediaViewBinding.imgThumb);
            LogUtils.LOGD(TAG, "initiateUI() returned: thumbImage " + mMediaModel.mediaThumbName);
            LogUtils.LOGD(TAG, "initiateUI() returned: type " + mMediaModel.mediaType);
            mFragmentMediaViewBinding.imgPlay.setVisibility(mMediaModel.mediaType.equalsIgnoreCase(MediaModel.MediaType.TYPE_VIDEO) ? View.VISIBLE : View.GONE);
            mFragmentMediaViewBinding.imgPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LogUtils.LOGD(TAG, "onClick() returned mMediaModel.path: " + mMediaModel.mediaName);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mMediaModel.mediaName));
                    intent.setDataAndType(Uri.parse(mMediaModel.mediaName), "video/*");
                    startActivity(intent);
                }
            });
        } else {
            mFragmentMediaViewBinding.progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void setListener() {
        LogUtils.LOGD(TAG, "setListener() called");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


}
