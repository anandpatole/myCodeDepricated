package com.cheep.custom_view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cheep.R;

/**
 * Created by pankaj on 8/17/16.
 */
public class BottomAlertDialog {
    BottomAlertDialog bottomAlertDialog;
    View view;
    BottomSheetDialog dialog;

//    test

    public BottomAlertDialog(Context context) {
//        view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_bottom_sheet, null);
        view = View.inflate(context, R.layout.alert_dialog_bottom_sheet, null);
        dialog = new BottomSheetDialog(context);
    }

    public void setExpandedInitially(boolean isExpanded) {
        if (isExpanded) {
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {

                    // In a previous life I used this method to get handles to the positive and negative buttons
                    // of a dialog in order to change their Typeface. Good ol' days.

                    BottomSheetDialog d = (BottomSheetDialog) dialog;

                    // This is gotten directly from the source of BottomSheetDialog
                    // in the wrapInBottomSheet() method
                    FrameLayout bottomSheet = (FrameLayout) d.findViewById(android.support.design.R.id.design_bottom_sheet);

                    // Right here!
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });
        } else {
            dialog.setOnShowListener(null);
        }
    }

    public void setCancelableOnTouchOutside(boolean isCancellable) {
        dialog.setCanceledOnTouchOutside(isCancellable);
    }

    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            view.findViewById(R.id.txt_title).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.txt_title)).setText(title);
        } else {
            view.findViewById(R.id.txt_title).setVisibility(View.GONE);
        }
    }

    public void setMessage(String msg) {
        view.findViewById(R.id.alert_container).setVisibility(View.VISIBLE);
        view.findViewById(R.id.view_container).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.txt_message)).setText(msg);
    }

    public void setCustomView(View inflatedView) {
        view.findViewById(R.id.alert_container).setVisibility(View.GONE);
        view.findViewById(R.id.view_container).setVisibility(View.VISIBLE);
        ((ViewGroup) view.findViewById(R.id.view_container)).removeAllViews();
        ((ViewGroup) view.findViewById(R.id.view_container)).addView(inflatedView);
    }

    public void addPositiveButton(String name, View.OnClickListener onClickListener) {
        ((Button) view.findViewById(R.id.btn_positive)).setText(name);
        view.findViewById(R.id.btn_positive).setOnClickListener(onClickListener);
    }

    public void addNegativeButton(String name, View.OnClickListener onClickListener) {
        ((Button) view.findViewById(R.id.btn_negative)).setText(name);
        view.findViewById(R.id.btn_negative).setOnClickListener(onClickListener);
    }


    public void showDialog() {
        if (view.getParent() != null)
            ((ViewGroup) view.getParent()).removeAllViews();
        dialog.setContentView(view);
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    //Customized method to hide button
    public void hideNegativeButton(boolean isHide) {
        view.findViewById(R.id.btn_negative).setVisibility(isHide ? View.GONE : View.VISIBLE);
    }
}
