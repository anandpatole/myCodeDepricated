package com.cheep;

import com.cheep.model.ProviderModel;
import com.cheep.utils.Utility;

/**
 * Created by pankaj on 9/27/16.
 */

public class BootstrapConstant {
    //    TODO: Change the lat long
    public static final String LAT = "23.454545";
    public static final String LNG = "72.454545";

    /*
    Expiry Date : 05/22
    CVV : 245
    */
    public static final String CC_DETAILS = "5123456789012346";


   /* public static final ArrayList<TaskChatModel> DUMMY_TASK_CHAT_LIST;
    public static final ArrayList<TaskChatModel> DUMMY_TASK_CHAT_LIST_INDIVIDUAL;
    public static final ArrayList<TaskChatMessageModel> DUMMY_TASK_CHAT_MESSAGE_LIST;*/

    static {

       /* DUMMY_TASK_CHAT_LIST = new ArrayList<>(Arrays.asList(
                new TaskChatModel("Carpenter", "This is Carpentor task discription, Lorem Ipsum dollar sit amet.", 8, 1475216508000L, null, null, true, R.drawable.dummy_person1)

                , new TaskChatModel("Plumber", "This is Plumber task discription, Lorem Ipsum dollar sit amet.", 3, 1475216117000L, "Lokesh Shah", null, true, R.drawable.dummy_person1)

                , new TaskChatModel("Washing Machine", "This is WashingMachine repaire task discription, Lorem Ipsum dollar sit amet.", 6, 1475204117000L, null, null, true, R.drawable.dummy_person1)

                , new TaskChatModel("AC Repair", "This is AC Repaire task discription, Lorem Ipsum dollar sit amet.", 3, 1472525717000L, null, null, false, R.drawable.dummy_person1)

                , new TaskChatModel("Dust Cleaner", "This is Dust cleaner task discription, Lorem Ipsum dollar sit amet.", 6, 1471316357000L, "Chirag Modi", null, false, R.drawable.dummy_person1)

                , new TaskChatModel("Pest Cleaner", "This is PestCleaner task discription, Lorem Ipsum dollar sit amet.", 7, 1471339817000L, "Bhavesh Patel", null, true, R.drawable.dummy_person1)
        ));

        DUMMY_TASK_CHAT_LIST_INDIVIDUAL = new ArrayList<>(Arrays.asList(
                new TaskChatModel("Carpenter", "This is Carpentor task discription, Lorem Ipsum dollar sit amet.", 8, 1475216508000L, "Rakesh Patel", null, true, R.drawable.dummy_person1)

                , new TaskChatModel("Plumber", "This is Plumber task discription, Lorem Ipsum dollar sit amet.", 3, 1475216117000L, "Lokesh Shah", null, true, R.drawable.dummy_person1)

                , new TaskChatModel("Washing Machine", "This is WashingMachine repaire task discription, Lorem Ipsum dollar sit amet.", 6, 1475204117000L, "Praveen Mehta", null, true, R.drawable.dummy_person1)

                , new TaskChatModel("AC Repair", "This is AC Repaire task discription, Lorem Ipsum dollar sit amet.", 3, 1472525717000L, "Suresh Prabhu", null, false, R.drawable.dummy_person1)

                , new TaskChatModel("Dust Cleaner", "This is Dust cleaner task discription, Lorem Ipsum dollar sit amet.", 6, 1471316357000L, "Chirag Modi", null, false, R.drawable.dummy_person1)

                , new TaskChatModel("Pest Cleaner", "This is PestCleaner task discription, Lorem Ipsum dollar sit amet.", 7, 1471339817000L, "Bhavesh Patel", null, true, R.drawable.dummy_person1)
        ));*/

        ProviderModel providerModel = new ProviderModel();
        providerModel.jobsCount = "31";
        providerModel.userName = "Lokesh Shah";
        providerModel.distance = "15 min";
        providerModel.isVerified = Utility.BOOLEAN.YES;
        providerModel.profileUrl = "http://52.76.3.59/cheepdemo/uploads/profile_img/cropped1904979902.jpg";
        providerModel.quotePrice = "1500";
//        providerModel.requestType = Utility.REQUEST_TYPE.QUOTE_REQUESTED;


     /*   DUMMY_TASK_CHAT_MESSAGE_LIST = new ArrayList<>(Arrays.asList(
                new TaskChatMessageModel("200", "100", "Can you please send me address?", "", Utility.CHAT_TYPE_MESSAGE, 0, 1477028898000L)
                , new TaskChatMessageModel("100", "200", "Ok,I will sent you.", "", Utility.CHAT_TYPE_MESSAGE, 0, 1477028898000L)
                , new TaskChatMessageModel("200", "100", "", "", Utility.CHAT_TYPE_MEDIA, 0, 1477028898000L)
                , new TaskChatMessageModel("200", "100", "", "", Utility.CHAT_TYPE_MONEY, 100, 1477028898000L)
                , new TaskChatMessageModel("100", "200", "Ok,that sound perfect", "", Utility.CHAT_TYPE_MESSAGE, 0, 1477028898000L)
        ));*/

    }
}
