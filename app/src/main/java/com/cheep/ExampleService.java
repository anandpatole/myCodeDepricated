package com.cheep;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.cheep.utils.Utility;

import java.lang.ref.WeakReference;

/**
 * Created by pankaj on 9/24/16.
 */

public class ExampleService extends Service {
    private DialogInterface.OnClickListener callback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ABC(this);
    }

    public void setCallback(DialogInterface.OnClickListener callback) {
        this.callback = callback;
    }

    public class ABC extends Binder {

        WeakReference<ExampleService> weekRefrence;

        ABC(ExampleService service) {

            weekRefrence = new WeakReference<ExampleService>(service);

        }

        public ExampleService getService() {
            return weekRefrence.get();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            callback.onClick(null, 5);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Message message = new Message();
        message.obj = Utility.EMPTY_STRING;

        handler.sendMessage(message);


        return START_STICKY;
    }

    public void startCounter() {

    }
}
