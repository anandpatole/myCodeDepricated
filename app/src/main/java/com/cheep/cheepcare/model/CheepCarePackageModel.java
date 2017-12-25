package com.cheep.cheepcare.model;

import com.cheep.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 12/21/17.
 */

public class CheepCarePackageModel implements Serializable {
    public String packageImage;
    public String packageTitle;
    public String packageDescription;

    public static List<CheepCarePackageModel> getCheepCarePackages(){
        List<CheepCarePackageModel> packageModels = new ArrayList<>();
        packageModels.add(new CheepCarePackageModel(){{
            packageImage = String.valueOf(R.drawable.banner_appliance_care);
            packageTitle = "Home Care";
            packageDescription = "Yearly plumbing, electricity & carpentry support for your home";
        }});

        packageModels.add(new CheepCarePackageModel(){{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Appliance Care";
            packageDescription = "Appliance care throughout the year";
        }});

        packageModels.add(new CheepCarePackageModel(){{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Tech Care";
            packageDescription = "Repairs for all yours workstation gadgets all year long";
        }});

        packageModels.add(new CheepCarePackageModel(){{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Biz Care";
            packageDescription = "Plumbing, electricity, & carpentry for your business for a year";
        }});
        packageModels.add(new CheepCarePackageModel(){{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Soci Care";
            packageDescription = "Year long plumbing, electricity, & carpentry services for your society";
        }});
        return packageModels;
    }
}
