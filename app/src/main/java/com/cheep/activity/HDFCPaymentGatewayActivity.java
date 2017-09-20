package com.cheep.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cheep.BuildConfig;
import com.cheep.R;
import com.payu.custombrowser.Bank;
import com.payu.custombrowser.PayUWebChromeClient;
import com.payu.custombrowser.PayUWebViewClient;
import com.payu.magicretry.Helpers.Util;
import com.payu.magicretry.MagicRetryFragment;

import java.util.HashMap;
import java.util.Map;


public class HDFCPaymentGatewayActivity extends AppCompatActivity implements MagicRetryFragment.ActivityCallback {

    Bundle bundle;

    //payment URL
    private String url;

    //post parameters to send to PayU server
    private String postData;

    boolean cancelTransaction = false;

    private static BroadcastReceiver mReceiver = null;
    private String UTF = "UTF-8";
    private boolean viewPortWide = false;
    private WebView mWebView;

    private String merchantHash;
    MagicRetryFragment magicRetryFragment;
    String txnId = null;


    public static void newInstance(Context context, String postData, int requestCode) {

        Intent intent = new Intent(context, HDFCPaymentGatewayActivity.class);
        intent.putExtra("url", BuildConfig.PAYUBIZ_HDFC_URL);
        intent.putExtra("postData", postData);
        // if task is generated from insta booking feature then addition payment field will not come in response
        ((AppCompatActivity) context).startActivityForResult(intent, requestCode);
    }

    public static void newInstance(Fragment fragment, String postData, int requestCode) {

        Intent intent = new Intent(fragment.getActivity(), HDFCPaymentGatewayActivity.class);
        intent.putExtra("url", BuildConfig.PAYUBIZ_HDFC_URL);
        intent.putExtra("postData", postData);
        // if task is generated from insta booking feature then addition payment field will not come in response
        fragment.startActivityForResult(intent, requestCode);
    }

   /* Bank bank = new Bank() {
        @Override
        public void registerBroadcast(BroadcastReceiver broadcastReceiver, IntentFilter filter) {
            mReceiver = broadcastReceiver;
            registerReceiver(broadcastReceiver, filter);
        }

        @Override
        public void unregisterBroadcast(BroadcastReceiver broadcastReceiver) {
            if(mReceiver != null){
                unregisterReceiver(mReceiver);
                mReceiver = null;
            }
        }

        @Override
        public void onHelpUnavailable() {
            findViewById(R.id.parent).setVisibility(View.GONE);
            findViewById(R.id.trans_overlay).setVisibility(View.GONE);
        }

        @Override
        public void onBankError() {
            findViewById(R.id.parent).setVisibility(View.GONE);
            findViewById(R.id.trans_overlay).setVisibility(View.GONE);
        }

        @Override
        public void onHelpAvailable() {
            findViewById(R.id.parent).setVisibility(View.VISIBLE);
        }
    };*/

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
          when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */

        if (savedInstanceState != null) {
            super.onCreate(null);
            finish();//call activity u want to as activity is being destroyed it is restarted
        } else {
            super.onCreate(savedInstanceState);
        }
        setContentView(R.layout.activity_payments);
        mWebView = (WebView) findViewById(R.id.webview);

        //WebView.setWebContentsDebuggingEnabled(true);

        //region Replace the whole code by the commented code if you are NOT using custombrowser
        // Replace the whole code by the commented code if you are NOT using custombrowser.

        bundle = getIntent().getExtras();
        mWebView = (WebView) findViewById(R.id.webview);


        //        byte[] encodedData = EncodingUtils.getBytes(payuConfig.getData(), "base64");
//        mWebView.postUrl(url, encodedData);


//        mWebView.getSettings().setSupportMultipleWindows(true);
//        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.getSettings().setDomStorageEnabled(true);
//        mWebView.setWebChromeClient(new WebChromeClient() {});
//        mWebView.setWebViewClient(new WebViewClient() {});

//        mWebView.setWebChromeClient(new WebChromeClient() );
//        mWebView.setWebViewClient(new WebViewClient());
//        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.getSettings().setDomStorageEnabled(true);
//        mWebView.postUrl(url, payuConfig.getData().getBytes());
//        mWebView.addJavascriptInterface(new Object() {
//            @JavascriptInterface
//            public void onSuccess() {
//                onSuccess("");
//            }
//
//            @JavascriptInterface
//            public void onSuccess(final String result) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent();
//                        intent.putExtra("result", result);
//                        setResult(RESULT_OK, intent);
//                        finish();
//                    }
////                }
//                });
//            }
//
//            @JavascriptInterface
//            public void onFailure() {
//                onFailure("");
//            }
//
//            @JavascriptInterface
//            public void onFailure(final String result) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent();
//                        intent.putExtra("result", result);
//                        setResult(RESULT_CANCELED, intent);
//                        finish();
//                    }
//                });
//            }
//
//
//            @JavascriptInterface
//            public void onMerchantHashReceived
// (final String result) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            JSONObject cvvObject = new JSONObject(result);
//                            // store the cvv in shared preferences.
//                            mPayuUtils.storeInSharedPreferences(HDFCPaymentGatewayActivity.this, cvvObject.getString(PayuConstants.CARD_TOKEN), cvvObject.getString(PayuConstants.MERCHANT_HASH));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//
//        }, "PayU");
        //endregion

//        bundle = getIntent().getExtras();
//        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
//        url = payuConfig.getEnvironment() == PayuConstants.PRODUCTION_ENV?  PayuConstants.PRODUCTION_PAYMENT_URL : PayuConstants.MOBILE_TEST_PAYMENT_URL ;

        // mWebView = (WebView) findViewById(R.id.webview);
        // mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }*/

        url = getIntent().getExtras().getString("url");
        postData = getIntent().getExtras().getString("postData");

        String[] list = postData.split("&");

        String merchantKey = null;
        for (String item : list) {
            String[] items = item.split("=");
            if (items.length >= 2) {
                String id = items[0];
                switch (id) {
                    case "txnid":
                        txnId = items[1];
                        break;
                    case "key":
                        merchantKey = items[1];
                        break;
                    case "pg":
                        if (items[1].contentEquals("NB")) {
                            viewPortWide = true;
                        }
                        break;

                }
            }
        }

        try {

            Class.forName("com.payu.custombrowser.Bank");
            final MyBank bank = new MyBank();
            /*final Bank bank = new Bank() {
                @Override
                public void registerBroadcast(BroadcastReceiver broadcastReceiver, IntentFilter filter) {
                    mReceiver = broadcastReceiver;
                    registerReceiver(broadcastReceiver, filter);
                }

                @Override
                public void unregisterBroadcast(BroadcastReceiver broadcastReceiver) {
                    if(mReceiver != null){
                        unregisterReceiver(mReceiver);
                        mReceiver = null;
                    }
                }

                @Override
                public void onHelpUnavailable() {
                    findViewById(R.id.parent).setVisibility(View.GONE);
                    findViewById(R.id.trans_overlay).setVisibility(View.GONE);
                }

                @Override
                public void onBankError() {
                    findViewById(R.id.parent).setVisibility(View.GONE);
                    findViewById(R.id.trans_overlay).setVisibility(View.GONE);
                }

                @Override
                public void onHelpAvailable() {
                    findViewById(R.id.parent).setVisibility(View.VISIBLE);
                }
            };*/
            Bundle args = new Bundle();
            args.putInt(Bank.WEBVIEW, R.id.webview);
            args.putInt(Bank.TRANS_LAYOUT, R.id.trans_overlay);
            args.putInt(Bank.MAIN_LAYOUT, R.id.r_layout);
            args.putBoolean(Bank.VIEWPORTWIDE, viewPortWide);
            args.putBoolean(Bank.AUTO_SELECT_OTP, true);
            args.putBoolean(Bank.AUTO_SELECT_OTP, false);
            args.putBoolean(Bank.AUTO_APPROVE, true);
            args.putString(Bank.TXN_ID, txnId == null ? String.valueOf(System.currentTimeMillis()) : txnId);
            args.putString(Bank.MERCHANT_KEY, null != merchantKey ? merchantKey : "could not find");

            if (getIntent().getExtras().containsKey("showCustom")) {
                args.putBoolean(Bank.SHOW_CUSTOMROWSER, getIntent().getBooleanExtra("showCustom", false));
            }
            args.putBoolean(Bank.SHOW_CUSTOMROWSER, true);

            //for android M
            //args.putBoolean(Bank.MERCHANT_SMS_PERMISSION, true);

            bank.setArguments(args);
            findViewById(R.id.parent).bringToFront();
            try {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.cb_fade_in, R.anim.cb_face_out).add(R.id.parent, bank).commit();
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
            initMagicRetry();


            mWebView.setWebChromeClient(new PayUWebChromeClient(bank));
//            TODO: for 6.1.1, need to enable below
            mWebView.setWebViewClient(new PayUWebViewClient(bank, magicRetryFragment, merchantKey));

            //            TODO: for 5.3.*, need to enable below
//            mWebView.setWebViewClient(new PayUWebViewClient(bank, magicRetryFragment));
            //mWebView is the WebView Object
            magicRetryFragment.setWebView(mWebView);
            // MR Integration - initMRSettingsFromSharedPreference
            magicRetryFragment.initMRSettingsFromSharedPreference(this);

            mWebView.postUrl(url, postData.getBytes());

        } catch (ClassNotFoundException e) {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            mWebView.getSettings().setSupportMultipleWindows(true);
            mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            // Setting view port for NB
            if (viewPortWide) {
                mWebView.getSettings().setUseWideViewPort(viewPortWide);
            }
            // Hiding the overlay
            View transOverlay = findViewById(R.id.trans_overlay);
            transOverlay.setVisibility(View.GONE);

            mWebView.addJavascriptInterface(new Object() {

                @JavascriptInterface
                public void onSuccess() {
                    onSuccess("");
                }

                @JavascriptInterface
                public void onSuccess(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Intent intent = new Intent();
                            intent.putExtra("result", result);
                            setResult(Activity.RESULT_OK, intent);
                            finish();

                        }
//                }
                    });
                }

                @JavascriptInterface
                public void onFailure() {
                    onFailure("");
                }

                @JavascriptInterface
                public void onFailure(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.putExtra("result", result);
                            setResult(RESULT_CANCELED, intent);
                            finish();
                        }
                    });
                }

            }, "PayU");

            mWebView.setWebChromeClient(new WebChromeClient());
            mWebView.setWebViewClient(new WebViewClient());
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setDomStorageEnabled(true);

            mWebView.postUrl(url, postData.getBytes());

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_payments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (cancelTransaction) {
            cancelTransaction = false;
            Intent intent = new Intent();
            intent.putExtra("result", "Transaction canceled due to back pressed!");
            setResult(RESULT_CANCELED, intent);
            super.onBackPressed();
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        ;
        alertDialog.setCancelable(false);
        alertDialog.setMessage(getString(R.string.confirm_cancel_transaction));
        alertDialog.setPositiveButton(getString(R.string.label_Ok_small), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelTransaction = true;
                dialog.dismiss();
                onBackPressed();
            }
        });
        alertDialog.setNegativeButton(getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initMagicRetry() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        magicRetryFragment = new MagicRetryFragment();
        Bundle newInformationBundle = new Bundle();
        newInformationBundle.putString(MagicRetryFragment.KEY_TXNID, txnId);
        magicRetryFragment.setArguments(newInformationBundle);

        Map<String, String> urlList = new HashMap<>();
        urlList.put(url, postData);
        magicRetryFragment.setUrlListWithPostData(urlList);

        fragmentManager.beginTransaction().add(R.id.magic_retry_container, magicRetryFragment, "magicRetry").commit();
        // magicRetryFragment = (MagicRetryFragment) fragmentManager.findFragmentBy(R.id.magicretry_fragment);

        toggleFragmentVisibility(Util.HIDE_FRAGMENT);

        magicRetryFragment.isWhiteListingEnabled(true);
    }


    public void toggleFragmentVisibility(int flag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (!isFinishing()) {
            if (flag == Util.SHOW_FRAGMENT) {
                // Show fragment
                ft.show(magicRetryFragment).commitAllowingStateLoss();
            } else if (flag == Util.HIDE_FRAGMENT) {
                // Hide fragment
                ft.hide(magicRetryFragment).commitAllowingStateLoss();
                // ft.hide(magicRetryFragment);
                Log.v("#### PAYU", "hiding magic retry");
            }
        }
    }

    @Override
    public void showMagicRetry() {
        toggleFragmentVisibility(Util.SHOW_FRAGMENT);
    }

    @Override
    public void hideMagicRetry() {
        toggleFragmentVisibility(Util.HIDE_FRAGMENT);
    }


    public static class MyBank extends Bank {

        @Override
        public void registerBroadcast(BroadcastReceiver broadcastReceiver, IntentFilter filter) {
            mReceiver = broadcastReceiver;
            getActivity().registerReceiver(broadcastReceiver, filter);
        }

        @Override
        public void unregisterBroadcast(BroadcastReceiver broadcastReceiver) {
            if (mReceiver != null) {
                getActivity().unregisterReceiver(mReceiver);
                mReceiver = null;
            }
        }

        @Override
        public void onHelpUnavailable() {
            getActivity().findViewById(R.id.parent).setVisibility(View.GONE);
            getActivity().findViewById(R.id.trans_overlay).setVisibility(View.GONE);
        }

        @Override
        public void onBankError() {
            getActivity().findViewById(R.id.parent).setVisibility(View.GONE);
            getActivity().findViewById(R.id.trans_overlay).setVisibility(View.GONE);
        }

        @Override
        public void onHelpAvailable() {
            getActivity().findViewById(R.id.parent).setVisibility(View.VISIBLE);
        }
    }
}
