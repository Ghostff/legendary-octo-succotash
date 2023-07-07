package com.android.montelongoworldwide.pages;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

public class PaymentCollect implements Toggleable {
    protected final TextView paymentAmountView;
    protected final ConstraintLayout paymentSwipeContainer;
    protected final ConstraintLayout paymentKeyInContainer;
    protected final TabLayout paymentTabNavLayout;
    protected final View layout;
    protected final Button submitPaymentButton;

    public PaymentCollect(PackageSelectionActivity mainActivity) {
        ViewGroup parentLayout = mainActivity.findViewById(R.id.mainPageContainer);
        this.layout = mainActivity.getLayout(R.layout.pages_payment_collection, parentLayout, false);
        // Add layout1 to the parent layout
        parentLayout.addView(this.layout);


        this.paymentTabNavLayout = this.layout.findViewById(R.id.paymentTabNavLayout);
        this.submitPaymentButton = this.layout.findViewById(R.id.submitPaymentButton);
        this.paymentAmountView = this.layout.findViewById(R.id.paymentAmountView);
        this.paymentSwipeContainer = this.layout.findViewById(R.id.paymentSwipeContainer);
        this.paymentKeyInContainer = this.layout.findViewById(R.id.paymentKeyInContainer);

        ImageView gifImageView = this.paymentSwipeContainer.findViewById(R.id.gifImageView);
        Glide.with(mainActivity)
                .asGif()
                .load(R.raw.waiting_for_swipe)  // Replace `your_gif_file` with the actual name of your GIF file
                .into(gifImageView);

        // hide key-in
        this.paymentKeyInContainer.setVisibility(View.GONE);

        this.paymentTabNavLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Perform action when a tab is selected
                int position = tab.getPosition();
                // Handle the selected tab position
                switch (position) {
                    case 0:
                        paymentKeyInContainer.setVisibility(View.GONE);
                        paymentSwipeContainer.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        paymentSwipeContainer.setVisibility(View.GONE);
                        paymentKeyInContainer.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Perform action when a tab is unselected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Perform action when a tab is reselected
            }
        });

        this.submitPaymentButton.setOnClickListener(v -> {
            // Handle the cardContainer click event here
            mainActivity.setLastTransactionId("12232");
        });
    }

    @Override
    public void setVisibility(boolean visible) {
        this.layout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setVisibility(boolean visible, Package.Model selectedPackage) {
        this.paymentAmountView.setText(selectedPackage.getHumanPrice());
        this.setVisibility(visible);
    }
}
