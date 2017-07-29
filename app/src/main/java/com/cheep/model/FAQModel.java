package com.cheep.model;

import android.support.annotation.Keep;

/**
 * Created by pankaj on 10/7/16.
 */
@Keep
public class FAQModel {
    public String faq_title;
    public String faq_content;

    public FAQModel(String faq_title, String faq_content) {
        this.faq_title = faq_title;
        this.faq_content = faq_content;
    }
}
