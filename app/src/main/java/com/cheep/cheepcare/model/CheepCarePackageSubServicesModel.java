package com.cheep.cheepcare.model;

import java.io.Serializable;

/**
 * Created by pankaj on 12/26/17.
 */

public class CheepCarePackageSubServicesModel implements Serializable {

    public String subSubCatName;

    public String price;

    public String basePrice;

    public int qty = 1;
    public int maxQty = 15;

    public String subSubCatId;

    public String package_description;


    public boolean isSelected = false;


}
