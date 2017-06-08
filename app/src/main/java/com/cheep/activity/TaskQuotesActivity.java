package com.cheep.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.cheep.R;
import com.cheep.adapter.TaskQuotesRecyclerViewAdapter;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.custom_view.GridImageView;

import java.util.Arrays;

public class TaskQuotesActivity extends BaseAppCompatActivity {
    private Toolbar mToolbar;
    private TextView tvTitle;
    private GridImageView mGridImageView;
    private RecyclerView mRecyclerView;
    private Uri[] urls = new Uri[]{Uri.parse("http://www.animated-gifs.eu/category_leisure/avatars-100x100-music/0016.gif"), Uri.parse("http://www.smailikai.com/avatar/skelet/avatar_4348.gif"), Uri.parse("http://www.boorp.com/avatars_100x100_for_myspace/25.png"), Uri.parse("http://www.boorp.com/avatars_100x100_for_myspace/25.png"), Uri.parse("http://www.boorp.com/avatars_100x100_for_myspace/25.png")};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_quotes);

        initiateUI();
    }

    @Override
    protected void initiateUI() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.text_title);
        mGridImageView = (GridImageView) findViewById(R.id.gridImageView);
        mGridImageView.createWithUrls(Arrays.asList(urls));

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new TaskQuotesRecyclerViewAdapter(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal));
        setupActionbar();
    }

    @Override
    protected void setListeners() {

    }

    private void setupActionbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        tvTitle.setText("Plumber");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }
}
