package com.cheep.strategicpartner;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class QueAnsModel {

    @SerializedName("data")
    @Expose
    public List<DataModel> data = null;

    public class DataModel {

        @SerializedName("question_id")
        @Expose
        public String questionId;
        @SerializedName("answer_type")
        @Expose
        public String answerType;
        @SerializedName("question")
        @Expose
        public String question;
        @SerializedName("choices")
        @Expose
        public String choices;

        public String answer;

        public ArrayList<String> medialList = null;

    }
}