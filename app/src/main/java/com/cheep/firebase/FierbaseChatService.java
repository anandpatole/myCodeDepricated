package com.cheep.firebase;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.cheep.model.UserDetails;
import com.cheep.utils.PreferenceUtility;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by pankaj on 5/17/16.
 */
public class FierbaseChatService extends Service
{
    private static final String TAG = FierbaseChatService.class.getSimpleName();

    private UserDetails mUserDetails;

    /**
     * Setting binders
     */
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder
    {
        FierbaseChatService getService()
        {
            /**
             * Return this instance of LocalService so clients can call public methods
             */
            return FierbaseChatService.this;
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mUserDetails = PreferenceUtility.getInstance(FierbaseChatService.this).getUserDetails();
        if(mUserDetails==null)
        {
            stopSelf();
        }
        Log.e(TAG,"OnCreate");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        Log.e(TAG,"onBind");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        Log.e(TAG,"onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG,"onDestroy");
        removeRecentChatListener();
        super.onDestroy();
    }

    public void addRecentChatListener()
    {
        /*final DatabaseReference databaseReference=FirebaseHelper.getRecentChatsRef(mUserDetail.iUserID);
        final RealmResults<RecentChat> recentChatList=realm.where(RecentChat.class).findAllSorted(FirebaseHelper.KEY_TIMESTAMP, Sort.DESCENDING);

        if(recentChatList==null|| recentChatList.size()==0)
        {
            Query query=databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP);
                  query.addChildEventListener(chatChildEventListener);
        }
        else
        {
            Query query=databaseReference.orderByChild(FirebaseHelper.KEY_TIMESTAMP).startAt(recentChatList.first().getTimestampLong()+1);
            query.addChildEventListener(chatChildEventListener);
        }*/
    }

    /**
     * Used to remove recent chat listener
     */
    public void removeRecentChatListener()
    {
        if(mUserDetails!=null && !TextUtils.isEmpty(mUserDetails.UserID))
        {
            DatabaseReference databaseReference = FirebaseHelper.getRecentChatRef(mUserDetails.UserID);
            databaseReference.removeEventListener(chatChildEventListener);
        }
    }

    ChildEventListener chatChildEventListener = new ChildEventListener()
    {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s)
        {
            Log.e(TAG,"onChildAdded");
            if (dataSnapshot.getValue() != null)
            {

            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s)
        {
            Log.e(TAG, "onChildChanged");
            if (dataSnapshot.exists())
            {

            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot)
        {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s)
        {

        }

        @Override
        public void onCancelled(DatabaseError databaseError)
        {

        }
    };
}
