package com.cheep.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by giteeka on 11/6/18.
 */
public class AddressSizeModel {

    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("value")
    @Expose
    public String value;

    @SerializedName("normal")
    @Expose
    public PriceModel normalPriceModel;

    @SerializedName("premium")
    @Expose
    public PriceModel premiumPriceModel;

    public boolean isSelected = false;

    public String toStringNormalPriceModel() {
        return "normalPriceModel.gstFor3 " + normalPriceModel.gstFor3 + "\n"
                + "normalPriceModel.gstFor6 " + normalPriceModel.gstFor6 + "\n"
                + "normalPriceModel.gstFor12 " + normalPriceModel.gstFor12 + "\n"
                + "normalPriceModel.monthCostFor3 " + normalPriceModel.monthCostFor3 + "\n"
                + "normalPriceModel.monthCostFor6 " + normalPriceModel.monthCostFor6 + "\n"
                + "normalPriceModel.monthCostFor12 " + normalPriceModel.monthCostFor12 + "\n"
                + "-----------------------------------";
    }

    public String toStringPremiumPriceModel() {
        return "premiumPriceModel.gstFor3 " + premiumPriceModel.gstFor3 + "\n"
                + "premiumPriceModel.gstFor6 " + premiumPriceModel.gstFor6 + "\n"
                + "premiumPriceModel.gstFor12 " + premiumPriceModel.gstFor12 + "\n"
                + "premiumPriceModel.monthCostFor3 " + premiumPriceModel.monthCostFor3 + "\n"
                + "premiumPriceModel.monthCostFor6 " + premiumPriceModel.monthCostFor6 + "\n"
                + "premiumPriceModel.monthCostFor12 " + premiumPriceModel.monthCostFor12 + "\n"
                + "-----------------------------------";
    }

}

