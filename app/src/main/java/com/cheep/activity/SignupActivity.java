package com.cheep.activity;

import android.Manifest;
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
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.ActivitySignupBinding;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.Utility;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.cheep.utils.Utility.REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE;
import static com.cheep.utils.Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE;

/**
 * Created by pankaj on 9/26/16.
 */

public class SignupActivity extends BaseAppCompatActivity {
    private static final String TAG = "SignupActivity";
    private ActivitySignupBinding mActivitySignupBinding;

    private String mCurrentPhotoPath;

    private UserDetails userDetails;
//    private String selectedImagePath;

    public static void newInstance(Context context, UserDetails userDetails) {
        Intent intent = new Intent(context, SignupActivity.class);
        if (userDetails != null) {
            intent.putExtra(Utility.Extra.USER_DETAILS, Utility.getJsonStringFromObject(userDetails));
        }
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivitySignupBinding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        getWindow().setBackgroundDrawableResource(R.drawable.splash_gradient);

        //Setup terms & condition text
        /*SpannableStringBuilder mSpannableStringBuilder = new SpannableStringBuilder(getString(R.string.label_sign_agreement));

        mSpannableStringBuilder.setSpan(new ClickableSpan() {
                                            @Override
                                            public void onClick(View view) {
                                                InfoActivity.newInstance(mContext, NetworkUtility.TAGS.PAGEID_TYPE.TERMS);
                                            }
                                        },
                mActivitySignupBinding.textTermsCondition.getText().toString().indexOf("Terms"),
                mActivitySignupBinding.textTermsCondition.getText().toString().indexOf("Terms") + 5,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mSpannableStringBuilder.setSpan(new UnderlineSpan(),
                mActivitySignupBinding.textTermsCondition.getText().toString().indexOf("Terms"),
                mActivitySignupBinding.textTermsCondition.getText().toString().indexOf("Terms") + 5,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mSpannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.white)),
                mActivitySignupBinding.textTermsCondition.getText().toString().indexOf("Terms"),
                mActivitySignupBinding.textTermsCondition.getText().toString().indexOf("Terms") + 5,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //Privacy Policy Underline and Clickable

        mSpannableStringBuilder.setSpan(new ClickableSpan() {
                                            @Override
                                            public void onClick(View view) {
                                                InfoActivity.newInstance(mContext, NetworkUtility.TAGS.PAGEID_TYPE.PRIVACY);
                                            }
                                        },
                mActivitySignupBinding.textTermsCondition.getText().toString().indexOf("Privacy"),
                mActivitySignupBinding.textTermsCondition.getText().toString().lastIndexOf("Policy") + 6,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mSpannableStringBuilder.setSpan(new UnderlineSpan(),
                mActivitySignupBinding.textTermsCondition.getText().toString().indexOf("Privacy"),
                mActivitySignupBinding.textTermsCondition.getText().toString().lastIndexOf("Policy") + 6,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mSpannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.white)),
                mActivitySignupBinding.textTermsCondition.getText().toString().indexOf("Privacy"),
                mActivitySignupBinding.textTermsCondition.getText().toString().indexOf("Policy") + 6,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mActivitySignupBinding.textTermsCondition.setText(mSpannableStringBuilder);
        mActivitySignupBinding.textTermsCondition.setMovementMethod(LinkMovementMethod.getInstance());*/


        //Check if we got any user details that need to be updated
        if (getIntent().hasExtra(Utility.Extra.USER_DETAILS)) {
            userDetails = (UserDetails) Utility.getObjectFromJsonString(getIntent().getExtras().getString(Utility.Extra.USER_DETAILS), UserDetails.class);
        }

        //Update FirstName & LastName, if Found
        if (!TextUtils.isEmpty(userDetails.UserName)) {
            String firstName = userDetails.UserName.split(Utility.ONE_CHARACTER_SPACE)[0];
            String lastName = userDetails.UserName.split(Utility.ONE_CHARACTER_SPACE).length > 1
                    ? userDetails.UserName.split(Utility.ONE_CHARACTER_SPACE)[1]
                    : Utility.EMPTY_STRING;
            mActivitySignupBinding.editFirstName.setText(firstName);
            mActivitySignupBinding.editLastName.setText(lastName);
        }

        // Update Mobile number if found
        if (!TextUtils.isEmpty(userDetails.PhoneNumber)) {
            mActivitySignupBinding.editUserMobileNumber.setText(userDetails.PhoneNumber);
        }

        //Update Name and Email if Found
        if (!TextUtils.isEmpty(userDetails.Email)) {
            mActivitySignupBinding.editEmailAddress.setText(userDetails.Email);
        }

        // Check if User comes from Mobile, User can not change Mobile Number
        if (!TextUtils.isEmpty(userDetails.LoginWith) && userDetails.LoginWith.equalsIgnoreCase(NetworkUtility.TAGS.LOGINWITHTYPE.MOBILE)) {
            mActivitySignupBinding.editUserMobileNumber.setEnabled(false);
        } else {
            mActivitySignupBinding.editUserMobileNumber.setEnabled(true);
        }

        //Setting toolbar
        setSupportActionBar(mActivitySignupBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivitySignupBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }


    }

    @Override
    protected void setListeners() {
//        mActivitySignupBinding.imgProfile.setOnClickListener(onClickListener);
        mActivitySignupBinding.btnGo.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_go:
                    onClickOnSignUp();
                    break;
                case R.id.img_profile:
                    showPictureChooserDialog();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: CurrentPath" + mCurrentPhotoPath);

            File f = new File(mCurrentPhotoPath);
//            Uri contentUri = Uri.fromFile(f);
            Uri contentUri = FileProvider.getUriForFile(mContext, BuildConfig.FILE_PROVIDER_URL, f);

            Intent intent = CropImage
                    .activity(contentUri)

                    //Set Aspect ration
                    .setAspectRatio(1, 1)

                    //Set Activity Title
                    .setActivityTitle(getString(R.string.label_crop_image))
                    .setActivityMenuIconColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //AutoZoom enabled
                    .setAutoZoomEnabled(true)

                    //Allow rotation
                    .setAllowRotation(true)

                    //Check output compression quality
                    .setOutputCompressQuality(100)

                    //Whether guidelines will be shown or not
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setGuidelinesColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //Border Color
                    .setBorderLineColor(ContextCompat.getColor(mContext, R.color.white))
                    .setBorderLineThickness(getResources().getDimension(R.dimen.scale_3dp))

                    //Border Corner Color
                    .setBorderCornerColor(ContextCompat.getColor(mContext, R.color.white))
                    .setOutputUri(getCroppedUri())
//                    Shape
                    .setCropShape(CropImageView.CropShape.OVAL)

                    .getIntent(this);

            startActivityForResult(intent, Utility.REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE);
        } else if (requestCode == Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: " + data.getData().toString());

            Intent intent = CropImage
                    .activity(data.getData())

                    //Set Aspect ration
                    .setAspectRatio(1, 1)

                    //Set Activity Title
                    .setActivityTitle(getString(R.string.label_crop_image))
                    .setActivityMenuIconColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //AutoZoom enabled
                    .setAutoZoomEnabled(true)

                    //Allow rotation
                    .setAllowRotation(true)

                    //Check output compression quality
                    .setOutputCompressQuality(100)

                    //Whether guidelines will be shown or not
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setGuidelinesColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //Border Color
                    .setBorderLineColor(ContextCompat.getColor(mContext, R.color.white))
                    .setBorderLineThickness(getResources().getDimension(R.dimen.scale_3dp))

                    //Border Corner Color
                    .setBorderCornerColor(ContextCompat.getColor(mContext, R.color.white))
                    .setOutputUri(getCroppedUri())
//                    Shape
                    .setCropShape(CropImageView.CropShape.OVAL)

                    .getIntent(this);

            startActivityForResult(intent, REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE);


        } else if (requestCode == Utility.REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //Checking if current path is already exist then no need to get it from result
                if (TextUtils.isEmpty(mCurrentPhotoPath)) {
                    Uri resultUri = result.getUri();
                    mCurrentPhotoPath = Utility.getPath(this, resultUri);
                }
                Log.i(TAG, "onActivityResult: Path:" + mCurrentPhotoPath);
                //Load the image from Glide
                Utility.showCircularImageView(mContext, TAG, mActivitySignupBinding.imgProfile, mCurrentPhotoPath, R.drawable.icon_profile_img, true);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.i(TAG, "onActivityResult: Crop Error" + error.toString());
            }
        }*/
    }

    public Uri getCroppedUri() {
        // Create the cropped File where the cropped photo should go
        File croppedFile = null;
        try {
            croppedFile = createCroppedImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        Uri croppedURI = null;
        // Continue only if the File was successfully created
        if (croppedFile != null) {
            croppedURI = FileProvider.getUriForFile(this,
                    BuildConfig.FILE_PROVIDER_URL,
                    croppedFile);
        }
        return croppedURI;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_COVER) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_COVER);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                Utility.showSnackBar(getString(R.string.permission_denied_read), mActivitySignupBinding.getRoot());
            }
        } else if (requestCode == Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                Utility.showSnackBar(getString(R.string.permission_denied_read), mActivitySignupBinding.getRoot());
            }
        } else if (requestCode == Utility.REQUEST_CODE_ADD_PROFILE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                startCameraCaptureChooser(Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied Camera");
                Utility.showSnackBar(getString(R.string.permission_denied_camera), mActivitySignupBinding.getRoot());
            }
        }
    }

    /**************************************************************************************************************
     * *************************************************************************************************************
     * *****************************************Profile Pic Integration [Start]**************************************
     * *************************************************************************************************************
     ************************************************************************************************************/

    private void showPictureChooserDialog() {
        Log.d(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.choose_image)
                .setItems(R.array.choose_image_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            dispatchTakePictureIntent(REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE, Utility.REQUEST_CODE_ADD_PROFILE_CAMERA);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestPermissionCode);
            }
        } else {
            //Go ahead with Camera capturing
            startCameraCaptureChooser(requestCode);
        }
    }

    /*public void startCameraCaptureChooser(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.FILE_PROVIDER_URL,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }*/

    public void startCameraCaptureChooser(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.FILE_PROVIDER_URL,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Grant URI permission START
                // Enableing the permission at runtime
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip =
                            ClipData.newUri(getContentResolver(), "A photo", photoURI);
                    takePictureIntent.setClipData(clip);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    List<ResolveInfo> resInfoList =
                            getPackageManager()
                                    .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, photoURI,
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        /*File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );*/

        File photoFile = new File(new File(getFilesDir(), "CheepImages"), imageFileName);
        mCurrentPhotoPath = photoFile.getAbsolutePath();
        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = photoFile.getAbsolutePath();
        return photoFile;
        /*
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;*/
    }

    public File createCroppedImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "Cropped_JPEG_" + timeStamp + ".jpg";
        /*File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );*/

        File photoFile = new File(new File(getFilesDir(), "CheepImages"), imageFileName);
        mCurrentPhotoPath = photoFile.getAbsolutePath();
        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = photoFile.getAbsolutePath();
        return photoFile;
        /*
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Cropped_JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;*/
    }

    /**
     * On Click of Search
     */
    public void choosePictureFromGallery(int requestFileChooserCode, int requestPermissionCode) {
        Log.d(TAG, "choosePictureFromGallery() called with: requestFileChooserCode = [" + requestFileChooserCode + "], requestPermissionCode = [" + requestPermissionCode + "]");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
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
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        }
    }

    /**************************************************************************************************************
     * *************************************************************************************************************
     * *****************************************Profile Pic Integration [End]**************************************
     * *************************************************************************************************************
     ************************************************************************************************************/

    /**************************************************************************************************************
     * *************************************************************************************************************
     * *****************************************Webservice Integration [Start]**************************************
     * *************************************************************************************************************
     ************************************************************************************************************/
    private void onClickOnSignUp() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivitySignupBinding.getRoot());
            return;
        }

        if (isValidate()) {
            //Set diffeerent Details
            userDetails.UserName = mActivitySignupBinding.editFirstName.getText().toString().trim()
                    + " "
                    + mActivitySignupBinding.editLastName.getText().toString().trim();
            userDetails.Email = mActivitySignupBinding.editEmailAddress.getText().toString().trim();
            userDetails.PhoneNumber = mActivitySignupBinding.editUserMobileNumber.getText().toString().trim();
            callsendOTPWS();
        }
    }

    /**
     * To validate all user entered fields
     */
    public boolean isValidate() {

        // User Name
        if (TextUtils.isEmpty(mActivitySignupBinding.editFirstName.getText())) {
            Utility.showSnackBar(getString(R.string.validate_empty_username), mActivitySignupBinding.getRoot());
            return false;
        }

        if (mActivitySignupBinding.editFirstName.getText().toString().trim().length() < 3) {
            Utility.showSnackBar(getString(R.string.validate_username_length), mActivitySignupBinding.getRoot());
            return false;
        }

        /*//Email address
        if (TextUtils.isEmpty(mActivitySignupBinding.editEmailAddress.getText())) {
            Utility.showSnackBar(getString(R.string.validate_empty_email), mActivitySignupBinding.getRoot());
            return false;
        }*/

        //Check for valid email address
        if ((!TextUtils.isEmpty(mActivitySignupBinding.editEmailAddress.getText()))
                &&
                !Utility.isValidEmail(mActivitySignupBinding.editEmailAddress.getText().toString())) {
            Utility.showSnackBar(getString(R.string.validate_pattern_email), mActivitySignupBinding.getRoot());
            return false;
        }

        /*// Password
        if (TextUtils.isEmpty(mActivitySignupBinding.textPassword.getText())) {
            Utility.showSnackBar(getString(R.string.validate_empty_password), mActivitySignupBinding.getRoot());
            return false;
        }
        //Length of password Feild must be atleast 6 characters
        if (mActivitySignupBinding.textPassword.getText().length() < Utility.PASSWORD_MIN_LENGTH) {
            Utility.showSnackBar(getString(R.string.validate_password_length), mActivitySignupBinding.getRoot());
            return false;
        }*/

        if (TextUtils.isEmpty(mActivitySignupBinding.editUserMobileNumber.getText())) {
            Utility.showSnackBar(getString(R.string.validate_phone_number), mActivitySignupBinding.getRoot());
            return false;
        }

        //Length of phone number must bhi 10 in length
        if (!Utility.isValidPhoneNumber(mActivitySignupBinding.editUserMobileNumber.getText().toString().trim())) {
            Utility.showSnackBar(getString(R.string.validate_phone_number_length), mActivitySignupBinding.getRoot());
            return false;
        }

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        Log.i(TAG, "onSaveInstanceState: ");
    }

    @Override
    protected void onDestroy() {
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SEND_OTP);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SIGNUP);
        super.onDestroy();
    }

    /**
     * Call Verify OTP WS Key Webservice
     */
    private void callsendOTPWS() {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivitySignupBinding.getRoot());
            return;
        }

        //Check if we are having proper userdetails
        if (userDetails == null) {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySignupBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, BuildConfig.X_API_KEY);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.PHONE_NUMBER, userDetails.PhoneNumber);
        mParams.put(NetworkUtility.TAGS.EMAIL_ADDRESS, userDetails.Email);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.SEND_OTP
                , mCallSendOTPWSErrorListener
                , mCallSendOTPWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(this).addToRequestQueue(mVolleyNetworkRequest);

    }

    /**
     * Listeners for tracking Webservice calls
     */
    Response.Listener mCallSendOTPWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        String correct_otp = jsonObject.getString(NetworkUtility.TAGS.OTP_CODE);

                        //Redirect the VerificationActivity
                        VerificationActivity.newInstance(mContext, userDetails, null, mCurrentPhotoPath, correct_otp);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySignupBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivitySignupBinding.getRoot());
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallSendOTPWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallSendOTPWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySignupBinding.getRoot());
        }
    };

    /**************************************************************************************************************
     * *************************************************************************************************************
     * *****************************************Webservice Integration [End]**************************************
     * *************************************************************************************************************
     ************************************************************************************************************/


}
