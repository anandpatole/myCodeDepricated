package com.cheep.cheepcare.model;

import com.cheep.model.AddressModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.model.MediaModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;

public class SubscribedTaskDetailModel {

    public JobCategoryModel jobCategoryModel;
    public AddressModel addressModel;
    public String carePackageId;
    public ArrayList<SubServiceDetailModel> freeServiceList;
    public ArrayList<SubServiceDetailModel> paidServiceList;
    public String startDateTime;
    public String taskDesc;
    public String taskType;
    public AdminSettingModel adminSettingModel;
    public double taskExcessLimitFees = 0;
    public double paidServiceTotal = 0;
    public double freeServiceTotal = 0;
    public double subtotal = 0;
    public double total = 0;
    public double nonWorkingHourFees = 0;
    public String paymentMethod = Utility.EMPTY_STRING;
    public String paymentLog = Utility.EMPTY_STRING;

    public String paybleAmount;
    public ArrayList<MediaModel> mediaFileList;
}
