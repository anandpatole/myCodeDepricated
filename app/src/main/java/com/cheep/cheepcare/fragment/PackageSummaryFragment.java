package com.cheep.cheepcare.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.activity.PackageCustomizationActivity;
import com.cheep.cheepcare.adapter.SelectedPackageSummaryAdapter;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.databinding.FragmentPackageSummaryBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageSummaryFragment extends BaseFragment {

    public static final String TAG = "PackageSummaryFragment";
    private PackageCustomizationActivity mPackageCustomizationActivity;
    private FragmentPackageSummaryBinding mBinding;
    private boolean isVerified = false;
    private SelectedPackageSummaryAdapter mPackageAdapter;
    private AppCompatEditText edtCheepPromoCode;
    private AppCompatEditText edtCheepMateCode;
    private BottomAlertDialog cheepCodeDialog;
    private String cheepCode;

    public double totalPrice;
    public double payableAmount;
    public boolean isYearly = true;
    private double rate;
    private double gstRate;
    private double discountPrice;
    private double gstPrice;

    public static PackageSummaryFragment newInstance() {
        return new PackageSummaryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_package_summary, container, false);
        return mBinding.getRoot();
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
        Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
        if (!isVisibleToUser || mPackageCustomizationActivity == null) {
            return;
        }

        if (isVerified) {
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_THREE_VERIFIED);
        } else {
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_THREE_UNVERIFIED);
        }
        resetMateCodeValue();
        resetPromoCodeValue();

        if (mPackageAdapter != null && mPackageAdapter.getList() != null) {
            mPackageAdapter.getList().clear();
            mPackageAdapter.addPakcageList(getList());
            calculateTotalPrice();
        }


        // Hide the post task button
//        mPackageCustomizationActivity.showPostTaskButton(false, false);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof PackageCustomizationActivity) {
            mPackageCustomizationActivity = (PackageCustomizationActivity) activity;
        }
    }

    @Override
    public void initiateUI() {

        mBinding.rvBundlePackages.setNestedScrollingEnabled(false);

        // package recycler view item click listener
        mPackageAdapter = new SelectedPackageSummaryAdapter(new SelectedPackageSummaryAdapter.PackageItemClickListener() {

            @Override
            public void onPackageItemClick(int position, PackageDetail packageModel) {

            }

            @Override
            public void onRemovePackage(int position, PackageDetail packageModel) {

                for (PackageDetail detail : mPackageCustomizationActivity.getPackageList())
                    if (detail.id.equalsIgnoreCase(packageModel.id)) {
                        detail.isSelected = false;
                        detail.mSelectedAddress = null;
                        detail.packageOptionList = null;
                        mPackageAdapter.getList().remove(position);
                        calculateTotalPrice();
                    }
                mPackageAdapter.notifyDataSetChanged();
            }
        });

        mPackageAdapter.addPakcageList(getList());

        mBinding.rvBundlePackages.setLayoutManager(new LinearLayoutManager(
                mContext
                , LinearLayoutManager.VERTICAL
                , false
        ));

        mBinding.rvBundlePackages.setAdapter(mPackageAdapter);
        mBinding.ivHalfYearlyPackage.setSelected(!isYearly);
        mBinding.ivYearlyPackage.setSelected(isYearly);
        calculateTotalPrice();
    }

    @Override
    public void setListener() {
        mBinding.rlHalfYearlyPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYearly = false;
                calculateTotalPrice();
            }
        });


        mBinding.rlYearlyPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYearly = true;
                calculateTotalPrice();

            }
        });
    }

    private void calculateTotalPrice() {
        totalPrice = 0;
        mBinding.ivHalfYearlyPackage.setSelected(!isYearly);
        mBinding.ivYearlyPackage.setSelected(isYearly);
        for (PackageDetail detail : mPackageAdapter.getList()) {
            if (isYearly) {
                totalPrice += detail.yearlyPrice;
            } else {
                totalPrice += detail.halfYearlyPrice;
            }
        }

        LogUtils.LOGE(TAG, "calculateTotalPrice: totalPrice :: " + totalPrice);
        mBinding.textTotal.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(String.valueOf(totalPrice))));
    }

    private List<PackageDetail> getList() {
        ArrayList<PackageDetail> newList = new ArrayList<>();
        for (PackageDetail model : mPackageCustomizationActivity.getPackageList()) {
            if (model.isSelected) {
                newList.add(model);
            }
        }
        return newList;
    }

    private void showCheepCodeDialog() {
        View view = View.inflate(mContext, R.layout.dialog_add_promocode, null);
        edtCheepPromoCode = view.findViewById(R.id.edit_cheepcode);
        cheepCodeDialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtCheepPromoCode.getText().toString())) {
                    Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                    return;
                }
//                validateCheepCode(edtCheepcode.getText().toString());


                applyDiscount();


            }
        });
        edtCheepPromoCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        if (TextUtils.isEmpty(edtCheepPromoCode.getText().toString())) {
                            Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                            break;
                        }
                        validateCheepCode(edtCheepPromoCode.getText().toString());
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        cheepCodeDialog.setTitle(getString(R.string.label_cheepcode));
        cheepCodeDialog.setCustomView(view);
        cheepCodeDialog.showDialog();
    }


    private void showCheepMateCodeDialog() {
        View view = View.inflate(mContext, R.layout.dialog_add_promocode, null);
        edtCheepMateCode = view.findViewById(R.id.edit_cheepcode);
        edtCheepMateCode.setHint(R.string.hint_apply_cheep_mate_code);
        cheepCodeDialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtCheepMateCode.getText().toString())) {
                    Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                    return;
                }
//                validateCheepCode(edtCheepcode.getText().toString());
                Utility.hideKeyboard(mPackageCustomizationActivity, edtCheepMateCode);
                hideErrorMateCode();


            }
        });
        edtCheepMateCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        if (TextUtils.isEmpty(edtCheepMateCode.getText().toString())) {
                            Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                            break;
                        }
                        hideErrorMateCode();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        cheepCodeDialog.setTitle(getString(R.string.label_cheepcode));
        cheepCodeDialog.setCustomView(view);
        cheepCodeDialog.showDialog();
    }


    private void resetPromoCodeValue() {
        mBinding.ivInfoPromoCode.setVisibility(View.GONE);
        mBinding.ivTickPromoCode.setVisibility(View.INVISIBLE);
        mBinding.txtPromoCodeMessage.setVisibility(View.INVISIBLE);
        mBinding.txtApplyPromoCode.setText(getString(R.string.hint_apply_cheep_promo_code));
    }

    private void resetMateCodeValue() {
        mBinding.ivTickMateCode.setVisibility(View.INVISIBLE);
        mBinding.ivInfoMateCode.setVisibility(View.GONE);
        mBinding.txtMateCodeMessage.setVisibility(View.INVISIBLE);
        mBinding.txtApplyPromoCode.setText(getString(R.string.hint_apply_cheep_mate_code));

    }

    private void hideErrorPromoCode() {
        mBinding.ivTickPromoCode.setVisibility(View.VISIBLE);
        mBinding.ivTickPromoCode.setSelected(true);

        mBinding.ivInfoPromoCode.setVisibility(View.INVISIBLE);
        mBinding.txtPromoCodeMessage.setVisibility(View.INVISIBLE);

        mBinding.txtApplyPromoCode.setText(edtCheepPromoCode.getText().toString().trim());
    }


    private void hideErrorMateCode() {
        mBinding.ivTickPromoCode.setVisibility(View.VISIBLE);
        mBinding.ivTickPromoCode.setSelected(true);

        mBinding.ivInfoPromoCode.setVisibility(View.INVISIBLE);
        mBinding.txtPromoCodeMessage.setVisibility(View.INVISIBLE);

        mBinding.txtApplyMateCode.setText(edtCheepMateCode.getText().toString().trim());
    }

    private void showErrorPromoCode() {
        mBinding.ivInfoPromoCode.setVisibility(View.VISIBLE);
        mBinding.ivTickPromoCode.setVisibility(View.VISIBLE);
        mBinding.ivTickPromoCode.setSelected(false);
        mBinding.txtPromoCodeMessage.setVisibility(View.VISIBLE);
        mBinding.txtPromoCodeMessage.setText(getString(R.string.label_invalid_code));
    }

    private void showErrorMateCode() {
        mBinding.ivInfoMateCode.setVisibility(View.VISIBLE);
        mBinding.txtMateCodeMessage.setVisibility(View.VISIBLE);
        mBinding.ivTickMateCode.setVisibility(View.VISIBLE);
        mBinding.ivTickMateCode.setSelected(false);
        mBinding.txtMateCodeMessage.setText(getString(R.string.label_invalid_code));
    }


    private void applyDiscount() {

        // TODO : remove this dummy validation

        if (edtCheepPromoCode.getText().toString().trim().equalsIgnoreCase("GOCHEEP")) {
            rate = 10;

            gstRate = 18;

            discountPrice = totalPrice * (rate / 100);

            payableAmount = totalPrice - discountPrice;

            gstPrice = payableAmount * (gstRate / 100);

            payableAmount = payableAmount + gstRate;

            hideErrorPromoCode();

            mBinding.textTotal.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(String.valueOf(payableAmount))));

        } else {
            showErrorPromoCode();
        }


    }

    private void validateCheepCode(String cheepCode) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        String url = NetworkUtility.WS.VALIDATE_CHEEP_CODE;
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                , mCallValidateCheepCodeWSErrorListener
                , mCallValidateCheepCodeWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    private Response.ErrorListener mCallValidateCheepCodeWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            Utility.hideKeyboard(mPackageCustomizationActivity, edtCheepPromoCode);
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
        }
    };
    private Response.Listener mCallValidateCheepCodeWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            String strResponse = (String) response;
            try {
                Utility.hideKeyboard(mPackageCustomizationActivity, edtCheepPromoCode);
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        if (edtCheepPromoCode != null) {
                            cheepCode = edtCheepPromoCode.getText().toString().trim();
                            cheepCodeDialog.dismiss();

                            String total = jsonObject.optString(NetworkUtility.TAGS.QUOTE_AMOUNT);
                            String discount = jsonObject.optString(NetworkUtility.TAGS.DISCOUNT_AMOUNT);
                            String payable = jsonObject.optString(NetworkUtility.TAGS.PAYABLE_AMOUNT);


                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showToast(mContext, error_message);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        mPackageCustomizationActivity.finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallValidateCheepCodeWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };


}