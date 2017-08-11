package com.cheep.strategicpartner;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Giteeka 31/7/2017
 * Question and Answer Json class model
 */
class QueAnsModel {


    @SerializedName("question_id")
    @Expose
    public String questionId;
    @SerializedName("answer_type")
    @Expose
    public String answerType;
    @SerializedName("question")
    @Expose
    public String question;
    @SerializedName("dropdown_answer")
    @Expose
    public ArrayList<DropDownModel> dropDownList;

    public String answer;

    public ArrayList<String> medialList = null;


    public class DropDownModel {

        public boolean isSelected = false;
        @SerializedName("dropdown_id")
        @Expose
        public String dropdown_id;
        @SerializedName("dropdown_answer")
        @Expose
        public String dropdown_answer;
    }
}