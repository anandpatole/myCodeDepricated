package com.cheep.fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.TaskCreationActivity;
import com.cheep.adapter.AddressRecyclerViewAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.FragmentEnterTaskDetailBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.LocationInfo;
import com.cheep.model.ProviderModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.FetchLocationInfoUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.cheep.R.id.edit_address;
import static com.cheep.R.id.edit_address_initials;
import static com.cheep.utils.Utility.getObjectFromJsonString;

/**
 * Created by bhavesh on 28/4/17.
 */

public class EnterTaskDetailFragment extends BaseFragment {
    public static final String TAG = "EnterTaskDetailFragment";
    private FragmentEnterTaskDetailBinding mFragmentEnterTaskDetailBinding;
    private TaskCreationActivity mTaskCreationActivity;
    public boolean isTotalVerified = false;

    public boolean isTaskDescriptionVerified = false;
    public boolean isTaskWhenVerified = false;
    public boolean isTaskWhereVerified = false;

    // For When
    public SuperCalendar startDateTimeSuperCalendar = SuperCalendar.getInstance();

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
        // Task Description
        if (mTaskCreationActivity.getSelectedSubService().sub_cat_id != -1) {
            isTaskDescriptionVerified = true;
        } else {
            if (TextUtils.isEmpty(mFragmentEnterTaskDetailBinding.editTaskDesc.getText().toString().trim())) {
                isTaskDescriptionVerified = false;
            } else {
                isTaskDescriptionVerified = true;
            }
        }

        // When Verification
        if (TextUtils.isEmpty(mFragmentEnterTaskDetailBinding.textTaskWhen.getText().toString().trim())) {
            isTaskWhenVerified = false;
        } else {
            isTaskWhenVerified = true;
        }

        // Where Verification
        if (TextUtils.isEmpty(mFragmentEnterTaskDetailBinding.textTaskWhere.getText().toString().trim())) {
            isTaskWhereVerified = false;
        } else {
            isTaskWhereVerified = true;
        }

        updateFinalVerificationFlag();
    }

    private void updateFinalVerificationFlag() {
        if (isTaskDescriptionVerified && isTaskWhenVerified && isTaskWhereVerified) {
            isTotalVerified = true;
            mTaskCreationActivity.setTaskState(TaskCreationActivity.STEP_TWO_VERIFIED);
        } else {
            isTotalVerified = false;
            mTaskCreationActivity.setTaskState(TaskCreationActivity.STEP_TWO_UNVERIFIED);
        }

        // let activity know that post task button needs to be shown now.
        mTaskCreationActivity.showPostTaskButton(true, isTotalVerified);
    }

    private void updateTaskDetails() {
        //Update SubCategory
        mFragmentEnterTaskDetailBinding.textSubCategoryName.setText(mTaskCreationActivity.getSelectedSubService().name);


    }

    @Override
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateHeightOfLinearLayout();
            }
        }, 500);

        mFragmentEnterTaskDetailBinding.editTaskDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateTaskVerificationFlags();
            }
        });

        //Update Where lable with icon
        updateWhereLabelWithIcon(false, Utility.EMPTY_STRING);

        //On Click event of attachment
        mFragmentEnterTaskDetailBinding.imgDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Hide Keyboard if already open
                Utility.hideKeyboard(mContext, mFragmentEnterTaskDetailBinding.editTaskDesc);

                showPictureChooserDialog();
            }
        });

        // On Click event of When
        mFragmentEnterTaskDetailBinding.lnWhen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyboard(mContext, mFragmentEnterTaskDetailBinding.editTaskDesc);
                showDateTimePickerDialog();
            }
        });

        // On Click event of Where
        mFragmentEnterTaskDetailBinding.lnWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Validate When first
                if (!isTaskWhenVerified) {
                    Utility.showSnackBar(getString(R.string.validate_date), mFragmentEnterTaskDetailBinding.getRoot());
                    return;
                }

                Utility.hideKeyboard(mContext, mFragmentEnterTaskDetailBinding.editTaskDesc);
                showAddressDialog();
            }
        });

        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            // Update the SP lists for Normal User
            callSPListWS(mTaskCreationActivity.mJobCategoryModel.catId,
                    false,
                    null,
                    null);
        } else {
            // Update the SP lists for Normal User
            callSPListWS(mTaskCreationActivity.mJobCategoryModel.catId,
                    false,
                    null,
                    null);
        }
        mFragmentEnterTaskDetailBinding.cvInstaBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("myLog", "" + isTotalVerified);
                mTaskCreationActivity.onInstaBookClicked();
            }
        });

        mFragmentEnterTaskDetailBinding.cvGetQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("myLog", "" + isTotalVerified);

                mTaskCreationActivity.onGetQuoteClicked();

            }
        });

        // Update the SP lists
//        callSPListWS(mTaskCreationActivity.mJobCategoryModel.catId, userDetails.CityID, Utility.EMPTY_STRING);
    }

    public void updateHeightOfLinearLayout() {
        // Change the Linearlayout bottom passing
        int paddingBottomInPix = (int) Utility.convertDpToPixel(20, mContext);
        paddingBottomInPix = paddingBottomInPix + mTaskCreationActivity.getPostButtonHeight();
        mFragmentEnterTaskDetailBinding.lnRoot.setPadding(0, 0, 0, paddingBottomInPix);
    }

    @Override
    public void setListener() {
        Log.d(TAG, "setListener() called");
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
            mFragmentEnterTaskDetailBinding.iconTaskWhere.setImageResource(R.drawable.ic_icon_task_where_inactive);
            mFragmentEnterTaskDetailBinding.textWhere.setTextColor(ContextCompat.getColor(mContext, R.color.grey_varient_11));
            mFragmentEnterTaskDetailBinding.textTaskWhere.setVisibility(View.GONE);
            mFragmentEnterTaskDetailBinding.textTaskWhere.setText(Utility.EMPTY_STRING);
        } else {
            mFragmentEnterTaskDetailBinding.iconTaskWhere.setImageResource(R.drawable.ic_icon_task_where_active);
            mFragmentEnterTaskDetailBinding.textWhere.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
            mFragmentEnterTaskDetailBinding.textTaskWhere.setVisibility(View.VISIBLE);
            mFragmentEnterTaskDetailBinding.textTaskWhere.setText(whereValue);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Show Picture  related portion of app[START]//////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public String mCurrentPhotoPath;

    private void showPictureChooserDialog() {
        Log.d(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.choose_image)
                .setItems(R.array.choose_image_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            dispatchTakePictureIntent(Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE, Utility.REQUEST_CODE_WRITE_EXTERNAL_STORAGE_ADD_PROFILE_CAMERA);
                        } else {
                            //Select Gallery
                            // In case Choose File from Gallery
                            choosePictureFromGallery(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY, Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY);
                        }
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();
    }

    private void dispatchTakePictureIntent(int requestCode, int requestPermissionCode) {
        //Go ahead with Camera capturing
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mTaskCreationActivity, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(mTaskCreationActivity, new String[]{Manifest.permission.CAMERA}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(mTaskCreationActivity, new String[]{Manifest.permission.CAMERA}, requestPermissionCode);
            }
        } else {
            //Go ahead with Camera capturing
            startCameraCaptureChooser(requestCode);
        }
    }

    public void startCameraCaptureChooser(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                mCurrentPhotoPath = photoFile.getAbsolutePath();
                if (photoFile.exists()) {
                    photoFile.delete();
                } else {
                    photoFile.getParentFile().mkdirs();
                }

            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(mContext,
                        BuildConfig.FILE_PROVIDER_URL,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Grant URI permission START
                // Enableing the permission at runtime
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip =
                            ClipData.newUri(mContext.getContentResolver(), "A photo", photoURI);
                    takePictureIntent.setClipData(clip);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    List<ResolveInfo> resInfoList =
                            mContext.getPackageManager()
                                    .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        mContext.grantUriPermission(packageName, photoURI,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                }
                //Grant URI permission END
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss"/*, Locale.US*/).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        /*File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );*/

        File photoFile = new File(new File(mContext.getFilesDir(), "CheepImages"), imageFileName);
        mCurrentPhotoPath = photoFile.getAbsolutePath();
        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = photoFile.getAbsolutePath();
        return photoFile;
    }


    //// Gallery /////
    public void choosePictureFromGallery(int requestFileChooserCode, int requestPermissionCode) {
        Log.d(TAG, "choosePictureFromGallery() called with: requestFileChooserCode = [" + requestFileChooserCode + "], requestPermissionCode = [" + requestPermissionCode + "]");
        if (ContextCompat.checkSelfPermission(mTaskCreationActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mTaskCreationActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(mTaskCreationActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(mTaskCreationActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            }
        } else {
            //Go ahead with file choosing
            startIntentFileChooser(requestFileChooserCode);
        }
    }

    public void startIntentFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.PLACE_PICKER_REQUEST) {
            isPlacePickerClicked = false;
            if (resultCode == RESULT_OK) {
                isAddressPickYouLocationVerified = true;
                isAddressNameVerified = true;
                final Place place = PlacePicker.getPlace(mContext, data);
                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                ln_pick_your_location.setVisibility(View.GONE);
                ln_address_row.setVisibility(View.VISIBLE);
                if (edtAddress != null) {
                    edtAddress.setText(address);
                   /* edtAddress.setFocusable(true);
                    edtAddress.setFocusableInTouchMode(true);*/
                    edtAddress.setTag(place.getLatLng());
                }
            } else {
                if (TextUtils.isEmpty(edtAddress.getText().toString().trim())) {
                    isAddressPickYouLocationVerified = false;
                } else {
                    isAddressPickYouLocationVerified = true;
                    isAddressNameVerified = true;
                }
            }
            checkAddAddressVerified();
            hideProgressDialog();
        } else if (requestCode == Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: CurrentPath" + mCurrentPhotoPath);
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mCurrentPhotoPath = Utility.getPath(mContext, contentUri);
            Utility.loadImageView(mContext, mFragmentEnterTaskDetailBinding.imgPicture, mCurrentPhotoPath, 0);
            mFragmentEnterTaskDetailBinding.imgPicture.setVisibility(View.VISIBLE);

        } else if (requestCode == Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: " + data.getData().toString());

            mCurrentPhotoPath = Utility.getPath(mContext, data.getData());
            Utility.loadImageView(mContext, mFragmentEnterTaskDetailBinding.imgPicture, mCurrentPhotoPath, 0);
            mFragmentEnterTaskDetailBinding.imgPicture.setVisibility(View.VISIBLE);

        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Show PIcture related portion of app[END]/////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////WHEN Feature [START]//////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
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
                            SuperCalendar calAfter3Hours = SuperCalendar.getInstance().getNext3HoursTime();

//                            TODO: This needs to Be UNCOMMENTED DO NOT FORGET
//                            if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase(Utility.DEBUG)) {
                            if (superCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {
                                Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours), mFragmentEnterTaskDetailBinding.getRoot());
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
                showAddAddressDialog(null);
//                addAddressDialog.dismiss();
            }
        });
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addressRecyclerViewAdapter != null && addressRecyclerViewAdapter.getmList().isEmpty() == false) {
                    AddressModel model = addressRecyclerViewAdapter.getSelectedAddress();
                    if (model != null) {
                        String address;
                        if (model.address_initials.length() > 0) {
                            address = model.address_initials + ", " + model.address;
                        } else {
                            address = model.address;
                        }
                        updateWhereLabelWithIcon(true, address);
                        mSelectedAddressModel = model;
                        updateTaskVerificationFlags();
                        addressDialog.dismiss();
                    }

                    //refresh list based on address
//                    TODO: This would needs to be changed
//                    pageNo = 0;
//                    isFilterApplied = false;
//                    errorLoadingHelper.showLoading();
//                    final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                    if (mSelectedAddressModel != null)
                        if (Integer.parseInt(mSelectedAddressModel.address_id) < 0) {
                            // Guest User so pass the data accordingly
                            callSPListWS(mTaskCreationActivity.mJobCategoryModel.catId,
                                    true,
                                    null,
                                    model);
                        } else {
                            callSPListWS(mTaskCreationActivity.mJobCategoryModel.catId,
                                    true,
                                    mSelectedAddressModel.address_id,
                                    null);
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
            showAddAddressDialog(null);
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
                showAddAddressDialog(model);
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
        if (addressList == null || (addressList != null && addressList.isEmpty())) {
            return true;
        }
        return false;
    }
///////////////////////////// DELETE CONFIRMATION DIALOG//////////////////////////////////////

    private void showAddressDeletionConfirmationDialog(final AddressModel model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.label_address_delete_title));
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
        });*/

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
        });*/
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
           /* isAddressNameVerified = true;
            isAddressPickYouLocationVerified = addressModel.address_initials.trim().length() > 0;*/
            checkAddAddressVerified();

        } else {
            btnAdd.setText(getString(R.string.label_add));
            radioHome.setChecked(true);
           /* edtAddress.setFocusable(false);
            edtAddress.setFocusableInTouchMode(false);*/
        }

        addAddressDialog = new BottomAlertDialog(mContext);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               /* if (TextUtils.isEmpty(edtName.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address_nickname));
                } else*/
                if (TextUtils.isEmpty(edtAddress.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address));
                } /*else if (TextUtils.isEmpty(edtAddressInitials.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address_initials));
                } */ else {
                    if (addressModel != null) {
                        callUpdateAddressWS(addressModel.address_id,
                                (radioHome.isChecked()
                                        ? NetworkUtility.TAGS.ADDRESS_TYPE.HOME
                                        : radio_office.isChecked() ? NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE : NetworkUtility.TAGS.ADDRESS_TYPE.OTHERS)
                                /*, edtName.getText().toString().trim()*/
                                , edtAddress.getText().toString().trim()
                                , edtAddressInitials.getText().toString().trim()
                                , (LatLng) edtAddress.getTag());
                    } else {

                        callAddAddressWS(
                                (radioHome.isChecked()
                                        ? NetworkUtility.TAGS.ADDRESS_TYPE.HOME
                                        : radio_office.isChecked() ? NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE : NetworkUtility.TAGS.ADDRESS_TYPE.OTHERS)
                                /*, edtName.getText().toString().trim()*/
                                , edtAddress.getText().toString().trim()
                                , edtAddressInitials.getText().toString().trim()
                                , (LatLng) edtAddress.getTag());
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

    boolean isPlacePickerClicked = false;

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
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
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

    /**
     * Calling Update Address WS
     *
     * @param addressType
     * @param address
     */
    private void callUpdateAddressWS(final String addressId, final String addressType,/* String addressName,*/ final String address, final String addressInitials, LatLng latLng) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentEnterTaskDetailBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            /**
             * Guest User so just save it locally & Return.
             */
            showProgressDialog();
            FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                    mContext,
                    new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                        @Override
                        public void onLocationInfoAvailable(LocationInfo mLocationIno) {
                            hideProgressDialog();
                            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
                            if (addressRecyclerViewAdapter != null) {
                                AddressModel mAddressModel = addressRecyclerViewAdapter.getSelectedAddress();
                                mAddressModel.address_id = addressId;
                                mAddressModel.category = addressType;
                                mAddressModel.address = address;
                                mAddressModel.address_initials = addressInitials;
                                mAddressModel.lat = mLocationIno.lat;
                                mAddressModel.lng = mLocationIno.lng;
                                mAddressModel.countryName = mLocationIno.Country;
                                mAddressModel.stateName = mLocationIno.State;
                                mAddressModel.cityName = mLocationIno.City;
                                addressRecyclerViewAdapter.updateItem(mAddressModel);
                                // Saving information in sharedpreference
                                guestUserDetails.addressList = addressRecyclerViewAdapter.getmList();
                            }
                            PreferenceUtility.getInstance(mContext).saveGuestUserDetails(guestUserDetails);
                            if (addAddressDialog != null) {
                                addAddressDialog.dismiss();
                            }
                        }

                        @Override
                        public void internetConnectionNotFound() {
                            hideProgressDialog();
                            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentEnterTaskDetailBinding.getRoot());
                        }
                    },
                    false
            );
            mFetchLocationInfoUtility.getLocationInfo(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
            return;
        } else {
            /*
             * For Logged In user,
             */
            showProgressDialog();
            FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                    mContext,
                    new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                        @Override
                        public void onLocationInfoAvailable(LocationInfo mLocationIno) {
                            // Show Progress
                            showProgressDialog();

                            // Add Header parameters
                            Map<String, String> mHeaderParams = new HashMap<>();
                            mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
                            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

                            // Add Params
                            Map<String, Object> mParams = new HashMap<>();
                            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
                            mParams.put(NetworkUtility.TAGS.CATEGORY, addressType);
                            mParams.put(NetworkUtility.TAGS.ADDRESS, address);
                            mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, addressInitials);
                            mParams.put(NetworkUtility.TAGS.LAT, mLocationIno.lat);
                            mParams.put(NetworkUtility.TAGS.LNG, mLocationIno.lng);
                            mParams.put(NetworkUtility.TAGS.COUNTRY, mLocationIno.Country);
                            mParams.put(NetworkUtility.TAGS.STATE, mLocationIno.State);
                            mParams.put(NetworkUtility.TAGS.CITY_NAME, mLocationIno.City);

                            // Url is based on condition if address id is greater then 0 then it means we need to update the existing address
                            VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.EDIT_ADDRESS
                                    , mCallUpdateAddressWSErrorListener
                                    , mCallUpdateAddressResponseListener
                                    , mHeaderParams
                                    , mParams
                                    , null);
                            Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.EDIT_ADDRESS);
                        }

                        @Override
                        public void internetConnectionNotFound() {
                            hideProgressDialog();
                            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentEnterTaskDetailBinding.getRoot());
                        }
                    },
                    false
            );
            mFetchLocationInfoUtility.getLocationInfo(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
        }
    }


    Response.Listener mCallUpdateAddressResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                if (addAddressDialog != null) {
                    addAddressDialog.dismiss();
                }
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        AddressModel addressModel = (AddressModel) getObjectFromJsonString(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).toString(), AddressModel.class);

                        if (!TextUtils.isEmpty(TEMP_ADDRESS_ID)) {
                            if (addressRecyclerViewAdapter != null) {
                                addressRecyclerViewAdapter.updateItem(addressModel);
                            }
                        }

                        //Saving information in sharedpreference
                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.addressList = addressRecyclerViewAdapter.getmList();
                        PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);


//                        String message = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.OTP_CODE);
//                        VerificationActivity.newInstance(mContext, PreferenceUtility.getInstance(mContext).getUserDetails(), TEMP_PHONE_NUMBER, message);

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
                mCallUpdateAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallUpdateAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentEnterTaskDetailBinding.getRoot());
        }
    };


    /**
     * Calling Add Address WS
     *
     * @param addressType
     * @param address
     */
    private void callAddAddressWS(final String addressType, /*String addressName,*/ final String address, final String addressInitials, final LatLng latLng) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentEnterTaskDetailBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            showProgressDialog();
            FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                    mContext,
                    new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                        @Override
                        public void onLocationInfoAvailable(LocationInfo mLocationIno) {
                            hideProgressDialog();

                            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();

                            /**
                             * In case og Guest User we want to save it locally.
                             */
                            AddressModel addressModel = new AddressModel();
                            // Creating Dynamic AddressID but it would be nagative values always to differentiat with logged in users address.
                            addressModel.address_id = "-" + (guestUserDetails.addressList == null ? "1" : String.valueOf(guestUserDetails.addressList.size() + 1));
                            addressModel.address = address;
                            addressModel.cityName = mLocationIno.City;
                            addressModel.countryName = mLocationIno.Country;
                            addressModel.stateName = mLocationIno.State;
                            addressModel.address_initials = addressInitials;
                            addressModel.category = addressType;
                            addressModel.lat = String.valueOf(latLng.latitude);
                            addressModel.lng = String.valueOf(latLng.longitude);

                            if (addressRecyclerViewAdapter != null) {
                                addressRecyclerViewAdapter.add(addressModel);
                                addressDialog.view.findViewById(R.id.btn_submit).setVisibility(View.VISIBLE);
                            }

                            //Saving information in sharedpreference
                            guestUserDetails.addressList = addressRecyclerViewAdapter.getmList();
                            PreferenceUtility.getInstance(mContext).saveGuestUserDetails(guestUserDetails);

                            if (addAddressDialog != null) {
                                addAddressDialog.dismiss();
                            }
                        }

                        @Override
                        public void internetConnectionNotFound() {
                            hideProgressDialog();
                            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentEnterTaskDetailBinding.getRoot());
                        }
                    },
                    false
            );
            mFetchLocationInfoUtility.getLocationInfo(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
            return;
        } else {
            /**
             * Logged In User so first need to fetch other location info and then call add address
             * Webservice.
             */
            showProgressDialog();
            FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                    mContext,
                    new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                        @Override
                        public void onLocationInfoAvailable(LocationInfo mLocationIno) {

                            //Add Header parameters
                            Map<String, String> mHeaderParams = new HashMap<>();
                            mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
                            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

                            //Add Params
                            Map<String, Object> mParams = new HashMap<>();
                            mParams.put(NetworkUtility.TAGS.CATEGORY, addressType);
                            mParams.put(NetworkUtility.TAGS.ADDRESS, address);
                            mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, addressInitials);
                            mParams.put(NetworkUtility.TAGS.LAT, mLocationIno.lat);
                            mParams.put(NetworkUtility.TAGS.LNG, mLocationIno.lng);
                            mParams.put(NetworkUtility.TAGS.COUNTRY, mLocationIno.Country);
                            mParams.put(NetworkUtility.TAGS.STATE, mLocationIno.State);
                            mParams.put(NetworkUtility.TAGS.CITY_NAME, mLocationIno.City);

                            Utility.hideKeyboard(mContext);
                            //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
                            VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ADD_ADDRESS
                                    , mCallAddAddressWSErrorListener
                                    , mCallAddAddressResponseListener
                                    , mHeaderParams
                                    , mParams
                                    , null);
                            Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.ADD_ADDRESS);
                        }

                        @Override
                        public void internetConnectionNotFound() {
                            hideProgressDialog();
                            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentEnterTaskDetailBinding.getRoot());
                        }
                    },
                    false
            );
            mFetchLocationInfoUtility.getLocationInfo(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
            return;
        }
    }


    Response.Listener mCallAddAddressResponseListener = new Response.Listener() {
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

                        AddressModel addressModel = (AddressModel) getObjectFromJsonString(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).toString(), AddressModel.class);

                        if (addressRecyclerViewAdapter != null) {
                            addressRecyclerViewAdapter.add(addressModel);
                            addressDialog.view.findViewById(R.id.btn_submit).setVisibility(View.VISIBLE);
                        }

                        //Saving information in sharedpreference
                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.addressList = addressRecyclerViewAdapter.getmList();
                        PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);

                        if (addAddressDialog != null) {
                            addAddressDialog.dismiss();
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
                        Utility.showToast(mContext, error_message);
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
                mCallAddAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallAddAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
        }
    };
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////WHERE Feature [START]/////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Reload SP Listing based on AddressID [START]//////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Calling Get SP list web service from server
     */
    private void callSPListWS(String categoryId, boolean shouldGoForAddress, String addressId, AddressModel addressModel) {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentEnterTaskDetailBinding.getRoot());
            return;
        }

        //Show Progress
//        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
        }

        // Add Params
        Map<String, Object> mParams = new HashMap<>();

        // for pagination
        // Sending @pageNo Hard-Coded for now as it won't required here
        mParams.put(NetworkUtility.TAGS.PAGE_NUM, "0");
        // Set Category ID
        mParams.put(NetworkUtility.TAGS.CAT_ID, categoryId);

        if (shouldGoForAddress) {
            if (!TextUtils.isEmpty(addressId)) {
                mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
            } else {
                mParams.put(NetworkUtility.TAGS.ADDRESS, addressModel.address);
                mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, addressModel.address_initials);
                mParams.put(NetworkUtility.TAGS.CATEGORY, addressModel.category);
                mParams.put(NetworkUtility.TAGS.LAT, addressModel.lat);
                mParams.put(NetworkUtility.TAGS.LNG, addressModel.lng);
                mParams.put(NetworkUtility.TAGS.COUNTRY, addressModel.countryName);
                mParams.put(NetworkUtility.TAGS.STATE, addressModel.stateName);
                mParams.put(NetworkUtility.TAGS.CITY_NAME, addressModel.cityName);
            }
        } else {
            // Check if user is logged in if yes pass the address details accordingly.
            if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
                mParams.put(NetworkUtility.TAGS.CITY_NAME, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mCityName);
                mParams.put(NetworkUtility.TAGS.LAT, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mLat);
                mParams.put(NetworkUtility.TAGS.LNG, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mLng);
                mParams.put(NetworkUtility.TAGS.COUNTRY, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mCountryName);
                mParams.put(NetworkUtility.TAGS.STATE, PreferenceUtility.getInstance(mContext).getGuestUserDetails().mStateName);
            } else {
                mParams.put(NetworkUtility.TAGS.CITY_NAME, PreferenceUtility.getInstance(mContext).getUserDetails().mCityName);
                mParams.put(NetworkUtility.TAGS.LAT, PreferenceUtility.getInstance(mContext).getUserDetails().mLat);
                mParams.put(NetworkUtility.TAGS.LNG, PreferenceUtility.getInstance(mContext).getUserDetails().mLng);
                mParams.put(NetworkUtility.TAGS.COUNTRY, PreferenceUtility.getInstance(mContext).getUserDetails().mCountry);
                mParams.put(NetworkUtility.TAGS.STATE, PreferenceUtility.getInstance(mContext).getUserDetails().mStateName);
            }
        }


        String url = NetworkUtility.WS.SP_LIST;

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                , mCallSPListWSErrorListener
                , mCallSPListResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList, NetworkUtility.WS.SP_LIST);
    }

    Response.Listener mCallSPListResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        ArrayList<ProviderModel> list = Utility.getObjectListFromJsonString(jsonObject.getString(NetworkUtility.TAGS.DATA), ProviderModel[].class);
                        Log.i(TAG, "onResponse: size>>" + list.size());
                        updateSPImageStacks(list);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        updateSPImageStacks(new ArrayList<ProviderModel>());
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentEnterTaskDetailBinding.getRoot());
//                        errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
//                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        updateSPImageStacks(new ArrayList<ProviderModel>());
                        // Show message
//                        Utility.showSnackBar(error_message, mFragmentEnterTaskDetailBinding.getRoot());
//                        errorLoadingHelper.failed(error_message, 0, onRetryBtnClickListener);
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
                mCallSPListWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };


    Response.ErrorListener mCallSPListWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentEnterTaskDetailBinding.getRoot());

            updateSPImageStacks(new ArrayList<ProviderModel>());

            hideProgressDialog();
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Reload SP Listing based on AddressID [END]//////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////WHERE Feature [START]/////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Fetch Task Description
     *
     * @return
     */
    public String getTaskDescription() {
        return mFragmentEnterTaskDetailBinding.editTaskDesc.getText().toString();
    }


    /**
     * This method would going to update the SP list of images
     *
     * @param list of Providers available for task
     */
    private void updateSPImageStacks(ArrayList<ProviderModel> list) {
        Log.d(TAG, "updateSPImageStacks() called with: list = [" + list.size() + "]");
        for (int i = 0; i < 5; i++) {
            switch (i) {
                case 0:
                    if (list.size() > 0 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mFragmentEnterTaskDetailBinding.img1, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mFragmentEnterTaskDetailBinding.img1.setVisibility(View.VISIBLE);
                    } else {
                        mFragmentEnterTaskDetailBinding.img1.setVisibility(View.GONE);
                    }
                    break;
                case 1:
                    if (list.size() > 1 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mFragmentEnterTaskDetailBinding.img2, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mFragmentEnterTaskDetailBinding.img2.setVisibility(View.VISIBLE);
                    } else {
                        mFragmentEnterTaskDetailBinding.img2.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    if (list.size() > 2 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mFragmentEnterTaskDetailBinding.img3, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mFragmentEnterTaskDetailBinding.img3.setVisibility(View.VISIBLE);
                    } else {
                        mFragmentEnterTaskDetailBinding.img3.setVisibility(View.GONE);
                    }
                    break;
                case 3:
                    if (list.size() > 3 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mFragmentEnterTaskDetailBinding.img4, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mFragmentEnterTaskDetailBinding.img4.setVisibility(View.VISIBLE);
                    } else {
                        mFragmentEnterTaskDetailBinding.img4.setVisibility(View.GONE);
                    }
                    break;
                case 4:
                    if (list.size() > 4 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mFragmentEnterTaskDetailBinding.img5, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mFragmentEnterTaskDetailBinding.img5.setVisibility(View.VISIBLE);
                    } else {
                        mFragmentEnterTaskDetailBinding.img5.setVisibility(View.GONE);
                    }
                    break;
            }
        }

        // Check if list size is more than 5
        if (list.size() > 5) {
            int extra_count = list.size() - 5;
            mFragmentEnterTaskDetailBinding.extraProCount.setVisibility(View.VISIBLE);
            mFragmentEnterTaskDetailBinding.extraProCount.setText("+" + String.valueOf(extra_count));
        } else {
            mFragmentEnterTaskDetailBinding.extraProCount.setVisibility(View.GONE);
        }
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
