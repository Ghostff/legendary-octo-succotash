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
import com.android.montelongoworldwide.pages.Market;
import com.android.montelongoworldwide.pages.Package;
import com.android.montelongoworldwide.pages.Page;
import com.android.montelongoworldwide.pages.User;

import java.util.ArrayList;
import java.util.List;


public class PackageSelectionActivity extends AppCompatActivity {
    protected Market.Model selectedMarket;
    protected User.Model selectedUser;


    protected Market market;
    protected User user;
    protected TextView selectedMarketView;
    protected TextView selectedUserView;
    private CardView footerCardView;
    private Package.Model selectedPackage;
    private Package pkg;

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

        this.market = new Market(this);
        this.pkg = new Package(this);
        (this.user = new User(this)).setVisibility(false);

        ((EditText) findViewById(R.id.searchEditText)).addTextChangedListener(new TextWatcher() {
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
        for (Page page : new Page[] { this.market, this.user, this.pkg}) {
            page.setVisibility(false);
        }

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
        } else {
            Log.e("text", "Should go to payment");
        }
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

    public static  <T> List<T> filterList(List<T> inputList, Predicate<? super T> filter) {
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