package com.cheep.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.model.GooglePlaceModel;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;

/**
 * Created by pankaj on 9/29/16.
 */

public class GoogleMapUtils {
    public static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    public static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    public static final String TYPE_DETAIL = "/details";
    public static final String OUT_JSON = "/json";
    public static final String API_KEY = BuildConfig.PLACES_API_KEY;

    public static void getLatLongForPlace(final Context mContext, final String placeid, final String referenceId, final OnGetLatLongCallback callback) {

        String url = PLACES_API_BASE + TYPE_DETAIL + OUT_JSON + "?";
        url += "key=" + BuildConfig.PLACES_API_KEY;

        if (placeid != null)
            url += "&placeid=" + placeid;
        else if (referenceId != null)
            url += "&reference=" + referenceId;

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(url
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onSuccess(null, null, new Exception("Cant get location"));

            }
        }
                , new Response.Listener() {
            @Override
            public void onResponse(Object jsonResults) {
                try {
                    // Create a JSON object hierarchy from the results
                    JSONObject jsonObj = new JSONObject(jsonResults.toString());
                    JSONObject resultsjsonObj = jsonObj.optJSONObject("result");
                    JSONObject geometryJsonObject = resultsjsonObj.optJSONObject("geometry");
                    JSONObject locationJsonObject = geometryJsonObject.optJSONObject("location");
                    String lat = locationJsonObject.optString("lat");
                    String lng = locationJsonObject.optString("lng");

                    callback.onSuccess(lat, lng, null);

                } catch (Exception e) {
                    Log.e("GoogleMapUtils", "Error connecting to Places API", e);

                }
            }
        }
                , null
                , null
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }


    public interface OnGetLatLongCallback {
        public void onSuccess(String lat, String lng, Exception exception);
    }

    public static AsyncTask<String, Void, GooglePlaceModel> getAddressFromLatLng(final Context context, final String lat, final String lng, final OnGetAddressCallback callback) {
        return getAddressFromLatLng(context, Double.parseDouble(lat), Double.parseDouble(lng), callback);
    }

    public static AsyncTask<String, Void, GooglePlaceModel> getAddressFromLatLng(final Context context, final double lat, final double lng, final OnGetAddressCallback callback) {

        AsyncTask<String, Void, GooglePlaceModel> task = new AsyncTask<String, Void, GooglePlaceModel>() {
            @Override
            protected GooglePlaceModel doInBackground(String... params) {


                try {
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(context, Locale.getDefault());

                    addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    GooglePlaceModel place = new GooglePlaceModel();
                    if (addresses.size() > 0) {
                        String address = "";

                        for (int i = 0; i <= addresses.get(0).getMaxAddressLineIndex(); i++) {
                            if (!TextUtils.isEmpty(addresses.get(0).getAddressLine(i)))
                                address += addresses.get(0).getAddressLine(i) + ", ";
                        }
                        address = address.trim();
                        address = address.replaceAll(",$", "");
//                        address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String countryCode = addresses.get(0).getCountryCode();

                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName();

                        place.mainString = city;
                        place.address = addresses.get(0);
                        place.description = address;
                        place.countryCode = countryCode;
                        place.country = country;
                        place.lat = String.valueOf(lat);
                        place.lng = String.valueOf(lng);
                        return place;
                    }
                } catch (MalformedURLException e) {
                    Log.e("GoogleMapUtils", "Error processing Places API URL", e);
                    return null;
                } catch (IOException e) {
                    Log.e("GoogleMapUtils", "Error connecting to Places API", e);
                    return null;
                } catch (Exception e) {
                    Log.e("GoogleMapUtils", "Error connecting to Places API", e);
                    return null;
                } finally {

                }
                return null;
            }

            protected void onPostExecute(GooglePlaceModel result) {
                if (result != null) {
                    callback.onSuccess(result, null);
                } else {
                    callback.onSuccess(null, new Exception("Cant get address"));
                }
            }
        };
        task.execute();
        return task;
    }

    public interface OnGetAddressCallback {
        public void onSuccess(GooglePlaceModel place, Exception exception);
    }
}
