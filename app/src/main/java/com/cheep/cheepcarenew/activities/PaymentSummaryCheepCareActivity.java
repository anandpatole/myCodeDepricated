package com.cheep.cheepcarenew.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcarenew.model.CheepCarePaymentDataModel;
import com.cheep.cheepcarenew.model.CityLandingPageModel;
import com.cheep.cheepcarenew.model.PackageDetail;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.databinding.ActivityPaymentSummaryNewBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.ComparisionChart.ComparisionChartModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class PaymentSummaryCheepCareActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PaymentSummaryCheepCareActivity";

    private PackageDetail packageDetail;
    private AddressModel addressModel;
    private ComparisionChartModel comparisionChartModel;
    private CheepCarePaymentDataModel paymentDataModel;
    private CityLandingPageModel cityLandingPageModel;
    private ActivityPaymentSummaryNewBinding mBinding;
    private AppCompatEditText edtCheepPromoCode;
    private BottomAlertDialog cheepCodeDialog;
    private String cheepCode = "";
    private String cheepMateCode = "";
    private AppCompatEditText edtCheepMateCode;
    private double discountRate;
    double oldPrice = 0;
    double newPrice = 0;
    double profit = 0;
    private String packageId;
    private double totalPackageAmount;
    double discountAmount = 0.0;
    double taxtAmount = 0.0;
    private String selectedMonth;
    private double amountAfterDiscount;
    private String packageType;
    private String isHomeOrOffice;


    public static void newInstance(Context context) {
        Intent intent = new Intent(context, PaymentSummaryCheepCareActivity.class);
        context.startActivity(intent);
    }


    public static void newInstance(Context context, PackageDetail packageDetail, AddressModel addressModel) {
        Intent intent = new Intent(context, PaymentSummaryCheepCareActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(packageDetail));
        intent.putExtra(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(addressModel));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_summary_new);
        initiateUI();
        setListeners();
        setInitialColorOfCardView();
        updateSaveAmountForMonth();
        updatePrice(12);
        selectedMonth = "12";
        EventBus.getDefault().register(this);

    }

    @Override
    protected void initiateUI() {
        // Setting up Toolbar
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }

        if (getIntent() != null && getIntent().hasExtra(Utility.Extra.DATA) && getIntent().hasExtra(Utility.Extra.DATA_2)) {
            packageDetail = (PackageDetail) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), PackageDetail.class);
            addressModel = (AddressModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), AddressModel.class);
            Log.e(TAG, "initiateUI: -------------" + addressModel.address);

        }
        comparisionChartModel = PreferenceUtility.getInstance(mContext).getComparisonChatDetails();
        cityLandingPageModel = PreferenceUtility.getInstance(mContext).getCityLandingPageModel();

        setPrice();
        setAddress();
        setCityBannerData();

    }

    @Override
    protected void setListeners() {
        mBinding.card3Months.setOnClickListener(this);
        mBinding.card6Months.setOnClickListener(this);
        mBinding.card12Months.setOnClickListener(this);
        mBinding.rlPromoCode.setOnClickListener(this);
        mBinding.rlMateCode.setOnClickListener(this);
        mBinding.tvPayNow.setOnClickListener(this);
    }


    private void setPrice() {
        for (int i = 0; comparisionChartModel.priceLists.size() > i; i++) {

            String TYPE = comparisionChartModel.priceLists.get(i).type;

            if (TYPE.equalsIgnoreCase(PreferenceUtility.getInstance(mContext).getTypeOfPackage())) {
                packageType = TYPE;
                mBinding.tvNewPrice.setText(Utility.getCheepCarePackageMonthlyPrice(mBinding.tvNewPrice.getContext()
                        , R.string.rupee_symbol_x_package_price, comparisionChartModel.priceLists.get(i).newPrice));

                mBinding.tvOldPrice.setText(Utility.getCheepCarePackageMonthlyPrice(mBinding.tvOldPrice.getContext()
                        , R.string.rupee_symbol_x_package_price, comparisionChartModel.priceLists.get(i).oldPrice));


                newPrice = Double.parseDouble(comparisionChartModel.priceLists.get(i).newPrice);
                oldPrice = Double.parseDouble(comparisionChartModel.priceLists.get(i).oldPrice);

            } else if (TYPE.equalsIgnoreCase(PreferenceUtility.getInstance(mContext).getTypeOfPackage())) {
                packageType = TYPE;
                mBinding.tvNewPrice.setText(Utility.getCheepCarePackageMonthlyPrice(mBinding.tvNewPrice.getContext()
                        , R.string.rupee_symbol_x_package_price, comparisionChartModel.priceLists.get(i).newPrice));

                mBinding.tvOldPrice.setText(Utility.getCheepCarePackageMonthlyPrice(mBinding.tvOldPrice.getContext()
                        , R.string.rupee_symbol_x_package_price, comparisionChartModel.priceLists.get(i).oldPrice));

                newPrice = Double.parseDouble(comparisionChartModel.priceLists.get(i).newPrice);
                oldPrice = Double.parseDouble(comparisionChartModel.priceLists.get(i).oldPrice);
            }

        }
    }

    private void setAddress() {
        for (int i = 0; cityLandingPageModel.packageDetailList.size() > i; i++) {

            String TYPE = cityLandingPageModel.packageDetailList.get(i).type;

            if (TYPE.equalsIgnoreCase(PreferenceUtility.getInstance(mContext).getTypeOfPackage())) {
                mBinding.tvPackageName.setText(cityLandingPageModel.packageDetailList.get(i).title);
                mBinding.tvPackageDescription.setText(cityLandingPageModel.packageDetailList.get(i).subtitle);
                packageId = cityLandingPageModel.packageDetailList.get(i).id;

            } else if (TYPE.equalsIgnoreCase(PreferenceUtility.getInstance(mContext).getTypeOfPackage())) {
                mBinding.tvPackageName.setText(cityLandingPageModel.packageDetailList.get(i).title);
                mBinding.tvPackageDescription.setText(cityLandingPageModel.packageDetailList.get(i).subtitle);
                packageId = cityLandingPageModel.packageDetailList.get(i).id;
            }

        }

        if(addressModel.category.equalsIgnoreCase(NetworkUtility.TAGS.HOME)){
            isHomeOrOffice = Utility.HOME;
        }else{
            isHomeOrOffice = Utility.OFFICE;
        }

        final SpannableStringBuilder sb = new SpannableStringBuilder("  "+isHomeOrOffice +"  "+ addressModel.getAddressWithInitials());
        final ForegroundColorSpan fcs = new ForegroundColorSpan(getResources().getColor(R.color.splash_gradient_end));
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
        sb.setSpan(bss, 0, 6, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 6 characters Bold
        sb.setSpan(fcs, 0, 6, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        int resId = R.drawable.icon_address_home_active;
        switch (addressModel.category) {
            case NetworkUtility.TAGS.HOME:
                resId = R.drawable.icon_address_home_active;
                break;
            case NetworkUtility.TAGS.OFFICE:
                resId = R.drawable.icon_address_office_active;
                break;
        }


        ImageSpan span = new ImageSpan(mContext, resId, ImageSpan.ALIGN_BASELINE);
        sb.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
        sb.setSpan(span, 0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mBinding.tvAddress.setText(sb);

    }


    private void setCityBannerData() {
        ViewTreeObserver mViewTreeObserver = mBinding.ivCityImage.getViewTreeObserver();
        mViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.ivCityImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = mBinding.ivCityImage.getMeasuredWidth();
                ViewGroup.LayoutParams params = mBinding.ivCityImage.getLayoutParams();
                params.height = Utility.getHeightFromWidthForOneHalfIsToOneRatio(width);
                mBinding.ivCityImage.setLayoutParams(params);

                int resId = R.drawable.img_landing_screen_mumbai;
                switch (cityLandingPageModel.careCityDetail.citySlug) {
                    case NetworkUtility.CARE_CITY_SLUG.MUMBAI:
                        resId = R.drawable.img_landing_screen_mumbai;
                        break;
                    case NetworkUtility.CARE_CITY_SLUG.HYDRABAD:
                        resId = R.drawable.img_landing_screen_hydrabad;
                        break;
                    case NetworkUtility.CARE_CITY_SLUG.BENGALURU:
                        resId = R.drawable.img_landing_screen_bengaluru;
                        break;
                    case NetworkUtility.CARE_CITY_SLUG.DELHI:
                        resId = R.drawable.img_landing_screen_delhi;
                        break;
                    case NetworkUtility.CARE_CITY_SLUG.CHENNAI:
                        resId = R.drawable.img_landing_screen_chennai;
                        break;
                }

                // Load the image now.
                GlideUtility.loadImageView(mContext, mBinding.ivCityImage
                        , resId
                        , R.drawable.hotline_ic_image_loading_placeholder);
            }
        });

        Glide.with(mContext)
                .load(R.drawable.ic_home_with_heart_text)
                .asGif()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mBinding.ivCheepCareGif);



        mBinding.tvCityName.setText(cityLandingPageModel.careCityDetail.cityName);

        final SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getString(R.string.cheep_care_value));
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
        sb.setSpan(bss, 0, 5, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
        mBinding.tvCheepCare.setText(sb);


    }

    @SuppressLint("ResourceType")
    private void setInitialColorOfCardView() {
        mBinding.card3Months.setSelected(false);
        mBinding.tv3Month.setSelected(false);
        mBinding.tv3SaveMonth.setSelected(false);


        mBinding.card6Months.setSelected(false);
        mBinding.tv6Month.setSelected(false);
        mBinding.tv6SaveMonth.setSelected(false);

        mBinding.card12Months.setSelected(true);
        mBinding.tv12Month.setSelected(true);
        mBinding.tv12SaveMonth.setSelected(true);
    }

    private void updatePrice(int howManyMonth) {
        profit = oldPrice - newPrice;

        DecimalFormat formatter = new DecimalFormat("#,###");
        formatter.format(Double.valueOf(newPrice * howManyMonth));
        mBinding.tvMeanPackageAmount.setText(Utility.CHEEP_CARE.RS + formatter.format(Double.valueOf(newPrice* howManyMonth)) );
       // mBinding.tvMeanPackageAmount.setText(Utility.CHEEP_CARE.RS + String.valueOf(newPrice * howManyMonth));
        totalPackageAmount=newPrice * howManyMonth;
       // totalPackageAmount = Double.parseDouble(Utility.removeFirstChar(mBinding.tvMeanPackageAmount.getText().toString()));
        Log.e(TAG, "TOTAL_AMOUNT " + totalPackageAmount);
    }

    private void updateSaveAmountForMonth() {
        profit = oldPrice - newPrice;
        mBinding.tv3SaveMonth.setText(Utility.CHEEP_CARE.SAVE + String.valueOf(profit * 3));
        mBinding.tv6SaveMonth.setText(Utility.CHEEP_CARE.SAVE + String.valueOf(profit * 6));
        mBinding.tv12SaveMonth.setText(Utility.CHEEP_CARE.SAVE + String.valueOf(profit * 12));

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
                Utility.hideKeyboard(mContext, edtCheepMateCode);


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

    //Double.parseDouble(Utility.removeFirstChar(mBinding.tvMeanPackageAmount.getText().toString()));
    private void storeAllDataForPayment() {
        paymentDataModel = new CheepCarePaymentDataModel();
        paymentDataModel.totalAmount = totalPackageAmount;
        paymentDataModel.promocode = cheepCode;
        paymentDataModel.discountAmount = discountAmount;
        paymentDataModel.packageType = packageType;
        paymentDataModel.taxAmount = String.valueOf(taxtAmount);
        paymentDataModel.packageDuration = selectedMonth;
        paymentDataModel.dsaCode = cheepMateCode;
        paymentDataModel.paidAmount = totalPackageAmount ;//Double.parseDouble(Utility.removeFirstChar(mBinding.tvMeanPackageAmount.getText().toString()));
        paymentDataModel.packageId = packageId;
        paymentDataModel.packageTitle = packageDetail.title;
        paymentDataModel.addressId = addressModel.address_id;
        paymentDataModel.addressAssetTypeId = addressModel.addressSizeModel.id;

    }

    private void calculateDiscountPrice() {
        discountAmount = totalPackageAmount * discountRate / 100;

    }

    private void calculatePaidAmountPrice() {
        amountAfterDiscount = totalPackageAmount - discountAmount;
    }

    // View.OnClickListener
    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.card_3_months:
                mBinding.card3Months.setSelected(true);
                mBinding.tv3Month.setSelected(true);
                mBinding.tv3SaveMonth.setSelected(true);

                mBinding.card6Months.setSelected(false);
                mBinding.tv6Month.setSelected(false);
                mBinding.tv6SaveMonth.setSelected(false);

                mBinding.card12Months.setSelected(false);
                mBinding.tv12Month.setSelected(false);
                mBinding.tv12SaveMonth.setSelected(false);

                updatePrice(3);
                selectedMonth = "3";


                break;
            case R.id.card_6_months:
                mBinding.card3Months.setSelected(false);
                mBinding.tv3Month.setSelected(false);
                mBinding.tv3SaveMonth.setSelected(false);

                mBinding.card6Months.setSelected(true);
                mBinding.tv6Month.setSelected(true);
                mBinding.tv6SaveMonth.setSelected(true);

                mBinding.card12Months.setSelected(false);
                mBinding.tv12Month.setSelected(false);
                mBinding.tv12SaveMonth.setSelected(false);
                updatePrice(6);
                selectedMonth = "6";
                break;
            case R.id.card_12_months:
                mBinding.card3Months.setSelected(false);
                mBinding.tv3Month.setSelected(false);
                mBinding.tv3SaveMonth.setSelected(false);

                mBinding.card6Months.setSelected(false);
                mBinding.tv6Month.setSelected(false);
                mBinding.tv6SaveMonth.setSelected(false);

                mBinding.card12Months.setSelected(true);
                mBinding.tv12Month.setSelected(true);
                mBinding.tv12SaveMonth.setSelected(true);
                updatePrice(12);
                selectedMonth = "12";
                break;

            case R.id.rl_promo_code:
                showCheepCodeDialog();
                break;
            case R.id.rl_mate_code:
                showCheepMateCodeDialog();
                break;
            case R.id.tv_pay_now:
                storeAllDataForPayment();
                PaymentChoiceCheepCareActivity.newInstance(getApplicationContext(), "", paymentDataModel, cityLandingPageModel.careCityDetail,addressModel
                );
                break;

        }
    }


    private void validateCheepCode() {
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


        mParams.put(NetworkUtility.TAGS.CARE_CITY_ID, cityLandingPageModel.careCityDetail.id);
        mParams.put(NetworkUtility.TAGS.CHEEP_CARE_CODE, cheepCode);
        mParams.put(NetworkUtility.TAGS.CARE_PACKAGE_ID, packageId);
        // mParams.put(NetworkUtility.TAGS.CARE_PACKAGE_ID, mPackageAdapter.getList().get(0).id);
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

            Utility.hideKeyboard(mContext, edtCheepPromoCode);
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
        }
    };
    private Response.Listener mCallValidateCheepCodeWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            String strResponse = (String) response;
            try {
                Utility.hideKeyboard(mContext, edtCheepPromoCode);
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
                        calculateDiscountPrice();
                        mBinding.ivTickPromoCode.setVisibility(View.VISIBLE);
                        mBinding.ivTickPromoCode.setSelected(true);
                        mBinding.ivInfoPromoCode.setVisibility(View.GONE);
                        mBinding.txtPromoCodeMessage.setVisibility(View.VISIBLE);
                        mBinding.txtPromoCodeMessage.setText(getString(R.string.label_applied_promo_code_message_cheep_care, rate, "%"));
                        mBinding.txtApplyPromoCode.setText(cheepCode);
                        calculatePaidAmountPrice();
                        mBinding.tvMeanPackageAmount.setText(Utility.CHEEP_CARE.RS + amountAfterDiscount);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        discountRate = 0;
                        //discountPrice = 0;
                        // calculateTotalPrice();
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
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallValidateCheepCodeWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    /**
     * Event Bus Callbacks
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_SUCCESSFULLY:
                finish();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
