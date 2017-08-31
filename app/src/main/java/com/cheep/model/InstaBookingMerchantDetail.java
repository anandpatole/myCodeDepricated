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
public class InstaBookingMerchantDetail implements Parcelable{

    @SerializedName("sp_id")
    @Expose
    private String spId;
    @SerializedName("avg_ratings")
    @Expose
    private String avgRatings;
    @SerializedName("user_name")
    @Expose
    private String userName;
    @SerializedName("profile_img")
    @Expose
    private String profileImg;
    @SerializedName("experience")
    @Expose
    private String experience;
    @SerializedName("rate")
    @Expose
    private String rate;
    @SerializedName("verified")
    @Expose
    private String verified;
    @SerializedName("pro_level")
    @Expose
    private String proLevel;


    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public String getSubCatName() {
        return subCatName;
    }

    public void setSubCatName(String subCatName) {
        this.subCatName = subCatName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getUserTiming() {
        return userTiming;
    }

    public void setUserTiming(String userTiming) {
        this.userTiming = userTiming;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    String catName = "";
    String subCatName = "";
    String taskName = "";
    String userTiming = "";
    String userAddress = "";

    protected InstaBookingMerchantDetail(Parcel in) {
        spId = in.readString();
        avgRatings = in.readString();
        userName = in.readString();
        profileImg = in.readString();
        experience = in.readString();
        rate = in.readString();
        verified = in.readString();
        proLevel = in.readString();
    }

    public static final Creator<InstaBookingMerchantDetail> CREATOR = new Creator<InstaBookingMerchantDetail>() {
        @Override
        public InstaBookingMerchantDetail createFromParcel(Parcel in) {
            return new InstaBookingMerchantDetail(in);
        }

        @Override
        public InstaBookingMerchantDetail[] newArray(int size) {
            return new InstaBookingMerchantDetail[size];
        }
    };

    public String getSpId() {
        return spId;
    }

    public void setSpId(String spId) {
        this.spId = spId;
    }

    public String getAvgRatings() {
        return avgRatings;
    }

    public void setAvgRatings(String avgRatings) {
        this.avgRatings = avgRatings;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getProLevel() {
        return proLevel;
    }

    public void setProLevel(String proLevel) {
        this.proLevel = proLevel;
    }

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

    public InstaBookingMerchantDetail(){

    }
}
