package com.android.montelongoworldwide.pages;

import android.view.View;

public abstract class AbstractToggleable {
    protected View layout;

    public void setVisibility(boolean visible) {
        this.layout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
