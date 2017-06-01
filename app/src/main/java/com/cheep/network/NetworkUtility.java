package com.cheep.network;


import com.cheep.BuildConfig;

/**
 * Created by bhavesh on 13/8/16.
 */
public class NetworkUtility {

    public static final class WS {
        private static final String BASE_URL = BuildConfig.BASE_URL;

        // Endpoints
        public static final String LOGIN = BASE_URL + "customers/auth/cheep_login";
        public static final String LOGOUT = BASE_URL + "customers/profile/logout";
        public static final String SIGNUP = BASE_URL + "customers/auth/registration";
        public static final String SEND_OTP = BASE_URL + "customers/auth/send_otp_to_number";
        public static final String VERIFY_OTP = BASE_URL + "customers/auth/verify_otp_code";
        public static final String PROFILE = BASE_URL + "customers/profile/details";
        public static final String FORG0T_PASSWORD = BASE_URL + "customers/auth/forgot_password";
        public static final String FAQS = BASE_URL + "customers/page_content/faqs";
        public static final String PAGE_CONTENT = BASE_URL + "customers/page_content";
        public static final String CHANGE_PASSWORD = BASE_URL + "customers/profile/change_password";
        public static final String EDIT_PHONE_NUMBER = BASE_URL + "customers/profile/edit_phone_number";
        public static final String VERIFY_OTP_CODE = BASE_URL + "customers/profile/edit_verify_otp";
        public static final String UPDATE_EMERGENCY_CONTACTS = BASE_URL + "customers/profile/update_emergency_contact";
        public static final String ADD_ADDRESS = BASE_URL + "customers/profile/add_address";
        public static final String EDIT_ADDRESS = BASE_URL + "customers/profile/edit_address";
        public static final String DELETE_ADDRESS = BASE_URL + "customers/profile/delete_address";
        public static final String UPDATE_PROFILE = BASE_URL + "customers/profile/edit";
        public static final String CATEGORY_LIST = BASE_URL + "customers/category/all_cats";
        public static final String UPDATE_LOCATION = BASE_URL + "customers/profile/update_location";
        public static final String CREATE_TASK = BASE_URL + "customers/tasks/create";
        public static final String SP_LIST = BASE_URL + "customers/sp_list/listing";
        public static final String SP_LIST_FILTER = BASE_URL + "customers/sp_list/filter_sp_list";

        public static final String ACTION_ON_DETAIL = BASE_URL + "customers/sp_list/actionOnDetail";

        public static final String UPDATE_LANGUAGE = BASE_URL + "customers/profile/updateLanguage";


        public static final String SP_PROFILE_DETAILS = BASE_URL + "customers/sp_profile/details";
        public static final String SP_ADD_TO_FAV = BASE_URL + "customers/sp_profile/sp_favourite";
        public static final String ADD_REVIEW = BASE_URL + "customers/sp_profile/add_review";
        public static final String CANCEL_TASK = BASE_URL + "customers/tasks/cancel_task";
        public static final String SP_LIST_TASK_WISE = BASE_URL + "customers/sp_list/listing_of_sp_relate_task";
        public static final String RESCHEDULE_TASK = BASE_URL + "customers/tasks/rescheduleTask";

        public static final String REPORT_SP = BASE_URL + "customers/sp_profile/report_abuse";
        public static final String TASK_DETAIL = BASE_URL + "customers/tasks/task_details";

        public static final String PENDING_TASK = BASE_URL + "customers/tasks/my_pending_tasks";
        public static final String PAST_TASK = BASE_URL + "customers/tasks/my_past_tasks_list";
        public static final String REVIEW_LIST = BASE_URL + "customers/sp_profile/customer_reviews";
        public static final String COMMENT_LIST = BASE_URL + "customers/sp_profile/review_comments_list";
        public static final String FAV_SP_LIST = BASE_URL + "customers/sp_list/favourite_sp_list";
        public static final String NOTIFICATION_LIST = BASE_URL + "customers/profile/notification_list";

        public static final String ADD_COMMENT = BASE_URL + "customers/sp_profile/add_comment";

        public static final String CHANGE_TASK_STATUS = BASE_URL + "customers/tasks/change_task_status";
        public static final String DECLINE_ADDITIONAL_PAYMENT_REQUEST = BASE_URL + "customers/tasks/rejectAdditionalPaymentRequest";
        public static final String GET_TASK_STATUS = BASE_URL + "customers/tasks/getTaskStatus";

        public static final String VALIDATE_CHEEP_CODE = BASE_URL + "customers/payment/check_cheepcode";
        public static final String GET_PAYMENT_HASH = BASE_URL + "customers/payment/generate_hash";
        public static final String PAYMENT = BASE_URL + "customers/payment/pay_money";
        public static final String PAYMENT_HISTORY = BASE_URL + "customers/payment/payment_history";
        public static final String EMERGENCY_ALERT = BASE_URL + "customers//sp_profile/emergency_alert";

        // Chat Image upload
        public static final String IMAGE_UPLOAD_FOR_CHAT = BASE_URL + "customers/profile/imageUploadForChat";

        // Call to Admin
        public static final String CALL_TO_ADMIN = BASE_URL + "customers/profile/call_to_admin";

        // Call to User
        public static final String CALL_TO_OTHER = BASE_URL + "customers/profile/call_to_other";

        // Check Version of application
        public static final String CHECK_APP_VERSION = BASE_URL + "customers/version/checkVersion";

        // Fetch List of Subservice
        public static final String FETCH_SUB_SERVICE_LIST = BASE_URL + "customers/category/all_sub_cats";
    }

    public static class TAGS {

        public static final String vVERSION = "vVersion";
        public static final String ePLATFORM = "ePlatform";
        public static final String eUSERTYPE = "eUserType";

        public static final String X_API_KEY = "x_api_key";
        public static final String EMAIL_ADDRESS = "email_address";

        public static final String ADDRESS_INITIALS = "address_initials";
        public static final String ADDRESS = "address";
        public static final String CATEGORY = "category";

        public static final String PASSWORD = "password";
        public static final String LAT = "lat";
        public static final String LNG = "lng";

        public static final String OLD_PASSWORD = "old_password";
        public static final String NEW_PASSWORD = "new_password";

        public static final String PLATFORM = "platform";
        public static final String LANGUAGE = "language";

        public static final String USERNAME = "user_name";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String FB_APP_ID = "fb_app_id"; // if Login with "fb"
        public static final String TWITTER_APP_ID = "tw_app_id"; // if Login with "tw"
        public static final String GOOGLE_PLUS_APP_ID = "gp_app_id"; // if Login with "gp"

        public static final String PROFILE_IMAGE = "profile_img";
        public static final String PROFILE_BANNER = "profile_banner";
        public static final String PRICE = "price";
        public static final String DISTANCE = "distance";
        public static final String IS_FAVOURITE = "is_favourite";
        public static final String DATA = "data";
        public static final String WS_ACCESS_KEY = "ws_access_key";
        public static final String USER_ID = "user_id";
        public static final String PAGE_ID = "page_id";
        public static final String NUMBER = "number";
        public static final String NAME = "name";
        public static final String EMERGENCY_DATA = "emergency_data";
        public static final String ADDRESS_ID = "address_id";
        public static final String CITY_NAME = "city_name";
        public static final String LOCALITY = "locality";
        public static final String REQUEST_DETAIL_STATUS = "request_detail_status";
        public static final String vVERSION_TYPE = "vVersionType";


        public static final String QUOTE_AMOUNT = "quote_amount";
        public static final String PAYABLE_AMOUNT = "payable_amount";
        public static final String DISCOUNT_AMOUNT = "discount_amount";

        public static final String TASK_DESC = "task_desc";
        public static final String CITY_ID = "city_id";
        public static final String CAT_ID = "cat_id";
        public static final String SUBCATEGORY_ID = "subcategory_id";

        public static final String START_DATETIME = "start_datetime";
        public static final String TASK_IMAGE = "task_image";
        public static final String SP_EXTRA_IMAGES = "sp_extra_imgs";
        public static final String SP_DATA = "sp_data";
        public static final String SP_USER_ID = "sp_user_id";
        public static final String REQ_FOR = "req_for";
        public static final String TASK_ID = "task_id";
        public static final String TASK_STARTDATE = "task_startdate";

        public static final String LAST_ID = "last_id";
        public static final String RATINGS = "ratings";
        public static final String REVIEW_ID = "review_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String MONTH_YEAR = "month_year";
        public static final String PAGE_NUM = "page_num";
        public static final String COMMENT = "comment";
        public static final String REPORT_ABUSE = "report_abuse";
        public static final String CHEEPCODE = "cheepcode";
        public static final String TRANSACTION_ID = "txnid";

        public static final String REASON = "reason";
        public static final String MONTHLY_SAVED_TOTAL = "monthly_saved_total";
        public static final String TOTAL_EARNED = "total_earned";
        public static final String MONTHLY_TOTAL = "monthly_total";
        public static final String TASK_STATUS = "task_status";
        public static final String TASK_PAID_AMOUNT = "task_paid_amount";
        public static final String ADDITIONAL_QUOTE_AMOUNT = "additional_quote_amount";

        public static final String SP_COUNTS = "sp_counts";
        public static final String MAX_QUOTE_PRICE = "max_quote_price";

        public static final String TYPE = "type";
        public static final String CHAT_IMG = "chat_img";

        public static class VERSION_CHANGE_TYPE {
            public static final int NORMAL = 0;
            public static final int RECOMMENDED_TO_UPGRADE = 1;
            public static final int FORCE_UPGARDE_REQUIRED = 2;
        }

        public static class PAGEID_TYPE {
            public static final String FAQ = "1";
            public static final String TERMS = "2";
            public static final String PRIVACY = "3";
        }

        public static class PLATFORMTYPE {
            public static final String ANDROID = "android";
            public static final String IOS = "ios";
        }

        public static final String DEVICE_TOKEN = "device_token";

        //Value can be between ( "mobile" | "fb" | "gp" | "tw" )
        public static final String LOGINWITH = "login_with";

        public static class LOGINWITHTYPE {
            public static final String MOBILE = "mobile";
            public static final String FACEBOOK = "fb";
            public static final String GOOGLEPLUS = "gp";
            public static final String TWITTER = "tw";
        }

        public static final String STATUS_CODE = "status_code";
        public static final String STATUS = "status";
        public static final String TASK_ENDDATE = "task_enddate";
        public static final String TASK_RATINGS = "task_ratings";

        public static final String PAYMENT_STATUS = "payment_status";
        public static final String PAYMENT_LOG = "payment_log";
        public static final String IS_FOR_ADDITIONAL_QUOTE = "isForAdditionalQuote";

        public static class STATUSCODETYPE {
            public static final int SUCCESS = 200;
            public static final int DISPLAY_GENERALIZE_MESSAGE = 400; //Something went wrong
            public static final int DISPLAY_ERROR_MESSAGE = 444; //Error Message
            public static final int SIGNUP_REQUIRED = 445;
            public static final int FORCE_LOGOUT_REQUIRED = 446;
            public static final int USER_DELETED = 447;
        }

        public static final String MESSAGE = "message";
        public static final String VERSION_DESC = "version_desc";

        public static final String OTP_CODE = "otp_code";

        public class LANGUAGE_TYPE {
            public static final String ENGLISH = "english";
            public static final String HINDI = "hindi";
        }

        public class ADDRESS_TYPE {
            public static final String HOME = "home";
            public static final String OTHERS = "other";
        }


    }

}

