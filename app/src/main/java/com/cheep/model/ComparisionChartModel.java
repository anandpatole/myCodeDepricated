package com.cheep.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by majid on 06-06-2018.
 */

public class ComparisionChartModel implements Parcelable {

    public String feature;
    public String premium;
    public String normal;

    public ComparisionChartModel(){

    }

    public ComparisionChartModel(JSONObject jsonObject) throws JSONException {
        feature = jsonObject.getString("feature");
        premium = jsonObject.getString("premium");
        normal = jsonObject.getString("normal");


    }

    protected ComparisionChartModel(Parcel in) {
        feature = in.readString();
        premium = in.readString();
        normal = in.readString();
    }

    public static final Creator<ComparisionChartModel> CREATOR = new Creator<ComparisionChartModel>() {
        @Override
        public ComparisionChartModel createFromParcel(Parcel in) {
            return new ComparisionChartModel(in);
        }

        @Override
        public ComparisionChartModel[] newArray(int size) {
            return new ComparisionChartModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(feature);
        parcel.writeString(premium);
        parcel.writeString(normal);
    }
}
