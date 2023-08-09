package com.android.montelongoworldwide;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.R;
import com.android.montelongoworldwide.pages.*;
import com.android.montelongoworldwide.pages.Package;
import com.creditcall.chipdnamobile.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.*;


public class PackageSelectionActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String CRM_BEARER_TOKEN = "$2y$12$MpOmiW7Q6UYlodBzLdzaTefy8n7frp5YU2thqT6Sb7iXMwvYoYNUi";
    protected Market.Model selectedMarket;
    protected Event.Model selectedEvent;
    protected Datetime.Model selectedDatetime;
    protected User.Model selectedUser;
    protected Package.Model selectedPackage;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 200;


    protected Market market;
    protected User user;
    protected TextView selectedMarketView;
    protected TextView selectedUserView;
    protected CardView footerCardView;
    protected ArrayList<Transaction> allTransactions = new ArrayList<>();
    protected Package pkg;
    protected EditText searchEditText;
    protected PaymentAmount paymentAmount;
    protected PaymentCollect paymentCollect;
    protected PaymentCompleted paymentCompleted;
    protected PurchaseAgreement purchaseAgreement;
    private Event event;
    private Datetime datetime;
    protected Hashtable<Integer, AbstractToggleable> pages = new Hashtable<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    protected int pageIndex = 0;
    private AbstractToggleable activePage;

    public PackageSelectionActivity()
    {
        PackageSelectionActivity context = this;
        new Request<JSONArray>(Utils.url("mobile-app/markets"), PackageSelectionActivity.CRM_BEARER_TOKEN) {
            @Override
            public void onSuccess(JSONArray markets) {
                market.setModels(markets);
            }

            @Override
            public void onError(String e) {
                Utils.alert(context, "Error", "Could not load markets.\n" + e, null, null);
            }
        }.get();

        new Request<JSONArray>(Utils.url("mobile-app/packages?include_fe=false"), PackageSelectionActivity.CRM_BEARER_TOKEN) {
            @Override
            public void onSuccess(JSONArray markets) {
                pkg.setModels(markets);
            }

            @Override
            public void onError(String e) {
                try {
                    // Fallback if quickbooks fails
                    pkg.setShowPrice(true).setModels(new JSONArray("[{\"Id\":\"5\",\"Name\":\"Platinum\",\"UnitPrice\":\"30000\",\"Description\":\"3 Days Workshop\"},{\"Id\":\"7\",\"Name\":\"Gold\",\"UnitPrice\":\"17000\",\"Description\":\"Advanced Bus Tour\"},{\"Id\":\"8\",\"Name\":\"Silver\",\"UnitPrice\":\"10000\",\"Description\":\"Mentoring\"}]"));
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }.get();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_selection);

        if (!this.isBluetoothPermissionGranted()) {
            this.requestBluetoothPermissions();
        }

        this.swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        this.footerCardView = findViewById(R.id.footerCardView);
        this.selectedMarketView = findViewById(R.id.selectedMarketView);
        this.selectedUserView = findViewById(R.id.selectedUserView);
        (this.searchEditText = findViewById(R.id.searchEditText)).setVisibility(View.GONE);

        pages.put(0, new ConnectPinPad(this));
        pages.put(1, this.market = new Market(this));
        pages.put(2, this.event = new Event(this));
        pages.put(3, this.datetime = new Datetime(this));
        pages.put(4, this.user = new User(this));
        pages.put(5, this.pkg = new Package(this));
        pages.put(6, this.paymentAmount = new PaymentAmount(this));
        pages.put(7, this.paymentCollect = new PaymentCollect(this));
        pages.put(8, this.paymentCompleted = new PaymentCompleted(this));
        pages.put(9, this.purchaseAgreement = new PurchaseAgreement(this));

        this.footerCardView.setVisibility(View.GONE);
        this.searchEditText.addTextChangedListener(new TextWatcher() {
            private String lastKeyword = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim().toLowerCase();
                // only rerender when search changes
                if (!keyword.equals(this.lastKeyword)) {
                    Page<?> page = (Page<?>) pages.get(pageIndex);
                    if (page != null) {
                        page.render(this.lastKeyword = keyword);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        this.render(0); // initial render.
    }

    public void toggleRefreshing(boolean isVisible)
    {
        this.swipeRefreshLayout.setRefreshing(isVisible);
        if (this.activePage != null) this.activePage.onRefreshStateChange(isVisible);
    }

    protected void renderFooter()
    {
        this.footerCardView.setVisibility(pageIndex > 5 || pageIndex < 2  ? View.GONE : View.VISIBLE);
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
        this.searchEditText.setVisibility(View.GONE);
        if (pageIndex > 1 && pageIndex < 4) {
            this.searchEditText.setText("");
            this.searchEditText.setVisibility(View.VISIBLE);
        }

        for (Map.Entry<Integer, AbstractToggleable> entry : this.pages.entrySet()) {
            AbstractToggleable page = entry.getValue();
            if (entry.getKey() == pageIndex) {
                this.activePage = page;
            } else {
                page.setVisibility(false);
                this.toggleRefreshing(false);
            }
        }

        this.activePage.setVisibility(true);
        this.swipeRefreshLayout.setOnRefreshListener(() -> {
            activePage.onRefresh(swipeRefreshLayout);
            activePage.onRefreshStateChange(true);
        });

        this.renderFooter();
    }

    public void moveForward()
    {
        this.render(Math.min(++this.pageIndex, this.pages.size()));
    }

    public void moveBackward()
    {
        this.render(Math.max(0, --this.pageIndex));
    }

    public void setPinPad(Parameters params)
    {
        // When param is null, we are continuing without pinpad.
        boolean usingPinPad = params != null;
        if (usingPinPad && (!params.containsKey(ParameterKeys.Result) || !params.getValue(ParameterKeys.Result).equalsIgnoreCase("true"))) {
            this.toggleRefreshing(false);
            return;
        }

        this.paymentCollect.RegisterCollectEvents(usingPinPad);
        this.moveForward();
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
        this.moveForward();
    }

    public void setPackage(Package.Model pkg)
    {
        this.selectedPackage = pkg;
        String eventName = this.selectedMarketView.getText().toString();
        this.paymentAmount.setLabel(eventName, this.selectedUser.name, this.selectedPackage.name);
        this.paymentAmount.setAmount(this.getAmountDifference());
        this.moveForward();
    }

    public void setPaymentCollect(int amount)
    {
        Transaction transaction = new Transaction(
                amount,
                this.selectedMarket.id,
                this.selectedEvent.id,
                this.selectedDatetime.id,
                this.selectedUser.id,
                this.selectedPackage.id
        );

        this.paymentCollect.startTransaction(transaction);
        this.moveForward();
    }

    public void setPaymentCompleted(Transaction transaction)
    {
        this.allTransactions.add(transaction);
        this.moveForward();
    }

    public void addAnotherPayment()
    {
        this.pageIndex = 4;
        this.moveForward();
    }

    public void signPurchaseAgreement()
    {
        this.purchaseAgreement.sign(this.allTransactions, this.selectedMarket.type);
        this.moveForward();
    }

    @Override
    public void onBackPressed() {
        if (this.pageIndex > 3) {
            this.moveBackward();
        }
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
            Utils.alert(
                this,
                "Confirm Purchase Agreement Signing",
                "You have not paid the full amount for this package. Are you sure you want to proceed with signing the purchase agreement?",
                new Utils.AlertAction("Yes", (dialog, whichButton) -> signPurchaseAgreement()),
                new Utils.AlertAction("Cancel", (dialog, whichButton) -> {})
            );
        } else {
            this.signPurchaseAgreement();
        }
    }

    public void purchaseAgreementSigned()
    {
        this.pageIndex = 2;
        this.allTransactions.clear();
        this.moveForward();
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

    public View getLayout(@LayoutRes int resource, ViewGroup root) {
        return LayoutInflater.from(this).inflate(resource, root);
    }

    public View getLayout(@LayoutRes int resource, ViewGroup root, boolean attachToRoot) {
        return LayoutInflater.from(this).inflate(resource, root, attachToRoot);
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            final String[] android12BluetoothPermissions = new String[] {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
            };

            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    &&  checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, android12BluetoothPermissions, BLUETOOTH_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @SuppressLint("NewApi")
    private boolean isBluetoothPermissionGranted()  {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }
        return checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Bluetooth permissions have not been granted", Toast.LENGTH_SHORT).show();
                }
            }

            // Location permissions are required for BLE to return scan results
            if (!this.isLocationPermissionGranted()) {
                this.requestLocationPermissions();
            }
        }
    }
}