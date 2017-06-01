package com.cheep.utils;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

public abstract class TypingWatcher implements TextWatcher {

    public static final int DELAY_TIME = 300;
    boolean isTypingStarts = false;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isTypingStarts) {
                isTypingStarts = false;
                onTypingStop();
            }
        }
    };

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isTypingStarts == true) {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, DELAY_TIME);
        } else {
            if (s.toString().length() > 0) {
                isTypingStarts = true;
                handler.postDelayed(runnable, DELAY_TIME);
                onTypingStart();
            }
        }
    }


    public void onTypingStart() {
    }


    public void onTypingStop() {
    }


    public void onTextChanged(CharSequence s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}