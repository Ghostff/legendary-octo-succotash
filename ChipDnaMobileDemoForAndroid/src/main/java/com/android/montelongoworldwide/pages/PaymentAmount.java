package com.android.montelongoworldwide.pages;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;

import java.util.ArrayList;
import java.util.List;

public class PaymentAmount implements Toggleable {

    protected final TextView selectedPackageView;
    private final Button continueButton;
    protected TextView amount;
    protected List<String> numbers = new ArrayList<>();

    protected View layout;


    public PaymentAmount(PackageSelectionActivity mainActivity)
    {
        ViewGroup parentLayout = mainActivity.findViewById(R.id.mainPageContainer);
        this.layout = mainActivity.getLayout(R.layout.pages_payment_amount, parentLayout, false);

        // Add layout1 to the parent layout
        parentLayout.addView(this.layout);
        this.amount = this.layout.findViewById(R.id.amount);
        keypadListener(this.layout.findViewById(R.id.keypad_1));
        keypadListener(this.layout.findViewById(R.id.keypad_2));
        keypadListener(this.layout.findViewById(R.id.keypad_3));
        keypadListener(this.layout.findViewById(R.id.keypad_4));
        keypadListener(this.layout.findViewById(R.id.keypad_5));
        keypadListener(this.layout.findViewById(R.id.keypad_6));
        keypadListener(this.layout.findViewById(R.id.keypad_7));
        keypadListener(this.layout.findViewById(R.id.keypad_8));
        keypadListener(this.layout.findViewById(R.id.keypad_9));
        keypadListener(this.layout.findViewById(R.id.keypad_0));
        keypadListener(this.layout.findViewById(R.id.keypad_backspace));

        this.selectedPackageView = this.layout.findViewById(R.id.selectedPackageView);
        this.continueButton = this.layout.findViewById(R.id.continueButton);

        this.continueButton.setOnClickListener(v -> {
            // Handle the cardContainer click event here
            mainActivity.setAmount(getAmount());
        });
    }

    @SuppressLint("DefaultLocale")
    protected void updateAmount()
    {
        amount.setText(String.format("$%,.2f", this.getAmount() / 100.0));
    }

    protected int getAmount()
    {
        if (this.numbers.size() == 0) {
            return 0;
        }

        return Integer.parseInt(String.join("", this.numbers));
    }

    public void setVisibility(boolean visible, Market.Model market, User.Model user, Package.Model pkg)
    {
        this.layout.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (market != null && user != null && pkg != null) {
            this.numbers.clear();
            this.numbers.add(String.valueOf(pkg.price));
            this.updateAmount();
            this.selectedPackageView.setText(market + "\n" + user.name + "\n" + pkg.title);
        }
    }

    public void clearAmount()
    {
        this.numbers.clear();
        this.updateAmount();
    }

    public void setVisibility(boolean visible)
    {
        this.setVisibility(visible, null, null, null);
    }

    public void keypadListener(Button button)
    {
        button.setOnClickListener(v -> {
            String number = ((Button) v).getText().toString();
            if (number.equals("⌫")) {
                int lastIndex = this.numbers.size() - 1;
                if (lastIndex >= 0) {
                    this.numbers.remove(lastIndex);
                }
            } else {
                this.numbers.add(number);
            }
            this.updateAmount();
        });
    }
}