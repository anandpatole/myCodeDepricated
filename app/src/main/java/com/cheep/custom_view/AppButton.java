package com.cheep.custom_view;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.cheep.R;


/**
 * Created by pankaj on 30/3/16.
 */
public class AppButton extends AppCompatButton {
    public Context mContext;

    public AppButton(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public AppButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public AppButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        setTypeface(TypeFaceProvider.get(mContext, getResources().getString(R.string.font_normal)));
    }
}
