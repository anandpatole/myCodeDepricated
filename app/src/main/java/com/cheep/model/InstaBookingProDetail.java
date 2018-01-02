package com.cheep.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by mihir on 29/8/17.
 */

@Keep
public class InstaBookingProDetail implements Parcelable {

    @SerializedName("sp_user_id")
    @Expose
    public String spId;
    @SerializedName("avg_ratings")
    @Expose
    public String avgRatings;
    @SerializedName("user_name")
    @Expose
    public String userName;
    @SerializedName("profile_img")
    @Expose
    public String profileImg;
    @SerializedName("experience")
    @Expose
    public String experience;
    @SerializedName("rate")
    @Expose
    public String rate;
    @SerializedName("verified")
    @Expose
    public String verified;

    @SerializedName("sp_ratings_count")
    public String rating;

    @SerializedName("pro_level")
    @Expose
    public String proLevel;

    @SerializedName("gstrate")
    @Expose
    public String rateGST;


    String catName = "";
    String subCatName = "";
    String taskName = "";
    String userTiming = "";
    String userAddress = "";

    protected InstaBookingProDetail(Parcel in) {
        spId = in.readString();
        avgRatings = in.readString();
        userName = in.readString();
        profileImg = in.readString();
        experience = in.readString();
        rate = in.readString();
        verified = in.readString();
        proLevel = in.readString();
    }

    public static final Creator<InstaBookingProDetail> CREATOR = new Creator<InstaBookingProDetail>() {
        @Override
        public InstaBookingProDetail createFromParcel(Parcel in) {
            return new InstaBookingProDetail(in);
        }

        @Override
        public InstaBookingProDetail[] newArray(int size) {
            return new InstaBookingProDetail[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(spId);
        parcel.writeString(avgRatings);
        parcel.writeString(userName);
        parcel.writeString(profileImg);
        parcel.writeString(experience);
        parcel.writeString(rate);
        parcel.writeString(verified);
        parcel.writeString(proLevel);
    }

    public InstaBookingProDetail() {

    }
}
