package com.cheep.cheepcarenew.dialogs;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcarenew.adapters.DropDownCityAdapter;
import com.cheep.databinding.DialogCheepCareNotInCityBinding;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.model.CityModel;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import java.util.ArrayList;
//Webservice to search city for modaaal window
//
//        {{url}}/customers/profile/searchCity
//        param => search_text
//
//        Ashwin, 16:19
//        Webservice to add vote for cheep care city
//
//        {{url}}/customers/care/voteCityForCheepCare
//        param => phone_number, city_id

public class CheepCareNotInYourCityDialog extends DialogFragment {
    public static final String TAG = CheepCareNotInYourCityDialog.class.getSimpleName();
    private DialogCheepCareNotInCityBinding mDialogCheepCareNotInCityBinding;
    private AcknowledgementInteractionListener mListener;
    private ArrayList<CityModel> mCityList = new ArrayList<>();
    private DropDownCityAdapter dropDownAdapter;
    private BaseAppCompatActivity activity;
    private CityModel mSelectedCity;

    /*
    Empty Constructor
     */
    public CheepCareNotInYourCityDialog() {

    }

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static void newInstance(Context context, AcknowledgementInteractionListener listener) {
        CheepCareNotInYourCityDialog f = new CheepCareNotInYourCityDialog();
        f.setListener(listener);
//        Bundle args = new Bundle();
//        args.putString(Utility.Extra.DATA, carePackageName);
//        f.setArguments(args);
        // Supply num input as an argument.
        f.show(((AppCompatActivity) context).getSupportFragmentManager(), CheepCareNotInYourCityDialog.TAG);
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
        activity = (BaseAppCompatActivity) getContext();
        mDialogCheepCareNotInCityBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_cheep_care_not_in_city, container, false);
        dropDownAdapter = new DropDownCityAdapter(mCityList);
        callGetListWS();

        mDialogCheepCareNotInCityBinding.tvVote.setVisibility(View.VISIBLE);
//        mDialogCheepCareNotInCityBinding.viewVertical.setVisibility(View.VISIBLE);
        mDialogCheepCareNotInCityBinding.edtMobileNumber.setVisibility(View.VISIBLE);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.label_title_cheep_is_here));
        spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);

//        Drawable img = ContextCompat.getDrawable(getContext(), R.drawable.emoji_bird);
//        img.setBounds(0, 0, 25, 25);


//        ImageSpan span = new ImageSpan(img, ImageSpan.ALIGN_BOTTOM);
//        spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1
//                , spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//
//        mDialogCheepCareNotInCityBinding.title.setText(spannableStringBuilder);

        String text1 = getString(R.string.label_desc_cheep_care_not_in_city_1) ;
        String text2 = getString(R.string.label_desc_cheep_care_not_in_city_2);

        SpannableStringBuilder spanDesc1 = new SpannableStringBuilder(text1 + text2);
        Drawable heartImg = ContextCompat.getDrawable(getContext(), R.drawable.emoji_heart);
        heartImg.setBounds(0, 0, 30, 30);
        ImageSpan span1 = new ImageSpan(heartImg, ImageSpan.ALIGN_BASELINE);

        spanDesc1.setSpan(span1, text1.length() - 2
                , text1.length()-1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);


        Drawable handsImg = ContextCompat.getDrawable(getContext(), R.drawable.emoji_folded_hands);
        handsImg.setBounds(0, 0, 30, 30);
        ImageSpan span2 = new ImageSpan(handsImg, ImageSpan.ALIGN_BASELINE);
        spanDesc1.setSpan(span2, spanDesc1.length() - 1
                , spanDesc1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        mDialogCheepCareNotInCityBinding.textDescription.setText(spanDesc1);

        // Click event of Okay button
        mDialogCheepCareNotInCityBinding.tvVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verify())
                    onVoteClick();
            }
        });
        // Click event of Okay button
//        mDialogCheepCareNotInCityBinding.tvBookTask.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //Callback to activity
//                dismiss();
//                mListener.onAcknowledgementAccepted();
//            }
//        });

        mDialogCheepCareNotInCityBinding.edtCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCityList.isEmpty())
                    showDropDownMenu(v);
            }
        });

        mDialogCheepCareNotInCityBinding.edtMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                verify();
            }
        });

        return mDialogCheepCareNotInCityBinding.getRoot();

    }

    private boolean verify() {
        String mobileNo = mDialogCheepCareNotInCityBinding.edtMobileNumber.getText().toString();
        if (mobileNo.length() >= 10 && mSelectedCity != null) {
            mDialogCheepCareNotInCityBinding.tvVote.setTextColor(ContextCompat.getColor(activity, R.color.splash_gradient_end));
            return true;
        } else {
            mDialogCheepCareNotInCityBinding.tvVote.setTextColor(ContextCompat.getColor(activity, R.color.grey_varient_17));
            return false;
        }
    }

    private void callGetListWS() {
        if (!Utility.isConnected(getContext())) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mDialogCheepCareNotInCityBinding.getRoot());
            return;
        }
        activity.showProgressDialog();

        WebCallClass.getCity(getContext(), commonResponseListener, new WebCallClass.GetCityListListener() {
            @Override
            public void getCityNames(ArrayList<CityModel> list) {
                activity.hideProgressDialog();
                mCityList.addAll(list);
                dropDownAdapter.notifyDataSetChanged();
            }
        });
    }

    WebCallClass.CommonResponseListener commonResponseListener = new WebCallClass.CommonResponseListener() {
        @Override
        public void volleyError(VolleyError error) {
            activity.hideProgressDialog();
            Utility.showToast(activity, getString(R.string.label_something_went_wrong));

        }

        @Override
        public void showSpecificMessage(String message) {
            activity.hideProgressDialog();
            Utility.showToast(activity, message);

        }

        @Override
        public void forceLogout() {
            activity.hideProgressDialog();
        }
    };

    private void onVoteClick() {
        if (!Utility.isConnected(getContext())) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mDialogCheepCareNotInCityBinding.getRoot());
            return;
        }
        activity.showProgressDialog();

        WebCallClass.voteCityForCheepCare(getContext(), mDialogCheepCareNotInCityBinding.edtMobileNumber.getText().toString(), mSelectedCity.id
                , commonResponseListener, new WebCallClass.AddVoteForCheepCareCityListListener() {
                    @Override
                    public void onSuccessOfVote() {
                        activity.hideProgressDialog();
                        Utility.showToast(activity, getString(R.string.label_vote_for_city_registered_successflly, mSelectedCity.city));
                        dismiss();
                        mListener.onAcknowledgementAccepted();
                    }
                });

//        mDialogCheepCareNotInCityBinding.tvVote.setVisibility(View.GONE);
//        mDialogCheepCareNotInCityBinding.viewVertical.setVisibility(View.GONE);
//        mDialogCheepCareNotInCityBinding.edtMobileNumber.setVisibility(View.GONE);
//
//
//        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.label_title_we_hear_you));
//        spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
//        Drawable img = ContextCompat.getDrawable(getContext(), R.drawable.emoji_blue_heart);
//        img.setBounds(0, 0, 20, 20);
//
//        ImageSpan span = new ImageSpan(img, ImageSpan.ALIGN_BOTTOM);
//        spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1
//                , spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//
//        mDialogCheepCareNotInCityBinding.title.setText(spannableStringBuilder);
//        mDialogCheepCareNotInCityBinding.textDescription.setText(getString(R.string.label_desc_we_hear_you));

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

    private void showDropDownMenu(final View view) {
        Log.i(TAG, "showDropDownMenu: ");
        final View mFilterPopupWindow = View.inflate(view.getContext(), R.layout.layout_city_drop_down, null);

        final PopupWindow mPopupWindow = new PopupWindow(view.getContext());
        RecyclerView recyclerview = mFilterPopupWindow.findViewById(R.id.listMultipleChoice);
        recyclerview.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerview.setAdapter(dropDownAdapter);

        DropDownCityAdapter.ClickItem clickListener = new DropDownCityAdapter.ClickItem() {
            @Override
            public void clickItem(int i) {
                mSelectedCity = mCityList.get(i);
                verify();
                mDialogCheepCareNotInCityBinding.edtCity.setText(mSelectedCity.city);
                mPopupWindow.dismiss();
            }

            @Override
            public void dismissDialog() {
                mPopupWindow.dismiss();
            }
        };
        dropDownAdapter.setListener(clickListener);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setContentView(mFilterPopupWindow);
        mPopupWindow.setWidth(view.getWidth());
        mPopupWindow.setHeight(ListView.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);

        // No animation at present
        mPopupWindow.setAnimationStyle(0);

        // Displaying the popup at the specified location, + offsets.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mPopupWindow.showAsDropDown(view, 0, -view.getHeight(), Gravity.NO_GRAVITY);
        } else {
            mPopupWindow.showAsDropDown(view, 0, -view.getHeight());
        }
    }


}