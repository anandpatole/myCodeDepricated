package com.cheep.cheepcare.model;

import com.cheep.R;
import com.cheep.cheepcare.adapter.PackageBundlingAdapter;
import com.cheep.custom_view.expandablerecycleview.Parent;
import com.cheep.model.AttachmentModel;
import com.cheep.model.JobCategoryModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 12/21/17.
 */

public class CheepCarePackageModel implements Parent<JobCategoryModel>, Serializable {

    public String packageImage;
    public String packageTitle;
    public String packageDescription;
    public String price;
    public String subscribedDescription;
    public String daysLeft;
    public boolean isSelected = false;

    public List<String> live_lable_arr;

    public List<JobCategoryModel> subItems = new ArrayList<>();
    public int rowType = PackageBundlingAdapter.ROW_PACKAGE_NOT_SELECTED;

    @Override
    public List<JobCategoryModel> getChildList() {
        return subItems;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return true;
    }

    public static List<CheepCarePackageModel> getCheepCarePackages() {
        final List<JobCategoryModel> itemsHome = new ArrayList<>();
        itemsHome.add(new JobCategoryModel() {{
            catId = "17";
            catName = "Plumber";
            catDesc = "Plumber";
            catSlug = "plumber";
            catIcon = "http://35.154.147.32/admin/uploads/category_img/logo.png";
            isFavourite = "no";
            proImagesPerCategory = new ArrayList<>();
            proImagesPerCategory
                    .add("https://s3.ap-south-1.amazonaws.com/cheepapp/service_provider/profile/thumb/1503741437_59a145fd203f3_image.jpg");
            live_lable_arr = new ArrayList<>();
            live_lable_arr
                    .add("91 users are viewing Plumbers right now");
            live_lable_arr
                    .add("Over 60 Plumbers booked through CHEEP this month");
            catImage = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/thumb/Untitled.jpg";
            catImageExtras = new AttachmentModel() {{
                thumb = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/thumb/Untitled.jpg";
                medium = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/medium/Untitled.jpg";
                original = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/original/Untitled.jpg";
            }};
            catJobs = "0";
            spCount = "0";
        }});

        itemsHome.add(new JobCategoryModel() {{
            catId = "1";
            catName = "Carpenter";
            catDesc = "Carpenter";
            catSlug = "carpenter";
            catIcon = "http://35.154.147.32/admin/uploads/category_img/logo.png";
            isFavourite = "no";
            proImagesPerCategory = new ArrayList<>();
            proImagesPerCategory
                    .add("https://s3.ap-south-1.amazonaws.com/cheepapp/service_provider/profile/thumb/1503741437_59a145fd203f3_image.jpg");
            live_lable_arr = new ArrayList<>();
            live_lable_arr
                    .add("77 users are viewing Carpenters right now");
            live_lable_arr
                    .add("Over 44 Carpenters booked through CHEEP this month");
            catImage = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/thumb/_F1A065431.jpg";
            catImageExtras = new AttachmentModel() {{
                thumb = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/thumb/_F1A065431.jpg";
                medium = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/medium/_F1A065431.jpg";
                original = "https=//s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/original/_F1A065431.jpg";
            }};
            catJobs = "0";
            spCount = "0";
        }});

        List<CheepCarePackageModel> packageModels = new ArrayList<>();
        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.banner_appliance_care);
            packageTitle = "Home Care";
            packageDescription = "Yearly plumbing, electricity & carpentry support for your home";
            isSelected = true;
            price = "20000";
            subscribedDescription = "Plumbing + Electrician + Carpentry on tap";
            daysLeft = "226";
            this.subItems = itemsHome;
        }});

        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Appliance Care";
            packageDescription = "Appliance care throughout the year";
            price = "150";
            subscribedDescription = "Keep all your appliances in tip-top shape";
            daysLeft = "22";
        }});

        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Tech Care";
            packageDescription = "Repairs for all yours workstation gadgets all year long";
            price = "5";
        }});

        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Biz Care";
            packageDescription = "Plumbing, electricity, & carpentry for your business for a year";
            price = "500";
            daysLeft = "26";
            this.subItems = itemsHome;
        }});
        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Soci Care";
            packageDescription = "Year long plumbing, electricity, & carpentry services for your society";
            price = "800";
            isSelected = true;
            daysLeft = "226";
        }});
        return packageModels;
    }

    public static List<CheepCarePackageModel> getCheepCareBoughtPackages() {
        final List<JobCategoryModel> itemsHome = new ArrayList<>();

        itemsHome.add(new JobCategoryModel() {{
            catId = "17";
            catName = "Plumber";
            catDesc = "Plumber";
            catSlug = "plumber";
            catIcon = "http://35.154.147.32/admin/uploads/category_img/logo.png";
            isFavourite = "no";
            proImagesPerCategory = new ArrayList<>();
            proImagesPerCategory
                    .add("https://s3.ap-south-1.amazonaws.com/cheepapp/service_provider/profile/thumb/1503741437_59a145fd203f3_image.jpg");
            live_lable_arr = new ArrayList<>();
            live_lable_arr
                    .add("91 users are viewing Plumbers right now");
            live_lable_arr
                    .add("Over 60 Plumbers booked through CHEEP this month");
            catImage = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/thumb/Untitled.jpg";
            catImageExtras = new AttachmentModel() {{
                thumb = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/thumb/Untitled.jpg";
                medium = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/medium/Untitled.jpg";
                original = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/original/Untitled.jpg";
            }};
            catJobs = "0";
            spCount = "0";
        }});

        itemsHome.add(new JobCategoryModel() {{
            catId = "1";
            catName = "Carpenter";
            catDesc = "Carpenter";
            catSlug = "carpenter";
            catIcon = "http://35.154.147.32/admin/uploads/category_img/logo.png";
            isFavourite = "no";
            proImagesPerCategory = new ArrayList<>();
            proImagesPerCategory
                    .add("https://s3.ap-south-1.amazonaws.com/cheepapp/service_provider/profile/thumb/1503741437_59a145fd203f3_image.jpg");
            live_lable_arr = new ArrayList<>();
            live_lable_arr
                    .add("77 users are viewing Carpenters right now");
            live_lable_arr
                    .add("Over 44 Carpenters booked through CHEEP this month");
            catImage = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/thumb/_F1A065431.jpg";
            catImageExtras = new AttachmentModel() {{
                thumb = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/thumb/_F1A065431.jpg";
                medium = "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/medium/_F1A065431.jpg";
                original = "https=//s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/original/_F1A065431.jpg";
            }};
            catJobs = "0";
            spCount = "0";
        }});

        List<CheepCarePackageModel> packageModels = new ArrayList<>();
        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.banner_appliance_care);
            packageTitle = "Home Care";
            packageDescription = "Yearly plumbing, electricity & carpentry support for your home";
            isSelected = true;
            price = "20000";
            subscribedDescription = "Plumbing + Electrician + Carpentry on tap";
            daysLeft = "6";
            this.subItems = itemsHome;
        }});

        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Appliance Care";
            packageDescription = "Appliance care throughout the year";
            price = "150";
            subscribedDescription = "Keep all your appliances in tip-top shape";
            daysLeft = "225";
        }});
        return packageModels;
    }

    public static List<CheepCarePackageModel> getManageSubscriptionPackageListing() {
        final List<JobCategoryModel> itemsHome = new ArrayList<>();

        List<CheepCarePackageModel> packageModels = new ArrayList<>();
        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.banner_appliance_care);
            packageTitle = "Home Care";
            packageDescription = "Yearly plumbing, electricity & carpentry support for your home";
            price = "200";
        }});

        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Appliance Care";
            price = "250";
            packageDescription = "Keep all your appliances in tip-top shape";
        }});

        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Tech Care";
            packageDescription = "Maintain all your funky gadgets like never before";
            price = "250";
        }});

        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Biz Care";
            packageDescription = "Plumbing, electricity, & carpentry for your business for a year";
            price = "1667";
        }});
        packageModels.add(new CheepCarePackageModel() {{
            packageImage = String.valueOf(R.drawable.ic_home_with_heart_text);
            packageTitle = "Soci Care";
            packageDescription = "Year long plumbing, electricity, & carpentry services for your society";
            price = "417";
        }});

        return packageModels;
    }
}