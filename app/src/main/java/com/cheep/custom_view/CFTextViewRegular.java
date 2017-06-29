
package com.cheep.custom_view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ScaleXSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.cheep.R;


/**
 * Custom Edittext for regular font
 */
public class CFTextViewRegular extends AppCompatTextView {
    private static final String TAG = "CFTextViewRegular";
    private Context mContext;

    private static final String ELLIPSIZE = "... ";
    private static final String MORE = "Read More";
    private static final String LESS = "less";
    private String mFullText;
    private int mMaxLines;

    public CFTextViewRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(attrs, defStyle);
    }

    public CFTextViewRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs, 0);
    }

    public CFTextViewRegular(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        setTypeface(TypeFaceProvider.get(mContext, getResources().getString(R.string.font_normal)));
    }

    public void setText(String text) {
        super.setText(text);

        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && letterSpacing >= 0) {
            applyLetterSpacing(letterSpacing);
        }*/

    }

    float letterSpacing;

    private void init(AttributeSet attrs, int defStyleAttr) {
        init();
        /*TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomViews, defStyleAttr, 0);
        letterSpacing = a.getFloat(R.styleable.CustomViews_letterSpacing, -1);*/
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

    public void makeExpandable(int maxLines) {
        makeExpandable(getText().toString(), maxLines);
    }

    public void makeExpandable(String fullText, final int maxLines) {
        mFullText = fullText;
        mMaxLines = maxLines;
        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = getViewTreeObserver();
                obs.removeOnGlobalLayoutListener(this);
                if (getLineCount() <= maxLines) {
                    setText(mFullText);
                } else {
                    setMovementMethod(LinkMovementMethod.getInstance());
                    showLess();
                }
            }
        });
    }

    /**
     * truncate text and append a clickable {@link #MORE}
     */
    private void showLess() {
        Log.i(TAG, "showLess: ");
        int lineEndIndex = getLayout().getLineEnd(mMaxLines - 1);
        String newText = mFullText.substring(0, lineEndIndex - (ELLIPSIZE.length() + MORE.length() + 1)) + ELLIPSIZE + MORE;
        SpannableStringBuilder builder = new SpannableStringBuilder(newText);
       /* builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                showMore();
            }
        }, newText.length() - MORE.length(), newText.length(), 0);*/
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.splash_gradient_end)), newText.length() - MORE.length(), newText.length(), 0);
        builder.setSpan(new StyleSpan(Typeface.BOLD), newText.length() - MORE.length(), newText.length(), 0);
        setText(builder, BufferType.SPANNABLE);
    }

    /**
     * show full text and append a clickable {@link #LESS}
     */
    private void showMore() {
        SpannableStringBuilder builder = new SpannableStringBuilder(mFullText + LESS);
        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                showLess();
            }
        }, builder.length() - LESS.length(), builder.length(), 0);
        builder.setSpan(new ForegroundColorSpan(this.getCurrentTextColor()), builder.length() - LESS.length(), builder.length(), 0);
        builder.setSpan(new StyleSpan(Typeface.BOLD), builder.length() - LESS.length(), builder.length(), 0);
        setText(builder, BufferType.SPANNABLE);
    }

}

