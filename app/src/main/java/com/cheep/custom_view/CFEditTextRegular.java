
package com.cheep.custom_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.widget.AppCompatEditText;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.cheep.R;


/**
 * Custom Edittext for regular font
 */
public class CFEditTextRegular extends AppCompatEditText {

    private Context mContext;

    public CFEditTextRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(attrs, defStyle);
    }

    public CFEditTextRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs, 0);
    }

    public CFEditTextRegular(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        setTypeface(TypeFaceProvider.get(mContext, getResources().getString(R.string.font_normal)));
    }

    public void setText(String text) {
        super.setText(text);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && letterSpacing >= 0) {
            applyLetterSpacing(letterSpacing);
        }

    }

    float letterSpacing;

    private void init(AttributeSet attrs, int defStyleAttr) {
        init();
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomViews, defStyleAttr, 0);
        letterSpacing = a.getFloat(R.styleable.CustomViews_letterSpacing, -1);
        a.recycle();
    }

    private void applyLetterSpacing(float letterSpacing) {

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

    /**
     * Below method needs to be override in order to fix the bug mentioned below.
     * <p>
     * https://stackoverflow.com/questions/15317157/android-adjustpan-not-working-after-the-first-time/26017060#26017060
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            clearFocus();
        }
        return super.onKeyPreIme(keyCode, event);
    }
}

