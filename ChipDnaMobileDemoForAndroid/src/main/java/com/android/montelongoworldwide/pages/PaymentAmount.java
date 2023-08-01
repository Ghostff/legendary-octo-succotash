package com.android.montelongoworldwide.pages;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.android.montelongoworldwide.Utils;

import java.util.ArrayList;
import java.util.List;

public class PaymentAmount extends AbstractToggleable {

    protected final TextView selectedPackageView;
    private final Button continueButton;
    private final TextView remainingAmount;
    protected TextView amount;
    protected int maxAmount;
    protected List<String> numbers = new ArrayList<>();


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
        this.remainingAmount = this.layout.findViewById(R.id.remainingAmount);

        this.continueButton.setOnClickListener(v -> mainActivity.setAmount(getAmount()));
        this.setVisibility(false);
    }

    @SuppressLint("DefaultLocale")
    protected void updateAmount()
    {
        amount.setText(Utils.formatAmount(this.getAmount()));
    }

    protected int getAmount()
    {
        if (this.numbers.size() == 0) {
            return 0;
        }

        return Integer.parseInt(String.join("", this.numbers));
    }

    @SuppressLint("SetTextI18n")
    public void setLabel(String eventName, String studentName, String packageTitle)
    {
        this.selectedPackageView.setText(eventName + "\n" + studentName + "\n" + packageTitle);
    }

    public void setAmount(int amount)
    {
        this.numbers.clear();
        this.numbers.add(String.valueOf(this.maxAmount = amount));
        this.remainingAmount.setText(Utils.formatAmount(amount));
        this.updateAmount();
    }

    public void clearAmount()
    {
        this.numbers.clear();
        this.updateAmount();
    }

    public void keypadListener(Button button)
    {
        button.setOnClickListener(v -> {
            String number = ((Button) v).getText().toString();
            if (number.equals("⌫")) {
               this.removeLastNumber();
            } else {
                this.numbers.add(number);
            }
            this.updateAmount();

            // When the entered amount exceeds the package/remaining amount, we want to remove the last entered number
            // but before that, we want to show that we got the input, that way it doesn't seem like a bug.
            new CountDownTimer(200, 100) {
                @Override
                public void onTick(long millisUntilFinished) {}

                @Override
                public void onFinish() {
                    if (getAmount() > maxAmount) {
                        removeLastNumber();
                        updateAmount();
                    }
                }
            }.start();

        });
    }

    protected void removeLastNumber()
    {
        int lastIndex = this.numbers.size() - 1;
        if (lastIndex >= 0) {
            this.numbers.remove(lastIndex);
        }
    }
}