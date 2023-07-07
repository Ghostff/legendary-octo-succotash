package com.android.montelongoworldwide.pages;

import android.view.View;
import android.view.ViewGroup;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;

public class PaymentCompleted implements Toggleable {

    protected final View layout;
    public PaymentCompleted(PackageSelectionActivity mainActivity) {
        ViewGroup parentLayout = mainActivity.findViewById(R.id.mainPageContainer);
        this.layout = mainActivity.getLayout(R.layout.pages_payment_completed, parentLayout, false);
        // Add layout1 to the parent layout
        parentLayout.addView(this.layout);
    }

    @Override
    public void setVisibility(boolean visible) {
        this.layout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
