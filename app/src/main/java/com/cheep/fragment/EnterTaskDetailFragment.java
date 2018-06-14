package com.cheep.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.TaskCreationActivity;
import com.cheep.adapter.AddressRecyclerViewAdapter;
import com.cheep.cheepcare.adapter.SelectedSubServiceAdapter;
import com.cheep.cheepcare.dialogs.BottomAddAddressDialog;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.FragmentEnterTaskDetailBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.MediaModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhavesh on 28/4/17.
 */

public class EnterTaskDetailFragment extends BaseFragment {
    public static final String TAG = EnterTaskDetailFragment.class.getSimpleName();
    private FragmentEnterTaskDetailBinding mFragmentEnterTaskDetailBinding;
    private TaskCreationActivity mTaskCreationActivity;
    public boolean isTotalVerified = false;

    public boolean isTaskWhenVerified = false;
    public boolean isTaskWhereVerified = false;
    //    public MediaRecycleAdapter mMediaRecycleAdapter;
    // For When
    public SuperCalendar startDateTimeSuperCalendar = SuperCalendar.getInstance();
    private BottomAddAddressDialog dialog;
    private int numberOfMedia = 0;
    private ArrayList<MediaModel> mediaList;

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

        if (mTaskCreationActivity.getSubCatList() == null || mTaskCreationActivity.getSubCatList().isEmpty()) {
            return;
        }

        // Task Description

        // When Verification
        isTaskWhenVerified = !TextUtils.isEmpty(mFragmentEnterTaskDetailBinding.textTaskWhen.getText().toString().trim());

        // Where Verification
//        isTaskWhereVerified = !TextUtils.isEmpty(mFragmentEnterTaskDetailBinding.textTaskWhere.getText().toString().trim());

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
        updateWhereLabelWithIcon(false, Utility.EMPTY_STRING);


        // On Click event of When
        mFragmentEnterTaskDetailBinding.lnTaskWhen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickerDialog();
            }
        });

        // On Click event of Where
        mFragmentEnterTaskDetailBinding.lnTaskWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Validate When first
                if (!isTaskWhenVerified) {
                    Utility.showSnackBar(getString(R.string.validate_date), mFragmentEnterTaskDetailBinding.getRoot());
                    return;
                }

                showAddressDialog();
            }
        });


        mFragmentEnterTaskDetailBinding.cvInstaBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFinalVerificationFlag();
                Log.i("myLog", "" + isTotalVerified);
                if (!mTaskCreationActivity.isValidationCompleted()) {
                    return;
                }
                mTaskCreationActivity.onInstaBookClickedNew();
            }
        });

    }

    @Override
    public void setListener() {

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof TaskCreationActivity) {
            mTaskCreationActivity = (TaskCreationActivity) activity;
        }
    }

    public void updateWhereLabelWithIcon(boolean isEnabled, String whereValue) {
        if (!isEnabled) {
            mFragmentEnterTaskDetailBinding.textWhere.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icon_task_where_inactive, 0, 0, 0);
            mFragmentEnterTaskDetailBinding.textWhere.setTextColor(ContextCompat.getColor(mContext, R.color.grey_varient_11));
            mFragmentEnterTaskDetailBinding.cvAddress.setVisibility(View.GONE);
            Log.e(TAG, "updateWhereLabelWithIcon: not enabled");
        } else {
//            mFragmentEnterTaskDetailBinding.iconTaskWhere.setImageResource(R.drawable.ic_icon_task_where_active);
            mFragmentEnterTaskDetailBinding.textWhere.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icon_task_where_active, 0, 0, 0);
            mFragmentEnterTaskDetailBinding.textWhere.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
            mFragmentEnterTaskDetailBinding.cvAddress.setVisibility(View.VISIBLE);
            Log.e(TAG, "updateWhereLabelWithIcon: enabled");
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
                            if (superCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {
                                Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours, "3"), mFragmentEnterTaskDetailBinding.getRoot());
                                mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                updateTaskVerificationFlags();
                                return;
                            }
//                            }

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


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////WHERE Feature [START]/////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private BottomAlertDialog addressDialog;
    private AddressRecyclerViewAdapter addressRecyclerViewAdapter;
    @Nullable
    public AddressModel mSelectedAddressModel;

    private void showAddressDialog() {
        View view = View.inflate(mContext, R.layout.dialog_choose_address_new_task, null);
        boolean shouldOpenAddAddress = fillAddressRecyclerView((RecyclerView) view.findViewById(R.id.recycler_view));
        addressDialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_add_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomAddressDialog(null);
//                addAddressDialog.dismiss();
            }
        });
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addressRecyclerViewAdapter != null && !addressRecyclerViewAdapter.getmList().isEmpty()) {
                    AddressModel model = addressRecyclerViewAdapter.getSelectedAddress();
                    if (model != null) {
                        String address;
                        address = model.getAddressWithInitials();
                        mSelectedAddressModel = model;
                        updateWhereLabelWithIcon(true, address);
                        updateTaskVerificationFlags();
                        addressDialog.dismiss();
                    }


                }
            }
        });
        addressDialog.setTitle(getString(R.string.label_address));
        addressDialog.setCustomView(view);
        addressDialog.setExpandedInitially(true);
        addressDialog.showDialog();

        if (shouldOpenAddAddress) {
            addressDialog.view.findViewById(R.id.btn_submit).setVisibility(View.GONE);
            showBottomAddressDialog(null);
        }
    }

    /**
     * Loads address in choose address dialog box in recycler view
     */
    private String TEMP_ADDRESS_ID = "";

    private boolean fillAddressRecyclerView(RecyclerView recyclerView) {

        ArrayList<AddressModel> addressList;

        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            addressList = PreferenceUtility.getInstance(mContext).getUserDetails().addressList;
        } else {

            addressList = PreferenceUtility.getInstance(mContext).getGuestUserDetails().addressList;
        }
        //Setting RecyclerView Adapter
        addressRecyclerViewAdapter = new AddressRecyclerViewAdapter(addressList, new AddressRecyclerViewAdapter.AddressItemInteractionListener() {
            @Override
            public void onEditClicked(AddressModel model, int position) {
                TEMP_ADDRESS_ID = model.address_id;
                showBottomAddressDialog(model);
            }

            @Override
            public void onDeleteClicked(AddressModel model, int position) {
//                TEMP_ADDRESS_ID = model.address_id;
//                callDeleteAddressWS(model.address_id);
                showAddressDeletionConfirmationDialog(model);
            }

            @Override
            public void onRowClicked(AddressModel model, int position) {

            }
        });
        addressRecyclerViewAdapter.setSelectedAddressId(mSelectedAddressModel == null ? Utility.EMPTY_STRING : mSelectedAddressModel.address_id);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(addressRecyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_16dp)));

        //Here we are checking if address is not there then open add address dialog immediatly
        return addressList == null || (addressList != null && addressList.isEmpty());
    }
///////////////////////////// DELETE CONFIRMATION DIALOG//////////////////////////////////////

    private void showAddressDeletionConfirmationDialog(final AddressModel model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.cheep_all_caps));
        builder.setMessage(getString(R.string.label_address_delete_message));
        builder.setPositiveButton(getString(R.string.label_Ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick() called with: dialogInterface = [" + dialogInterface + "], i = [" + i + "]");
                TEMP_ADDRESS_ID = model.address_id;
                callDeleteAddressWS(model);
            }
        });
        builder.setNegativeButton(getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick() called with: dialogInterface = [" + dialogInterface + "], i = [" + i + "]");
            }
        });
        builder.show();
    }

    private BottomAlertDialog addAddressDialog;
    private TextView edtAddress;
    private EditText edtAddressInitials;
    private LinearLayout ln_pick_your_location;
    private LinearLayout ln_address_row;
    private Button btnAdd;
    private boolean isAddressNameVerified = false;
    private boolean isAddressPickYouLocationVerified = false;
    private boolean isAddressFlatNoVerified = false;

/*
    private void showAddAddressDialog(final AddressModel addressModel) {
        if (addressModel == null) {
            isAddressPickYouLocationVerified = false;
            isAddressNameVerified = false;
        } else {
            isAddressPickYouLocationVerified = true;
            isAddressNameVerified = true;
        }

        View view = View.inflate(mContext, R.layout.dialog_add_address, null);
        final RadioButton radioHome = (RadioButton) view.findViewById(R.id.radio_home);
        final RadioButton radio_office = (RadioButton) view.findViewById(R.id.radio_office);
        final RadioButton radioOther = (RadioButton) view.findViewById(R.id.radio_other);
//        final EditText edtName = (EditText) view.findViewById(R.id.edit_name);
        edtAddress = (TextView) view.findViewById(edit_address);
        edtAddressInitials = (EditText) view.findViewById(edit_address_initials);
        ln_pick_your_location = (LinearLayout) view.findViewById(R.id.ln_pick_your_location);
        ln_address_row = (LinearLayout) view.findViewById(R.id.ln_address_row);

        */
/*edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtName.getText().toString().trim().length() > 0) {
                    isAddressNameVerified = true;
                    checkAddAddressVerified();
                } else {
                    isAddressNameVerified = false;
                    checkAddAddressVerified();
                }
            }
        });*//*


        edtAddressInitials.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtAddressInitials.getText().toString().trim().length() > 0) {
                    isAddressFlatNoVerified = true;
                    checkAddAddressVerified();
                } else {
                    isAddressFlatNoVerified = false;
                    checkAddAddressVerified();
                }
            }
        });

        ln_pick_your_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePickerDialog(false);
            }
        });

        edtAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePickerDialog(false);
            }
        });

        if (addressModel != null) {
            ln_address_row.setVisibility(View.VISIBLE);
            ln_pick_your_location.setVisibility(View.GONE);
        } else {
            ln_address_row.setVisibility(View.GONE);
            ln_pick_your_location.setVisibility(View.VISIBLE);
        }
       */
/* edtAddress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    final int DRAWABLE_LEFT = 0;
                    final int DRAWABLE_TOP = 1;
                    final int DRAWABLE_RIGHT = 2;
                    final int DRAWABLE_BOTTOM = 3;

                    if (edtAddress.getTag() != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (event.getRawX() >= (edtAddress.getRight() - edtAddress.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // your action here
                            showPlacePickerDialog(false);
                            return true;
                        }
                    } else if (edtAddress.getTag() == null) {
                        showPlacePickerDialog(false);
                        return true;
                    }
                }
                return false;
            }
        });*//*

        btnAdd = (Button) view.findViewById(R.id.btn_add);

        if (addressModel != null) {
            if (NetworkUtility.TAGS.ADDRESS_TYPE.HOME.equalsIgnoreCase(addressModel.category)) {
                radioHome.setChecked(true);
//                radioHome.setSelected(true);
            } else if (NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE.equalsIgnoreCase(addressModel.category)) {
                radio_office.setChecked(true);
//                radioOther.setSelected(true);
            } else {
                radioOther.setChecked(true);
            }
            edtAddress.setTag(addressModel.getLatLng());
//            edtName.setText(addressModel.name);
            edtAddress.setText(addressModel.address);
            edtAddressInitials.setText(addressModel.address_initials);
            btnAdd.setText(getString(R.string.label_update));

            // Initiaze the varfication tags accordingly.
            isAddressFlatNoVerified = true;
           */
/* isAddressNameVerified = true;
            isAddressPickYouLocationVerified = addressModel.address_initials.trim().length() > 0;*//*

            checkAddAddressVerified();

        } else {
            btnAdd.setText(getString(R.string.label_add));
            radioHome.setChecked(true);
           */
/* edtAddress.setFocusable(false);
            edtAddress.setFocusableInTouchMode(false);*//*

        }

        addAddressDialog = new BottomAlertDialog(mContext);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               */
/* if (TextUtils.isEmpty(edtName.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address_nickname));
                } else*//*

                if (TextUtils.isEmpty(edtAddress.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address));
                } else if (TextUtils.isEmpty(edtAddressInitials.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address_initials));
                } else {
                    if (addressModel != null) {
                        callUpdateAddressWS(addressModel.address_id,
                                (radioHome.isChecked()
                                        ? NetworkUtility.TAGS.ADDRESS_TYPE.HOME
                                        : radio_office.isChecked() ? NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE : NetworkUtility.TAGS.ADDRESS_TYPE.OTHERS)
                                */
    /*, edtName.getText().toString().trim()*//*

                                , edtAddress.getText().toString().trim()
                                , edtAddressInitials.getText().toString().trim()
                                , (LatLng) edtAddress.getTag());
                    } else {

                        AddressModel model = new AddressModel();
                        model.category = radioHome.isChecked()
                                ? NetworkUtility.TAGS.ADDRESS_TYPE.HOME
                                : radio_office.isChecked() ? NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE : NetworkUtility.TAGS.ADDRESS_TYPE.OTHERS;
                        model.address = edtAddress.getText().toString().trim();
                        model.address_initials = edtAddressInitials.getText().toString().trim();

                        callAddAddressWS(model, (LatLng) edtAddress.getTag());
                    }
                }
            }
        });
        if (addressModel == null) {
            addAddressDialog.setTitle(getString(R.string.label_add_address));
        } else {
            addAddressDialog.setTitle(getString(R.string.label_update_address));
        }
        addAddressDialog.setCustomView(view);
        addAddressDialog.showDialog();

        checkAddAddressVerified();
    }
*/

    private void checkAddAddressVerified() {
        /*if (isAddressFlatNoVerified
                && isAddressPickYouLocationVerified
                && isAddressNameVerified) {
            btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        } else {
            btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_varient_14));
        }*/
        if (isAddressPickYouLocationVerified
                && isAddressNameVerified) {
            btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        } else {
            btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_varient_14));
        }
    }


    public void showPlacePickerDialog(boolean isForceShow) {

        /*if (isForceShow == false) {
            if (mTaskCreationActivity.mLocationTrackService != null) {
                isPlacePickerClicked = true;
                mTaskCreationActivity.mLocationTrackService.requestLocationUpdate();
                return;
            }
            *//*if (isLocationEnabled() == false) {
                if (isGPSEnabled() == false) {
                    showGPSEnableDialog();
                    return;
                }
            }*//*

         *//*String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (locationProviders == null || locationProviders.equals("")) {
                //show gps disabled and enable gps dialog here
                showGPSEnableDialog();
                return;
            }

            LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //show gps disabled and enable gps dialog here
                showGPSEnableDialog();
                return;
            }*//*
        }*/

        try {
            Utility.hideKeyboard(mContext);
            showProgressDialog();
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(mTaskCreationActivity);
            startActivityForResult(intent, Utility.PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {

            //TODO: Adding dummy place when playservice is not there
            if (edtAddress != null) {
//                edtAddress.setText("Dummy Address with " + Utility.STATIC_LAT + "," + Utility.STATIC_LNG);
                edtAddress.setText(getString(R.string.label_dummy_address, Utility.STATIC_LAT, Utility.STATIC_LNG));
                edtAddress.setFocusable(true);
                edtAddress.setFocusableInTouchMode(true);
                try {
                    edtAddress.setTag(new LatLng(Double.parseDouble(Utility.STATIC_LAT), Double.parseDouble(Utility.STATIC_LNG)));
                } catch (Exception exe) {
                    exe.printStackTrace();
                    edtAddress.setTag(new LatLng(0, 0));
                }
            }

            e.printStackTrace();
            Utility.showToast(mContext, getString(R.string.label_playservice_not_available));
        }
    }

    /**
     * Calling delete address Web service
     *
     * @param addressModel
     */
    private void callDeleteAddressWS(AddressModel addressModel) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentEnterTaskDetailBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
            if (addressRecyclerViewAdapter != null) {
                addressRecyclerViewAdapter.delete(addressModel);
                // Saving information in sharedpreference
                guestUserDetails.addressList = addressRecyclerViewAdapter.getmList();
                if (addressRecyclerViewAdapter.getItemCount() == 0)
                    addressDialog.view.findViewById(R.id.btn_submit).setVisibility(View.GONE);

                if (mSelectedAddressModel != null && mSelectedAddressModel.address_id.equalsIgnoreCase(addressModel.address_id)) {
                    mSelectedAddressModel = null;
                    updateWhereLabelWithIcon(false, "");
                    updateTaskVerificationFlags();
                }

            }
            PreferenceUtility.getInstance(mContext).saveGuestUserDetails(guestUserDetails);
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, String.valueOf(addressModel.address_id));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.DELETE_ADDRESS
                , mCallDeleteAddressWSErrorListener
                , mCallDeleteAddressResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.DELETE_ADDRESS);
    }

    Response.Listener mCallDeleteAddressResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;

                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        if (addressRecyclerViewAdapter != null) {
                            addressRecyclerViewAdapter.delete(TEMP_ADDRESS_ID);
                            if (addressRecyclerViewAdapter.getItemCount() == 0)
                                addressDialog.view.findViewById(R.id.btn_submit).setVisibility(View.GONE);

                            // Saving information in sharedpreference
                            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                            userDetails.addressList = addressRecyclerViewAdapter.getmList();
                            PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);


                            if (mSelectedAddressModel != null && mSelectedAddressModel.address_id.equalsIgnoreCase(TEMP_ADDRESS_ID)) {
                                mSelectedAddressModel = null;
                                updateWhereLabelWithIcon(false, "");
                                updateTaskVerificationFlags();
                            }
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentEnterTaskDetailBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mFragmentEnterTaskDetailBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        mTaskCreationActivity.finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallDeleteAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallDeleteAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentEnterTaskDetailBinding.getRoot());
        }
    };


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////WHERE Feature [START]/////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    private void showBottomAddressDialog(AddressModel model) {
        dialog = new BottomAddAddressDialog(EnterTaskDetailFragment.this, new BottomAddAddressDialog.AddAddressListener() {
            @Override
            public void onAddAddress(AddressModel addressModel) {
//                    mList.add(addressModel);
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (addressRecyclerViewAdapter != null) {
                    addressRecyclerViewAdapter.add(addressModel);
                    addressDialog.view.findViewById(R.id.btn_submit).setVisibility(View.VISIBLE);
                }

                //Saving information in sharedpreference
                UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                if (userDetails != null) {
                    userDetails.addressList = addressRecyclerViewAdapter.getmList();
                    PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);
                } else {
                    GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
                    guestUserDetails.addressList = addressRecyclerViewAdapter.getmList();
                    PreferenceUtility.getInstance(mContext).saveGuestUserDetails(guestUserDetails);
                }


            }

            @Override
            public void onUpdateAddress(AddressModel addressModel) {

                if (dialog != null) {
                    dialog.dismiss();
                }
                if (!TextUtils.isEmpty(TEMP_ADDRESS_ID)) {
                    if (addressRecyclerViewAdapter != null) {
                        addressRecyclerViewAdapter.updateItem(addressModel);
                    }
                }

                //Saving information in sharedpreference
                UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                if (userDetails != null) {
                    userDetails.addressList = addressRecyclerViewAdapter.getmList();
                    PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);
                } else {
                    GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
                    guestUserDetails.addressList = addressRecyclerViewAdapter.getmList();
                    PreferenceUtility.getInstance(mContext).saveGuestUserDetails(guestUserDetails);
                }


            }
        }, new ArrayList<String>(), model);

        dialog.showDialog();
    }

    public ArrayList<MediaModel> getMediaList() {
        return mediaList;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Cancell All Webservice Request
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ADD_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.EDIT_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.DELETE_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SP_LIST);
    }
}
