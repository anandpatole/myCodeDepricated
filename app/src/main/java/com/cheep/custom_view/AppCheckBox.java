package com.cheep.custom_view;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.cheep.R;


/**
 * Created by pankaj on 30/3/16.
 */
public class AppCheckBox extends AppCompatCheckBox {
    public Context mContext;
    public AppCheckBox(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public AppCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.appCheckBoxStyle);
        mContext = context;
        init();
    }

    public AppCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.appCheckBoxStyle);
        mContext = context;
        init();
    }

    private void init() {
        setTypeface(TypeFaceProvider.get(mContext, getResources().getString(R.string.font_normal)));
    }
}
