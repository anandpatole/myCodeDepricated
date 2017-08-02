package com.cheep.model;

import android.support.annotation.Keep;

@Keep
public class SlideMenuListModel {
    public String title;
    public int image_id;
    public boolean isSelected = false;
    public boolean separatorEnabled = false;

    public SlideMenuListModel(String title, int image_id, boolean isSelected, boolean separatorEnabled) {
        this.title = title;
        this.image_id = image_id;
        this.isSelected = isSelected;
        this.separatorEnabled = separatorEnabled;
    }
}