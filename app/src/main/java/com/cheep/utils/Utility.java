package com.cheep.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatEditText;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.activity.HomeActivity;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatTaskModel;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.strategicpartner.AmazonUtils;
import com.cheep.strategicpartner.model.AllSubSubCat;
import com.cheep.strategicpartner.model.MediaModel;
import com.cheep.strategicpartner.model.QueAnsModel;
import com.cheep.strategicpartner.model.StrategicPartnerServiceModel;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mixpanel.android.java_websocket.util.Base64;

import org.cryptonode.jncryptor.AES256JNCryptor;
import org.cryptonode.jncryptor.CryptorException;
import org.cryptonode.jncryptor.JNCryptor;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.cheep.utils.SuperCalendar.SuperFormatter;


/**
 * Created by pankaj on 9/26/16.
 * common utilities methods
 */

public class Utility {

    public static final String MERCHANT = "merchant";
    public static final String EMAIL = "email";
    public static final String PUBLIC_PROFILE = "public_profile";
    public static final String BIRTH_DATE = "user_birthday";
    public static final String UTC = "UTC";
    public static final String FACEBOOK_EMAIL_KEY = "email";
    public static final String FACEBOOK_NAME_KEY = "name";
    public static final String FACEBOOK_FIELDS_KEY = "fields";
    public static final String NORMAL = "normal";
    public static final String DYNAMIC_LINK_CATEGORY_HOME = "home";
    public static final java.lang.String COUPON_DUNIA_CODE_PREFIX = "CHPCD";
    public static final String NEW_LINE = "\n";

    public static boolean allPermissionGranted(String[] permissions, int[] grantResults) {
        boolean allGranted = true;
        for (int i = 0; i < permissions.length; i++) {
            allGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    public static int getAddressCategoryString(String category) {
        if (TextUtils.isEmpty(category))
            return R.string.label_empty_string;
        else if (category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.HOME))
            return R.string.label_home;
        else if (category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE))
            return R.string.label_office;
        else if (category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.BIZ))
            return R.string.label_biz;
        else if (category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.SOCI))
            return R.string.label_soci;
        else
            return R.string.label_other;
    }

    public static int getAddressCategoryBlueIcon(String category) {
        if (TextUtils.isEmpty(category))
            return R.drawable.icon_address_home_active;
        else if (category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.HOME))
            return R.drawable.icon_address_home_active;
        else if (category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE))
            return R.drawable.icon_address_office_active;
        else if (category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.BIZ))
            return R.drawable.icon_address_office_active;
        else if (category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.SOCI))
            return R.drawable.icon_address_home_active;
        else
            return R.drawable.icon_address_other_active;
    }

    public interface GUEST_STATIC_INFO {
        public static final String USERNAME = "Guest";
    }

    public static final String STATIC_LAT = "19.1363246";
    public static final String STATIC_LNG = "72.82766";

    private static final String TAG = "Utility";
    public static final String EMPTY_STRING = "";
    public static final String ZERO_STRING = "0";
    public static final String ONE_CHARACTER_SPACE = " ";
    public static final String DEFAULT_LOCATION = "0.0";
    public static final String ACTION_NEW_JOB_CREATE = "action_new_job_create";
    public static final String ACTION_HIRE_PROVIDER = "action_hire_provider";
    public static final String ACTION_HIRE_PROVIDER_WITH_REFRESHING_TASK_DETAILS = "action_hire_provider_with_refreshing_task_details";

    public static final String ACTION_REGISTER = "action_register";
    public static final String ACTION_LOGIN = "action_login";
    public static final String ACTION_CHANGE_PHONE_NUMBER = "action_change_phone_number";
    public static final String CLICK = "Click";
    public static final String BASIC = "Basic";
    public static final String COLON = ":";
    public static final String ADD_MONEY = "ADD_MONEY";
    public static final String WITHDRAW = "WITHDRAW";
    public static final String DEFAULT = "DEFAULT";
    public static final String COMMA = ",";
    public static final String INR = "INR";
    public static final String PPI = "PPI";
    public static final String USRPWD = "USRPWD";

    //Date Formats
    public static final String DATE_FORMAT_DD_MM_YY = SuperFormatter.DATE + "/" + SuperFormatter.MONTH_NUMBER + "/" + SuperFormatter.YEAR_4_DIGIT;
    public static final String DATE_FORMAT_YYYY_MM_DD = SuperFormatter.YEAR_4_DIGIT + "-" + SuperFormatter.MONTH_NUMBER + "-" + SuperFormatter.DATE;
    public static final String DATE_FORMAT_DD_MMM = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JAN;
    public static final String DATE_FORMAT_DD_MMMM = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JANUARY;
    public static final String DATE_FORMAT_DD_MMM_YYYY = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JAN + " " + SuperFormatter.YEAR_4_DIGIT;
    //TODO :: commented on 13 sept 2017 as per 24 hours formation
//    public static final String DATE_FORMAT_HH_MM_AM = SuperFormatter.HOUR_12_HOUR_2_DIGIT + ":" + SuperFormatter.MINUTE + " " + SuperFormatter.AM_PM;
//    public static final String DATE_FORMAT_DD_MMM_HH_MM_AM = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JAN + " " + SuperFormatter.HOUR_12_HOUR_2_DIGIT + ":" + SuperFormatter.MINUTE + "" + SuperFormatter.AM_PM;
//    public static final String DATE_FORMAT_TASK_HAS_BEEN_PAID_TIME = SuperFormatter.HOUR_12_HOUR_2_DIGIT + ":" + SuperFormatter.MINUTE + "" + SuperFormatter.AM_PM;

    public static final String DATE_FORMAT_HH_MM_AM = SuperFormatter.HOUR_24_HOUR + ":" + SuperFormatter.MINUTE + " ";
    public static final String DATE_FORMAT_DD_MMM_HH_MM_AM = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JAN + " " + SuperFormatter.HOUR_24_HOUR + ":" + SuperFormatter.MINUTE + "";
    public static final String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JAN;
    public static final String TIME_FORMAT_24HH_MM = SuperFormatter.HOUR_24_HOUR + ":" + SuperFormatter.MINUTE + "";
    public static final String DATE_TIME_FORMAT_SERVICE_YEAR = SuperFormatter.YEAR_4_DIGIT + "-" + SuperFormatter.MONTH_NUMBER + "-" + SuperFormatter.DATE + " " + SuperFormatter.HOUR_24_HOUR + ":" + SuperFormatter.MINUTE + ":" + SuperFormatter.SECONDS;
    //    dd MMMM, HH:mm a
    public static final String DATE_TIME_DD_MMMM_HH_MM = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JANUARY + " " + SuperFormatter.HOUR_24_HOUR + ":" + SuperFormatter.MINUTE + "";
    public static final String DATE_FORMAT_FULL_DATE = SuperFormatter.FULL_DATE;

    public static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PHONE_MIN_LENGTH = 10;

    public static final int DEFAULT_PROFILE_SRC = R.drawable.icon_profile_img_solid;
    public static final int DEFAULT_CHEEP_LOGO = R.drawable.ic_cheep_circular_icon;
//    public static final String DEFAULT_PROFILE_URL = "http://msz.uniklinikum-dresden.de/zkn/images/mitarbeiter/dummy120_dagobert83_female_user_icon.png";//"http://lorempixel.com/200/200/people/";

    public static final String LOCALE_FOR_HINDI = "hi";
    public static final String LOCALE_FOR_ENGLISH = "en";

    public static final String ACTION_CALL = "call";
    public static final String ACTION_CHAT = "chat";
    /*
    * chat types
    * */
    public static final String CHAT_TYPE_DATE = "date";
    public static final String CHAT_TYPE_MESSAGE = "message";
    public static final String CHAT_TYPE_MEDIA = "media";
    public static final String CHAT_TYPE_MONEY = "money";

    public static final int REQUEST_CODE_PERMISSION_LOCATION = 101;

    public static final int REQUEST_CODE_GET_FILE_ADD_COVER = 104;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_COVER = 106;

    public static final int REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE = 110;
    public static final int REQUEST_CODE_CHANGE_LOCATION = 108;
    public static final int REQUEST_CODE_CHECK_LOCATION_SETTINGS = 112;

    public static final int REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE = 113;
    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_ADD_PROFILE_CAMERA = 114;
    public static final int REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY = 115;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY = 116;
    public static final int PLACE_PICKER_REQUEST = 117;
    public static final int REQUEST_CODE_ADD_PROFILE_CAMERA = 118;

    /**
     * Image/Video intent request code for task images
     */
    public static final int REQUEST_CODE_VIDEO_CAPTURE = 1111;
    public static final int REQUEST_CODE_IMAGE_CAPTURE = 1112;
    public static final int REQUEST_CODE_VIDEO_SELECT = 1113;
    public static final int REQUEST_CODE_IMAGE_SELECT = 1114;

    /*
    * @Sanjay
    * Chat image chooser constants
    * */
    public static final int REQUEST_CODE_IMAGE_CAPTURE_CHAT_MEDIA = 121;
    public static final int REQUEST_CODE_GET_FILE_CHAT_MEDIA_GALLERY = 122;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_CHAT_MEDIA = 123;
    public static final int REQUEST_CODE_GET_FILE_CHAT_MEDIA = 124;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_CHAT_MEDIA_GALLERY = 125;
    public static final int REQUEST_CODE_CHAT_MEDIA_CAMERA = 126;

    public static final int REQUEST_CODE_IMAGE_CAPTURE_ADD_COVER = 127;
    public static final int REQUEST_CODE_GET_FILE_ADD_COVER_GALLERY = 128;
    public static final int REQUEST_CODE_CROP_GET_FILE_ADD_COVER = 129;


    public static final int REQUEST_START_PAYMENT = 200;
    public static final int ADDITIONAL_REQUEST_START_PAYMENT = 300;
    public static final int REQUEST_START_PAYMENT_FOR_STRATEGIC_PARTNER = 400;
    public static final int REQUEST_CODE_TASK_CREATE_FOR_STRATEGIC_PARTNER = 401;
    public static final int REQUEST_START_PAYMENT_CHEEP_CARE = 500;

    public static final String REMOVE = "remove";
    public static final String ADD = "add";

    //Different Types of Braoadcast Actions
    public static final String BR_ON_TASK_CREATED = "com.cheep.ontaskcreated";
    //    public static final String BR_ON_TASK_CREATED_FOR_INSTA_BOOKING = "com.cheep.ontaskcreated.instabooking";
//    public static final String BR_ON_TASK_CREATED_FOR_STRATEGIC_PARTNER = "com.cheep.ontaskcreated.strategicpartner";
    public static final String BR_NEW_TASK_ADDED = "com.cheep.newtask.added";
    public static final String BR_ON_LOGIN_SUCCESS = "com.cheep.login.success";
    public static final int CHAT_PAGINATION_RECORD_LIMIT = 20;

    public static final int X_RATIO = 16;
    public static final int Y_RATIO = 9;
    public static final String DEBUG = "debug";

    /*
    Home Screen Category Image Ratio
     */
    public static final float CATEGORY_IMAGE_RATIO = (float) 2.3972;


    /**
     * Regular expression for mobile number
     */
    public static final String MOBILE_REGREX = "(((\\+*)((0[ -]+)*|(91[- ]+)*)(\\d{12}+|\\d{10}+))|\\d{5}([- ]*)\\d{6})";
//    public static final String MOBILE_REGREX_IOS = "\"(?\\+\\d\\d\\s+)?((?:\\(\\d\\d\\)|\\d\\d)\\s+)?)(\\d{4,10}\\-?\\d{4})\"";

    /**
     * ongoing user chatId
     */
    public static String CURRENT_CHAT_ID = "";
    public static Boolean IS_FROM_NOTIFICATION = false;

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == Utility.PHONE_MIN_LENGTH;
    }

    public static int parseInt(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getJobs(Context context, String integer) {
        if (parseInt(integer) == 0)
            return context.getString(R.string.label_x_jobs, "No");
        else if (Utility.parseInt(integer) == 1)
            return context.getString(R.string.label_x_job, integer);
        else
            return context.getString(R.string.label_x_jobs, integer);
    }

    public static String getTasks(Context context, String integer) {
        if (parseInt(integer) == 0)
            return context.getString(R.string.label_x_tasks, "No");
        else if (Utility.parseInt(integer) == 1)
            return context.getString(R.string.label_x_task, integer);
        else
            return context.getString(R.string.label_x_tasks, integer);
    }

    public static String getReviews(Context context, String integer) {
        if (parseInt(integer) == 0)
            return context.getString(R.string.label_x_reviews, "No");
        else if (Utility.parseInt(integer) == 1)
            return context.getString(R.string.label_x_review, integer);
        else
            return context.getString(R.string.label_x_reviews, integer);
    }

    public static String getComments(Context context, String integer) {
        if (parseInt(integer) == 0)
            return context.getString(R.string.label_no_comments_yet);
        else if (Utility.parseInt(integer) == 1)
            return context.getString(R.string.label_x_comment, integer);
        else
            return context.getString(R.string.label_x_comments, integer);
    }

    /**
     * return price1 if it is not blank else return price2;
     *
     * @param price1 String
     * @param price2 String
     * @return price1 and if it is empty then return price2
     */
    public static String getActualPrice(String price1, String price2) {
        if (TextUtils.isEmpty(price1))
            return price2;
        return price1;
    }

    public static void showRating(String rating, RatingBar ratingBar) {
        try {
            ratingBar.setRating(Float.parseFloat(rating));
        } catch (Exception e) {
            ratingBar.setRating(0);
            e.printStackTrace();
        }
    }

    public static String getUniqueTagForNetwork(Context context, String url) {
        return url + "_" + System.identityHashCode(context);
    }

    public static void setSwipeRefreshLayoutColors(SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.splash_gradient_start, R.color.splash_gradient_end);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method would do AES Encryption while doing Payment
     *
     * @param orgString : Original String
     * @return : Encrypted String
     */
    public static String applyAESEncryption(String orgString) {
        JNCryptor cryptor = new AES256JNCryptor();
        byte[] plaintext = orgString.getBytes();
        String password = BuildConfig.X_API_KEY;

        // Encrype it
        byte[] encryptedtextInBytes = null;
        try {
            encryptedtextInBytes = cryptor.encryptData(plaintext, password.toCharArray());
        } catch (CryptorException e) {
            // Something went wrong
            e.printStackTrace();
        }
        if (encryptedtextInBytes == null) {
            return EMPTY_STRING;
        }
        String encryptedText = Base64.encodeBytes(encryptedtextInBytes);
        LogUtils.LOGI("TAG", "applyAESEncryption: Encrypted Text" + encryptedText);
        return encryptedText;
    }

    /*public static int getHeightFromWidthForSixteenNineRatio(int width) {
        LogUtils.LOGD(TAG, "getHeightFromWidthForSixteenNineRatio() called with: width = [" + width + "]");
        LogUtils.LOGD(TAG, "getHeightFromWidthForSixteenNineRatio() returned: " + ((width * 9) / 16));
        return ((width * 9) / 16);
    }*/

    public static int getHeightFromWidthForTwoOneRatio(int width) {
//        LogUtils.LOGD(TAG, "getHeightFromWidthForTwoOneRatio() called with: width = [" + width + "]");
//        LogUtils.LOGD(TAG, "getHeightFromWidthForTwoOneRatio() returned: " + (width / 2));
        return (width / 2);
    }

    public static int getHeightFromWidthForOneHalfIsToOneRatio(int width) {
//        LogUtils.LOGD(TAG, "getHeightFromWidthForTwoOneRatio() called with: width = [" + width + "]");
//        LogUtils.LOGD(TAG, "getHeightFromWidthForTwoOneRatio() returned: " + (width / 2));
        return (int) (((float) width) / 1.52542373);
    }

    /**
     * Below would provide dynamic height of image based on #Utility.CATEGORY_IMAGE_RATIO
     *
     * @param width int
     * @return round value
     */
    public static int getHeightCategoryImageBasedOnRatio(int width) {
//        LogUtils.LOGD(TAG, "getHeightCategoryImageBasedOnRatio() called with: width = [" + width + "]");
//        LogUtils.LOGD(TAG, "getHeightCategoryImageBasedOnRatio() called with: width = [" + Math.round((width / CATEGORY_IMAGE_RATIO)) + "]");
        return Math.round((width / CATEGORY_IMAGE_RATIO));
    }

    public static String getUniqueTransactionId() {
        return String.valueOf(System.currentTimeMillis());
    }


    public static final String NO_INTERNET_CONNECTION = "Hey, we see a problem with your internet" + new String(Character.toChars(0x1f914)) + ". We\'ll wait while you check your connection and try again";

    //Bundle Extra parameters
    public interface Extra {
        String WHICH_FRAG = "which_frag";
        String USER_DETAILS = "userdetails";
        String INFO_TYPE = "infoType";
        String DATA = "DATA";
        String IS_FIRST_TIME = "isFirstTime";
        String DATA_2 = "DATA_2";
        String DATA_3 = "DATA_3";
        String PASSWORD = "password";
        String SELECTED_IMAGE_PATH = "selectedImagePath";
        String CORRECT_OTP = "correctOTP";
        String CATEGORY = "category";
        String PHONE_NUMBER = "phone_number";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String CITY_NAME = "city_name";
        String CITY_DETAIL = "city_detail";
        String SESSION_EXPIRE = "session_expire";
        String IMAGE_URL = "image_url";
        String ACTION = "action";
        String USER_NAME = "user_name";
        String ADDRESS_ID = "address_id";
        String ADDRESS_TEXT = "address_text";
        String CATEGORY_ID = "category_id";
        String IS_DATE_SELECTED = "is_date_selected";
        String DATE_TIME = "date_time";
        String TASK_DETAIL = "task_detail";
        String TASK_STATUS = "task_status";
        String TASK_DETAIL_MODEL = "task_detail_model";
        String TASK_ID = "task_id";
        String PAYMENT_VIEW = "payment_view";
        String PAYMENT_VIEW_IS_ADDITIONAL_CHARGE = "isAdditional";
        String CHAT_NOTIFICATION_DATA = "chat_notification_data";
        String PROFILE_FROM_FAVOURITE = "from_favorite";
        String FROM_WHERE = "from_where";
        String TASK_TYPE_IS_INSTA = "isInsta";
        String TASK_TYPE_IS_STRATEGIC = "isStrategic";
        String SELECTED_ADDRESS_MODEL = "selectedAddressModel";
        String LOCATION_INFO = "location_info";
        String IS_INSTA_BOOKING_TASK = "isInstaBookingTask";
        String IS_PAYMENT_SUCCESSFUL = "isPaymentSuccessful";
        String PAYU_RESPONSE = "payu_response";
        String AMOUNT = "amount";
        String REFER_CODE = "refer_code";
        String MOBILE_NUMBER = "mobile_number";
        String STATE = "state";
        String PAYABLE_AMOUNT = "payable_amount";
        String ACCESS_TOKEN = "access_token";
        String CUST_ID = "cust_id";
        String PAYTM_WALLET_BALANCE = "paytm_wallet_balance";
        String URL = "url";
        String POST_DATA = "postData";
        String RESULT = "result";
        String DATE = "date";
        String MODEL = "model";
        String IS_PAY_NOW = "isPayNow";
        String DYNAMIC_LINK_URI = "DYNAMIC_LINK_URI";
        String POSITION = "position";
        String SELECTED_PACKAGE_ID = "selectedPackageID";
        String PACKAGE_LIST = "packageList";
        String ACTIVITY_TYPE = "activityType";
        String ADMIN_SETTING = "adminSetting";
    }


    /*1 for platinum
    2 for Gold
    3 for Silver
    4 for bronze*/
    public interface PRO_LEVEL {
        String PLATINUM = "1";
        String GOLD = "2";
        String SILVER = "3";
        String BRONZE = "4";
    }

    /**
     * Keyboard related methods for hiding and showing
     */
    public static void hideKeyboard(Context context) {
        Activity activity = (Activity) context;
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            View view = activity.getCurrentFocus();
            if (view != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static void hideKeyboard(Context context, AppCompatEditText edt) {
        Activity activity = (Activity) context;
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            if (edt != null) {
                edt.clearFocus();
                inputManager.hideSoftInputFromWindow(edt.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static void showKeyboard(Context context, AppCompatEditText edt) {
        Activity activity = (Activity) context;
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            if (edt != null) {
                edt.requestFocus();
                inputManager.showSoftInput(edt,
                        InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    //Method to do necessary thing at logout
    public static void logout(Context mContext, boolean isSessionExpire, int action) {
        // Clear the Users Preference Information
        PreferenceUtility.getInstance(mContext).onUserLogout();

        //Redirect user to Home Screen
//        LoginActivity.newInstance(mContext, isSessionExpire, action);
        HomeActivity.newInstance(mContext, null);
    }

    //Method to get Device width and height in array, [0] = width, [1] = height
    public static int[] getDeviceWidthHeight(Activity activity) {

        int size[] = new int[2];

        DisplayMetrics displayMetrics = activity.getResources()
                .getDisplayMetrics();
        size[0] = displayMetrics.widthPixels;
        size[1] = displayMetrics.heightPixels;
        return size;
    }

    /**
     * Get Application version name
     *
     * @param context Context
     * @return application version name
     */
    public static String getApplicationVersion(Context context) {
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "0.0.0";
    }


    @SuppressLint("SimpleDateFormat")
    public static String getDate(long milliSeconds, String dateFormat) {
        String finalDate = "";
        try {
            // Create a DateFormatter object for displaying date in specified format.
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat/*, Locale.US*/);

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(milliSeconds);

            finalDate = formatter.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalDate;
    }

    /*
     * get formated date from Server Date
     * */
    @SuppressLint("SimpleDateFormat")
    public static String getFormatedDate(String dateS, String inputDate, String OutputDate) {
        String shoppingdate = dateS;

        try {
            //7/18/2016 12:00:00 AM

            SimpleDateFormat inputFormat = new SimpleDateFormat(inputDate/*, Locale.US*/);
            SimpleDateFormat outputFormat = new SimpleDateFormat(OutputDate/*, Locale.US*/);
            Date date;

            try {
                date = inputFormat.parse(dateS);
                shoppingdate = outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return shoppingdate;
    }

    public static String getDateDifference(String date) {
        if (TextUtils.isEmpty(date))
            return "";

        Date mFutureDate = com.cheep.firebase.DateUtils.getFormatedDate(date, Utility.DATE_FORMAT_FULL_DATE);
        String timespan = DateUtils.getRelativeTimeSpanString(mFutureDate != null ? mFutureDate.getTime() : 0).toString();
        LogUtils.LOGE(TAG, "getDateDifference() returned: " + timespan);
        return timespan;
        /*String sCurrentDt = DateUtils.getFormatedDate(Calendar.getInstance().getTime(), Utility.DATE_FORMAT_FULL_DATE);
        Date mCurrentDate=DateUtils.getFormatedDate(sCurrentDt,Utility.DATE_FORMAT_FULL_DATE);
        Date mFutureDate=DateUtils.getFormatedDate(date,Utility.DATE_FORMAT_FULL_DATE);
        if(mCurrentDate==null || mFutureDate==null)
            return "";

        long diff = mFutureDate.getTime() - mCurrentDate.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        int diffInDays = (int) ((mFutureDate.getTime() - mCurrentDate.getTime()) / (1000 * 60 * 60 * 24));
        NumberFormat numberFormat = new DecimalFormat("00");
        if (diffInDays >0)
        {
            return String.format("%d days",diffInDays);
        }
        else
        {
            return String.format("%s:%s hrs",numberFormat.format(diffHours),numberFormat.format(diffMinutes));
        }*/
    }

    public static String getDateDifference(Context mContext, String date) {
        if (TextUtils.isEmpty(date))
            return "";

        String sCurrentDt = com.cheep.firebase.DateUtils.getFormatedDate(Calendar.getInstance().getTime(), Utility.DATE_FORMAT_FULL_DATE);
        Date mFutureDate = com.cheep.firebase.DateUtils.getFormatedDate(date, Utility.DATE_FORMAT_FULL_DATE);
        Date mCurrentDate = com.cheep.firebase.DateUtils.getFormatedDate(sCurrentDt, Utility.DATE_FORMAT_FULL_DATE);
        long diff = (mFutureDate != null ? mFutureDate.getTime() : 0) - (mCurrentDate != null ? mCurrentDate.getTime() : 0);

        String timespan = DateUtils.getRelativeTimeSpanString(mFutureDate != null ? mFutureDate.getTime() : 0, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        LogUtils.LOGD(TAG, "getDateDifference() returned: " + timespan);
        if (diff > 0) {
            return mContext.getString(R.string.format_task_start_time, timespan);
        } else {
            return mContext.getString(R.string.format_task_start_soon);
        }
        /*String sCurrentDt = DateUtils.getFormatedDate(Calendar.getInstance().getTime(), Utility.DATE_FORMAT_FULL_DATE);
        Date mCurrentDate=DateUtils.getFormatedDate(sCurrentDt,Utility.DATE_FORMAT_FULL_DATE);
        Date mFutureDate=DateUtils.getFormatedDate(date,Utility.DATE_FORMAT_FULL_DATE);
        if(mCurrentDate==null || mFutureDate==null)
            return "";

        long diff = mFutureDate.getTime() - mCurrentDate.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        int diffInDays = (int) ((mFutureDate.getTime() - mCurrentDate.getTime()) / (1000 * 60 * 60 * 24));
        NumberFormat numberFormat = new DecimalFormat("00");
        if (diffInDays >0)
        {
            return String.format("%d days",diffInDays);
        }
        else
        {
            return String.format("%s:%s hrs",numberFormat.format(diffHours),numberFormat.format(diffMinutes));
        }*/
    }

    //Loading Circular image from url to imageview
    public static void showCircularImageView(Context context, String tag, ImageView img, String url, int placeholderRes) {
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(url)
                .transform(new CircleTransform(context, url, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    /*public static void showCircularImageView(Context context, ImageView img, int imageToLoad, int placeholderRes) {

        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }*/

    public static void showCircularImageView(Context context, String tag, ImageView img, String imageToLoad, int placeholderRes, boolean isRounded, float strockwidthIndp) {
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, Color.WHITE, (int) Utility.convertDpToPixel(strockwidthIndp, context), imageToLoad, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void showCircularImageView(Context context, String tag, ImageView img, String imageToLoad, int placeholderRes, boolean isRounded) {
//        LogUtils.LOGD(TAG, "showCircularImageView() called with: context = [" + context + "], tag = [" + tag + "], img = [" + img + "], imageToLoad = [" + imageToLoad + "], placeholderRes = [" + placeholderRes + "], isRounded = [" + isRounded + "]");
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, Color.WHITE, 5, imageToLoad, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, Context context) {
//int color, int cornerDips, int borderDips,
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
//
//        final int borderSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) R.dimen.scale_2dp,
//                context.getResources().getDisplayMetrics());
//        final int cornerSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) R.dimen.scale_5dp,
//                context.getResources().getDisplayMetrics());
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        // prepare canvas for transfer
        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, 15, 15, paint);

        // draw bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        // draw border
        paint.setColor(ContextCompat.getColor(context, R.color.grey_varient_1));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) 15);
        canvas.drawRoundRect(rectF, 15, 15, paint);

        return output;
    }


    public static void showCircularImageViewWithColorBorder(Context context, String tag, ImageView img, String imageToLoad, int placeholderRes, int color, boolean isRounded) {
//        LogUtils.LOGD(TAG, "showCircularImageView() called with: context = [" + context + "], tag = [" + tag + "], img = [" + img + "], imageToLoad = [" + imageToLoad + "], placeholderRes = [" + placeholderRes + "], isRounded = [" + isRounded + "]");
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, ContextCompat.getColor(context, color), 5, imageToLoad, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void showCircularImageViewWithColorBorder(Context context, String tag, ImageView img, int imageToLoad, int color, boolean isRounded) {
//        LogUtils.LOGD(TAG, "showCircularImageView() called with: context = [" + context + "], tag = [" + tag + "], img = [" + img + "], imageToLoad = [" + imageToLoad + "], placeholderRes = [" + placeholderRes + "], isRounded = [" + isRounded + "]");
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, ContextCompat.getColor(context, color), 5, imageToLoad, tag))
                .crossFade()
                .into(img);
    }

    /*public static void showCircularImageView(Context context, String tag, ImageView img, String imageToLoad, int placeholderRes, boolean isRounded, int cbo) {
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, Color.WHITE, 5, imageToLoad, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }*/

    public static void loadImageView(Context context, ImageView img, String imageToLoad, int placeholderRes) {

        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void loadImageView(Context context, ImageView img, String imageToLoad) {
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .crossFade()
                .into(img);
    }

    public static void loadImageView(Context context, ImageView img, int imageToLoad, int placeholderRes) {

        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void loadImageView(Context context, ImageView img, String imageToLoad, int placeholderRes, RequestListener requestListener) {

        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .listener(requestListener)
                .crossFade()
                .into(img);
    }

    private static boolean isActivityCorrectForGlide(Context context) {
        if (context instanceof Activity) {
            if (context == null || ((Activity) context).isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && ((Activity) context).isDestroyed())) {
                return false;
            }
        }
        return true;
    }

    /**
     * GSON UTILITY Methods
     */
    private static Gson gson = new GsonBuilder().create();

    public static Object getObjectFromJsonString(String jsonData, Class modelClass) {
        return gson.fromJson(jsonData, modelClass);
    }

    public static String getJsonStringFromObject(Object modelClass) {
        return gson.toJson(modelClass);
    }

    public static <T> String getJsonStringFromObject(List<T> objectArrayList) {
        return gson.toJson(objectArrayList, new TypeToken<List<T>>() {
        }.getType());
    }

    public static <T> ArrayList<T> getObjectListFromJsonString(String jsonData, Class myclass) {
        return new ArrayList<>(Arrays.asList((T[]) gson.fromJson(jsonData, myclass)));
    }

    public static void openCustomerCareCallDialer(Context context) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + context.getString(R.string.custom_care_phone_number)));
        context.startActivity(intent);
    }

    public static void openCustomerCareCallDialer(Context context, String mobileNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mobileNumber));
        context.startActivity(intent);
    }

    /**
     * Check whether device is having google playservice up to date or note
     *
     * @param context Context of activity
     * @return true if device is having google playservice up to date, false if not.
     */
    public static int checkGooglePlayService(Context context) {
        //Check if Google PLayservice is available
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        return googleApiAvailability.isGooglePlayServicesAvailable(context);
    }

    /**
     * Checking whether there is internet connection or not
     *
     * @param mContext The Context
     * @return Whether there is internet connection or not
     */
    public static boolean isConnected(Context mContext) {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void showSnackBar(String message, View view) {
        Snackbar.make(view, message, 3000).show();
    }

    public static void showToast(Context mContext, String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }


    // copy text to clipboard
    public static void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(context.getString(R.string.label_copied_text), text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                try {
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                } catch (NumberFormatException e) {
                    return null;
                }

            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public interface FILTER_TYPES {
        String FILTER_TYPE_FEATURED = "Featured";
        String FILTER_TYPE_POPULAR = "Popular";
        String FILTER_TYPE_FAVOURITES = "Favourites";
        String FILTER_TYPE_SUBSCRIBED = "Subscribed";
    }

    public interface BOOLEAN {
        String YES = "yes";
        String NO = "no";
    }

    public interface BROADCAST_TYPE {
        int UPDATE_FAVOURITE = 1;
        int UPDATE_COMMENT_COUNT = 2;
        int TASK_PAID = 3;
        int TASK_RATED = 4;
        int TASK_CANCELED = 5;
        int NEW_NOTIFICATION = 6;
        int TASK_RESCHEDULED = 7;
        int TASK_PROCESSING = 8;

        // When quote requested by any of the PRO
        int QUOTE_REQUESTED_BY_PRO = 9;

        // When detail request sent by PRO
        int REQUEST_FOR_DETAIL = 10;

        // When Task status changes
        int TASK_STATUS_CHANGE = 11;

        // When additional payment requested
        int ADDITIONAL_PAYMENT_REQUESTED = 12;

        // When detail request getting rejected by User
        int DETAIL_REQUEST_REJECTED = 13;

        // When AnyTask is Created & We need to check for Alert Disable/Enable in @HomeActivity
        int TASK_START_ALERT = 14;

        // When detail request getting accepted by User
        int DETAIL_REQUEST_ACCEPTED = 15;

        //When Payment has been paid by the user, Need to redirect the user to MyTask Screen
        int PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN = 16;

        // when payment is done for insta booked task and complete
        int TASK_PAID_FOR_INSTA_BOOKING = 17;

        // When Payment response comes from Paytm
        int PAYTM_RESPONSE = 18;

        // when user has verified Paytm otp and account is linked.
        int PAYTM_LINKED = 19;

        // According to new flow Pay later
        // when pro is booked
        int TASK_PRO_BOOKED = 20;
        int TASK_PAID_SUCCESSFULLY = 21;
        int PACKAGE_SUBSCRIBED_SUCCESSFULLY = 22;
    }

    public interface REQUEST_TYPE {
        String QUOTE_REQUESTED = "quote";//sp added quote
        String DETAIL_REQUIRED = "detail";//sp requested detailed information
    }

    public interface SEND_TASK_DETAIL_REQUESTED_STATUS {
        String INITIAL = "no";
        String ALREADY_REQUESTED = "pending";
        String ACCEPTED = "accepted";
        String REJECTED = "rejected";
    }

    public interface PAYMENT_STATUS {
        String PAYMENT_INITIATED = "payment_initiated";//not using it
        String PROCESSING = "processing";//not using it
        String COMPLETED = "completed";
        String FAILED = "failed";
    }

    public interface TASK_STATUS {
        /**
         * 1->If task created and only quotes is there.
         * 2->Task created and user paid to sp, but sp not started the task yet.
         */
        String PENDING = "pending";

        /**
         * If user Payed and task is in progress
         */
        String PAID = "paid";
        String COD = "cod";

        /**
         * If user starts task on my home
         */
        String PROCESSING = "processing";

        /**
         * If user tries to reschdule the task.
         */
        String RESCHEDULE_REQUESTED = "reschedule_requested";

        /**
         * If Task's Reschedule Request has been cancelled by User
         */
        String RESCHEDULE_REQUEST_REJECTED = "reschedule_request_rejected";

        /**
         * If tasks completed by SP
         */
        String COMPLETION_REQUEST = "completion_request";

        /**
         * If tasks completed confirmed by User
         */
        String COMPLETION_CONFIRM = "completion_confirm";

        /**
         * If user starts task on my home
         */
        String CANCELLED_CUSTOMER = "cancel_by_customer";

        /**
         * If user starts task on my home
         */
        String CANCELLED_SP = "cancel_by_sp";

        /**
         * If Task is Disputed
         */
        String DISPUTED = "dispute";

        /**
         * If Task is Elapsed
         */
        String ELAPSED = "elapsed";

        /**
         * If Additional Payment is Requested by SP
         */
        String ADDITIONAL_PAYMENT_REQUESTED = "additional_payment_requested";

        /**
         * This is used for new flow - Pay later functionality where user will book pro
         * and will pay after completion of task
         */
        String PAY_LATER = "pay_later";

    }

    public interface TASK_TYPE {
        String STRATEGIC = "strategic"; //1->if task created and only quotes is there, 2-> task created and user paid to sp, but sp not started the task yet.
        String NORMAL = "normal";//if user payed and task is in progress
        String INSTA_BOOK = "instabook";//if user payed and task is in progress
    }

    public interface STRATEGIC_PARTNER_BRAND {
        String VLCC = "VLCC"; //1->if task created and only quotes is there, 2-> task created and user paid to sp, but sp not started the task yet.
    }

    private static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static String urlEncodeUTF8(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    urlEncodeUTF8(entry.getKey().toString()),
                    urlEncodeUTF8(entry.getValue().toString())
            ));
        }
        return sb.toString();
    }

    public interface NOTIFICATION_TYPE {
        String QUOTE_REQUEST = "QUOTE_REQUEST";
        String TASK_STATUS_CHANGE = "TASK_STATUS_CHANGE";
        String ADDITIONAL_PAYMENT_REQUESTED = "additional_payment_requested";
        String REQUEST_FOR_DETAIL = "REQUEST_FOR_DETAIL";
        String CHAT_MESSAGE = "FIREBASE";
        String TASK_CREATE = "TASK_CREATE";
        String TASK_START_ALERT = "TASK_START_ALERT";
        String WEB_CUSTOM_NOTIFICATION = "WEB_CUSTOM_NOTIFICATION";
    }

    public static final String SESSION_EXPIRE = "session_expire";

    /**
     * Call CheepHelpline number
     *
     * @param mContext Context
     */
    public static void initiateCallToCheepHelpLine(Context mContext) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(BuildConfig.CHEEP_HELPLINE_NUMBER));
        mContext.startActivity(intent);
    }

    /**
     * Redirect the user to Play store
     */
    public static void redirectUserToPlaystore(Context context) {
        // User cancelled the dialog
        Uri uri = Uri.parse(BuildConfig.PLAYSTORE_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (context.getPackageManager().resolveActivity(intent, 0) != null) {
            context.startActivity(intent);
        }
    }


    /**
     * Tags for Strategic Partner Questionnary screen
     **/
    public static String TEMPLATE_DATE_PICKER = "calendar";
    public static String TEMPLATE_TIME_PICKER = "timepicker";
    public static String TEMPLATE_UPLOAD = "upload";
    public static String TEMPLATE_LOCATION = "location";
    public static String TEMPLATE_TEXT_FIELD = "textbox";
    public static String TEMPLATE_DROPDOWN = "dropdown";


    /**
     * get decimal value to show price
     *
     * @param quotePrice string value
     * @return decimal value
     */
    public static String getQuotePriceFormatter(String quotePrice) {

        if (quotePrice == null || quotePrice.equalsIgnoreCase("null"))
            return "0.00";
        if (quotePrice.equalsIgnoreCase("") || quotePrice.equalsIgnoreCase("0") || quotePrice.equalsIgnoreCase("0.0") || quotePrice.equalsIgnoreCase("0.00"))
            return "0.00";
        DecimalFormat formatter = new DecimalFormat("#,##,##0.00");
        double price = Double.parseDouble(quotePrice);
        return formatter.format(price);
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getSelectedMediaJsonString(ArrayList<MediaModel> list) {

        if (list != null && !list.isEmpty()) {
            JsonArray selectedMediaArray = new JsonArray();
            for (int i = 0; i < list.size(); i++) {
                MediaModel model = list.get(i);
                JsonObject obj = new JsonObject();
                obj.addProperty("media_name", AmazonUtils.getFileNameWithExt(model.mediaName, true));
                obj.addProperty("media_type", model.mediaType);
                selectedMediaArray.add(obj);
            }
            return selectedMediaArray.toString();
        }
        return "";
    }

    public static String getExperienceString(String exp, String separator) {
        try {
            float expFloat = Float.parseFloat(exp);
            if (expFloat > 1) {
                return exp + " Years " + separator + "Experience";
            } else {
                return exp + " Year " + separator + "Experience";
            }
        } catch (NumberFormatException e) {
            return exp + " Year " + separator + "Experience";
        }
    }

    public static int getProLevelBadge(String pro_level) {
        if (TextUtils.isEmpty(pro_level))
            return 0;
        switch (pro_level) {
            case Utility.PRO_LEVEL.PLATINUM:
                return R.drawable.ic_badge_platinum;
            case Utility.PRO_LEVEL.GOLD:
                return R.drawable.ic_badge_gold;
            case Utility.PRO_LEVEL.SILVER:
                return R.drawable.ic_badge_silver;
            case Utility.PRO_LEVEL.BRONZE:
                return R.drawable.ic_badge_bronze;
        }
        return 0;
    }

    public static String checkNonNullAndSet(String text) {
        return text != null ? text.trim() : "";
    }

    public static String get2HourTimeSlots(String timeStamp) {
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
        try {
            superCalendar.setTimeInMillis(Long.parseLong(timeStamp));

            superCalendar.setLocaleTimeZone();
//        String task_original_date_time = superCalendar.format(Utility.DATE_FORMAT_DD_MMM + " " + Utility.DATE_FORMAT_HH_MM_AM);

            Date d = superCalendar.getCalendar().getTime();

            SimpleDateFormat timeFormatter = new SimpleDateFormat(Utility.TIME_FORMAT_24HH_MM);
            String fromHour = timeFormatter.format(d);
            SuperCalendar superCalendarToDate = SuperCalendar.getInstance();
            superCalendarToDate.setTimeInMillis(superCalendar.getCalendar().getTimeInMillis());
            superCalendarToDate.getCalendar().add(Calendar.HOUR_OF_DAY, 2);
            Date toDate = superCalendarToDate.getCalendar().getTime();
            String toHour = timeFormatter.format(toDate);

            return fromHour + " hrs - " + toHour + " hrs";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String get2HourTimeSlotsForPastTaskScreen(String timeStamp) {
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
        try {
            superCalendar.setTimeInMillis(Long.parseLong(timeStamp));

            superCalendar.setLocaleTimeZone();
//        String task_original_date_time = superCalendar.format(Utility.DATE_FORMAT_DD_MMM + " " + Utility.DATE_FORMAT_HH_MM_AM);

            Date d = superCalendar.getCalendar().getTime();

            SimpleDateFormat timeFormatter = new SimpleDateFormat(Utility.TIME_FORMAT_24HH_MM);
            String fromHour = timeFormatter.format(d);
            SuperCalendar superCalendarToDate = SuperCalendar.getInstance();
            superCalendarToDate.setTimeInMillis(superCalendar.getCalendar().getTimeInMillis());
            superCalendarToDate.getCalendar().add(Calendar.HOUR_OF_DAY, 2);
            Date toDate = superCalendarToDate.getCalendar().getTime();
            String toHour = timeFormatter.format(toDate);

            return "between " + fromHour + " hrs to " + toHour + " hrs";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String fetchMessageFromDateOfMonth(Context context, int day, SuperCalendar
            superStartDateTimeCalendar, ProviderModel providerModel) {
        String date;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_TH = SuperCalendar.SuperFormatter.DATE + context.getString(R.string.label_th_date) + SuperCalendar.SuperFormatter.MONTH_JAN;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ST = SuperCalendar.SuperFormatter.DATE + context.getString(R.string.label_st_date) + SuperCalendar.SuperFormatter.MONTH_JAN;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_RD = SuperCalendar.SuperFormatter.DATE + context.getString(R.string.label_rd_date) + SuperCalendar.SuperFormatter.MONTH_JAN;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ND = SuperCalendar.SuperFormatter.DATE + context.getString(R.string.label_nd_date) + SuperCalendar.SuperFormatter.MONTH_JAN;

        if (day >= 11 && day <= 13) {
            date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_TH);
        } else {
            switch (day % 10) {
                case 1:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ST);
                    break;
                case 2:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ND);
                    break;
                case 3:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_RD);
                    break;
                default:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_TH);
                    break;
            }
        }
        // as per  24 hour format 13 spt 2017
//        String DATE_FORMAT_TASK_HAS_BEEN_PAID_TIME = SuperCalendar.SuperFormatter.HOUR_12_HOUR_2_DIGIT + ":" + SuperCalendar.SuperFormatter.MINUTE + "' '" + SuperCalendar.SuperFormatter.AM_PM;
//        String time = superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);

        // set time format 24 hours
        Date d = superStartDateTimeCalendar.getCalendar().getTime();

        SimpleDateFormat timeFormatter = new SimpleDateFormat(Utility.TIME_FORMAT_24HH_MM);
        String fromHour = timeFormatter.format(d);
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeInMillis(superStartDateTimeCalendar.getCalendar().getTimeInMillis());
        superCalendar.getCalendar().add(Calendar.HOUR_OF_DAY, 2);

        Date toDate = superCalendar.getCalendar().getTime();
        String toHour = timeFormatter.format(toDate);

        String message = context.getString(R.string.desc_task_payment_done_acknowledgement
                , providerModel.userName, date + context.getString(R.string.label_between) + fromHour + " hrs - " + toHour + " hrs");
        message = message.replace(".", "");
//        message = message.replace(getString(R.string.label_am_caps), getString(R.string.label_am_small)).replace(getString(R.string.label_pm_caps), getString(R.string.label_pm_small));
        return message + ".";
    }

    /*
        * Update finalized sp id on firebase.
        * @Sanjay 20 Feb 2016
        * */
    public static void updateSelectedSpOnFirebase(final Context context,
                                                  final TaskDetailModel taskDetailModel,
                                                  final ProviderModel providerModel,
                                                  final boolean isInstaBooking) {

        String formattedTaskId = FirebaseUtils.getPrefixTaskId(taskDetailModel.taskId);
        String formattedSpId = FirebaseUtils.getPrefixSPId(providerModel.providerId);
        String formattedUserId = "";
        final UserDetails userDetails = PreferenceUtility.getInstance(context).getUserDetails();
        if (userDetails != null) {
            formattedUserId = FirebaseUtils.getPrefixUserId(userDetails.userID);
        }
        FirebaseHelper.getRecentChatRef(formattedUserId).child(formattedTaskId).removeValue();
        if (!TextUtils.isEmpty(formattedTaskId) && !TextUtils.isEmpty(formattedSpId)) {
            FirebaseHelper.getTaskRef(formattedTaskId).child(FirebaseHelper.KEY_SELECTEDSPID).setValue(formattedSpId);
        }

        final String formattedId = FirebaseUtils.get_T_SP_U_FormattedId(formattedTaskId, formattedSpId, formattedUserId);
        final String finalFormattedUserId = formattedUserId;
        FirebaseHelper.getTaskChatRef(formattedTaskId).child(formattedId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    TaskChatModel taskChatModel = dataSnapshot.getValue(TaskChatModel.class);
                    if (taskChatModel != null) {
                        taskChatModel.chatId = formattedId;
                    }
                    if (taskChatModel != null) {
                        FirebaseHelper.getRecentChatRef(finalFormattedUserId).child(taskChatModel.chatId).setValue(taskChatModel);
                    }

                    if (isInstaBooking) {
        /* * Add new task detail on firebase
         * @Giteeka sep 7 2017 for insta booking
         */
                        ChatTaskModel chatTaskModel = new ChatTaskModel();
                        chatTaskModel.taskId = FirebaseUtils.getPrefixTaskId(taskDetailModel.taskId);
                        chatTaskModel.taskDesc = taskDetailModel.taskDesc;
                        chatTaskModel.categoryId = taskDetailModel.categoryId;
                        chatTaskModel.categoryName = taskDetailModel.categoryName;
                        chatTaskModel.selectedSPId = providerModel.providerId;
                        UserDetails userDetails = PreferenceUtility.getInstance(context).getUserDetails();
                        chatTaskModel.userId = FirebaseUtils.getPrefixUserId(userDetails.userID);
                        FirebaseHelper.getTaskRef(chatTaskModel.taskId).setValue(chatTaskModel);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void onSuccessfulInstaBookingTaskCompletion(Context context, JSONObject jsonObject, ProviderModel providerModel) {
        Utility.showToast(context, context.getString(R.string.label_task_created_successfully));
        TaskDetailModel taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);

        if (providerModel != null) {
            // add task and pro entry for firebase
            Utility.updateSelectedSpOnFirebase(context, taskDetailModel, providerModel, taskDetailModel.taskType.equalsIgnoreCase(TASK_TYPE.INSTA_BOOK));
        }

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PAID_FOR_INSTA_BOOKING;
        EventBus.getDefault().post(messageEvent);

        ((Activity) context).finish();
    }


    public static String getQuestionAnswerDetailsJsonString(ArrayList<QueAnsModel> mList) {
        JsonArray quesArray = new JsonArray();
        for (int i = 0; i < mList.size(); i++) {
            QueAnsModel queAnsModel = mList.get(i);
            if (!queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_DATE_PICKER)
                    && !queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER)
                    && !queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TEXT_FIELD)
                    && !queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION)
                    && !queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(NetworkUtility.TAGS.QUESTION_ID, queAnsModel.questionId);
                if (queAnsModel.answer != null)
                    jsonObject.addProperty(NetworkUtility.TAGS.ANSWER, queAnsModel.answer);
                else
                    jsonObject.addProperty(NetworkUtility.TAGS.ANSWER, Utility.EMPTY_STRING);
                quesArray.add(jsonObject);
            }
        }
        return quesArray.toString();
    }

    //    media name will be with extension
//    [{"media_name" : "5","media_type" : "288"},{"media_name" : "5","media_type" : "288"}]

    public static String getSelectedServicesJsonString(ArrayList<StrategicPartnerServiceModel> mSelectedServicesList) {
        JsonArray selectedServiceArray = new JsonArray();
        ArrayList<StrategicPartnerServiceModel> list = mSelectedServicesList;
        for (int i = 0; i < list.size(); i++) {
            StrategicPartnerServiceModel model = list.get(i);
            for (int j = 0; j < model.allSubSubCats.size(); j++) {
                AllSubSubCat allSubSubCat = model.allSubSubCats.get(j);
                JsonObject obj = new JsonObject();
                obj.addProperty(NetworkUtility.TAGS.SUBCATEGORY_ID, model.sub_cat_id);
                obj.addProperty(NetworkUtility.TAGS.SUB_SUB_CAT_ID, allSubSubCat.subSubCatId);
                obj.addProperty(NetworkUtility.TAGS.PRICE, allSubSubCat.price);
                selectedServiceArray.add(obj);
            }
        }
        return selectedServiceArray.toString();
    }

    public static SpannableString getCheepCarePackageMonthlyPrice(Context context, int resId, String price) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);
        SpannableString spannableString = new SpannableString(context.getString(resId, format.format(Double.valueOf(price))));
        int start = spannableString.toString().lastIndexOf("T");
        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(0.5f);

        spannableString.setSpan(relativeSizeSpan, start, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new CustomCharacterSpan(), start, spannableString.length(),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }


}
