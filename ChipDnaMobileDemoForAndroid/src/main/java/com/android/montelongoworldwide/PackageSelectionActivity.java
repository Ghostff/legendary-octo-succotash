package com.android.montelongoworldwide;

import android.app.AlertDialog;
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
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import com.android.R;
import com.android.montelongoworldwide.pages.*;
import com.android.montelongoworldwide.pages.Package;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


public class PackageSelectionActivity extends AppCompatActivity {
    public static final String CRM_BEARER_TOKEN = "$2y$12$MpOmiW7Q6UYlodBzLdzaTefy8n7frp5YU2thqT6Sb7iXMwvYoYNUi";
    protected Market.Model selectedMarket;
    protected Event.Model selectedEvent;
    protected Datetime.Model selectedDatetime;
    protected User.Model selectedUser;

    protected Package.Model selectedPackage;


    protected Market market;

    protected User user;
    protected TextView selectedMarketView;
    protected TextView selectedUserView;
    protected CardView footerCardView;
    protected Transaction currentTransaction;
    protected ArrayList<Transaction> allTransactions = new ArrayList<>();
    protected Dictionary<String, String> lastTransaction = new Hashtable<>();
    protected boolean signedPA = false;
    protected boolean showPurchaseAgreementPage = false;
    protected Package pkg;
    protected EditText searchEditText;
    protected PaymentAmount paymentAmount;
    protected PaymentCollect paymentCollect;
    protected PaymentCompleted paymentCompleted;
    protected PurchaseAgreement purchaseAgreement;
    private LinearLayoutCompat loadingLayout;
    private Event event;
    private Datetime datetime;
    protected Hashtable<Integer, AbstractToggleable> pages = new Hashtable<>();
    protected JSONArray packages = new JSONArray();
    protected int pageIndex = 0;

    public PackageSelectionActivity()
    {
        new JsonRequest<JSONArray>(Utils.url("mobile-app/markets"), PackageSelectionActivity.CRM_BEARER_TOKEN) {
            @Override
            public void onSuccess(JSONArray markets) {
                market.setModels(markets).setVisibility(true);
                refresh();
            }

            @Override
            public void onError(String e) {
                Log.e("error", e);
            }
        }.dispatch();

        try {
            this.packages.put(new JSONObject()
                    .put("name", "Platinum")
                    .put("price", 30_000 * 100)
                    .put("title", "3 Days Workshop"));

            this.packages.put(new JSONObject()
                    .put("name", "Platinum")
                    .put("price", 17_000 * 100)
                    .put("title", "Advanced Bus Tour"));

            this.packages.put(new JSONObject()
                    .put("name", "Platinum")
                    .put("price", 10_000 * 100)
                    .put("title", "Mentoring"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

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

        this.loadingLayout = findViewById(R.id.loadingLayout);
        this.footerCardView = findViewById(R.id.footerCardView);
        this.selectedMarketView = findViewById(R.id.selectedMarketView);
        this.selectedUserView = findViewById(R.id.selectedUserView);
        this.searchEditText = findViewById(R.id.searchEditText);

        pages.put(0, this.market = new Market(this));
        pages.put(1, this.event = new Event(this));
        pages.put(2, this.datetime = new Datetime(this));
        pages.put(3, this.user = new User(this));
        pages.put(4, this.pkg = new Package(this));
        pages.put(5, this.paymentAmount = new PaymentAmount(this));
        pages.put(6, this.paymentCollect = new PaymentCollect(this));
        pages.put(7, this.paymentCompleted = new PaymentCompleted(this));
        pages.put(8, this.purchaseAgreement = new PurchaseAgreement(this));

        this.footerCardView.setVisibility(View.GONE);
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
    }

    protected void variableRender(String searchKeyword)
    {
        for (AbstractToggleable view : new AbstractToggleable[] {
                this.market,
                this.user,
                this.pkg,
                this.paymentAmount,
                this.paymentCollect,
                this.paymentCompleted,
                this.purchaseAgreement
        }) {
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
        } else {
            this.searchEditText.setVisibility(View.GONE);
            this.footerCardView.setVisibility(View.GONE);

            if (this.showPurchaseAgreementPage) {
                this.purchaseAgreement.setVisibility(true);
                return;
            }

            if (this.currentTransaction == null) {
                String packageTitle = this.selectedPackage.title;
                int amount = this.getAmountDifference();
//                this.paymentAmount.setVisibility(true, this.selectedMarket, this.selectedUser, packageTitle, amount);
            } else if (this.currentTransaction.id == null) {
//                this.paymentCollect.setVisibility(true, this.currentTransaction);
            } else {
                this.allTransactions.add(this.currentTransaction);
                this.paymentCompleted.setVisibility(true);
            }
        }

        this.loadingLayout.setVisibility(View.GONE);
    }

    public void refresh()
    {
        this.variableRender(null);
    }

    public void makeBusy()
    {
        this.loadingLayout.setVisibility(View.VISIBLE);
    }


    protected void renderFooter()
    {
        if (this.selectedMarket == null) return;

        StringBuilder market = new StringBuilder(this.selectedMarket.toString());
        if (this.selectedEvent != null) market.append(" [").append(this.selectedEvent.shortName).append("]");
        if (this.selectedDatetime != null) market.append(" ").append(this.selectedDatetime.datetime);

        this.selectedMarketView.setText(market.toString());
        this.selectedUserView.setText(this.selectedUser == null ? "" : this.selectedUser.name);
    }

    protected void render(int pageIndex)
    {
        // hide footer and search after package is selected
        int visibility = pageIndex > 4 || pageIndex == 0  ? View.GONE : View.VISIBLE;
        this.searchEditText.setVisibility(visibility);
        this.footerCardView.setVisibility(visibility);

        for (AbstractToggleable page : this.pages.values()) {
            page.setVisibility(false);
        }

        AbstractToggleable page = this.pages.get(pageIndex);
        if (page != null) {
            page.setVisibility(true);
        }

        this.renderFooter();
    }

    protected void moveForward()
    {
        this.render(Math.min(++this.pageIndex, this.pages.size()));
    }

    protected void moveBackward()
    {
        this.render(Math.max(0, --this.pageIndex));
    }

    public void setMarket(Market.Model market)
    {
        this.selectedMarket = market;
        this.event.setModels(market.events);
        this.moveForward();
    }

    public void setEvent(Event.Model event)
    {
        this.selectedEvent = event;
        this.datetime.setModels(event.datetimes);
        this.moveForward();
    }

    public void setDatetime(Datetime.Model datetime)
    {
        this.selectedDatetime = datetime;
        this.user.setModels(datetime.users);
        this.moveForward();
    }

    public void setUser(User.Model user)
    {
        this.selectedUser = user;
        this.pkg.setModels(this.packages);
        this.moveForward();
    }

    public void setPackage(Package.Model pkg)
    {
        this.selectedPackage = pkg;
        String eventName = this.selectedMarketView.getText().toString();
        this.paymentAmount.setLabel(eventName, this.selectedUser.name, this.selectedPackage.title);
        this.paymentAmount.setAmount(this.getAmountDifference());
        this.moveForward();
    }

    public void setAmount(int amount)
    {
        this.paymentCollect.setAmount(amount);
        this.moveForward();
    }

    public void setTransaction(Transaction transaction)
    {
        this.allTransactions.add(transaction);
        this.moveForward();
    }

    public void addAnotherPayment()
    {
        this.pageIndex = 4;
        this.setPackage(this.selectedPackage);
    }

    @Override
    public void onBackPressed() {
        this.moveBackward();

        // cant go back if PA is not signed.
        /*if(!this.lastTransaction.isEmpty() && !this.signedPA) {
            return;
        }

        if (this.currentTransaction != null) {
            this.paymentAmount.clearAmount();
            this.setTransaction(null);
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

        super.onBackPressed();*/
    }

    /**
     * Gets amount difference between package and user specified.
     * e.g if package is 10k and user paid 8k, this method will return 2k
     *
     * @return int
     */
    public int getAmountDifference()
    {
        if (this.allTransactions.isEmpty()) {
            return selectedPackage.price;  // fail-safe. always have a diff
        }

        int total = 0;
        for (Transaction transaction : this.allTransactions) {
            total += transaction.amount;
        }

        return this.selectedPackage.price - total;
    }

    public void signPurchaseAgreements()
    {
        if (this.getAmountDifference() >= 1) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Purchase Agreement Signing")
                    .setMessage("You have not paid the full amount for this package. Are you sure you want to proceed with signing the purchase agreement?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> moveForward())
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        } else {
            this.moveForward();
        }
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