package com.android.montelongoworldwide;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import com.android.R;
import com.android.montelongoworldwide.pages.*;
import com.android.montelongoworldwide.pages.Package;

import java.util.ArrayList;
import java.util.List;


public class PackageSelectionActivity extends AppCompatActivity {
    protected Market.Model selectedMarket;
    protected User.Model selectedUser;


    protected Market market;
    protected User user;
    protected TextView selectedMarketView;
    protected TextView selectedUserView;
    protected CardView footerCardView;
    protected Package.Model selectedPackage;
    protected double selectedAmount = 0.0;
    protected Package pkg;
    protected EditText searchEditText;
    protected PaymentAmount paymentAmount;

    public View getLayout(@LayoutRes int resource, ViewGroup root) {
        return LayoutInflater.from(this).inflate(resource, root);
    }

    public View getLayout(@LayoutRes int resource, ViewGroup root, boolean attachToRoot) {
        return LayoutInflater.from(this).inflate(resource, root, attachToRoot);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_selection);

        this.footerCardView = findViewById(R.id.footerCardView);
        this.selectedMarketView = findViewById(R.id.selectedMarketView);
        this.selectedUserView = findViewById(R.id.selectedUserView);
        this.searchEditText = findViewById(R.id.searchEditText);

        this.market = new Market(this);
        this.pkg = new Package(this);
        (this.user = new User(this)).setVisibility(false);
        (this.paymentAmount = new PaymentAmount(this)).setVisibility(false);

        this.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call a method to filter the card components based on the search input
                variableRender(s.toString().trim().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        this.variableRender(null);
    }

    protected void variableRender(String searchKeyword)
    {
        for (Toggleable view : new Toggleable[] { this.market, this.user, this.pkg, this.paymentAmount}) {
            view.setVisibility(false);
        }

        this.searchEditText.setVisibility(View.VISIBLE);
        renderFooter();

        // Call a method to filter the card components based on the search input
        if (this.selectedMarket == null) {
            this.market.render(searchKeyword);
            this.market.setVisibility(true);
        } else if (this.selectedUser == null) {
            this.user.render(searchKeyword);
            this.user.setVisibility(true);
        } else if (this.selectedPackage == null) {
            this.pkg.render(searchKeyword);
            this.pkg.setVisibility(true);
        } else if (this.selectedAmount == 0.0) {
            this.searchEditText.setVisibility(View.GONE);
            this.footerCardView.setVisibility(View.GONE);
            this.paymentAmount.setVisibility(true, this.selectedMarket, this.selectedUser, this.selectedPackage);
        } else {

            Log.e("text", "Should go to card swipe");
        }
    }

    @Override
    public void onBackPressed() {
        if (this.selectedAmount > 0.0) {
            this.paymentAmount.clearAmount();
            this.setAmount(0.0);
            return;
        }

        if (this.selectedPackage != null) {
            this.setPackage(null);
            return;
        }

        if (this.selectedUser != null) {
            this.setUser(null);
            return;
        }

        if (this.selectedMarket != null) {
            this.setMarket(null);
            return;
        }

        super.onBackPressed();
    }

    protected void renderFooter()
    {
        if (this.selectedUser != null) {
            this.selectedUserView.setText(this.selectedUser.name);
            this.selectedUserView.setVisibility(View.VISIBLE);
        } else {
            this.selectedUserView.setVisibility(View.GONE);
        }

        if (this.selectedMarket != null) {
            this.selectedMarketView.setText(this.selectedMarket.toString());
            this.footerCardView.setVisibility(View.VISIBLE);;
        } else {
            this.footerCardView.setVisibility(View.GONE);
        }
    }

    public void setMarket(Market.Model market)
    {
        this.selectedMarket = market;
        if (market != null) {
            this.user.load();
        }

        this.variableRender(null);
    }

    public void setUser(User.Model user)
    {
        this.selectedUser = user;
        this.variableRender(null);
    }

    public void setPackage(Package.Model pkg)
    {
        this.selectedPackage = pkg;
        this.variableRender(null);
    }

    public void setAmount(double amount)
    {
        this.selectedAmount = amount;
        this.variableRender(null);
    }

    public static View addVerticalMargin(View cardView, int margin)
    {
        return PackageSelectionActivity.addMargins(cardView, 0, margin, 0, margin);
    }

    public static View addMargins(View cardView, int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(left, top, right, bottom); // Set the desired margin values
        cardView.setLayoutParams(layoutParams);

        return cardView;
    }

    public static <T> List<T> filterList(List<T> inputList, Predicate<? super T> filter) {
        List<T> resultList = new ArrayList<>();
        for (T item : inputList) {
            if (filter.test(item)) {
                resultList.add(item);
            }
        }
        return resultList;
    }

    public interface Predicate<T> {
        boolean test(T t);
    }
}