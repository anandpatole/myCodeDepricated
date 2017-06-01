package com.cheep.activity;

public interface Act_ImpMethods {

    //test2
    public static final int MESSAGE_TYPE_SUCCESS = 1;
    public static final int MESSAGE_TYPE_WARNING = 2;
    public static final int MESSAGE_TYPE_ERROR = 3;

    public void initVariable();

    public void initView();

    public void postInitView();

    public void addAdapter();

    public void loadData();
}
