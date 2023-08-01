package com.android.montelongoworldwide.pages;

import android.annotation.SuppressLint;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.android.montelongoworldwide.Utils;
import org.json.JSONArray;

import java.util.ArrayList;

public class PurchaseAgreement extends AbstractToggleable {

    private final WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    public PurchaseAgreement(PackageSelectionActivity mainActivity) {
        ViewGroup parentLayout = mainActivity.findViewById(R.id.mainPageContainer);
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
        JSONArray array = new JSONArray();
        for (Transaction transaction : allTransactions) {
            array.put(transaction.toJson());
        }

        this.webView.loadUrl(Utils.APP_URL + "/purchase-agreements/" + marketType + "/direct?transactions=" + array);
    }
}
