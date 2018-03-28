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
import com.cheep.cheepcare.model.CheepCarePaymentDataModel;
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

    public double totalPrice = 0;
    public double payableAmount = 0;
    public boolean isYearly = true;
    private double gstRate = 0;
    private double discountPrice = 0;
    private double gstPrice = 0;
    private String cheepMateCode = "";
    private double bundledDiscountRate = 0;
    private double bundledDiscountPrice = 0;
    private double discountRate;

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
        resetPromoCodeValue();
        gstRate = Double.valueOf(mPackageCustomizationActivity.settingModel.gstRate);

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
                SelectPackageSpecificationsFragment fragment = (SelectPackageSpecificationsFragment) mPackageCustomizationActivity.mPackageCustomizationPagerAdapter.getItem(PackageCustomizationActivity.STAGE_1);
                mPackageCustomizationActivity.mSelectedPackageModel = packageModel;
                if (fragment != null) {
                    fragment.initiateUI();
                    mPackageCustomizationActivity.gotoStep(PackageCustomizationActivity.STAGE_1);
                }
            }

            @Override
            public void onRemovePackage(int position, PackageDetail packageModel) {

                if (mPackageAdapter.getList().size() == 1) {
                    mPackageCustomizationActivity.showAlertDialog(true);
                    return;
                }

                for (PackageDetail detail : mPackageCustomizationActivity.getPackageList())
                    if (detail.id.equalsIgnoreCase(packageModel.id)) {
                        detail.isSelected = false;
                        detail.mSelectedAddressList = null;
                        detail.packageOptionList = null;
                        mPackageAdapter.getList().remove(position);
                        calculateTotalPrice();
                    }
                mPackageCustomizationActivity.updateCartCount();
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
        resetMateCodeValue();
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
        mBinding.txtApplyPromoCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPackageAdapter.getList().size() > 1) {
                    Utility.showToast(mPackageCustomizationActivity, getString(R.string.label_error_promo_code_with_bundled_discount));
                } else {
                    showCheepCodeDialog();
                }
            }
        });
        mBinding.txtApplyMateCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCheepMateCodeDialog();
            }
        });

        mBinding.ivTickPromoCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isSelected()) {
                    resetPromoCodeValue();
                }
            }
        });
    }

    /**
     * apply bundled discount
     * apply GST
     * show final price
     */
    private void calculateTotalPrice() {
        totalPrice = 0;
        mBinding.ivHalfYearlyPackage.setSelected(!isYearly);
        mBinding.ivYearlyPackage.setSelected(isYearly);
        for (PackageDetail detail : mPackageAdapter.getList()) {
            LogUtils.LOGE(TAG, "calculateTotalPrice: -----------------");
            LogUtils.LOGE(TAG, "calculateTotalPrice: detail.yearlyPrice  " + detail.yearlyPrice);
            LogUtils.LOGE(TAG, "calculateTotalPrice: detail.halfYearlyPrice  " + detail.halfYearlyPrice);
            totalPrice += isYearly ? detail.yearlyPrice : detail.halfYearlyPrice;
        }
        LogUtils.LOGE(TAG, "calculateTotalPrice: totalPrice :: " + totalPrice);

        if (mPackageAdapter.getList().size() > 1) {
            // bundled discount
            int bundledPkgCnt = mPackageAdapter.getList().size() - 1;
            bundledDiscountRate = 5 * bundledPkgCnt;
            bundledDiscountPrice = totalPrice * bundledDiscountRate / 100;
            payableAmount = totalPrice - bundledDiscountPrice;
            LogUtils.LOGE(TAG, "calculateTotalPrice: discountPrice :: " + bundledDiscountPrice);
        } else {
            discountPrice = totalPrice * discountRate / 100;
            payableAmount = totalPrice - discountPrice;
            LogUtils.LOGE(TAG, "calculateTotalPrice: discountPrice :: " + discountPrice);
        }

        // apply GST RATE
        LogUtils.LOGE(TAG, "calculateTotalPrice: totalPrice :: " + totalPrice);

        gstPrice = payableAmount * gstRate / 100;
        payableAmount = payableAmount + gstPrice;


        LogUtils.LOGE(TAG, "calculateTotalPrice: gstPrice :: " + gstPrice);
        LogUtils.LOGE(TAG, "calculateTotalPrice: totalPrice :: " + totalPrice);


        mBinding.textTotal.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(String.valueOf(payableAmount))));
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
        edtCheepPromoCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        if (TextUtils.isEmpty(edtCheepPromoCode.getText().toString())) {
                            Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                            break;
                        }
                        validateCheepCode();
                        cheepCodeDialog.dismiss();

//       validateCheepCode(edtCheepPromoCode.getText().toString());
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        view.findViewById(R.id.btn_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtCheepPromoCode.getText().toString())) {
                    Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                    return;
                }
//                validateCheepCode(edtCheepcode.getText().toString());


                validateCheepCode();
                cheepCodeDialog.dismiss();

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
        edtCheepMateCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        if (TextUtils.isEmpty(edtCheepMateCode.getText().toString())) {
                            Utility.showToast(mContext, getString(R.string.validate_cheep_mate_code));
                            break;
                        }
                        validateCheepMateCode();
                        cheepCodeDialog.dismiss();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        view.findViewById(R.id.btn_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtCheepMateCode.getText().toString())) {
                    Utility.showToast(mContext, getString(R.string.validate_cheepcode));

                    return;

                }
                validateCheepMateCode();
                cheepCodeDialog.dismiss();

//                validateCheepCode(edtCheepcode.getText().toString());
                Utility.hideKeyboard(mPackageCustomizationActivity, edtCheepMateCode);


            }
        });
        cheepCodeDialog.setTitle(getString(R.string.label_cheep_mate_code));
        cheepCodeDialog.setCustomView(view);
        cheepCodeDialog.showDialog();
    }

    private void validateCheepMateCode() {
        cheepMateCode = edtCheepMateCode.getText().toString().trim();
        mBinding.ivTickMateCode.setSelected(true);
        mBinding.ivTickMateCode.setVisibility(View.VISIBLE);
        mBinding.ivInfoMateCode.setVisibility(View.INVISIBLE);
        mBinding.txtMateCodeMessage.setVisibility(View.INVISIBLE);
        mBinding.txtApplyMateCode.setText(cheepMateCode);
    }


    private void resetPromoCodeValue() {

        cheepCode = "";
        discountPrice = 0;
        discountRate = 0;
        calculateTotalPrice();
        mBinding.ivTickPromoCode.setVisibility(View.GONE);
        mBinding.ivInfoPromoCode.setVisibility(View.INVISIBLE);
        mBinding.txtPromoCodeMessage.setVisibility(View.INVISIBLE);
        mBinding.txtApplyPromoCode.setText(getString(R.string.hint_apply_cheep_promo_code));
    }

    private void resetMateCodeValue() {
        mBinding.ivTickMateCode.setVisibility(View.GONE);
        mBinding.ivInfoMateCode.setVisibility(View.INVISIBLE);
        mBinding.txtMateCodeMessage.setVisibility(View.INVISIBLE);
        mBinding.txtApplyPromoCode.setText(getString(R.string.hint_apply_cheep_mate_code));
    }


    private void validateCheepCode() {
        // TODO : remove this dummy validation
        cheepCode = edtCheepPromoCode.getText().toString().trim();


        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (userDetails != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();


        mParams.put(NetworkUtility.TAGS.CARE_CITY_ID, mPackageCustomizationActivity.mCareCityDetail.id);
        mParams.put(NetworkUtility.TAGS.CHEEP_CARE_CODE, cheepCode);
        mParams.put(NetworkUtility.TAGS.CARE_PACKAGE_ID, mPackageAdapter.getList().get(0).id);
        //Url is bas
        // ed on condition if address id is greater then 0 then it means we need to update the existing address
        String url = NetworkUtility.WS.CHECK_CHEEP_CARE_CODE;
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                , mCallValidateCheepCodeWSErrorListener
                , mCallValidateCheepCodeWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).

                addToRequestQueue(mVolleyNetworkRequestForSPList);

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

                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                        String rate = jsonData.optString(NetworkUtility.TAGS.DISCOUNT);
                        discountRate = Double.parseDouble(rate);
                        calculateTotalPrice();
                        mBinding.ivTickPromoCode.setVisibility(View.VISIBLE);
                        mBinding.ivTickPromoCode.setSelected(true);
                        mBinding.ivInfoPromoCode.setVisibility(View.GONE);
                        mBinding.txtPromoCodeMessage.setVisibility(View.VISIBLE);
                        mBinding.txtPromoCodeMessage.setText(getString(R.string.label_applied_promo_code_message_cheep_care, rate, "%"));
                        mBinding.txtApplyPromoCode.setText(cheepCode);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        discountRate = 0;
                        discountPrice = 0;
                        calculateTotalPrice();
                        mBinding.ivTickPromoCode.setVisibility(View.VISIBLE);
                        mBinding.ivTickPromoCode.setSelected(false);
                        mBinding.ivInfoPromoCode.setVisibility(View.VISIBLE);
                        mBinding.txtPromoCodeMessage.setVisibility(View.VISIBLE);
                        mBinding.txtPromoCodeMessage.setText(getString(R.string.label_invalid_code));
                        mBinding.txtApplyPromoCode.setText(cheepCode);

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

    public CheepCarePaymentDataModel getPaymentData() {
        CheepCarePaymentDataModel paymentDataModel = new CheepCarePaymentDataModel();
        paymentDataModel.bundlediscountPercent = bundledDiscountRate;
        paymentDataModel.bundlediscountPrice = bundledDiscountPrice;
//        paymentDataModel.careCityId = mPackageCustomizationActivity.mCareCityDetail.id;
        paymentDataModel.dsaCode = cheepMateCode;
        paymentDataModel.isAnnually = isYearly ? Utility.BOOLEAN.YES : Utility.BOOLEAN.NO;
        paymentDataModel.payableAmount = (double) Math.round(payableAmount * 100) / 100;
        paymentDataModel.totalAmount = totalPrice;
        paymentDataModel.promocode = cheepCode;
        paymentDataModel.promocodePrice = discountPrice;
        paymentDataModel.taxAmount = String.valueOf(gstPrice);
        return paymentDataModel;
    }


}