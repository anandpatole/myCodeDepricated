package com.cheep.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pankaj on 10/7/16.
 */

@Keep
public class ReviewModel {

    @SerializedName("review_id")
    public String reviewId;

    @SerializedName("task_id")
    public String taskId;

    @SerializedName("reviewer_user_id")
    public String reviewerUserId;

    @SerializedName("reviewer_name")
    public String reviewerName;

    @SerializedName("reviewer_profile_image")
    public String reviewerProfileImage;

    @SerializedName("reviewer_ratings")
    public String reviewerRatings;

    @SerializedName("reviewer_message")
    public String reviewerMessage;

    @SerializedName("review_date")
    public String reviewDate;

    @SerializedName("review_comment_count")
    public String commentCount;

    public ReviewModel(String reviewId, String reviewerUserId, String reviewerName, String reviewerProfileImage, String reviewerRatings, String reviewerMessage, String reviewDate) {
        this.reviewId = reviewId;
        this.reviewerUserId = reviewerUserId;
        this.reviewerName = reviewerName;
        this.reviewerProfileImage = reviewerProfileImage;
        this.reviewerRatings = reviewerRatings;
        this.reviewerMessage = reviewerMessage;
        this.reviewDate = reviewDate;
    }
}













