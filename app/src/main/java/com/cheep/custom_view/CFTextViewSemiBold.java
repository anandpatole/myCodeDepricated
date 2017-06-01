package com.cheep.custom_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cheep.R;


/**
 * Custom Edittext for regular font
 */
public class CFTextViewSemiBold extends TextView {

    private Context mContext;

    public CFTextViewSemiBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(attrs, defStyle);
    }

    public CFTextViewSemiBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs, 0);
    }

    public CFTextViewSemiBold(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        setTypeface(TypeFaceProvider.get(mContext, getResources().getString(R.string.font_semi_bold)));
    }

    public void setText(String text) {
        super.setText(text);

     /*   if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && letterSpacing >= 0) {
            applyLetterSpacing(letterSpacing);
        }*/

    }

    float letterSpacing;

    private void init(AttributeSet attrs, int defStyleAttr) {
        init();
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomViews, defStyleAttr, 0);
        letterSpacing = a.getFloat(R.styleable.CustomViews_letterSpacing, 0);
    }

    private void applyLetterSpacing(float letterSpacing) {

        if (this.getText().toString() == null) return;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < getText().toString().length(); i++) {
            String c = "" + getText().toString().charAt(i);
            builder.append(c.toLowerCase());
            if (i + 1 < getText().toString().length()) {
                builder.append("\u00A0");
            }
        }
        SpannableString finalText = new SpannableString(builder.toString());
        if (builder.toString().length() > 1) {
            for (int i = 1; i < builder.toString().length(); i += 2) {
                finalText.setSpan(new ScaleXSpan((letterSpacing + 1) / 10), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        super.setText(finalText, BufferType.SPANNABLE);
    }

}
