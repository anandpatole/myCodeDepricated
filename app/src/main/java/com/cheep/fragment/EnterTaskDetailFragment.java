package com.cheep.fragment;

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
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cheep.BuildConfig;
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
import com.cheep.model.ProviderModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.AmazonUtils;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.MediaUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.RequestPermission;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.cheep.utils.recordvideo.RecordVideoNewActivity;
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

public class EnterTaskDetailFragment extends BaseFragment implements RequestPermission.OnRequestPermissionResult {
    public static final String TAG = EnterTaskDetailFragment.class.getSimpleName();
    private FragmentEnterTaskDetailBinding mFragmentEnterTaskDetailBinding;
    private TaskCreationActivity mTaskCreationActivity;
    public boolean isTotalVerified = false;

    public boolean isTaskDescriptionVerified = false;
    public boolean isTaskWhenVerified = false;
    public boolean isTaskWhereVerified = false;
    //    public MediaRecycleAdapter mMediaRecycleAdapter;
    // For When
    public SuperCalendar startDateTimeSuperCalendar = SuperCalendar.getInstance();
    private RequestPermission mRequestPermission;
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

        // Update Task related details
        updateTaskDetails();

        // Manage Task Verification
        updateTaskVerificationFlags();
        updateGetQuoteInstaButtonVisibility();
    }

    private void updateGetQuoteInstaButtonVisibility() {
        if (mTaskCreationActivity.mJobCategoryModel.catTaskType.equalsIgnoreCase(Utility.CAT_TASK_TYPE.INSTA_BOOK)) {
            mFragmentEnterTaskDetailBinding.llInstaProceed.setVisibility(View.VISIBLE);
            mFragmentEnterTaskDetailBinding.llGetQuotes.setVisibility(View.GONE);
        } else {
            mFragmentEnterTaskDetailBinding.llInstaProceed.setVisibility(View.GONE);
            mFragmentEnterTaskDetailBinding.llGetQuotes.setVisibility(View.VISIBLE);
        }
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
        isTaskDescriptionVerified = !mTaskCreationActivity.getSubCatList().get(0).sub_cat_id.equalsIgnoreCase("-1") ||
                !TextUtils.isEmpty(mFragmentEnterTaskDetailBinding.editTaskDesc.getText().toString().trim());

        // When Verification
        isTaskWhenVerified = !TextUtils.isEmpty(mFragmentEnterTaskDetailBinding.textTaskWhen.getText().toString().trim());

        // Where Verification
        isTaskWhereVerified = !TextUtils.isEmpty(mFragmentEnterTaskDetailBinding.textTaskWhere.getText().toString().trim());

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
        mTaskCreationActivity.showPostTaskButton(false, isTotalVerified);
    }

    private void updateTaskDetails() {
        //Update SubCategory
        if (mTaskCreationActivity != null && !mTaskCreationActivity.getSubCatList().isEmpty())
            mFragmentEnterTaskDetailBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            mFragmentEnterTaskDetailBinding.recyclerView.setAdapter(new SelectedSubServiceAdapter(mTaskCreationActivity.getSubCatList()));
    }

    @Override
    public void initiateUI() {

        Log.d(TAG, "initiateUI() called");
        mRequestPermission = new RequestPermission(EnterTaskDetailFragment.this, this);
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
//        mFragmentEnterTaskDetailBinding.imgAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                //Hide Keyboard if already open
//                Utility.hideKeyboard(mContext, mFragmentEnterTaskDetailBinding.editTaskDesc);
//
//                showMediaChooserDialog();
//            }
//        });

        //Set initial text for Where select address
        mFragmentEnterTaskDetailBinding.textTaskCheepSubscribed.setText(getString(R.string.please_select_your_address));

        mFragmentEnterTaskDetailBinding.imgMoreLessTask.setSelected(true);
        mFragmentEnterTaskDetailBinding.textViewMoreLessTask.setText(getString(R.string.view_less));

        // On Click event of When
        mFragmentEnterTaskDetailBinding.lnWhen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyboard(mContext, mFragmentEnterTaskDetailBinding.editTaskDesc);
                showDateTimePickerDialog();
            }
        });

        // On Click event of Where
        mFragmentEnterTaskDetailBinding.lnSelectAddressWhere.setOnClickListener(new View.OnClickListener() {
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

            // Update the SP lists for Normal User
            callSPListWS(mTaskCreationActivity.mJobCategoryModel.catId,
                    false,
                    null,
                    null);

        mFragmentEnterTaskDetailBinding.cvInstaBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("myLog", "" + isTotalVerified);
//                mTaskCreationActivity.onInstaBookClicked();
                mTaskCreationActivity.onInstaBookClickedNew();
            }
        });

        mFragmentEnterTaskDetailBinding.cvGetQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("myLog", "" + isTotalVerified);

                mTaskCreationActivity.onGetQuoteClicked();

            }
        });

        mFragmentEnterTaskDetailBinding.frameSelectPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyboard(mContext, mFragmentEnterTaskDetailBinding.editTaskDesc);
                if (mediaList == null || mediaList.isEmpty()) {
                    showMediaChooserDialog();
                } else {
                    mTaskCreationActivity.showMediaUI(mediaList);
                }
            }
        });

        // Update the SP lists
//        callSPListWS(mTaskCreationActivity.mJobCategoryModel.catId, userDetails.CityID, Utility.EMPTY_STRING);


//        mMediaRecycleAdapter = new MediaRecycleAdapter(new MediaRecycleAdapter.ItemClick() {
//            @Override
//            public void removeMedia() {
//                // after uploading 3 media file if any one is deleted then add image view again
//                if (mMediaRecycleAdapter.getItemCount() < 3)
//                    mFragmentEnterTaskDetailBinding.imgAdd.setVisibility(View.VISIBLE);
//
//            }
//        }, false);
//        mFragmentEnterTaskDetailBinding.recycleImg.setLayoutManager(new LinearLayoutManager(mTaskCreationActivity, LinearLayoutManager.HORIZONTAL, false));
//        mFragmentEnterTaskDetailBinding.recycleImg.setAdapter(mMediaRecycleAdapter);
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
        // Task Details View More/Less Listener
        mFragmentEnterTaskDetailBinding.lnTaskMoreLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFragmentEnterTaskDetailBinding.imgMoreLessTask.isSelected()) {
                    mFragmentEnterTaskDetailBinding.rlRootTaskToShowHide.setVisibility(View.GONE);
                    mFragmentEnterTaskDetailBinding.textViewMoreLessTask.setText(getString(R.string.view_more));
                    mFragmentEnterTaskDetailBinding.imgMoreLessTask.setSelected(false);
                } else {
                    mFragmentEnterTaskDetailBinding.rlRootTaskToShowHide.setVisibility(View.VISIBLE);
                    mFragmentEnterTaskDetailBinding.textViewMoreLessTask.setText(getString(R.string.view_less));
                    mFragmentEnterTaskDetailBinding.imgMoreLessTask.setSelected(true);
                }
            }
        });

        // Where View More/Less Listener
        mFragmentEnterTaskDetailBinding.lnWhereMoreLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFragmentEnterTaskDetailBinding.imgMoreLessWhere.isSelected()) {
                    mFragmentEnterTaskDetailBinding.textTaskWhere.setVisibility(View.GONE);
                    mFragmentEnterTaskDetailBinding.textViewMoreLessWhere.setText(getString(R.string.view_more));
                    mFragmentEnterTaskDetailBinding.imgMoreLessWhere.setSelected(false);
                } else {
                    mFragmentEnterTaskDetailBinding.textTaskWhere.setVisibility(View.VISIBLE);
                    mFragmentEnterTaskDetailBinding.textViewMoreLessWhere.setText(getString(R.string.view_less));
                    mFragmentEnterTaskDetailBinding.imgMoreLessWhere.setSelected(true);
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

    public void updateWhereLabelWithIcon(boolean isEnabled, String whereValue) {
        if (!isEnabled) {
            mFragmentEnterTaskDetailBinding.iconTaskWhere.setImageResource(R.drawable.ic_icon_task_where_inactive);
            mFragmentEnterTaskDetailBinding.textWhere.setTextColor(ContextCompat.getColor(mContext, R.color.grey_varient_11));
            mFragmentEnterTaskDetailBinding.textTaskWhere.setVisibility(View.GONE);
            mFragmentEnterTaskDetailBinding.textTaskWhere.setText(Utility.EMPTY_STRING);
            Log.e(TAG, "updateWhereLabelWithIcon: not enabled" );
        } else {
            mFragmentEnterTaskDetailBinding.iconTaskWhere.setImageResource(R.drawable.ic_icon_task_where_active);
            mFragmentEnterTaskDetailBinding.textWhere.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
            mFragmentEnterTaskDetailBinding.textTaskWhere.setVisibility(View.VISIBLE);
            mFragmentEnterTaskDetailBinding.textTaskWhere.setText(whereValue);
            mFragmentEnterTaskDetailBinding.imgMoreLessWhere.setSelected(true);
            mFragmentEnterTaskDetailBinding.textViewMoreLessWhere.setText(getString(R.string.view_less));
            setWhereCategoryAndSubscribeType();
            Log.e(TAG, "updateWhereLabelWithIcon: enabled" );
        }
    }

    private void setWhereCategoryAndSubscribeType() {

        mFragmentEnterTaskDetailBinding.textAddressCategory.setVisibility(View.VISIBLE);
        mFragmentEnterTaskDetailBinding.imgAddressCategory.setVisibility(View.VISIBLE);
        if(mSelectedAddressModel.nickname != null && !TextUtils.isEmpty(mSelectedAddressModel.nickname)) {
            mFragmentEnterTaskDetailBinding.textAddressCategory.setText(mSelectedAddressModel.nickname);
        } else {
            mFragmentEnterTaskDetailBinding.textAddressCategory.setText(mSelectedAddressModel.category.substring(0,1).toUpperCase() + mSelectedAddressModel.category.substring(1));
            Log.e(TAG, "setWhereCategoryAndSubscribeType: " +  mSelectedAddressModel.category);
        }

        //Set Subscribed yes/no
        if(mSelectedAddressModel.is_subscribe.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
            mFragmentEnterTaskDetailBinding.textTaskCheepSubscribed.setVisibility(View.VISIBLE);
        } else {
            mFragmentEnterTaskDetailBinding.textTaskCheepSubscribed.setVisibility(View.GONE);
        }

        mFragmentEnterTaskDetailBinding.imgAddressCategory.setImageResource(Utility.getAddressCategoryBlueIcon(mSelectedAddressModel.category));

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Show Picture  related portion of app[START]//////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Show Picture  related portion of app[START]//////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public String mCurrentPhotoPath;

    public void showMediaChooserDialog() {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(mTaskCreationActivity);
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
        Intent takePictureIntent = new Intent(mTaskCreationActivity, RecordVideoNewActivity.class);
        if (takePictureIntent.resolveActivity(mTaskCreationActivity.getPackageManager()) != null) {
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
            Uri photoURI = FileProvider.getUriForFile(mTaskCreationActivity,
                    BuildConfig.FILE_PROVIDER_URL,
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            // Grant URI permission START
            // Enabling the permission at runtime
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(mTaskCreationActivity.getContentResolver(), "A photo", photoURI);
                takePictureIntent.setClipData(clip);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                List<ResolveInfo> resInfoList =
                        mTaskCreationActivity.getPackageManager()
                                .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    mTaskCreationActivity.grantUriPermission(packageName, photoURI,
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
        if (intent.resolveActivity(mTaskCreationActivity.getPackageManager()) != null) {
            startActivityForResult(intent, Utility.REQUEST_CODE_VIDEO_SELECT);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////    IMAGE CAPTURE - CHOOSER   /////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    private void showPictureChooserDialog() {
        LogUtils.LOGD(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mTaskCreationActivity);
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
        if (takePictureIntent.resolveActivity(mTaskCreationActivity.getPackageManager()) != null) {
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
            Uri photoURI = FileProvider.getUriForFile(mTaskCreationActivity,
                    BuildConfig.FILE_PROVIDER_URL,
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            // Grant URI permission START
            // Enabling the permission at runtime
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(mTaskCreationActivity.getContentResolver(), "A photo", photoURI);
                takePictureIntent.setClipData(clip);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                List<ResolveInfo> resInfoList =
                        mTaskCreationActivity.getPackageManager()
                                .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    mTaskCreationActivity.grantUriPermission(packageName, photoURI,
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

        File photoFile = new File(new File(mTaskCreationActivity.getFilesDir(), "CheepImages"), imageFileName);
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

        File photoFile = new File(new File(mTaskCreationActivity.getFilesDir(), "CheepImages"), imageFileName);
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
        if (intent.resolveActivity(mTaskCreationActivity.getPackageManager()) != null) {
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
            mCurrentPhotoPath = MediaUtility.getPath(mTaskCreationActivity, contentUri);
            uploadFile(mCurrentPhotoPath, MediaModel.MediaType.TYPE_IMAGE);
        }

        // image chosen from gallery result
        else if (requestCode == Utility.REQUEST_CODE_IMAGE_SELECT && resultCode == Activity.RESULT_OK && data != null) {
            Log.i(TAG, "onActivityResult: " + data.getData().toString());
            mCurrentPhotoPath = MediaUtility.getPath(mTaskCreationActivity, data.getData());
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
            mCurrentPhotoPath = MediaUtility.getPath(mTaskCreationActivity, selectedImageUri);

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
            thumbPath = AmazonUtils.getVideoThumbPath(mTaskCreationActivity, path);
        else
            thumbPath = AmazonUtils.getImageThumbPath(mTaskCreationActivity, path);
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
        originalFileObserver = AmazonUtils.uploadMedia(mTaskCreationActivity, fileOriginal, s3pathOriginal, listener);
        thungFileObserver = AmazonUtils.uploadMedia(mTaskCreationActivity, fileThumb, s3PathThumb, listener);
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
                    if (mediaList == null)
                        mediaList = new ArrayList<>();
                    mediaList.add(mediaModel);
                    mediaAdded(mediaModel);
                    if (mediaList.size() != 1)
                        mTaskCreationActivity.addMedia(mediaModel);
//                    mMediaRecycleAdapter.addImage(mediaModel);

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

    private void mediaAdded(MediaModel mediaModel) {
        LogUtils.LOGI(TAG, "bind:>>  " + mediaModel.mediaName);

        ImageView imageViewToLoadImage = new ImageView(mContext);

        switch (numberOfMedia) {
            case 0:
                mFragmentEnterTaskDetailBinding.imgTaskPicture1.setVisibility(View.VISIBLE);
                imageViewToLoadImage = mFragmentEnterTaskDetailBinding.imgTaskPicture1;
                break;
            case 1:
                mFragmentEnterTaskDetailBinding.framePicture2.setVisibility(View.VISIBLE);
                imageViewToLoadImage = mFragmentEnterTaskDetailBinding.imgTaskPicture2;
                break;
            case 2:
                mFragmentEnterTaskDetailBinding.framePicture3.setVisibility(View.VISIBLE);
                imageViewToLoadImage = mFragmentEnterTaskDetailBinding.imgTaskPicture3;
                break;
            default:
                Log.d(TAG, "onStateChanged: numberOfMedia = " + numberOfMedia);
                break;
        }

        loadImageByGlide(mediaModel.localFilePath, imageViewToLoadImage);

        numberOfMedia++;
    }

    public void removeMediaItem(int position, MediaModel model) {
        numberOfMedia--;
        int itemPosition = -1;
        for (int i = 0; i < mediaList.size(); i++) {
            if (mediaList.get(i).localFilePath.equals(model.localFilePath)) {
                itemPosition = i;
                break;
            }
        }
        if (itemPosition != -1) {
            mediaList.remove(itemPosition);
            refreshMediaUI();
        }
    }

    private void refreshMediaUI() {
        mFragmentEnterTaskDetailBinding.imgTaskPicture1.setVisibility(View.GONE);
        mFragmentEnterTaskDetailBinding.framePicture2.setVisibility(View.GONE);
        mFragmentEnterTaskDetailBinding.framePicture3.setVisibility(View.GONE);

        ImageView imageViewToLoadImage = new ImageView(mContext);

        for (int i = 0; i < mediaList.size(); i++) {

            switch (i) {
                case 0:
                    mFragmentEnterTaskDetailBinding.imgTaskPicture1.setVisibility(View.VISIBLE);
                    imageViewToLoadImage = mFragmentEnterTaskDetailBinding.imgTaskPicture1;
                    break;
                case 1:
                    mFragmentEnterTaskDetailBinding.framePicture2.setVisibility(View.VISIBLE);
                    imageViewToLoadImage = mFragmentEnterTaskDetailBinding.imgTaskPicture2;
                    break;
                case 2:
                    mFragmentEnterTaskDetailBinding.framePicture3.setVisibility(View.VISIBLE);
                    imageViewToLoadImage = mFragmentEnterTaskDetailBinding.imgTaskPicture3;
                    break;
                default:
                    Log.d(TAG, "onStateChanged: numberOfMedia = " + numberOfMedia);
                    break;
            }

            loadImageByGlide(mediaList.get(i).localFilePath, imageViewToLoadImage);
        }
    }

    private void loadImageByGlide(String localFilePath, final ImageView imageView) {
        Glide.with(mContext)
                .load(localFilePath)
                .asBitmap()
                .thumbnail(0.2f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, Bitmap>() {

                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                        if (mIsStrategicPartner) {
//                            mImgThumb.setImageBitmap(Utility.getRoundedCornerBitmap(resource, mImgThumb.getContext()));
//                        } else {
                        imageView.setImageBitmap(resource);
//                        }
                        return true;
                    }
                }).into(imageView);
    }

    /**
     * check if 3 image/video is added then hide image add view
     * to preventing from adding more media files
     */

    private void checkMediaArraySize() {
        if (numberOfMedia == 3) {
            mTaskCreationActivity.shouldAddMediaClickListener(false);
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
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
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
                mParams = NetworkUtility.addGuestAddressParams(mParams, mSelectedAddressModel);

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
                        ArrayList<ProviderModel> list = GsonUtility.getObjectListFromJsonString(jsonObject.getString(NetworkUtility.TAGS.DATA), ProviderModel[].class);
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
                    if (!list.isEmpty() && list.get(i) != null) {
                        GlideUtility.showCircularImageView(mContext, TAG, mFragmentEnterTaskDetailBinding.img1, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mFragmentEnterTaskDetailBinding.img1.setVisibility(View.VISIBLE);
                    } else {
                        mFragmentEnterTaskDetailBinding.img1.setVisibility(View.GONE);
                    }
                    break;
                case 1:
                    if (list.size() > 1 && list.get(i) != null) {
                        GlideUtility.showCircularImageView(mContext, TAG, mFragmentEnterTaskDetailBinding.img2, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mFragmentEnterTaskDetailBinding.img2.setVisibility(View.VISIBLE);
                    } else {
                        mFragmentEnterTaskDetailBinding.img2.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    if (list.size() > 2 && list.get(i) != null) {
                        GlideUtility.showCircularImageView(mContext, TAG, mFragmentEnterTaskDetailBinding.img3, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mFragmentEnterTaskDetailBinding.img3.setVisibility(View.VISIBLE);
                    } else {
                        mFragmentEnterTaskDetailBinding.img3.setVisibility(View.GONE);
                    }
                    break;
                case 3:
                    if (list.size() > 3 && list.get(i) != null) {
                        GlideUtility.showCircularImageView(mContext, TAG, mFragmentEnterTaskDetailBinding.img4, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mFragmentEnterTaskDetailBinding.img4.setVisibility(View.VISIBLE);
                    } else {
                        mFragmentEnterTaskDetailBinding.img4.setVisibility(View.GONE);
                    }
                    break;
                case 4:
                    if (list.size() > 4 && list.get(i) != null) {
                        GlideUtility.showCircularImageView(mContext, TAG, mFragmentEnterTaskDetailBinding.img5, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
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
