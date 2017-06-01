package com.cheep.adapter;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.cheep.R;
import com.cheep.model.GooglePlaceModel;
import com.cheep.utils.GoogleMapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class PlacesAutoCompleteAdapter extends RecyclerView.Adapter<PlacesAutoCompleteAdapter.ViewHolder> {
    private ArrayList<GooglePlaceModel> resultList;
    private OnPlaceClickListener onPlaceClickListener;

    /*public PlacesAutoCompleteAdapter() {

    }*/

    public PlacesAutoCompleteAdapter(OnPlaceClickListener onPlaceClickListener) {
        this.onPlaceClickListener = onPlaceClickListener;
    }

    PlacesFilter placesFilter;

    public PlacesFilter getFilter() {
        if (placesFilter == null)
            placesFilter = new PlacesFilter();
        return placesFilter;
    }

    AsyncTask<String, Void, Object> asyncTask;

    public class PlacesFilter extends Filter {
        @Override
        public FilterResults performFiltering(final CharSequence constraint) {

            if (constraint != null) {
                if (asyncTask != null && asyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                    asyncTask.cancel(true);
                }
                asyncTask = new AsyncTask<String, Void, Object>() {
                    @Override
                    protected Object doInBackground(String... voids) {
                        // Retrieve the autocomplete results.
                        return autocomplete(voids[0]);
                    }

                    @Override
                    protected void onPostExecute(Object resultList) {
                        super.onPostExecute(resultList);
                        FilterResults filterResults = new FilterResults();
                        filterResults.values = resultList;
                        publishResults(constraint, filterResults);
                    }
                }.execute(constraint.toString());
            }
            return null;
        }

        @Override
        public void publishResults(CharSequence constraint, FilterResults results) {

            resultList = (ArrayList<GooglePlaceModel>) results.values;
            notifyDataSetChanged();
        }
    }


    // INTERNET REQUEST FOR PLACES
    private static final String LOG_TAG = "PlacesAutoCompleteAdapter";

    private ArrayList<GooglePlaceModel> autocomplete(String input) {
        ArrayList<GooglePlaceModel> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(GoogleMapUtils.PLACES_API_BASE + GoogleMapUtils.TYPE_AUTOCOMPLETE + GoogleMapUtils.OUT_JSON);
            sb.append("?key=" + GoogleMapUtils.API_KEY);

            //Setting this default value, we can remove this
//            String countryCode = "in";
            String countryCode = "in"; //in

            if (!TextUtils.isEmpty(countryCode))
                sb.append("&components=country:").append(countryCode);
//            sb.append("&types=(cities)");
            sb.append("&input=").append(URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("PlaceAutoComplete", "Error processing Places API URL" + e.toString());
            return resultList;
        } catch (IOException e) {
            Log.e("PlaceAutoComplete", "Error connecting to Places API" + e.toString());
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<GooglePlaceModel>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {

                GooglePlaceModel place = new GooglePlaceModel();
                if (predsJsonArray.getJSONObject(i).getJSONObject("structured_formatting") != null) {
                    JSONObject jsonStructuredFormating = predsJsonArray.getJSONObject(i).getJSONObject("structured_formatting");
                    place.mainString = jsonStructuredFormating.getString("main_text");
                }
                place.description = predsJsonArray.getJSONObject(i).getString("description");
                place.placeid = predsJsonArray.getJSONObject(i).getString("place_id");
                place.reference = predsJsonArray.getJSONObject(i).getString("reference");

                resultList.add(place);
            }
        } catch (JSONException e) {
            Log.e("PlaceAutoComplete", "Cannot process JSON results", e);
        }

        return resultList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_place_search, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.textResult.setText(getItem(holder.getAdapterPosition()).description);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlaceClickListener.onPlaceClicked(getItem(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (resultList != null)
            return resultList.size();
        return 0;
    }

    public GooglePlaceModel getItem(int index) {
        if (resultList != null)
            return resultList.get(index);
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textResult;

        public ViewHolder(View itemView) {
            super(itemView);
            textResult = (TextView) itemView;
        }
    }

    public interface OnPlaceClickListener {
        void onPlaceClicked(GooglePlaceModel googlePlaceModel);
    }
}