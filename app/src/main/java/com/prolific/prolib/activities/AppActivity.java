package com.prolific.prolib.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;

import com.prolific.prolib.app.R;

public class AppActivity extends Activity {
    private int mSwipeRefreshLayoutId;

    public void setSwipeRefresh(int layoutId, SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        mSwipeRefreshLayoutId = layoutId;
        final SwipeRefreshLayout layout = (SwipeRefreshLayout) findViewById(mSwipeRefreshLayoutId);
        if (layout != null) {
            layout.setColorScheme(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
            layout.setOnRefreshListener(onRefreshListener);
        }
    }

    public int getSwipeRefreshLayoutId() {
        return mSwipeRefreshLayoutId;
    }

    public void setRefreshing(boolean refreshing) {
        final SwipeRefreshLayout layout = (SwipeRefreshLayout) findViewById(mSwipeRefreshLayoutId);
        if (layout != null) {
            layout.setRefreshing(refreshing);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                overridePendingTransition(R.anim.activity_slide_in_back, R.anim.activity_slide_out_back);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public AppActivity getActivity() {
        return this;
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.activity_slide_in, R.anim.activity_slide_out);
    }
}
