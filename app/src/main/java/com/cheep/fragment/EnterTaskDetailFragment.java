package com.cheep.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.TaskCreationActivity;
import com.cheep.addresspopupsfortask.AddressListDialog;
import com.cheep.addresspopupsfortask.AddressSelectionListener;
import com.cheep.cheepcare.adapter.SelectedSubServiceAdapter;
import com.cheep.cheepcare.dialogs.BottomAddAddressDialog;
import com.cheep.cheepcare.dialogs.CheepCareNotInYourCityDialog;
import com.cheep.cheepcare.dialogs.NotSubscribedAddressDialog;
import com.cheep.cheepcare.model.CareCityDetail;
import com.cheep.cheepcare.model.CityLandingPageModel;
import com.cheep.cheepcarenew.activities.LandingScreenPickPackageActivity;
import com.cheep.databinding.FragmentEnterTaskDetailBinding;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.dialogs.OutOfOfficeHoursDialog;
import com.cheep.dialogs.UrgentBookingDialog;
import com.cheep.model.AddressModel;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by bhavesh on 28/4/17.
 */

public class EnterTaskDetailFragment extends BaseFragment implements UrgentBookingDialog.UrgentBookingListener, OutOfOfficeHoursDialog.OutOfOfficeHoursListener {
    public static final String TAG = EnterTaskDetailFragment.class.getSimpleName();
    private FragmentEnterTaskDetailBinding mFragmentEnterTaskDetailBinding;
    private TaskCreationActivity mTaskCreationActivity;
    public boolean isTotalVerified = false;
    public boolean isTaskWhenVerified = false;
    public boolean isTaskWhereVerified = false;
    public SuperCalendar startDateTimeSuperCalendar = SuperCalendar.getInstance();
    private BottomAddAddressDialog dialog;
    private ArrayList<AddressModel> mAddressList;
    private boolean isClicked = false;
    public AddressModel mSelectedAddress;
    UrgentBookingDialog ugent_dialog;
    OutOfOfficeHoursDialog out_of_office_dialog;
    private String subscriptionType = Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE;

    private WebCallClass.CommonResponseListener commonErrorListener = new WebCallClass.CommonResponseListener() {
        @Override
        public void volleyError(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            hideProgressDialog();
            mTaskCreationActivity.hideProgressDialog();
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentEnterTaskDetailBinding.getRoot());
        }

        @Override
        public void showSpecificMessage(String message) {
            mTaskCreationActivity.hideProgressDialog();
            hideProgressDialog();
            Utility.showSnackBar(message, mFragmentEnterTaskDetailBinding.getRoot());
        }

        @Override
        public void forceLogout() {
            mTaskCreationActivity.hideProgressDialog();
            hideProgressDialog();
            mTaskCreationActivity.finish();
        }
    };

    @SuppressWarnings("unused")
    public static EnterTaskDetailFragment newInstance() {
        EnterTaskDetailFragment fragment = new EnterTaskDetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentEnterTaskDetailBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_enter_task_detail, container, false);
        return mFragmentEnterTaskDetailBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
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
        if (!isVisibleToUser || mTaskCreationActivity == null) {
            return;
        }
        if (isVisibleToUser)
            mTaskCreationActivity.showPostTaskButton(false, isTotalVerified);

        // Update Task related details
        updateTaskDetails();

        // Manage Task Verification
        updateTaskVerificationFlags();
    }


    private void updateTaskVerificationFlags() {
        // Check Whether because of any issues, activity reference is NULL or not.
        if (mTaskCreationActivity == null) {
            return;
        }


//        if (mTaskCreationActivity.getSubCatList() == null || mTaskCreationActivity.getSubCatList().isEmpty()) {
//            return;
//        }

        // Task Description


        // When Verification
        isTaskWhenVerified = !TextUtils.isEmpty(mFragmentEnterTaskDetailBinding.textTaskWhen.getText().toString().trim());

        // Where Verification
        isTaskWhereVerified = mSelectedAddress != null;

        updateFinalVerificationFlag();


    }

    private void updateFinalVerificationFlag() {
        if (isTaskWhenVerified && isTaskWhereVerified) {
            isTotalVerified = true;
        } else {
            isTotalVerified = false;
        }
        mTaskCreationActivity.setTaskState(TaskCreationActivity.STEP_TWO_NORMAL);

        // let activity know that post task button needs to be shown now.
        mTaskCreationActivity.showPostTaskButton(false, isTotalVerified);
    }

    private void updateTaskDetails() {
        //Update SubCategory
        if (mTaskCreationActivity != null && !mTaskCreationActivity.getSubCatList().isEmpty())
            mFragmentEnterTaskDetailBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mFragmentEnterTaskDetailBinding.recyclerView.setAdapter(new SelectedSubServiceAdapter(mTaskCreationActivity.getSubCatList()));
        mFragmentEnterTaskDetailBinding.textSubCategoryName.setText(mTaskCreationActivity.mJobCategoryModel.catName);
    }

    @Override
    public void initiateUI() {

        Log.d(TAG, "initiateUI() called");

        //Update Where lable with icon
        updateWhereLabelWithIcon(false);

        initAddressUI();


    }

    @Override
    public void setListener() {
        // On Click event of When
        mFragmentEnterTaskDetailBinding.lnTaskWhen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickerDialog();
            }
        });

        mFragmentEnterTaskDetailBinding.cvInstaBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkwhen()) {
                    updateFinalVerificationFlag();
                    Log.i("myLog", "" + isTotalVerified);
                    if (!mTaskCreationActivity.isValidationCompleted()) {
                        return;
                    }
                    mTaskCreationActivity.onInstaBookClickedNew();
                }
            }
        });

        mFragmentEnterTaskDetailBinding.tvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isClicked = true;
//                mFragmentEnterTaskDetailBinding.spinnerAddressSelection.performClick();
                showAddressDialog();
            }
        });

        mFragmentEnterTaskDetailBinding.lnTaskWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAddressList.size() < 2) {
                    showAddressDialog();
                }
            }
        });
    }

    private void showAddressDialog() {
        boolean needToTaskAddressSize = mTaskCreationActivity.mJobCategoryModel.catSlug.equalsIgnoreCase(Utility.CAT_SLUG_TYPES.PEST_CONTROL);
        Log.e(TAG, "showAddressDialog:needToTaskAddressSize  :   " + needToTaskAddressSize);
        AddressListDialog addressListDialog = AddressListDialog.newInstance(subscriptionType, needToTaskAddressSize, new AddressSelectionListener() {
            @Override
            public void onAddressSelection(AddressModel addressModel) {
//                fillAddressView(addressModel);

                if (!addressModel.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE)) {
                    Log.d(TAG, "onItemSelected: ");
//                    callWS(addressModel);
                    fillAddressView(addressModel);
                } else if (addressModel.is_subscribe.equals(Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE)) {
                    if (mTaskCreationActivity.mJobCategoryModel.isSubscribed.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                        // this is scribed task
                        checkAddressForCheepCareCity(addressModel);
                    }
                    else{
                        // this is Ad hoc t
                        fillAddressView(addressModel);
                    }
                }
            }

        });
        addressListDialog.show(mTaskCreationActivity.getSupportFragmentManager(), AddressListDialog.TAG);
    }

    private void checkAddressForCheepCareCity(AddressModel addressModel) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentEnterTaskDetailBinding.getRoot());
            return;
        }
        showProgressDialog();
        WebCallClass.isCityAvailableForCare(mTaskCreationActivity, addressModel.address_id, commonErrorListener, new WebCallClass.CityAvailableCheepCareListener() {
            @Override
            public void getCityDetails(final CareCityDetail careCityDetail) {
                hideProgressDialog();
                if (TextUtils.isEmpty(careCityDetail.citySlug)) {
                    CheepCareNotInYourCityDialog.newInstance(mContext, new AcknowledgementInteractionListener() {
                        @Override
                        public void onAcknowledgementAccepted() {
                        }
                    });
                } else {
                    NotSubscribedAddressDialog.newInstance(mContext, new NotSubscribedAddressDialog.DialogInteractionListener() {
                        @Override
                        public void onSubscribeClicked() {
                            if (!Utility.isConnected(mContext)) {
                                Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentEnterTaskDetailBinding.getRoot());
                                return;
                            }
                            showProgressDialog();
                            WebCallClass.getCityCareDetail(mContext, careCityDetail.citySlug, commonErrorListener, new WebCallClass.GetCityCareDataListener() {
                                @Override
                                public void getCityCareData(CityLandingPageModel cityLandingPageModel) {
                                    hideProgressDialog();
                                    LandingScreenPickPackageActivity.newInstance(mContext, cityLandingPageModel.careCityDetail, GsonUtility.getJsonStringFromObject(cityLandingPageModel.packageDetailList));
                                }
                            });
                        }

                        @Override
                        public void onNotNowClicked() {
                        }
                    });
                }
            }
        });

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof TaskCreationActivity) {
            mTaskCreationActivity = (TaskCreationActivity) activity;
        }
    }

    public void updateWhereLabelWithIcon(boolean isEnabled) {
        isTaskWhereVerified = isEnabled;
        if (isEnabled) {
            mFragmentEnterTaskDetailBinding.textWhere.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icon_task_where_active, 0, 0, 0);
            mFragmentEnterTaskDetailBinding.textWhere.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
            mFragmentEnterTaskDetailBinding.cvAddress.setVisibility(View.VISIBLE);
        } else {
            mFragmentEnterTaskDetailBinding.textWhere.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icon_task_where_inactive, 0, 0, 0);
            mFragmentEnterTaskDetailBinding.textWhere.setTextColor(ContextCompat.getColor(mContext, R.color.grey_varient_11));
            mFragmentEnterTaskDetailBinding.cvAddress.setVisibility(View.GONE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.PLACE_PICKER_REQUEST) {
            dialog.onActivityResult(resultCode, data);
            hideProgressDialog();
        }

    }


    private void showDateTimePickerDialog() {
// Get Current Date
        final Calendar c = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (view.isShown()) {
                    Log.d(TAG, "onDateSet() called with: view = [" + view + "], year = [" + year + "], monthOfYear = [" + monthOfYear + "], dayOfMonth = [" + dayOfMonth + "]");
                    startDateTimeSuperCalendar.set(Calendar.YEAR, year);
                    startDateTimeSuperCalendar.set(Calendar.MONTH, monthOfYear);
                    startDateTimeSuperCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePickerDialog();
                }
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
        superCalendar = SuperCalendar.getInstance();
        datePickerDialog.getDatePicker().setMinDate(superCalendar.getTimeInMillis());
    }

    public SuperCalendar superCalendar;

    private void showTimePickerDialog() {
// Get Current Time
        final Calendar c = Calendar.getInstance();
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(mContext,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (view.isShown()) {


                            Log.d(TAG, "onTimeSet() called with: view = [" + view + "], hourOfDay = [" + hourOfDay + "], minute = [" + minute + "]");

                            startDateTimeSuperCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            startDateTimeSuperCalendar.set(Calendar.MINUTE, minute);

                            superCalendar = SuperCalendar.getInstance();
                            superCalendar.setTimeInMillis(startDateTimeSuperCalendar.getTimeInMillis());
                            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

                            // Get date-time for next 3 hours
                            SuperCalendar calAfter3Hours = SuperCalendar.getInstance().getNext3HoursTime(false);

//                            TODO: This needs to Be UNCOMMENTED DO NOT FORGET
//                            if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase(Utility.DEBUG)) {
                            if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                if (superCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {


                                    //    Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours, "3"), mFragmentEnterTaskDetailBinding.getRoot());
                                    // mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                    // mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                    String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                            + getString(R.string.label_at)
                                            + startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
                                    mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
                                    mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
                                    updateTaskVerificationFlags();
                                    ugent_dialog = UrgentBookingDialog.newInstance("500", EnterTaskDetailFragment.this);
                                    ugent_dialog.show(getFragmentManager(), "Urgent Booking");
                                    ugent_dialog.setCancelable(false);
                                    return;
                                }
                            } else {
                                mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
                                updateTaskVerificationFlags();
                                return;

                            }
//                            }
                            try {
                                if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                    if (isTimeBetweenTwoTime(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_SS))) {
                                        String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                                + getString(R.string.label_at)
                                                + startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
                                        updateTaskVerificationFlags();
                                        out_of_office_dialog = OutOfOfficeHoursDialog.newInstance("500", EnterTaskDetailFragment.this);

                                        out_of_office_dialog.show(getFragmentManager(), "Urgent Booking");
                                        out_of_office_dialog.show(getFragmentManager(), OutOfOfficeHoursDialog.TAG);

                                        out_of_office_dialog.setCancelable(false);
                                        return;
                                    }
                                } else {
                                    mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                    mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                    Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
                                    updateTaskVerificationFlags();
                                    return;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                        + getString(R.string.label_at)
                                        + startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
                                mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
                                mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
                                updateTaskVerificationFlags();
                            } else {
                                mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
                                updateTaskVerificationFlags();
                            }
                        }
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        timePickerDialog.show();

    }
///////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////WHEN Feature [END]//////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initAddressUI() {

        mAddressList = new ArrayList<>();
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            ArrayList<AddressModel> subscibedAddressList = new ArrayList<>();
            if (mTaskCreationActivity.mJobCategoryModel.catSlug.equalsIgnoreCase(Utility.CAT_SLUG_TYPES.PEST_CONTROL)) {
                subscriptionType = Utility.ADDRESS_SUBSCRIPTION_TYPE.PREMIUM;
                ArrayList<AddressModel> userAddressList = PreferenceUtility.getInstance(mContext).getUserDetails().addressList;
                for (AddressModel addressModel : userAddressList) {
                    if (addressModel.is_subscribe.equalsIgnoreCase(subscriptionType)) {
                        subscibedAddressList.add(addressModel);
                    }
                }

            } else if (mTaskCreationActivity.mJobCategoryModel.isSubscribed.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                subscriptionType = Utility.ADDRESS_SUBSCRIPTION_TYPE.NORMAL;
                ArrayList<AddressModel> userAddressList = PreferenceUtility.getInstance(mContext).getUserDetails().addressList;
                for (AddressModel addressModel : userAddressList) {
                    if (!addressModel.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE)) {
                        subscibedAddressList.add(addressModel);
                    }
                }

            } else {
                subscriptionType = Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE;
                ArrayList<AddressModel> userAddressList = PreferenceUtility.getInstance(mContext).getUserDetails().addressList;
                for (AddressModel addressModel : userAddressList) {
                    if (addressModel.is_subscribe.equalsIgnoreCase(subscriptionType)) {
                        subscibedAddressList.add(addressModel);
                    }
                }
            }

            mAddressList.addAll(subscibedAddressList);
        }

        if (!mAddressList.isEmpty())
            fillAddressView(mAddressList.get(0));

        // add dummy select adderss at last position for "Add new Address" row
        mAddressList.add(new AddressModel() {{
            address = getString(R.string.label_select_address);
            address_id = "";
        }});

    }


    private void fillAddressView(AddressModel model) {
        updateWhereLabelWithIcon(true);

        mFragmentEnterTaskDetailBinding.ivHome.setImageDrawable(ContextCompat.getDrawable(mContext
                , Utility.getAddressCategoryBlueIcon(model.category)));

        // show address's nick name or nick name is null then show category

        mFragmentEnterTaskDetailBinding.tvAddressNickname.setText(model.getNicknameString(mTaskCreationActivity));
        mFragmentEnterTaskDetailBinding.tvLabelAddressSubscribed.setText(!model.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE)
                ? getString(R.string.label_address_subscribed)
                : Utility.EMPTY_STRING);

        mFragmentEnterTaskDetailBinding.tvAddress.setText(model.getAddressWithInitials());
        mSelectedAddress = model;
    }

    @Override
    public void onUrgentPayNow() {
        updateFinalVerificationFlag();
        Log.i("myLog", "" + isTotalVerified);
        if (!mTaskCreationActivity.isValidationCompleted()) {
            return;
        }
        mTaskCreationActivity.onInstaBookClickedNew();
    }


    public boolean isTimeBetweenTwoTime(String argCurrentTime) throws ParseException {
        String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
        //
        String argStartTime = mContext.getResources().getString(R.string.start_time);
        String argEndTime = getString(R.string.end_time);
        if (argStartTime.matches(reg) && argEndTime.matches(reg)
                && argCurrentTime.matches(reg)) {
            boolean valid = false;
            // Start Time
            java.util.Date startTime = new SimpleDateFormat(Utility.DATE_FORMAT_HH_MM_SS)
                    .parse(argStartTime);
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startTime);

            // Current Time
            java.util.Date currentTime = new SimpleDateFormat(Utility.DATE_FORMAT_HH_MM_SS)
                    .parse(argCurrentTime);
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(currentTime);

            // End Time
            java.util.Date endTime = new SimpleDateFormat(Utility.DATE_FORMAT_HH_MM_SS)
                    .parse(argEndTime);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endTime);

            //
            if (currentTime.compareTo(endTime) < 0) {

                currentCalendar.add(Calendar.DATE, 1);
                currentTime = currentCalendar.getTime();

            }

            if (startTime.compareTo(endTime) < 0) {

                startCalendar.add(Calendar.DATE, 1);
                startTime = startCalendar.getTime();

            }
            //
            if (currentTime.before(startTime)) {

                System.out.println(" Time is Lesser ");

                valid = false;
            } else {

                if (currentTime.after(endTime)) {
                    endCalendar.add(Calendar.DATE, 1);
                    endTime = endCalendar.getTime();

                }

                System.out.println("Comparing , Start Time /n " + startTime);
                System.out.println("Comparing , End Time /n " + endTime);
                System.out
                        .println("Comparing , Current Time /n " + currentTime);

                if (currentTime.before(endTime)) {
                    System.out.println("RESULT, Time lies b/w");
                    valid = true;
                } else {
                    valid = false;
                    System.out.println("RESULT, Time does not lies b/w");
                }

            }
            return valid;

        } else {
            throw new IllegalArgumentException(
                    "Not a valid time, expecting HH:MM:SS format");
        }

    }

    @Override
    public void onOutofOfficePayNow() {
        updateFinalVerificationFlag();
        Log.i("myLog", "" + isTotalVerified);
        if (!mTaskCreationActivity.isValidationCompleted()) {
            return;
        }
        mTaskCreationActivity.onInstaBookClickedNew();
    }

    public boolean checkwhen() {
        //  Log.d(TAG, "onTimeSet() called with: view = [" + view + "], hourOfDay = [" + hourOfDay + "], minute = [" + minute + "]");

        // startDateTimeSuperCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        //  startDateTimeSuperCalendar.set(Calendar.MINUTE, minute);

        superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeInMillis(startDateTimeSuperCalendar.getTimeInMillis());
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

        // Get date-time for next 3 hours
        SuperCalendar calAfter3Hours = SuperCalendar.getInstance().getNext3HoursTime(false);

//                            TODO: This needs to Be UNCOMMENTED DO NOT FORGET
//                            if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase(Utility.DEBUG)) {
        if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
            if (superCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {


                //    Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours, "3"), mFragmentEnterTaskDetailBinding.getRoot());
                // mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                // mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                        + getString(R.string.label_at)
                        + startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
                mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
                mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
                updateTaskVerificationFlags();
                ugent_dialog = UrgentBookingDialog.newInstance("500", EnterTaskDetailFragment.this);
                ugent_dialog.show(getFragmentManager(), "Urgent Booking");
                ugent_dialog.setCancelable(false);
                return false;
            }
        } else {
            mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
            mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
            Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
            updateTaskVerificationFlags();
            return false;

        }
//                            }
        try {
            if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                if (isTimeBetweenTwoTime(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_SS))) {
                    String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                            + getString(R.string.label_at)
                            + startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
                    mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
                    mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
                    updateTaskVerificationFlags();
                    out_of_office_dialog = OutOfOfficeHoursDialog.newInstance("500", EnterTaskDetailFragment.this);
                    out_of_office_dialog.show(getFragmentManager(), "Urgent Booking");
                    out_of_office_dialog.setCancelable(false);
                    return false;
                }
            } else {
                mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
                updateTaskVerificationFlags();
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
            String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                    + getString(R.string.label_at)
                    + startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
            mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
            mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
            updateTaskVerificationFlags();
            return true;
        } else {
            mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
            mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
            Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
            updateTaskVerificationFlags();
            return false;
        }
    }
}
