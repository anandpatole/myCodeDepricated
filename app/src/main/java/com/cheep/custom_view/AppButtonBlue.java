package com.cheep.custom_view;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.cheep.R;


/**
 * Created by pankaj on 30/3/16.
 */
public class AppButtonBlue extends AppCompatButton {
    public Context mContext;

    public AppButtonBlue(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public AppButtonBlue(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.appButtonBlueStyle);
        mContext = context;
        init();
    }

    public AppButtonBlue(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.appButtonBlueStyle);
        mContext = context;
        init();
    }

    private void init() {
        setTypeface(TypeFaceProvider.get(mContext, getResources().getString(R.string.font_normal)));
    }
}
