package com.cheep.cheepcare.model;

import com.cheep.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 12/21/17.
 */

public class CheepCareFeatureModel implements Serializable {
    public String featureImage;
    public String featureTitle;
    public String featureDescription;

    public static List<CheepCareFeatureModel> getCheepCareFeatures(){
        List<CheepCareFeatureModel> featureModels = new ArrayList<>();
        featureModels.add(new CheepCareFeatureModel(){{
            featureImage = String.valueOf(R.drawable.ic_rupee_stroked_in_glass);
            featureTitle = "No hidden Charges";
            featureDescription = "Just pay once, and get all the goodness of CHEEP anytime you need it for no extra or hidden costs";
        }});

        featureModels.add(new CheepCareFeatureModel(){{
            featureImage = String.valueOf(R.drawable.ic_rupee_stroked_in_glass);
            featureTitle = "Verified, Insured & Experienced PRO's";
            featureDescription = "All jobs are done by Platinum/Gold category(min 7-10 yrs experience) PRO's only. Verified by TOPS " +
                    "and insured for your complete peace of mind!";
        }});

        featureModels.add(new CheepCareFeatureModel(){{
            featureImage = String.valueOf(R.drawable.ic_rupee_stroked_in_glass);
            featureTitle = "Guaranteed service within 24 hrs";
            featureDescription = "Hassle free repairs and maintenance services right at your doorstep within 24 hours or we will refund " +
                    "your money for that month";
        }});

        featureModels.add(new CheepCareFeatureModel(){{
            featureImage = String.valueOf(R.drawable.ic_rupee_stroked_in_glass);
            featureTitle = "24x7 Emergency Response Cover";
            featureDescription = "If you need any Emergency help during our PRO's visit to your office/home, simply press the " +
                    "alert button on the CHEEP App, and our TOPSLINE 1252 crew (and the police if needed), will rush to" +
                    " your rescue in minutes";
        }});
        return featureModels;
    }
}
