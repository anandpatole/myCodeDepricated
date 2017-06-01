package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import com.cheep.R;
import com.cheep.databinding.ActivityFaqDescBinding;
import com.cheep.model.FAQModel;
import com.cheep.utils.Utility;

/**
 * Created by pankaj on 9/7/16.
 */
public class FAQDescActivity extends BaseAppCompatActivity {

    ActivityFaqDescBinding mActivityFaqDescBinding;

    public static void newInstance(Context context, FAQModel faqModel) {
        Intent intent = new Intent(context, FAQDescActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(faqModel));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityFaqDescBinding = DataBindingUtil.setContentView(this, R.layout.activity_faq_desc);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        //Setting Toolbar
        setSupportActionBar(mActivityFaqDescBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivityFaqDescBinding.toolbar.setNavigationIcon(R.drawable.ic_arrow_white);
            mActivityFaqDescBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }

        mActivityFaqDescBinding.textTitle.setText(getString(R.string.label_faq));

        FAQModel faqModel = (FAQModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), FAQModel.class);
        mActivityFaqDescBinding.textFaqTitle.setText(faqModel.faq_title);
        mActivityFaqDescBinding.webview.loadDataWithBaseURL(null, faqModel.faq_content, "text/html", "utf-8", null);
       /* if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mActivityFaqDescBinding.textFaqDesc.setText(Html.fromHtml(faqModel.faq_content, Html.FROM_HTML_MODE_LEGACY));
        } else {
            mActivityFaqDescBinding.textFaqDesc.setText(Html.fromHtml(faqModel.faq_content));
        }*/

    }

    @Override
    protected void setListeners() {

    }
}
