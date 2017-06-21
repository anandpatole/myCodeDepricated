package com.cheep.model;

public class MessageEvent {
    public int BROADCAST_ACTION;
    public String id;
    public String isFav;
    public String commentCount;
    public String taskStartdate;
    public String taskStatus;

    // When TASK_PAID && TASK_PROCESSING as Broadcast type
    public String taskPaidAmount;

    // Used for QUOTE_REQUEST Notification Type
    public String max_quote_price;
    public String sp_counts;

    // used for REQUEST_FOR_DETAIL Notification Type
    public String request_detail_status;

    // Used for additional payment requested
    public String additional_quote_amount;

    // Used for Making Alert Enable/Disable
    public String total_ongoing_task;

}