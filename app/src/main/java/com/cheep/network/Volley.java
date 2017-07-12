package com.cheep.network;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;


public class Volley {

    /*private static final String TAG = "Volley";

    //    private static Volley mInstance;
    private RequestQueue mRequestQueue;
    private Context mCtx;

    private Volley(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized Volley getInstance(Context context) {
        return new Volley(context);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            Cache cache = new DiskBasedCache(mCtx.getCacheDir(), 10 * 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());

            //Changed over here, changed default pool size!
            //This worked out for making it work fast in older devices also!
            //Still not working (fastly in previous devices)
            mRequestQueue = new RequestQueue(cache, network, 10);

            // Don't forget to start the volley request queue
            mRequestQueue.start();
            //mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    //Original
    *//*public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }*//*

    //Changed by pankaj for added url as tag
    public <T> void addToRequestQueue(Request<T> req) {
        String tag = req.getUrl();
        addToRequestQueue(req, tag);
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is emptysd
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }*/

    private static final String TAG = "Volley";
    private static Volley mInstance;
    private RequestQueue mRequestQueue;

    private Volley(Context context) {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            Cache cache = new DiskBasedCache(context.getCacheDir(), 10 * 1024 * 1024); //10MB
            Network network = new BasicNetwork(new HurlStack());

            //Changed over here, changed default pool size!
            //This worked out for making it work fast in older devices also!
            //Still not working (fastly in previous devices)
            mRequestQueue = new RequestQueue(cache, network, 10);

            // Don't forget to start the volley request queue
            mRequestQueue.start();
            //mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
    }

    public static synchronized Volley getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Volley(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        mRequestQueue.add(req);
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        mRequestQueue.add(req);
    }

}
