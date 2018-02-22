package com.cheep.cheepcare.dialogs;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.DialogCheepCareNotInCityBinding;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

public class CheepCareNotInYourCityDialog extends DialogFragment {
    public static final String TAG = CheepCareNotInYourCityDialog.class.getSimpleName();
    private DialogCheepCareNotInCityBinding mDialogCheepCareNotInCityBinding;
    private AcknowledgementInteractionListener mListener;

    /*
    Empty Constructor
     */
    public CheepCareNotInYourCityDialog() {

    }

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static CheepCareNotInYourCityDialog newInstance(AcknowledgementInteractionListener listener) {
        CheepCareNotInYourCityDialog f = new CheepCareNotInYourCityDialog();
        f.setListener(listener);
//        Bundle args = new Bundle();
//        args.putString(Utility.Extra.DATA, carePackageName);
//        f.setArguments(args);
        // Supply num input as an argument.
        return f;
    }

    /**
     * Set Lister that would provide callback to called activity/fragment
     *
     * @param listener
     */
    public void setListener(AcknowledgementInteractionListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set Window Background as Transparent.
        if (getDialog() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        mDialogCheepCareNotInCityBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_cheep_care_not_in_city, container, false);


        mDialogCheepCareNotInCityBinding.tvVote.setVisibility(View.VISIBLE);
        mDialogCheepCareNotInCityBinding.viewVertical.setVisibility(View.VISIBLE);
        mDialogCheepCareNotInCityBinding.edtMobileNumber.setVisibility(View.VISIBLE);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.label_title_cheep_is_here));
        spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);


        ImageSpan span = new ImageSpan(getContext(), R.drawable.emoji_bird, ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1
                , spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        mDialogCheepCareNotInCityBinding.title.setText(spannableStringBuilder);

        String text1 = getString(R.string.label_desc_cheep_is_here_1) + Utility.ONE_CHARACTER_SPACE + Utility.ONE_CHARACTER_SPACE;
        String text2 = Utility.NEW_LINE + getString(R.string.label_desc_cheep_is_here_2) + Utility.ONE_CHARACTER_SPACE + Utility.ONE_CHARACTER_SPACE;

        SpannableStringBuilder spanDesc1 = new SpannableStringBuilder(text1 + text2);
        ImageSpan span1 = new ImageSpan(getContext(), R.drawable.emoji_thumbs_up, ImageSpan.ALIGN_BASELINE);
        spanDesc1.setSpan(span1, text1.length() - 1
                , text1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        ImageSpan span2 = new ImageSpan(getContext(), R.drawable.emoji_blue_heart, ImageSpan.ALIGN_BASELINE);
        spanDesc1.setSpan(span2, spanDesc1.length() - 1
                , spanDesc1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        mDialogCheepCareNotInCityBinding.textDescription.setText(spanDesc1);

        // Click event of Okay button
        mDialogCheepCareNotInCityBinding.tvVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onVoteClick();
            }
        });
        // Click event of Okay button
        mDialogCheepCareNotInCityBinding.tvBookTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Callback to activity
                mListener.onAcknowledgementAccepted();
                dismiss();
            }
        });
        return mDialogCheepCareNotInCityBinding.getRoot();
    }

    private void onVoteClick() {
        mDialogCheepCareNotInCityBinding.tvVote.setVisibility(View.GONE);
        mDialogCheepCareNotInCityBinding.viewVertical.setVisibility(View.GONE);
        mDialogCheepCareNotInCityBinding.edtMobileNumber.setVisibility(View.GONE);


        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.label_title_we_hear_you));
        spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
        Drawable img = ContextCompat.getDrawable(getContext(), R.drawable.emoji_blue_heart);
        img.setBounds(0, 0, 20, 20);

        ImageSpan span = new ImageSpan(img, ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1
                , spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        mDialogCheepCareNotInCityBinding.title.setText(spannableStringBuilder);
        mDialogCheepCareNotInCityBinding.textDescription.setText(getString(R.string.label_desc_we_hear_you));

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }


    @Override
    public void show(FragmentManager manager, String tag) {
        LogUtils.LOGE(TAG, "show: ");

        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.d(TAG, "Exception", e);
        }
        LogUtils.LOGE(TAG, "show: ------------------ ");

    }


}