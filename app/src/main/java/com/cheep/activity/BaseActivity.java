package com.cheep.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


/**
 * Created by aalap on 12/6/15.
 */
public abstract class BaseActivity extends AppCompatActivity implements Act_ImpMethods {

    public Context mActivity;

    @Override
    public void setContentView(int layoutResID) {
        try {
//            ExceptionHandler.register(this, "http://stylekart.net/2016/boomboomhunt/server.php");
            super.setContentView(layoutResID);

            mActivity = this;

            initVariable();
            initView();
            postInitView();
            addAdapter();
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            showToast(e.toString());
            Log.e("BaseActivity", this + " : " + e.toString());
        }
    }

    public View links(int resId) {
        return findViewById(resId);
    }


    public void setToolbar(int id) {
        setToolbar(id, true);
    }

    public void setToolbar(int id, boolean isBackEnabled) {
        Toolbar toolbar = (Toolbar) links(id);
        super.setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            if (isBackEnabled) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }
        }
    }

    public void setTitle(String title) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    public final void showToast(String msg) {
        if (!TextUtils.isEmpty(msg))
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public final void showSnackbar(String msg) {

//        Snackbar.make(findViewById(android.R.id.content), msg + "", Snackbar.LENGTH_SHORT).show();
        if (links(android.R.id.content) != null) {
            Snackbar snackbar = Snackbar.make(links(android.R.id.content), msg + "", Snackbar.LENGTH_LONG);
            snackbar.show();
        } else
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //test
    public final void showSnackbar(String msg, int type) {
        if (!TextUtils.isEmpty(msg)) {

//        Snackbar.make(findViewById(android.R.id.content), msg + "", Snackbar.LENGTH_SHORT).show();
            if (links(android.R.id.content) != null) {
                Snackbar snackbar = Snackbar.make(links(android.R.id.content), msg + "", Snackbar.LENGTH_LONG);
                if (type == MESSAGE_TYPE_SUCCESS) {
                    snackbar.getView().setBackgroundColor(Color.parseColor("#4F8A10"));
                } else if (type == MESSAGE_TYPE_WARNING) {
                    snackbar.getView().setBackgroundColor(Color.parseColor("#9F6000"));
                } else if (type == MESSAGE_TYPE_ERROR) {
                    snackbar.getView().setBackgroundColor(Color.parseColor("#D8000C"));
                }
                snackbar.show();
            } else
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public final void showSnackbar(int id, String msg, int type) {

        if (!TextUtils.isEmpty(msg)) {
//        Snackbar.make(findViewById(android.R.id.content), msg + "", Snackbar.LENGTH_SHORT).show();
            if (links(id) != null) {
                Snackbar snackbar = Snackbar.make(links(id), msg + "", Snackbar.LENGTH_LONG);
                if (type == MESSAGE_TYPE_SUCCESS) {
                    snackbar.getView().setBackgroundColor(Color.parseColor("#4F8A10"));
                } else if (type == MESSAGE_TYPE_WARNING) {
                    snackbar.getView().setBackgroundColor(Color.parseColor("#9F6000"));
                } else if (type == MESSAGE_TYPE_ERROR) {
                    snackbar.getView().setBackgroundColor(Color.parseColor("#D8000C"));
                }
                snackbar.show();
            } else
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    /*@Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        ArrayList<Class> list = new ArrayList<Class>(Arrays.asList(Utility.exludeAnimActivities));
        boolean isExludeThis = (new ArrayList<Class>(Arrays.asList(Utility.exludeAnimActivities))).contains(mActivity);
        boolean isExludeNew = false;
        try {
            isExludeNew = list.contains(Class.forName(intent.getComponent().getClassName()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        mActivity.overridePendingTransition(isExludeNew ? 0 : R.anim.slide_in_right, isExludeThis ? 0 : R.anim.slide_in_left);

    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        ArrayList<Class> list = new ArrayList<Class>(Arrays.asList(Utility.exludeAnimActivities));
        boolean isExludeThis = (new ArrayList<Class>(Arrays.asList(Utility.exludeAnimActivities))).contains(mActivity);
        boolean isExludeNew = false;
        try {
            isExludeNew = list.contains(Class.forName(intent.getComponent().getClassName()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        mActivity.overridePendingTransition(isExludeNew ? 0 : R.anim.slide_in_right, isExludeThis ? 0 : R.anim.slide_in_left);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        ArrayList<Class> list = new ArrayList<Class>(Arrays.asList(Utility.exludeAnimActivities));
        boolean isExludeThis = (new ArrayList<Class>(Arrays.asList(Utility.exludeAnimActivities))).contains(mActivity);
        boolean isExludeNew = false;
        try {
            isExludeNew = list.contains(Class.forName(intent.getComponent().getClassName()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        mActivity.overridePendingTransition(isExludeNew ? 0 : R.anim.slide_in_right, isExludeThis ? 0 : R.anim.slide_in_left);
    }

    @Override
    public void finish() {
        super.finish();

        ArrayList<Class> list = new ArrayList<Class>(Arrays.asList(Utility.exludeAnimActivities));
        boolean isExludeThis = !(new ArrayList<Class>(Arrays.asList(Utility.exludeAnimActivities))).contains(mActivity);

        mActivity.overridePendingTransition(isExludeThis ? 0 : R.anim.slide_in_right, isExludeThis ? 0 : R.anim.slide_in_left);

    }*/
}
