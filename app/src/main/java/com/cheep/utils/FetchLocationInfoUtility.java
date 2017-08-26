package com.cheep.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.cheep.model.GuestDetails;
import com.cheep.model.LocationInfo;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by bhavesh on 25/8/17.
 */
public class FetchLocationInfoUtility {
    private static final String TAG = "FetchLocationInfoUtilit";
    /**
     * https://maps.googleapis.com/maps/api/geocode/json?latlng='.$location_row['user_lat'].','.$location_row['user_lng'].'&sensor=false&key=AIzaSyDtxYbkO21G_uHSXNXuZayLskeEjFQ6HvY
     */
    private Context mContext;
    private FetchLocationInfoCallBack mListener;

    public FetchLocationInfoUtility(Context mContext, FetchLocationInfoCallBack mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
    }

    public static FetchLocationInfoUtility getInstance(Context mContext, FetchLocationInfoCallBack mListener) {
        FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(mContext, mListener);
        return mFetchLocationInfoUtility;
    }


    private String generateGoogleLocationURL(String lat, String lon) {
        return "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lon + "&sensor=false&key=AIzaSyDtxYbkO21G_uHSXNXuZayLskeEjFQ6HvY";
    }

    @SuppressWarnings("unchecked")
    public void getLocationInfo(final String lat, final String lon) {

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(
                generateGoogleLocationURL(lat, lon),
                null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object o) {
//                        Log.d(TAG, "onResponse() called with: o = [" + o.toString() + "]");
                        LocationInfo mLocationInfo = new LocationInfo();
                        mLocationInfo.lat = lat;
                        mLocationInfo.lon = lon;
                        try {
                            JSONObject jRoot = new JSONObject(o.toString());
                            JSONArray jArrayResults = jRoot.getJSONArray("results");
                            for (int i = 0; i < jArrayResults.length(); i++) {
                                JSONArray jArrayAddressComponents = jArrayResults.getJSONObject(i).getJSONArray("address_components");
                                for (int j = 0; j < jArrayAddressComponents.length(); j++) {
                                    JSONArray jArrayTypes = jArrayAddressComponents.getJSONObject(j).getJSONArray("types");
                                    for (int k = 0; k < jArrayTypes.length(); k++) {
                                        //City
                                        if (jArrayTypes.get(k).toString().equals("locality")) {
                                            mLocationInfo.City = jArrayAddressComponents.getJSONObject(j).getString("long_name");
                                        }

                                        // State
                                        if (jArrayTypes.get(k).toString().equals("administrative_area_level_1")) {
                                            mLocationInfo.State = jArrayAddressComponents.getJSONObject(j).getString("long_name");
                                        }

                                        //Country
                                        if (jArrayTypes.get(k).toString().equals("country")) {
                                            mLocationInfo.Country = jArrayAddressComponents.getJSONObject(j).getString("long_name");
                                        }

                                    }
                                }
                            }
                            //Save the relavent information to Pref
                            GuestDetails mGuestDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
                            mGuestDetails.mLat = mLocationInfo.lat;
                            mGuestDetails.mLon = mLocationInfo.lon;
                            mGuestDetails.mCityName = mLocationInfo.City;
                            mGuestDetails.mCountryName = mLocationInfo.Country;
                            mGuestDetails.mStateName = mLocationInfo.State;
                            PreferenceUtility.getInstance(mContext).saveGuestUserDetails(mGuestDetails);

                            // Send the callback to called activity
                            mListener.onLocationInfoAvailable(mLocationInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                null,
                null,
                null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }


    public interface FetchLocationInfoCallBack {
        void onLocationInfoAvailable(LocationInfo mLocationIno);
    }

}
