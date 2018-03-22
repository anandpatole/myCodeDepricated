package com.cheep.cheepcare.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by giteeka on 21/3/18.
 */

public class RatingModel {
    @SerializedName("review_type")
    public String reviewType;
    @SerializedName("review_type_rating")
    public String reviewTypeRating;

}
