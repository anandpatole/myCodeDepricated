package com.cheep.model;

import com.cheep.utils.Utility;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meet on 27/3/18.
 */

public class RateAndReviewModel {



    @SerializedName("last_id")
    @Expose
    public String lastId = Utility.EMPTY_STRING;

    @SerializedName("review_data")
    @Expose
    public List<ReviewData> reviewData = new ArrayList<>();
    @SerializedName("review_summary")
    @Expose
    public ReviewSummaryData reviewSummary = new ReviewSummaryData();


    public class ReviewData {

        @SerializedName("sp_user_id")
        @Expose
        public String spUserId;
        @SerializedName("sp_user_name")
        @Expose
        public String spUserName;
        @SerializedName("sp_profile_image")
        @Expose
        public String spProfileImage;
        @SerializedName("is_verified")
        @Expose
        public String isVerified;
        @SerializedName("sp_favourite")
        @Expose
        public String spFavourite;
        @SerializedName("ratings")
        @Expose
        public String ratings;
        @SerializedName("message")
        @Expose
        public String message;
        @SerializedName("task_category")
        @Expose
        public String taskCategory;
        @SerializedName("task_date")
        @Expose
        public String taskDate;

    }

    public class ReviewSummaryData {

        @SerializedName("avg_ratings")
        @Expose
        public String avgRatings;
        @SerializedName("rating_count")
        @Expose
        public String ratingCount;
        @SerializedName("review_comment_count")
        @Expose
        public String reviewCommentCount;
        @SerializedName("star_1")
        @Expose
        public String star1;
        @SerializedName("star_2")
        @Expose
        public String star2;
        @SerializedName("star_3")
        @Expose
        public String star3;
        @SerializedName("star_4")
        @Expose
        public String star4;
        @SerializedName("star_5")
        @Expose
        public String star5;


        public String getAvgRatings() {
            return avgRatings;
        }

        public void setAvgRatings(String avgRatings) {
            this.avgRatings = avgRatings;
        }

        public String getRatingCount() {
            return ratingCount;
        }

        public void setRatingCount(String ratingCount) {
            this.ratingCount = ratingCount;
        }

        public String getReviewCommentCount() {
            return reviewCommentCount;
        }

        public void setReviewCommentCount(String reviewCommentCount) {
            this.reviewCommentCount = reviewCommentCount;
        }

        public String getStar1() {
            return star1;
        }

        public void setStar1(String star1) {
            this.star1 = star1;
        }

        public String getStar2() {
            return star2;
        }

        public void setStar2(String star2) {
            this.star2 = star2;
        }

        public String getStar3() {
            return star3;
        }

        public void setStar3(String star3) {
            this.star3 = star3;
        }

        public String getStar4() {
            return star4;
        }

        public void setStar4(String star4) {
            this.star4 = star4;
        }

        public String getStar5() {
            return star5;
        }

        public void setStar5(String star5) {
            this.star5 = star5;
        }
    }
}
