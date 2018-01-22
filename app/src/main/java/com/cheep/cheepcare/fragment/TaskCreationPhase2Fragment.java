package com.cheep.cheepcare.fragment;

import android.app.Activity;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.TaskCreationActivity;
import com.cheep.adapter.AddressRecyclerViewAdapter;
import com.cheep.adapter.SelectedSubServiceAdapter;
import com.cheep.cheepcare.activity.TaskCreationCCActivity;
import com.cheep.cheepcare.adapter.AddressTaskCreateAdapter;
import com.cheep.cheepcare.dialogs.BottomAddAddressDialog;
import com.cheep.cheepcare.dialogs.SelectSpecificTimeDialog;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.FragmentTaskCreationPhase2Binding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.ProviderModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.strategicpartner.AmazonUtils;
import com.cheep.strategicpartner.MediaRecycleAdapter;
import com.cheep.strategicpartner.model.MediaModel;
import com.cheep.strategicpartner.recordvideo.RecordVideoNewActivity;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.RequestPermission;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by bhavesh on 28/4/17.
 */

public class TaskCreationPhase2Fragment extends BaseFragment
        implements
        RequestPermission.OnRequestPermissionResult
        , SelectSpecificTimeDialog.DialogInteractionListener {
    public static final String TAG = TaskCreationPhase2Fragment.class.getSimpleName();
    private FragmentTaskCreationPhase2Binding mBinding;
    private TaskCreationCCActivity mTaskCreationCCActivity;
    public boolean isTotalVerified = false;

    public boolean isTaskDescriptionVerified = false;
    public boolean isTaskWhereVerified = false;

    public MediaRecycleAdapter mMediaRecycleAdapter;
    // For When
    public SuperCalendar startDateTimeSuperCalendar = SuperCalendar.getInstance();
    private RequestPermission mRequestPermission;
    private BottomAddAddressDialog dialog;
    private AddressTaskCreateAdapter<AddressModel> mAddressAdapter;
    private AddressModel mSelectedAddress;

    @SuppressWarnings("unused")
    public static TaskCreationPhase2Fragment newInstance() {
        TaskCreationPhase2Fragment fragment = new TaskCreationPhase2Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_task_creation_phase2, container, false);
        return mBinding.getRoot();
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
        if (!isVisibleToUser || mTaskCreationCCActivity == null) {
            return;
        }

        // Update Task related details
        updateTaskDetails();

        // Manage Task Verification
        updateTaskVerificationFlags();
    }

    private void updateTaskVerificationFlags() {
        // Check Whether because of any issues, activity reference is NULL or not.
        if (mTaskCreationCCActivity == null) {
            return;
        }

//        if (mTaskCreationCCActivity.getSelectedSubService() == null) {
//            return;
//        }

        // Task Description
//        isTaskDescriptionVerified = mTaskCreationCCActivity.getSelectedSubService().sub_cat_id != -1 ||
//                !TextUtils.isEmpty(mBinding.editTaskDesc.getText().toString().trim());

        // When Verification
//        isTaskWhenVerified = !TextUtils.isEmpty(mBinding.textTaskWhen.getText().toString().trim());

        // Where Verification
        isTaskWhereVerified = !TextUtils.isEmpty(mBinding.tvAddress.getText().toString().trim());

        updateFinalVerificationFlag();
    }

    private void updateFinalVerificationFlag() {
        if (isTaskDescriptionVerified /*&& isTaskWhenVerified*/ && isTaskWhereVerified) {
            isTotalVerified = true;
            mTaskCreationCCActivity.setTaskState(TaskCreationActivity.STEP_TWO_VERIFIED);
        } else {
            isTotalVerified = false;
            mTaskCreationCCActivity.setTaskState(TaskCreationActivity.STEP_TWO_UNVERIFIED);
        }

        // let activity know that post task button needs to be shown now.
        mTaskCreationCCActivity.showPostTaskButton(true, isTotalVerified);
    }

    private void updateTaskDetails() {
        //Update SubCategory
        if (mTaskCreationCCActivity != null && mTaskCreationCCActivity.getSelectedSubServices() != null) {
            mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            mBinding.recyclerView.setAdapter(new SelectedSubServiceAdapter(mTaskCreationCCActivity.getSelectedSubServices()));
        }
    }

    @Override
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");
        mRequestPermission = new RequestPermission(TaskCreationPhase2Fragment.this, this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateHeightOfLinearLayout();
            }
        }, 500);

        //TODO: remove below
        // Update Task related details
        updateTaskDetails();

        resetWhenUI();

        /*mBinding.editTaskDesc.addTextChangedListener(new TextWatcher() {
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
        });*/

        //Update Where label with icon
        updateWhenLabelWithIcon(/*false*/true, Utility.EMPTY_STRING);

        /*//On Click event of attachment
        mBinding.imgAdd.setOnClickListene(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Hide Keyboard if already open
                Utility.hideKeyboard(mContext, mBinding.editTaskDesc);

                showMediaChooserDialog();
            }
        });*/

        /*// On Click event of When
        mBinding.textWhen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Utility.hideKeyboard(mContext, mBinding.editTaskDesc);
                showDateTimePickerDialog();
            }
        });*/

        /*// On Click event of Where
        mBinding.lnWhere.setOnClickListene(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Validate When first
                if (!isTaskWhenVerified) {
                    Utility.showSnackBar(getString(R.string.validate_date), mBinding.getRoot());
                    return;
                }

                Utility.hideKeyboard(mContext, mBinding.editTaskDesc);
                showAddressDialog();
            }
        });*/

        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            // Update the SP lists for Normal User
            callSPListWS(mTaskCreationCCActivity.mJobCategoryModel.catId,
                    false,
                    null,
                    null);
        } else {
            // Update the SP lists for Normal User
            callSPListWS(mTaskCreationCCActivity.mJobCategoryModel.catId,
                    false,
                    null,
                    null);
        }
        /*mBinding.cvInstaBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("myLog", "" + isTotalVerified);
                mTaskCreationCCActivity.onInstaBookClicked();
            }
        });*/

        /*mBinding.cvGetQuote.setOnClickListene(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("myLog", "" + isTotalVerified);

                mTaskCreationCCActivity.onGetQuoteClicked();

            }
        });*/

        // Update the SP lists
//        callSPListWS(mTaskCreationCCActivity.mJobCategoryModel.catId, userDetails.CityID, Utility.EMPTY_STRING);


        mMediaRecycleAdapter = new MediaRecycleAdapter(new MediaRecycleAdapter.ItemClick() {
            @Override
            public void removeMedia() {
                // after uploading 3 media file if any one is deleted then add image view again
                if (mMediaRecycleAdapter.getItemCount() < 3)
                    mBinding.imgAdd.setVisibility(View.VISIBLE);

            }
        }, false);
//        mBinding.recycleImg.setLayoutManager(new LinearLayoutManager(mTaskCreationCCActivity, LinearLayoutManager.HORIZONTAL, false));
//        mBinding.recycleImg.setAdapter(mMediaRecycleAdapter);

        final List<AddressModel> mAddressList = new ArrayList<>();
        AddressModel addressModel = new AddressModel();
        mAddressList.add(addressModel);
        mAddressList.add(addressModel);
        mAddressList.add(addressModel);

        mAddressAdapter = new AddressTaskCreateAdapter<>(mContext
                , android.R.layout.simple_spinner_item
                , mAddressList);
        mBinding.spinnerAddressSelection.setAdapter(mAddressAdapter);
        mBinding.spinnerAddressSelection.setFocusable(false);
        mBinding.spinnerAddressSelection.setPrompt("Prompt");
        mBinding.spinnerAddressSelection.setSelected(false);
        mBinding.spinnerAddressSelection.setFocusableInTouchMode(false);
        mBinding.spinnerAddressSelection.setSelection(-1);
        mBinding.spinnerAddressSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == mAddressList.size() - 1) {
                    showBottomAddressDialog(null);
                } else {
                    Log.d(TAG, "onItemSelected: ");
                }

               /* AddressModel model = mAddressList.get(position);

                mBinding.iconTaskWhere.setImageDrawable(ContextCompat.getDrawable(mContext
                        , Utility.getAddressCategoryBlueIcon(model.category)));

                // show address's nick name or nick name is null then show category
                String category;
                if (!TextUtils.isEmpty(model.nickname))
                    category = model.nickname;
                else
                    category = model.category;

                mBinding.tvAddressNickname.setText(category);

                mBinding.tvAddress.setText(model.address);
                mSelectedAddress = model;*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected() called with: parent = [" + parent + "]");
            }
        });
    }

    public void updateHeightOfLinearLayout() {
        // Change the Linearlayout bottom passing
//        int paddingBottomInPix = (int) Utility.convertDpToPixel(20, mContext);
//        paddingBottomInPix = paddingBottomInPix + mTaskCreationCCActivity.getPostButtonHeight();
//        mBinding.lnRoot.setPadding(0, 0, 0, paddingBottomInPix);
    }

    @Override
    public void setListener() {
        Log.d(TAG, "setListener() called");
        mBinding.textTaskWhen.setOnClickListener(mOnClickListener);
        mBinding.iconTaskWhen.setOnClickListener(mOnClickListener);
        mBinding.textWhen.setOnClickListener(mOnClickListener);
        mBinding.cvInstaBook.setOnClickListener(mOnClickListener);
        mBinding.textWhere.setOnClickListener(mOnClickListener);
        mBinding.ivHome.setOnClickListener(mOnClickListener);
        mBinding.tvAddressNickname.setOnClickListener(mOnClickListener);
        mBinding.viewDot.setOnClickListener(mOnClickListener);
        mBinding.tvLabelAddressSubscribed.setOnClickListener(mOnClickListener);
        mBinding.tvAddress.setOnClickListener(mOnClickListener);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof TaskCreationCCActivity) {
            mTaskCreationCCActivity = (TaskCreationCCActivity) activity;
        }
    }

    public void updateWhenLabelWithIcon(boolean isEnabled, String whereValue) {
        mBinding.iconTaskWhen.setSelected(isEnabled);
        mBinding.textWhen.setSelected(isEnabled);
        if (!isEnabled) {
            mBinding.iconWhenInfo.setVisibility(View.GONE);
            mBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
        } else {
            mBinding.iconWhenInfo.setVisibility(View.VISIBLE);
//            mBinding.textTaskWhen.setText(whereValue);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Show Picture  related portion of app[START]//////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Show Picture  related portion of app[START]//////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public String mCurrentPhotoPath;

    private void showMediaChooserDialog() {
        LogUtils.LOGD(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.choose_media))
                .setItems(R.array.choose_media_type, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position of the selected item
                        if (which == 0) {
                            showVideoChooserDialog();
                        } else {
                            showPictureChooserDialog();
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    private void showVideoChooserDialog() {
        LogUtils.LOGD(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mTaskCreationCCActivity);
        builder.setTitle(getString(R.string.choose_video))
                .setItems(R.array.choose_video_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            if (mRequestPermission.shouldCheckVideoCapturePermission()) {
                                requestPermissions(mRequestPermission.permissionsRequiredForVideoCapture, RequestPermission.REQUEST_PERMISSION_FOR_VIDEO_CAPTURE);
                            } else {
                                takeVideoIntent();
                            }
                        } else {
                            //Select Gallery
                            // In case Choose File from Gallery
                            if (mRequestPermission.shouldCheckOpenGalleryPermission()) {
                                requestPermissions(mRequestPermission.permissionsRequiredForOpenGallery, RequestPermission.REQUEST_PERMISSION_FOR_OPEN_GALLERY_VIDEO);
                            } else {
                                chooseVideoFromGallery();
                            }
                        }
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRequestPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void takeVideoIntent() {
        //Go ahead with Camera capturing
        //Go ahead with Camera capturing
        Intent takePictureIntent = new Intent(mTaskCreationCCActivity, RecordVideoNewActivity.class);
        if (takePictureIntent.resolveActivity(mTaskCreationCCActivity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            photoFile = createVideoFile();
            mCurrentPhotoPath = photoFile.getAbsolutePath();
            if (photoFile.exists()) {
                photoFile.delete();
            } else {
                photoFile.getParentFile().mkdirs();
            }

            // Continue only if the File was successfully created
            Uri photoURI = FileProvider.getUriForFile(mTaskCreationCCActivity,
                    BuildConfig.FILE_PROVIDER_URL,
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            // Grant URI permission START
            // Enabling the permission at runtime
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(mTaskCreationCCActivity.getContentResolver(), "A photo", photoURI);
                takePictureIntent.setClipData(clip);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                List<ResolveInfo> resInfoList =
                        mTaskCreationCCActivity.getPackageManager()
                                .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    mTaskCreationCCActivity.grantUriPermission(packageName, photoURI,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            }
            //Grant URI permission END
//                startActivityForResult(takePictureIntent, requestCode);
            startActivityForResult(takePictureIntent, Utility.REQUEST_CODE_VIDEO_CAPTURE);
        }
    }

    private void chooseVideoFromGallery() {
        //Go ahead with file choosing
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(mTaskCreationCCActivity.getPackageManager()) != null) {
            startActivityForResult(intent, Utility.REQUEST_CODE_VIDEO_SELECT);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////    IMAGE CAPTURE - CHOOSER   /////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    private void showPictureChooserDialog() {
        LogUtils.LOGD(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mTaskCreationCCActivity);
        builder.setTitle(getString(R.string.choose_image))
                .setItems(R.array.choose_image_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            if (mRequestPermission.shouldCheckImageCapturePermission())
                                requestPermissions(mRequestPermission.permissionsRequiredForImageCapture, RequestPermission.REQUEST_PERMISSION_FOR_IMAGE_CAPTURE);
                            else
                                startCameraCaptureChooser();
                        } else {
                            //Select Gallery
                            // In case Choose File from Gallery
                            if (mRequestPermission.shouldCheckOpenGalleryPermission())
                                requestPermissions(mRequestPermission.permissionsRequiredForOpenGallery, RequestPermission.REQUEST_PERMISSION_FOR_OPEN_GALLERY_IMAGE);
                            else
                                startIntentImageChooser();
                        }
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();
    }


    private void startCameraCaptureChooser() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mTaskCreationCCActivity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            photoFile = createImageFile();
            mCurrentPhotoPath = photoFile.getAbsolutePath();
            if (photoFile.exists()) {
                photoFile.delete();
            } else {
                photoFile.getParentFile().mkdirs();
            }

            // Continue only if the File was successfully created
            Uri photoURI = FileProvider.getUriForFile(mTaskCreationCCActivity,
                    BuildConfig.FILE_PROVIDER_URL,
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            // Grant URI permission START
            // Enabling the permission at runtime
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(mTaskCreationCCActivity.getContentResolver(), "A photo", photoURI);
                takePictureIntent.setClipData(clip);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                List<ResolveInfo> resInfoList =
                        mTaskCreationCCActivity.getPackageManager()
                                .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    mTaskCreationCCActivity.grantUriPermission(packageName, photoURI,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            }
            //Grant URI permission END
            startActivityForResult(takePictureIntent, Utility.REQUEST_CODE_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss"/*, Locale.US*/).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        /*File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );*/

        File photoFile = new File(new File(mTaskCreationCCActivity.getFilesDir(), "CheepImages"), imageFileName);
        mCurrentPhotoPath = photoFile.getAbsolutePath();
        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = photoFile.getAbsolutePath();
        return photoFile;
    }

    private File createVideoFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss"/*, Locale.US*/).format(new Date());
        String imageFileName = "VID_" + timeStamp + ".mp4";
        /*File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );*/

        File photoFile = new File(new File(mTaskCreationCCActivity.getFilesDir(), "CheepImages"), imageFileName);
        mCurrentPhotoPath = photoFile.getAbsolutePath();
        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = photoFile.getAbsolutePath();
        return photoFile;
    }


    //// Gallery /////

    private void startIntentImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(mTaskCreationCCActivity.getPackageManager()) != null) {
            startActivityForResult(intent, Utility.REQUEST_CODE_IMAGE_SELECT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.PLACE_PICKER_REQUEST) {
            dialog.onActivityResult(resultCode, data);
            hideProgressDialog();
        } else if (requestCode == Utility.REQUEST_CODE_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "onActivityResult: CurrentPath" + mCurrentPhotoPath);
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mCurrentPhotoPath = Utility.getPath(mTaskCreationCCActivity, contentUri);
            uploadFile(mCurrentPhotoPath, MediaModel.MediaType.TYPE_IMAGE);
        }

        // image chosen from gallery result
        else if (requestCode == Utility.REQUEST_CODE_IMAGE_SELECT && resultCode == Activity.RESULT_OK && data != null) {
            Log.i(TAG, "onActivityResult: " + data.getData().toString());
            mCurrentPhotoPath = Utility.getPath(mTaskCreationCCActivity, data.getData());
            uploadFile(mCurrentPhotoPath, MediaModel.MediaType.TYPE_IMAGE);
        }

        // video captured from camera result
        else if (requestCode == Utility.REQUEST_CODE_VIDEO_CAPTURE && resultCode == RESULT_OK && data != null) {
            mCurrentPhotoPath = data.getStringExtra("path");
            LogUtils.LOGE(TAG, "path >> " + mCurrentPhotoPath);
            uploadFile(mCurrentPhotoPath, MediaModel.MediaType.TYPE_VIDEO);
        }

        // video chosen from gallery result
        else if (requestCode == Utility.REQUEST_CODE_VIDEO_SELECT && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            mCurrentPhotoPath = Utility.getPath(mTaskCreationCCActivity, selectedImageUri);

            if (mCurrentPhotoPath != null && !mCurrentPhotoPath.equals("")) {
                long duration = AmazonUtils.getDuration(mCurrentPhotoPath);
                if (duration > 10) {
                    Utility.showToast(mContext, getString(R.string.message_file_heavy));
                } else if (duration <= 0) {
                    Utility.showToast(mContext, getString(R.string.message_file_something_wrong));
                } else {
                    try {
                        LogUtils.LOGE(TAG, "path >> " + mCurrentPhotoPath);
//                        mMediaRecycleAdapter.addImage(new MediaModel(mCurrentPhotoPath, MediaModel.MediaType.TYPE_VIDEO));
                        uploadFile(mCurrentPhotoPath, MediaModel.MediaType.TYPE_VIDEO);
//                        checkMediaArraySize();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }

        }
    }

    private boolean isDialogOnceShown = false;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.text_task_when:
                case R.id.icon_task_when:
                case R.id.text_when:
                    // On Click event of When
//                    Utility.hideKeyboard(mContext, mBinding.editTaskDesc);
                    if (!isDialogOnceShown) {
                        SelectSpecificTimeDialog selectSpecificTimeDialog = new SelectSpecificTimeDialog();
                        selectSpecificTimeDialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), SelectSpecificTimeDialog.TAG);
                        selectSpecificTimeDialog.setTargetFragment(TaskCreationPhase2Fragment.this, 0);
                    } else {
                        showDateTimePickerDialog();
                    }
                    break;
                case R.id.cvInstaBook:
                    Log.i("myLog", "" + isTotalVerified);
                    mTaskCreationCCActivity.onInstaBookClicked();
                    break;
                case R.id.text_where:
                case R.id.iv_home:
                case R.id.tv_address_nickname:
                case R.id.view_dot:
                case R.id.tv_label_address_subscribed:
                case R.id.tv_address:
                    // On Click event of Where
                    // Validate When first
//                    if (!isTaskWhenVerified) {
//                        Utility.showSnackBar(getString(R.string.validate_date), mBinding.getRoot());
//                        return;
//                    }
                    mBinding.spinnerAddressSelection.performClick();
//                    Utility.hideKeyboard(mContext, mBinding.editTaskDesc);
//                    showAddressDialog();
                    break;
                case R.id.cvGetQuote:
                    mTaskCreationCCActivity.onGetQuoteClicked();
                    break;
            }
        }
    };

    ///////////////////////// ********* Amazon code start here*********** //////////////////////////////////

    private void uploadFile(final String path, final String type) {
        if (path == null || path.equalsIgnoreCase("")) {
            return;
        }

        // async task for uploading file on amazon

//        new AsyncTask<Void, Void, Void>() {

        // thumb folder path for s3 amazon
        String s3PathThumb;
        // original file folder path for s3 amazon
        String s3pathOriginal;

        // to show thumbnail in recycler view in after uploading file on s3
        String localFilePath;

        showProgressDialog();

        File fileOriginal = new File(path);

        String thumbPath = "";
        // create thumbnail for uploading
        if (type.equalsIgnoreCase(MediaModel.MediaType.TYPE_VIDEO))
            thumbPath = AmazonUtils.getVideoThumbPath(mTaskCreationCCActivity, path);
        else
            thumbPath = AmazonUtils.getImageThumbPath(mTaskCreationCCActivity, path);
        final File fileThumb = new File(thumbPath);

//                this name is for creating s3 url for original and file file
        String name;
        String thumbName;
        String timeStamp = System.currentTimeMillis() + "";
        if (type.equalsIgnoreCase(MediaModel.MediaType.TYPE_IMAGE)) {
            name = "AND_IMG_" + timeStamp + AmazonUtils.getExtension(path);
            thumbName = "AND_IMG_" + timeStamp + ".jpg";
        } else {
            name = "AND_VID_" + timeStamp + AmazonUtils.getExtension(path);
            thumbName = "AND_VID_" + timeStamp + ".jpg";
        }

        localFilePath = thumbPath;
        s3pathOriginal = BuildConfig.TASK_ORIGINAL_FOLDER + File.separator + name;
        s3PathThumb = BuildConfig.TASK_THUMB_FOLDER + File.separator + thumbName;
        UploadListener listener = new UploadListener(s3PathThumb, s3pathOriginal, type, localFilePath);
        TransferObserver originalFileObserver;
        TransferObserver thungFileObserver;
        originalFileObserver = AmazonUtils.uploadMedia(mTaskCreationCCActivity, fileOriginal, s3pathOriginal, listener);
        thungFileObserver = AmazonUtils.uploadMedia(mTaskCreationCCActivity, fileThumb, s3PathThumb, listener);
        listener.originalFileObserver = originalFileObserver;
        listener.thungFileObserver = thungFileObserver;

    }

    @Override
    public void onPermissionGranted(int code) {
        switch (code) {
            case RequestPermission.REQUEST_PERMISSION_FOR_VIDEO_CAPTURE:
                takeVideoIntent();
                break;
            case RequestPermission.REQUEST_PERMISSION_FOR_OPEN_GALLERY_VIDEO:
                chooseVideoFromGallery();
                break;
            case RequestPermission.REQUEST_PERMISSION_FOR_IMAGE_CAPTURE:
                startCameraCaptureChooser();
                break;
            case RequestPermission.REQUEST_PERMISSION_FOR_OPEN_GALLERY_IMAGE:
                startIntentImageChooser();
                break;
        }
    }

    @Override
    public void onPermissionDenied(int code) {

    }

    @Override
    public void onSelectTimeClicked() {
        Log.d(TAG, "onSelectTimeClicked() called");
        showDateTimePickerDialog();
    }

    @Override
    public void onNoThanksClicked() {
        Log.d(TAG, "onNoThanksClicked() called");
        resetWhenUI();
    }

    private class UploadListener implements TransferListener {
        String s3PathThumb, s3pathOriginal, type, localFilePath;

        UploadListener(String s3PathThumb, String s3pathOriginal, String type, String localFilePath) {
            this.s3PathThumb = s3PathThumb;
            this.s3pathOriginal = s3pathOriginal;
            this.type = type;
            this.localFilePath = localFilePath;
        }

        // Keep tracks for media uploading
        TransferObserver originalFileObserver;
        TransferObserver thungFileObserver;


        @Override
        public void onError(int id, Exception e) {
            LogUtils.LOGE(TAG, "Error during upload: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            LogUtils.LOGD(TAG, "onProgressChanged: " + id + " , total: " + bytesTotal + " current: " + bytesCurrent);
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            LogUtils.LOGD(TAG, "onStateChanged: " + id + ", " + newState);
            LogUtils.LOGD(TAG, "originalFileObserver: " + originalFileObserver.getId() + ", " + originalFileObserver.getState());
            LogUtils.LOGD(TAG, "thungFileObserver: " + thungFileObserver.getId() + ", " + originalFileObserver.getState());
            if (originalFileObserver != null && thungFileObserver != null) {
                if (originalFileObserver.getState() == TransferState.COMPLETED && thungFileObserver.getState() == TransferState.COMPLETED) {
                    // get s3 urls
                    String thumbUrl = AmazonUtils.getThumbURL(s3PathThumb);
                    String originalUrl = AmazonUtils.getOriginalURL(s3pathOriginal);

                    // add image/video model to recycle view
                    MediaModel mediaModel = new MediaModel();
                    mediaModel.mediaName = originalUrl;
                    mediaModel.mediaThumbName = thumbUrl;
                    LogUtils.LOGE(TAG, "mediaName: " + originalUrl);
                    LogUtils.LOGE(TAG, "mediaThumbName: " + thumbUrl);
                    mediaModel.mediaType = type;
                    mediaModel.localFilePath = localFilePath;
                    mMediaRecycleAdapter.addImage(mediaModel);

                    // set update media list for strategic partner activity
                    // for when  user is not creating tasks but he press back buttons
                    // and goes to home screen that time all uploaded media will be deleted.


                    checkMediaArraySize();
                    // close progress
                    hideProgressDialog();
                }
            }
        }

    }

    /**
     * check if 3 image/video is added then hide image add view
     * to preventing from adding more media files
     */

    private void checkMediaArraySize() {
        if (mMediaRecycleAdapter.getItemCount() == 3) {
            mBinding.imgAdd.setVisibility(View.GONE);
        }
    }

    ///////////////////////// ********* Amazon code ends here*********** //////////////////////////////////
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

                            //this is to manage the dialog showing
                            isDialogOnceShown = true;

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
                                Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours), mBinding.getRoot());
//                                mBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
//                                mBinding.textTaskWhen.setVisibility(View.GONE);
                                updateTaskVerificationFlags();
                                superCalendar = null;
                                resetWhenUI();
                                return;
                            }
//                            }

                            if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                        + getString(R.string.label_at)
                                        + startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
                                mBinding.textTaskWhen.setText(selectedDateTime);
                                mBinding.textTaskWhen.setVisibility(View.VISIBLE);
                                mBinding.iconWhenInfo.setVisibility(View.INVISIBLE);
                                updateTaskVerificationFlags();
                            } else {
                                mBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                mBinding.textTaskWhen.setVisibility(View.GONE);
                                Utility.showSnackBar(getString(R.string.validate_future_date), mBinding.getRoot());
                                updateTaskVerificationFlags();
                            }
                        }
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        timePickerDialog.show();

    }

    private void resetWhenUI() {
        mBinding.iconWhenInfo.setVisibility(View.VISIBLE);

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        String cheepGuarantee = getString(R.string.msg_cheep_assurance_select_time);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(cheepGuarantee);
        spannableStringBuilder.setSpan(colorSpan, cheepGuarantee.indexOf("click"), cheepGuarantee.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.textTaskWhen.setText(spannableStringBuilder);
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
                if (addressRecyclerViewAdapter != null && addressRecyclerViewAdapter.getmList().isEmpty() == false) {
                    AddressModel model = addressRecyclerViewAdapter.getSelectedAddress();
                    if (model != null) {
                        String address;
                        if (model.address_initials.length() > 0) {
                            address = model.address_initials + ", " + model.address;
                        } else {
                            address = model.address;
                        }
//                        updateWhereLabelWithIcon(true, address);
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
                            callSPListWS(mTaskCreationCCActivity.mJobCategoryModel.catId,
                                    true,
                                    null,
                                    model);
                        } else {
                            callSPListWS(mTaskCreationCCActivity.mJobCategoryModel.catId,
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

        ln_pick_your_location.setOnClickListene(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePickerDialog(false);
            }
        });

        edtAddress.setOnClickListene(new View.OnClickListener() {
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
        btnAdd.setOnClickListene(new View.OnClickListener() {
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
            if (mTaskCreationCCActivity.mLocationTrackService != null) {
                isPlacePickerClicked = true;
                mTaskCreationCCActivity.mLocationTrackService.requestLocationUpdate();
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
            Intent intent = intentBuilder.build(mTaskCreationCCActivity);
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
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
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
//                    updateWhereLabelWithIcon(false, "");
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
//                                updateWhereLabelWithIcon(false, "");
                                updateTaskVerificationFlags();
                            }
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        mTaskCreationCCActivity.finish();
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
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
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
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
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
//                        if (list == null || list.isEmpty()) {
//                            isProAvailableForGivenAddress = false;
//                            showNoProForAddressDialog();
//                        }
                        updateSPImageStacks(list);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        updateSPImageStacks(new ArrayList<ProviderModel>());
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
//                        errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
//                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        updateSPImageStacks(new ArrayList<ProviderModel>());
                        // Show message
//                        Utility.showSnackBar(error_message, mBinding.getRoot());
//                        errorLoadingHelper.failed(error_message, 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        mTaskCreationCCActivity.finish();
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
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());

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
    /*public String getTaskDescription() {
        return mBinding.editTaskDesc.getText().toString();
    }*/


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
                    if (!list.isEmpty() && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mBinding.img1, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mBinding.img1.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img1.setVisibility(View.GONE);
                    }
                    break;
                case 1:
                    if (list.size() > 1 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mBinding.img2, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mBinding.img2.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img2.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    if (list.size() > 2 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mBinding.img3, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mBinding.img3.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img3.setVisibility(View.GONE);
                    }
                    break;
                case 3:
                    if (list.size() > 3 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mBinding.img4, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mBinding.img4.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img4.setVisibility(View.GONE);
                    }
                    break;
                case 4:
                    if (list.size() > 4 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mBinding.img5, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mBinding.img5.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img5.setVisibility(View.GONE);
                    }
                    break;
            }
        }

        // Check if list size is more than 5
        if (list.size() > 5) {
            int extra_count = list.size() - 5;
            mBinding.extraProCount.setVisibility(View.VISIBLE);
            mBinding.extraProCount.setText("+" + String.valueOf(extra_count));
        } else {
            mBinding.extraProCount.setVisibility(View.GONE);
        }
    }

    private void showBottomAddressDialog(AddressModel model) {
        dialog = new BottomAddAddressDialog(TaskCreationPhase2Fragment.this, new BottomAddAddressDialog.AddAddressListener() {
            @Override
            public void onAddAddress(AddressModel addressModel) {
//                    mList.add(addressModel);
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

                if (addAddressDialog != null) {
                    addAddressDialog.dismiss();
                }

                if (dialog != null) {
                    dialog.dismiss();
                }
            }

            @Override
            public void onUpdateAddress(AddressModel addressModel) {

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

                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        }, new ArrayList<String>(), model);

        dialog.showDialog();
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