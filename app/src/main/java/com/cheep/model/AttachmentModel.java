package com.cheep.model;

import android.support.annotation.Keep;

import java.io.Serializable;

/**
 * Created by pankaj on 12/1/16.
 */
@Keep
public class AttachmentModel implements Serializable {
    public String thumb;
    public String medium;
    public String original;
}
