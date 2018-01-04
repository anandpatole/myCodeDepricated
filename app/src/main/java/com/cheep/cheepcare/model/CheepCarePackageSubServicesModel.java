package com.cheep.cheepcare.model;

import java.io.Serializable;

/**
 * Created by pankaj on 12/26/17.
 */

public class CheepCarePackageSubServicesModel implements Serializable {

    public String subSubCatName;

    public String price;

    public String basePrice;

    public String subSubCatId;

    public String package_description;

    public String subCategoryName;

    public boolean isSelected = false;

    public String type;

    public interface SERVICE_TYPE {
        String SIMPLE = "simple";
        String UNIT = "unit";
    }
}
