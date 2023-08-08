package com.android.montelongoworldwide.pages;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.android.R;
import com.android.montelongoworldwide.JsonRequest;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.android.montelongoworldwide.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PurchaseAgreement extends AbstractToggleable {

    private final WebView webView;
    private final PackageSelectionActivity mainActivity;

    @SuppressLint("SetJavaScriptEnabled")
    public PurchaseAgreement(PackageSelectionActivity mainActivity) {
        ViewGroup parentLayout = (this.mainActivity = mainActivity).findViewById(R.id.mainPageContainer);
        this.layout = mainActivity.getLayout(R.layout.pages_purchase_agreement, parentLayout, false);
        // Add layout1 to the parent layout
        parentLayout.addView(this.layout);
        this.webView = this.layout.findViewById(R.id.paWebViewer);

        // Enable JavaScript (optional, some websites may require it)
        WebSettings webSettings = this.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Ensure links and redirects open within the WebView
        this.webView.setWebViewClient(new WebViewClient());
        this.setVisibility(false);
    }

    public void sign(ArrayList<Transaction> allTransactions, String marketType)
    {
        // while there can be many transactions, core vars like, marketId, eventId, eventDatetimeId, userId and
        // packageId are duplicated across each transaction
        this.watchForPASignAndCloseBrowser(allTransactions.get(0));

        JSONArray array = new JSONArray();
        for (Transaction transaction : allTransactions) {
            array.put(transaction.toJson());
        }

        this.webView.loadUrl(Utils.APP_URL + "/purchase-agreements/" + marketType + "/direct?transactions=" + array);
    }

    private void watchForPASignAndCloseBrowser(Transaction transaction)
    {
        String uri = "mobile-app/verify-purchase-agreement/" + transaction.eventDatetimeId + "/" + transaction.userId;
        new JsonRequest<JSONObject>(Utils.url(uri), PackageSelectionActivity.CRM_BEARER_TOKEN) {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    boolean status = result.getBoolean("status");
                    if (! status) {
                        Thread.sleep(500);
                        dispatch(); // If status is false, continue pinging the URL until status becomes true
                        return;
                    }
                    mainActivity.purchaseAgreementSigned();
                } catch (JSONException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @SuppressLint("LogNotTimber")
            @Override
            public void onError(String errorMessage) {
                Log.e("PA verification", errorMessage);
            }
        }.dispatch();
    }
}
