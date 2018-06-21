package com.cheep.model.ComparisionChart;

import java.io.Serializable;

public class CheepCarePackageModel implements Serializable
{
    String month;
    String previousPrice;
    String newPrice;
    String savePrice;
    String bestValue;

    CheepCarePackageModel(String month,String previousPrice,String newPrice,String savePrice,String bestValue)
    {
        this.month=month;
        this.previousPrice=previousPrice;
        this.newPrice=newPrice;
        this.savePrice=savePrice;
        this.bestValue=bestValue;
    }


}
