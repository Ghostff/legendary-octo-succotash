package com.creditcall.chipdnamobiledemo;

import android.annotation.SuppressLint;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView amount;
    private List<String> numbers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        this.amount = findViewById(R.id.amount);
        keypadListener(findViewById(R.id.keypad_1));
        keypadListener(findViewById(R.id.keypad_2));
        keypadListener(findViewById(R.id.keypad_3));
        keypadListener(findViewById(R.id.keypad_4));
        keypadListener(findViewById(R.id.keypad_5));
        keypadListener(findViewById(R.id.keypad_6));
        keypadListener(findViewById(R.id.keypad_7));
        keypadListener(findViewById(R.id.keypad_8));
        keypadListener(findViewById(R.id.keypad_9));
        keypadListener(findViewById(R.id.keypad_0));
        keypadListener(findViewById(R.id.keypad_backspace));
    }

    @SuppressLint("DefaultLocale")
    protected void updateAmount()
    {
        amount.setText(String.format("$%,.2f", this.getAmount() / 100));
    }

    protected double getAmount()
    {
        if (this.numbers.size() == 0) {
            return 0.0;
        }

        return Integer.parseInt(String.join("", this.numbers));
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