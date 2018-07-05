package com.cheep.network;


import com.cheep.BuildConfig;
import com.cheep.model.AddressModel;

import java.util.Map;

/**
 * Created by bhavesh on 13/8/16.
 */
public class NetworkUtility {

    public static final class WS {

        private static final String BASE_URL = BuildConfig.BASE_URL;

        private static final String BASE_URL_API = BuildConfig.BASE_URL_API;

        // Endpoints

        public static final String GET_RELATIONSHIP_LIST = BASE_URL + "customers/profile/getRelationshipList";
        public static final String LOGIN = BASE_URL + "customers/auth/cheep_login";
        public static final String LOGOUT = BASE_URL + "customers/profile/logout";
        public static final String FRESHCHAT_RESTORE_ID = BASE_URL + "customers/profile/add_restoreId";
        public static final String SIGNUP = BASE_URL + "customers/auth/registration";
        public static final String SEND_OTP = BASE_URL + "customers/auth/send_otp_to_number";
        public static final String VERIFY_OTP = BASE_URL + "customers/auth/verify_otp_code";
        public static final String PROFILE = BASE_URL + "customers/profile/details";
        public static final String NEED_HELP = BASE_URL + "customers/category/needHelpForCategory";
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
        public static final String CATEGORY_RATE_CARD = BASE_URL + "customers/care/getCategoryRateCard";
        public static final String ALL_BANNER = BASE_URL + "customers/category/all_banner";
        public static final String GET_CATEGORY_ID_BASED_ON_SLUG = BASE_URL + "customers/category/getCategoryIdBasedOnSlug";
        public static final String FAVOURITE_CATEGORY = BASE_URL + "customers/category/category_favourite";
        public static final String MANAGE_SUBSCRIPTION = BASE_URL + "customers/care/getUserSubscribedCarePackage";

        public static final String UPDATE_LOCATION = BASE_URL + "customers/profile/update_location";
        public static final String CREATE_TASK = BASE_URL + "customers/tasks/create";
        public static final String GET_PRO_FOR_INSTA_BOOKING = BASE_URL + "customers/tasks/getProForInstantBooking";

        public static final String SP_LIST = BASE_URL + "customers/sp_list/listing";
        public static final String SP_LIST_FILTER = BASE_URL + "customers/sp_list/filter_sp_list";

        public static final String ACTION_ON_DETAIL = BASE_URL + "customers/sp_list/actionOnDetail";

        public static final String UPDATE_LANGUAGE = BASE_URL + "customers/profile/updateLanguage";
        public static final String GET_USER_REVIEW_LIST = BASE_URL + "customers/profile/getUserReviewList";


        public static final String SP_PROFILE_DETAILS = BASE_URL + "customers/sp_profile/details";
        public static final String SP_ADD_TO_FAV = BASE_URL + "customers/sp_profile/sp_favourite";
        public static final String ADD_REVIEW = BASE_URL + "customers/sp_profile/add_review";
        public static final String CANCEL_TASK = BASE_URL + "customers/tasks/cancel_task";
        // TODO : renamed web service listing_of_sp_relate_task -> getQuoteList
        public static final String SP_LIST_TASK_WISE = BASE_URL + "customers/tasks/getQuoteList";
        //        public static final String SP_LIST_TASK_WISE = BASE_URL + "customers/sp_list/listing_of_sp_relate_task";
        public static final String RESCHEDULE_TASK = BASE_URL + "customers/tasks/rescheduleTask";

        public static final String REPORT_SP = BASE_URL + "customers/sp_profile/report_abuse";
        // TODO : renamed web service task_details -> getTaskDetail
//        public static final String TASK_DETAIL = BASE_URL + "customers/tasks/task_details";
        public static final String TASK_DETAIL = BASE_URL + "customers/care/getTaskDetail";
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
        // no longer used as per new changes in cheep care featuers
//        public static final String GET_PAYMENT_HASH_FOR_STRATEGIC_PARTNER = BASE_URL + "customers/payment/generate_hash_for_stratagic_partner";
        public static final String PAYMENT = BASE_URL + "customers/payment/pay_money";
        public static final String PAYMENT_HISTORY = BASE_URL + "customers/payment/payment_history";
        public static final String EMERGENCY_ALERT = BASE_URL + "customers/sp_profile/emergency_alert";
        public static final String CHECK_PROCESSING_TASK = BASE_URL + "customers/tasks/check_processing_task";
        public static final String BOOK_PRO_FOR_NORMAL_TASK = BASE_URL + "customers/tasks/bookProForNormalTask";

        //admin setting web service
        public static final String GET_ADMIN_SETTINGS = BASE_URL + "customers/category/getAdminSettings";

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

        // Check Task Status
        public static final String CHECK_TASK_STATUS = BASE_URL + "customers/tasks/check_task_status";

        // Fetch list of all strategic partber services and sub categories
        public static final String FETCH_SUB_CATS_STRATEGIC_PARTNER_LIST = BASE_URL + "customers/category/all_sub_cats_strategic_partner";

        // Fetch list of all strategic partner services and sub categories
        public static final String FETCH_SUB_CATEGORIES_QUESTIONNAIRE = BASE_URL + "customers/category/categories_questionnaire";
        public static final String CHECK_CHEEPCODE_FOR_STRATEGIC_PARTNER = BASE_URL + "customers/payment/check_cheepcode_for_strategic_partner";

        // Task Creation for Strategic Partner
        public static final String TASK_CREATE_STRATEGIC_PARTNER = BASE_URL + "customers/tasks/stratagic_partner_task_create";
        public static final String TASK_CREATE_INSTA_BOOKING = BASE_URL + "customers/tasks/instaBookingTaskCreate";

        public static final String CURL_NOTIFICATION_TO_SP = BASE_URL + "customers/tasks/curl_for_notification_to_sp";
        public static final String CHECK_PRO_AVAILABILITY_FOR_STRATEGIC_TASK = BASE_URL + "customers/tasks/check_pro_availability_for_Strategic_task";

        //Refer and Earn
        public static final String REFER_BALANCE = BASE_URL + "customers/profile/getReferBalance";
        public static final String GET_AMOUNT_WITH_GST = BASE_URL + "customers/payment/getAmountWithGST";
        public static final String PAY_TASK_PAYMENT = BASE_URL + "customers/payment/payTaskPayment";
        public static final String ACCEPT_ADDITIONAL_PAYMENT_REQUEST = BASE_URL + "customers/tasks/acceptAdditionalPaymentRequest";
        public static final String GET_PAYMENT_SUMMARY = BASE_URL + "customers/tasks/getPaymentSummary";

        // Paytm Verify Transaction based on Order
        // Save Paytm user details
        public static final String SAVE_PAYTM_USER_DETAILS = BASE_URL_API + "customers/payment/storePaymentGatewayData";
        public static final String GET_CHECKSUM_HASH = BASE_URL_API + "customers/payment/getPaytmChecksum";
        public static final String VERIFY_CHECKSUM = BASE_URL_API + "customers/Verifychecksum";
        public static final String FETCH_CALLBACK_RESPONSE_FROM_PAYTM = BASE_URL_API + "customers/payment/getCallBackResponse";
        public static final String PAYTM_GENERATE_OTP = BASE_URL_API + "customers/payment/generateOTP";
        public static final String PAYTM_VERIFY_OTP = BASE_URL_API + "customers/payment/verifyOTP";
        public static final String PAYTM_CHECK_BALANCE = BASE_URL_API + "customers/payment/checkPaytmBalance";
        public static final String PAYTM_WITHDRAW_MONEY = BASE_URL_API + "customers/payment/withdrawMoney";


        // CHEEP CARE web services
        public static final String GET_CITY_CARE_DETAIL = BASE_URL + "customers/care/getCityCareDetail";
        public static final String GET_CARE_PACKAGE_DETAILS = BASE_URL + "customers/care/getCarePackageDetails";
        public static final String VERIFY_ADDRESS_CHEEP_CARE = BASE_URL + "customers/profile/verify_address";
        public static final String CHECK_CHEEP_CARE_CODE = BASE_URL + "customers/care/check_cheepcarecode";
        public static final String GET_CARE_PACKAGE_TIP = BASE_URL + "customers/care/getCarePackageTip";
        public static final String PURCHASE_CARE_PACKAGE = BASE_URL + "customers/care/purchaseCarePackage";
        public static final String GET_USER_SUBSCRIBED_CARE_PACKAGE = BASE_URL + "customers/care/getUserSubscribedCarePackage";
        public static final String GET_CARE_FREE_PAID_SERVICES_FOR_CATEGORY = BASE_URL + "customers/care/getCareFreePaidServicesForCategory";
        public static final String CARE_CREATE_TASK = BASE_URL + "customers/care/createTask";
        public static final String IS_CITY_AVAILABLE_FOR_CARE = BASE_URL + "customers/care/isCityAvailableForCare";
        public static final String GET_EXTRA_CHARGE_AFTER_EXCEED_LIMIT = BASE_URL + "customers/care/getExtraChargeAfterExceedLimit";
        public static final String GET_TASK_FOR_PENDING_REVIEW = BASE_URL + "customers/Tasks/getTaskForPendingReview";
        public static final String SEARCH_CITY = BASE_URL + "customers/profile/searchCity";
        public static final String VOTE_CITY_FOR_CHEEP_CARE = BASE_URL + "customers/care/voteCityForCheepCare";
        public static final String GET_ASSET_AREA = BASE_URL + "customers/profile/getAssetArea";
        public static final String IDENTIFY_ADDRESS_IF_SUBSCRIBED = BASE_URL + "customers/care/identifyAddressIfSubscribed";


        // majid khan
        public static final String GET_PACKAGE_FEATURE_LIST = BASE_URL + "customers/care/getPackageFeatureList";
        public static final String GET_BADGE_MESSAGE = BASE_URL + "customers/care/getBadgeMessage";


        /**
         * this is ws is used for fetching  types of rating to be submitted
         * also when user has submitted values for rating this ws will be used to fetch those datas
         */
        public static final String GET_TASK_REVIEW = BASE_URL + "customers/tasks/getTaskReview";

    }

    public static class PAYTM {
        //This direct urls are not needed as payTM recommends to call these URLs from server side
        /*public static class OAUTH_APIS {
            private static final String BASE_URL = BuildConfig.PAYTM_OAUTH_BASE_URL;

            //End points
            public static final String SEND_OTP = BASE_URL + "/signin/otp";
            public static final String GET_ACCESS_TOKEN_SENDING_OTP = BASE_URL + "/signin/validate/otp";
            public static final String GET_USER_DETAILS_BY_VALIDATING_TOKEN = BASE_URL + "/user/details";
        }*/

        public static final class WALLET_APIS {
            private static final String BASE_URL = BuildConfig.PAYTM_WALLET_BASE_URL;

            //End points
            public static final String ADD_MONEY = BASE_URL + "/oltp-web/processTransaction";
//            public static final String WITHDRAW_MONEY = BASE_URL + "/oltp/HANDLER_FF/withdrawScw";
//            public static final String STATUS_QUERY = BASE_URL + "/oltp/HANDLER_INTERNAL/getTxnStatus";
//            public static final String REFUND_MONEY = BASE_URL + "/oltp/HANDLER_INTERNAL/REFUND";
//            public static final String REFUND_STATUS = BASE_URL + "/oltp/HANDLER_INTERNAL/REFUND_STATUS";
        }

//        public static final String CHECK_BALANCE_API = BuildConfig.PAYTM_CHECK_BALANCE_URL;

        public static final class PARAMETERS {

            public static final String phone = "phone";
            public static final String clientId = "clientId";
            public static final String scope = "scope";
            public static final String responseType = "responseType";
            public static final String responseCode = "responseCode";
            public static final String state = "state";
            public static final String Authorization = "Authorization";
            public static final String otp = "otp";
            public static final String access_token = "access_token";
            public static final String expires = "expires";
            public static final String resourceOwnerId = "resourceOwnerId";
            public static final String session_token = "session_token";
            public static final String merchantGuid = "merchantGuid";
            public static final String mid = "mid";
            public static final String request = "request";
            public static final String ssotoken = "ssotoken";
            public static final String token = "token";
            public static final String statusCode = "statusCode";
            public static final String requestGuid = "requestGuid";
            public static final String orderId = "orderId";
            public static final String paytmWalletBalance = "paytmWalletBalance";
            public static final String totalBalance = "totalBalance";
            public static final String response = "response";
            public static final String ownerGuid = "ownerGuid";
            public static final String walletGrade = "walletGrade";
            public static final String ssoId = "ssoId";
            public static final String MID = "MID";
            public static final String REQUEST_TYPE = "REQUEST_TYPE";
            public static final String ORDER_ID = "ORDER_ID";
            public static final String order_id = "order_id";
            public static final String CUST_ID = "CUST_ID";
            public static final String TXN_AMOUNT = "TXN_AMOUNT";
            public static final String CHANNEL_ID = "CHANNEL_ID";
            public static final String INDUSTRY_TYPE_ID = "INDUSTRY_TYPE_ID";
            public static final String WEBSITE = "WEBSITE";
            public static final String SSO_TOKEN = "SSO_TOKEN";
            public static final String CHECKSUMHASH = "CHECKSUMHASH";
            public static final String MOBILE_NO = "MOBILE_NO";
            public static final String EMAIL = "EMAIL";
            public static final String CALLBACK_URL = "CALLBACK_URL";
            public static final String RESPCODE = "RESPCODE";
            public static final String RESPMSG = "RESPMSG";
            public static final String STATUS = "STATUS";
            public static final String TXNAMOUNT = "TXNAMOUNT";
            public static final String ORDERID = "ORDERID";
            public static final String ReqType = "ReqType";
            public static final String TxnAmount = "TxnAmount";
            public static final String AppIP = "AppIP";
            public static final String OrderId = "OrderId";
            public static final String Currency = "Currency";
            public static final String DeviceId = "DeviceId";
            public static final String SSOToken = "SSOToken";
            public static final String PaymentMode = "PaymentMode";
            public static final String CustId = "CustId";
            public static final String IndustryType = "IndustryType";
            public static final String Channel = "Channel";
            public static final String AuthMode = "AuthMode";
            public static final String CheckSum = "CheckSum";
            public static final String JsonData = "JsonData";
            public static final String THEME = "THEME";
            public static final String ResponseCode = "ResponseCode";

            // new subscription params
            public static final String SUBS_SERVICE_ID = "SUBS_SERVICE_ID";
            public static final String SUBS_AMOUNT_TYPE = "SUBS_AMOUNT_TYPE";
            public static final String SUBS_FREQUENCY = "SUBS_FREQUENCY";
            public static final String SUBS_FREQUENCY_UNIT = "SUBS_FREQUENCY_UNIT";
            public static final String SUBS_ENABLE_RETRY = "SUBS_ENABLE_RETRY";
            public static final String SUBS_EXPIRY_DATE = "SUBS_EXPIRY_DATE";
        }

        public static final class RESPONSE_CODES {
            // Send otp API response codes
            public static final String LOGIN = "01";
            public static final String REGISTER = "02";
            public static final String INVALID_AUTHORIZATION = "430";
            public static final String INVALID_MOBILE = "431";
            public static final String LOGIN_FAILED = "432";
            public static final String ACCOUNT_BLOCKED = "433";
            public static final String BAD_REQUEST = "434";
            public static final String INVALID_EMAIL = "465";

            // Verify otp/get access token API response codes
            public static final String INVALID_OTP = "403";
            public static final String INVALID_CODE = "513";

            // Get User Info / validate token API response codes
            public static final String INVALID_TOKEN = "530";

            // Check Balance API response codes
            public static final String SUCCESS = "SUCCESS";
            public static final String USER_DOESNOT_EXISTS = "404";
            public static final String UNKNOWN_ERROR = "GE_0001";
            public static final String UNAUTHORIZED_ACCESS = "403";
            public static final String REQUEST_TIMED_OUT = "408";
            public static final String INTERNAL_SERVER_ERROR = "500";
            public static final String INCORRECT_MERCHANT_DETAILS = "CBM_1001";
            public static final String INCORRECT_PAYEE_DETAILS = "CBM_1002";
            public static final String PLEASE_TRY_AGAIN = "AM_1001";

        }
    }

    public interface TAGS {
        String DATA = "data";
        String CallbackResponse = "callbackresponse";
        String vVERSION = "vVersion";
        String ePLATFORM = "ePlatform";
        String eUSERTYPE = "eUserType";

        String X_API_KEY = "x_api_key";
        String EMAIL_ADDRESS = "email_address";

        String ADDRESS_INITIALS = "address_initials";
        String ADDRESS = "address";
        String CATEGORY = "category";
        String OUT_OF_OFFICE_HOURS_MSG = "msg";
        String URGENT_BOOKING_MSG = "msg";
        String PASSWORD = "password";
        String LAT = "lat";
        String LNG = "lng";
        String SORT_TYPE = "sortType";

        String OLD_PASSWORD = "old_password";
        String NEW_PASSWORD = "new_password";

        String PLATFORM = "platform";
        String LANGUAGE = "language";

        String USERNAME = "user_name";
        String TITLE = "title";
        String DESCRIPTION = "description";
        String PHONE_NUMBER = "phone_number";
        String FB_APP_ID = "fb_app_id"; // if Login with "fb"
        String TWITTER_APP_ID = "tw_app_id"; // if Login with "tw"
        String GOOGLE_PLUS_APP_ID = "gp_app_id"; // if Login with "gp"

        String PROFILE_IMAGE = "profile_img";
        String PROFILE_BANNER = "profile_banner";
        //        String PRICE = "price";
        String DISTANCE = "distance";
        String IS_FAVOURITE = "is_favourite";
        String SUB_CATS = "sub_cats";
        String TASK_DETAIL = "task_detail";
        //        for closest area
        String CLOSEST_AREA = "closest_address";
        String CLOSEST_CATEGORY = "category";
        String CLOSEST_ADDRESS = "address";
        String WS_ACCESS_KEY = "ws_access_key";
        String USER_ID = "user_id";
        String PAGE_ID = "page_id";
        String NUMBER = "number";
        String EMERGENCY_DATA = "emergency_data";
        String CITY_NAME = "city_name";
        String COUNTRY = "country";
        String STATE = "state";
        String LOCALITY = "locality";
        String REQUEST_DETAIL_STATUS = "request_detail_status";
        String vVERSION_TYPE = "vVersionType";
        String AVERAGE_RATING = "average_rating";


        String QUOTE_AMOUNT = "quote_amount";
        String PAYABLE_AMOUNT = "payable_amount";
        //String DISCOUNT_AMOUNT = "discount_amount";
//        public static final String PAYABLE_AMOUNT_WITH_GST = "payable_amount_with_gst";

        String TASK_DESC = "task_desc";
        String CITY_ID = "city_id";
        String CAT_ID = "cat_id";
        String SUBCATEGORY_ID = "sub_cat_id";

        String START_DATETIME = "start_datetime";
        String TASK_IMAGE = "task_image";
        String SP_EXTRA_IMAGES = "sp_extra_imgs";
        String SP_DATA = "sp_data";
        String SP_USER_ID = "sp_user_id";
        String SP_USER_NAME = "sp_user_name";
        String REQ_FOR = "req_for";
        String TASK_ID = "task_id";

        String TASK_STARTDATE = "task_startdate";

        String LAST_ID = "last_id";
        String RATINGS = "ratings";
        String REVIEW_ID = "review_id";
        String TIMESTAMP = "timestamp";
        String MONTH_YEAR = "month_year";
        String PAGE_NUM = "page_num";
        String COMMENT = "comment";
        String REPORT_ABUSE = "report_abuse";
        String CHEEPCODE = "cheepcode";
        String TRANSACTION_ID = "txnid";
        String OUT_OF_OFFICE_CHARGES = "non_office_hours_charge";
        String URGENT_BOOKING_CHARGES = "urgent_booking_charge";
        String REASON = "reason";
        String MONTHLY_SAVED_TOTAL = "monthly_saved_total";
        String TOTAL_EARNED = "total_earned";
        String MONTHLY_TOTAL = "monthly_total";
        String TASK_STATUS = "task_status";
        String TOTAL_ONGOING_TASK = "total_ongoing_task";
        String TASK_PAID_AMOUNT = "task_paid_amount";
        String ADDITIONAL_QUOTE_AMOUNT = "additional_quote_amount";

        String SP_COUNTS = "sp_counts";
        String MAX_QUOTE_PRICE = "max_quote_price";
        String QUOTED_SP_IMAGE_URL = "quoted_sp_image_url";

        String TYPE = "type";
        String CHAT_IMG = "chat_img";
        String IS_CHAT = "isChat";

        String ADD = "add";
        String REMOVE = "remove";

        String SUB_CATEGORY_DETAIL = "sub_category_detail";
        String QUESTION_DETAIL = "question_detail";
        String RESULT = "result";
        String MEDIA_FILE = "media_file";
        String TASK_TYPE = "task_type";
        String PROMOCODE_PRICE = "promocode_price";
        String HASH_STRING = "hash_string";

        String HASH_0 = "hash_0";
        String HASH_1 = "hash_1";
        String HASH_2 = "hash_2";
        String HASH_3 = "hash_3";
        String HASH_4 = "hash_4";
        String HASH_5 = "hash_5";
        String HASH_6 = "hash_6";
        String HASH_7 = "hash_7";

        String PAYMENT_METHOD = "payment_method";
        String PAYMENT_METHOD_TYPE_TAG = "payment_method_type";
        String REFER_CODE = "refer_code";
        // paytm params
        String ORDER_ID = "ORDER_ID";
        String TXN_AMOUNT = "TXN_AMOUNT";
        String CUST_ID = "CUST_ID";
        String CHECKSUMHASH = "CHECKSUMHASH";
        String PAYTM_CUST_ID = "paytm_cust_id";
        String PAYTM_ACCESS_TOKEN = "paytm_access_token";
        String PAYTM_PHONE_NUMBER = "paytm_phone_number";
        String SUBS_ID = "SUBS_ID";

        // refer and earn params
        String IS_REFER_CODE = "is_refer_code";
        String WALLET_BALANCE = "wallet_balance";
        String MAX_REFER_DISCOUNT = "max_refer_discount";
        String USED_WALLET_BALANCE = "used_wallet_balance";
        String IS_INSTA_BOOKING = "is_insta_booking";
        String AMOUNT = "amount";
        public static final String REFER_COUNT = "refer_count";
        String PAYTM_RESPONSE_DATA = "paytmResponseData";
        String PAYMENT_GATEWAY_DATA = "paymentGatewayData";
        String ACCESS_TOKEN_EXPIRES_TIMESTAMP = "access_token_expires_timestamp";
        String SUB_SUB_CAT_ID = "sub_sub_cat_id";
        String QUESTION_ID = "question_id";
        String ANSWER = "answer";
        String PRO_PAYMENT_STATUS = "pro_payment_status";
        String ADDITIONAL_PENDING_AMOUNT = "additional_pending_amount";
        String CAT_SLUG = "cat_slug";
        String CAT_TYPE = "cat_type";
        String NORMAL_BANNER = "normalBanner";
        String CARE_BANNER = "careBanner";
        String CARE_CITY_SLUG = "care_city_slug";
        String CARE_CITY_NAME = "care_city_name";
        String CARE_PACKAGE_ID = "care_package_id";
        String LANDMARK = "landmark";
        String PINCODE = "pincode";
        String NAME = "name";
        String NICKNAME = "nickname";
        String PACKAGE_OPTION_DETAILS = "package_option_details";
        String CHEEP_CARE_CODE = "cheepcarecode";
        String DISCOUNT = "discount";
        String ADMIN_SETTING = "admin_setting";
        String GST_RATE = "GST_RATE";
        String SUBTITLE = "subtitle";
        String TOTAL_AMOUNT = "total_amount";
        String PROMOCODE = "promocode";
        String TAX_AMOUNT = "tax_amount";
        String IS_ANNUALLY = "is_annually";
        String CARE_CITY_ID = "care_city_id";
        String DSA_CODE = "dsaCode";
        String BUNDLE_DISCOUNT_PERCENT = "bundlediscount_percent";
        String BUNDLE_DISCOUNT_PRICE = "bundlediscount_price";
        String DISCOUNT_AMOUNT = "discount_amount";
        String PAID_AMOUNT = "   paid_amount";
        //String PACKAGE_DURATION = "paid_amount";

        String CART_DETAIL = "cart_detail";
        String CITY_DETAIL = "cityDetail";
        String USER_PACKAGE_DETAIL = "userPackageDetail";
        String PACKAGE_DETAIL = "packageDetail";
        String PAID_SERVICE = "paid_service";
        String FREE_SERVICE = "free_service";
        String IS_PURCHASED = "is_purchased";
        String IS_SAME_PACKAGE_TYPE = "is_same_package_type";
        String PACKAGE_ID = "package_id";
        String PACKAGE_TITLE = "package_title";
        String PACKAGE_TYPE = "package_type";
        String ADDRESS_ID = "address_id";
        String PACKAGE_DURATION = "package_duration";
        String PACKAGE_OPTION_ID = "package_option_id";
        String PACKAGE_SUBOPTION_ID = "package_suboption_id";
        String SELECTED_UNIT = "selected_unit";
        String MONTHLY_PRICE = "monthly_price";
        String UNIT_PRICE = "unit_price";
        String PACKAGE_SUBOPTION = "package_suboption";
        String PACKAGE_OPTIONS = "package_options";
        String PACKAGE_DETAILS = "package_details";
        String CHARGE_SPECIFIC_TIME = "charge_specific_time";
        String CHARGE_EXCEED_LIMIT = "charge_exceed_limit";
        String FINAL_EXTRA_CHARGE = "finalExtraCharge";
        String TASK_SUB_CATEGORIES = "task_subcategories";
        String PRICE = "price";
        String DETAIL = "detail";
        String CAT_NAME = "cat_name";
        String HOME = "home";
        String OFFICE = "office";

        String REVIEW_BY_ME = "review_by_me";
        String RESTORE_ID = "restore_id";

        String IS_RENEW = "is_renew";
        String ASSET_TYPE_ID = "asset_type_id";


        String TEXT = "text";
        String CATEGORY_TIP = "category_tip";

        String FEATURE_LIST = "feature_list";
        String PRICE_LIST = "price_list";
        String PACKAGE_DATA = "packageData";
        String CAT_PRICE = "cat_price";
        String URGENT_BOOKING_CHARGE = "urgent_booking_charge";
        String NON_OFFICE_HOURS_CHARGE = "non_office_hours_charge";
        String MANAGE_SUBSCRIPTION_PACKAGE = "packageDetail";
        String MSG_TYPE= "msg_type";


        String DATA_0 = "data_0";
        String DATA_1 = "data_1";
        String DATA_2 = "data_2";
        String DATA_3 = "data_3";
        String DATA_4 = "data_4";
        String DATA_5 = "data_5";
        String DATA_6 = "data_6";
        String DATA_7 = "data_7";



        interface VERSION_CHANGE_TYPE {
            int NORMAL = 0;
            int RECOMMENDED_TO_UPGRADE = 1;
            int FORCE_UPGARDE_REQUIRED = 2;
        }

        interface PAGEID_TYPE {
            String FAQ = "1";
            String TERMS = "2";
            String PRIVACY = "3";
        }

        interface PLATFORMTYPE {
            String ANDROID = "android";
            String IOS = "ios";
        }


        String DEVICE_TOKEN = "device_token";

        //Value can be between ( "mobile" | "fb" | "gp" | "tw" )
        String LOGINWITH = "login_with";

        interface LOGINWITHTYPE {
            String MOBILE = "mobile";
            String FACEBOOK = "fb";
            String GOOGLEPLUS = "gp";
            String TWITTER = "tw";
        }

        String STATUS_CODE = "status_code";
        String STATUS = "status";
        String TASK_ENDDATE = "task_enddate";
        String TASK_RATINGS = "task_ratings";

        String PAYMENT_STATUS = "payment_status";
        String PAYMENT_LOG = "payment_log";
        String IS_FOR_ADDITIONAL_QUOTE = "isForAdditionalQuote";
        String RESCHEDULE_DATETIME = "reschedule_datetime";
        String SEARCH_TEXT = "search_text";


        interface STATUSCODETYPE {
            int SUCCESS = 200;
            int DISPLAY_GENERALIZE_MESSAGE = 400; //Something went wrong
            int DISPLAY_ERROR_MESSAGE = 444; //Error Message
            int SIGNUP_REQUIRED = 445;
            int FORCE_LOGOUT_REQUIRED = 446;
            int USER_DELETED = 447;
        }

        interface BANNER_TYPE {
            String STRATEGIC = "strategic";
            String REFERRAL = "referral";
            String NORMAL = "normal";
        }

        String MESSAGE = "message";
        String TEXT_OKAY = "textOkay";
        String PICTURE_URL = "picture_url";
        String RESOURCE_ID = "resourceid";
        String PROFILE_PIC_NEEDS_TOBE_SHOWN = "is_profile_pic_needs_be_shown";
        String VERSION_DESC = "version_desc";
        String SHOW_BADGE = "showBadge";

        String OTP_CODE = "otp_code";

        class LANGUAGE_TYPE {
            public static final String ENGLISH = "english";
            public static final String HINDI = "hindi";
        }

        class ADDRESS_TYPE {
            public static final String HOME = "home";
            public static final String OFFICE = "office";
            public static final String OTHERS = "other";
            public static final String BIZ = "biz";
            public static final String SOCI = "soci";
        }

        class APPSFLYER_CUSTOM_TRACK_EVENTS {
            public static final String REG_MOBILE = "RegisterWithMobile";
            public static final String REG_FB = "RegisterWithFB";
            public static final String REG_TWITTER = "RegisterWithTwitter";
            public static final String REG_GOOGLE = "RegisterWithGPlus";
            public static final String TASK_CREATE = "TaskCreation";
            public static final String COUPON_DUNIA_TASK_DEBUG = "CouponDuniaTaskDebug";
            public static final String COUPON_DUNIA_TASK_LIVE = "CouponDuniaTask";
//            public static final String INSTA_BOOK = "InstaBook";
        }
    }


    public interface PAYMENT_METHOD_TYPE {
        String MOBIKWIK = "mobikwik";
        String PAYTM = "paytm";
        String PAYU = "payu";
        String COD = "cod";
        String FREE = "free";
        String PAY_LATER = "pay_later";
    }

    public interface CARE_CITY_SLUG {
        String MUMBAI = "mumbai";
        String HYDRABAD = "hydrabad";
        String DELHI = "delhi";
        String CHENNAI = "chennai";
        String BENGALURU = "bengaluru";
    }

    public interface CARE_PACKAGE_SLUG {
        String BIZ_CARE = "bizcare";
        String HOME_CARE = "homecare";
        String SOCI_CARE = "socicare";
        String TECH_CARE = "techcare";
        String APPLIANCE_CARE = "appliancecare";
    }

    public interface PACKAGE_DETAIL_TYPE {
        String premium = "premium";
        String normal = "normal";
    }


    public static Map<String, Object> addGuestAddressParams(Map<String, Object> mParams, AddressModel mSelectedAddressModelForInsta) {
        mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mSelectedAddressModelForInsta.address_initials);
        mParams.put(NetworkUtility.TAGS.ADDRESS, mSelectedAddressModelForInsta.address);
        mParams.put(NetworkUtility.TAGS.CATEGORY, mSelectedAddressModelForInsta.category);
        mParams.put(NetworkUtility.TAGS.LAT, mSelectedAddressModelForInsta.lat);
        mParams.put(NetworkUtility.TAGS.LNG, mSelectedAddressModelForInsta.lng);
        mParams.put(NetworkUtility.TAGS.CITY_NAME, mSelectedAddressModelForInsta.cityName);
        mParams.put(NetworkUtility.TAGS.COUNTRY, mSelectedAddressModelForInsta.countryName);
        mParams.put(NetworkUtility.TAGS.STATE, mSelectedAddressModelForInsta.stateName);
        mParams.put(NetworkUtility.TAGS.LANDMARK, mSelectedAddressModelForInsta.landmark);
        mParams.put(NetworkUtility.TAGS.NICKNAME, mSelectedAddressModelForInsta.nickname);
        mParams.put(NetworkUtility.TAGS.PINCODE, mSelectedAddressModelForInsta.pincode);
        return mParams;
    }

}

