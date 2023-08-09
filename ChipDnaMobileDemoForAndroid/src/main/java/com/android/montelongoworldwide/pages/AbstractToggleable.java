package com.android.montelongoworldwide.pages;

import android.view.View;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public abstract class AbstractToggleable {
    protected View layout;

    public void setVisibility(boolean visible) {
        this.layout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void onRefresh(SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setRefreshing(false);
    }

    public void onRefreshStateChange(boolean isRefreshing) {
    }
}
