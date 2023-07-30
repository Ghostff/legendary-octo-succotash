package com.android.montelongoworldwide.pages;

import android.view.View;
import android.view.ViewGroup;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;

public class PaymentCompleted extends AbstractToggleable {

    private final View addAnotherPaymentBtn;
    private final PackageSelectionActivity mainActivity;

    public PaymentCompleted(PackageSelectionActivity mainActivity) {
        ViewGroup parentLayout = mainActivity.findViewById(R.id.mainPageContainer);
        this.layout = mainActivity.getLayout(R.layout.pages_payment_completed, parentLayout, false);
        this.mainActivity = mainActivity;

        this.addAnotherPaymentBtn = this.layout.findViewById(R.id.addAnotherPayment);
        this.addAnotherPaymentBtn.setOnClickListener(v -> mainActivity.addAnotherPayment());

        this.layout.findViewById(R.id.singPa).setOnClickListener(v -> {
            // Handle the cardContainer click event here
            mainActivity.signPurchaseAgreements();
        });;


        // Add layout1 to the parent layout
        parentLayout.addView(this.layout);
    }

    @Override
    public void setVisibility(boolean visible) {
        if (visible && this.mainActivity.getAmountDifference() <= 1) {
            this.addAnotherPaymentBtn.setVisibility(View.GONE);
        }

        super.setVisibility(visible);
    }
}
