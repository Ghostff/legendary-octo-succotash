package com.android.montelongoworldwide.pages;

import android.app.AlertDialog;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.android.montelongoworldwide.Utils;
import com.bumptech.glide.Glide;
import com.creditcall.chipdnamobile.*;
import com.google.android.material.tabs.TabLayout;

public class PaymentCollect extends AbstractToggleable {
    protected final TextView paymentAmountView;
    protected final ConstraintLayout paymentSwipeContainer;
    protected final ConstraintLayout paymentKeyInContainer;
    protected final TabLayout paymentTabNavLayout;
    protected final Button submitPaymentButton;
    private final PackageSelectionActivity context;
    private Transaction transaction;
    private AlertDialog alertDialog;

    public PaymentCollect(PackageSelectionActivity mainActivity) {
        ViewGroup parentLayout = mainActivity.findViewById(R.id.mainPageContainer);
        this.layout = (this.context = mainActivity).getLayout(R.layout.pages_payment_collection, parentLayout, false);
        // Add layout1 to the parent layout
        parentLayout.addView(this.layout);

        (this.alertDialog = Utils.alert(mainActivity, "", "")).hide();


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


        // we should process api payment here (this is just for demo)
        this.submitPaymentButton.setOnClickListener(v -> mainActivity.setPaymentCompleted(transaction));
        this.setVisibility(false);
    }

    public void startTransaction(Transaction transaction)
    {
        this.transaction = transaction;
        this.paymentAmountView.setText(Utils.formatAmount(transaction.amount));

        // Request Parameters are used as to communicate - with ChipDna Mobile - the parameters needed to complete a given command.
        // They are sent with the method call to ChipDna Mobile.
        Parameters requestParameters = new Parameters();

        // The following parameters are essential for the completion of a transaction.
        // In the current example the parameters are initialised as constants. They will need to be dynamically collected and initialised.
        requestParameters.add(ParameterKeys.Amount, String.valueOf(transaction.amount));
        requestParameters.add(ParameterKeys.AmountType, ParameterValues.AmountTypeActual);
        requestParameters.add(ParameterKeys.Currency, "USD");

        // The user reference is needed to be to able to access the transaction on WEBMis.
        // The reference should be unique to a transaction, so it is suggested that the reference is generated, similar to the example below.
        requestParameters.add(ParameterKeys.UserReference, transaction.refId);

        requestParameters.add(ParameterKeys.TransactionType, ParameterValues.Sale);
        requestParameters.add(ParameterKeys.PaymentMethod, ParameterValues.Card);
        requestParameters.add(ParameterKeys.AutoConfirm, ParameterValues.TRUE);
        //TransactionErrorCode.html#UserReferenceDuplicate

        // Use an instance of ChipDnaMobile to begin startTransaction.
        Parameters response = ChipDnaMobile.getInstance().startTransaction(requestParameters);

        if (response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result).equals(ParameterValues.FALSE)) {
            Utils.alert(this.context, "Error", response.getValue(ParameterKeys.Errors));
        }
    }

    private class TransactionListener implements ITransactionUpdateListener, ITransactionFinishedListener,
            IDeferredAuthorizationListener, ISignatureVerificationListener, IVoiceReferralListener,
            IPartialApprovalListener, IForceAcceptanceListener, IVerifyIdListener, IApplicationSelectionListener, IUserNotificationListener, ISignatureCaptureListener {
        @Override
        public void onTransactionUpdateListener(Parameters parameters) {
            String action = parameters.getValue(ParameterKeys.TransactionUpdate);
            if (action.equalsIgnoreCase("CardTapped")
                || action.equalsIgnoreCase("SmartcardInserted")
                || action.equalsIgnoreCase("CardSwiped")) {
                context.runOnUiThread(() -> Utils.alert(alertDialog, "Processing", "Processing, Please wait...", true));
            }

            log(parameters.getValue(ParameterKeys.TransactionUpdate));
        }

        @Override
        public void onTransactionFinishedListener(Parameters parameters) {
            log("onTransactionFinishedListener =>", parameters);

            String errorMsg = parameters.getValue(ParameterKeys.ErrorDescription);
            String receipt = parameters.getValue(ParameterKeys.PreformattedCustomerReceipt);
            String type = parameters.getValue(ParameterKeys.PaymentMethod);
            String transactionId = parameters.getValue(ParameterKeys.TransactionId);
            String cardLast4 = parameters.getValue(ParameterKeys.MaskedPan);

            context.runOnUiThread(() -> {
                context.toggleRefreshing(false);
                if (errorMsg != null) {
                    context.runOnUiThread(() -> Utils.alert(alertDialog, "Error", errorMsg));
                    ChipDnaMobile.getInstance().terminateTransaction(null);
                    startTransaction(transaction);
                    return;
                }

                alertDialog.dismiss();
                transaction.id = transactionId;
                transaction.type = type.equalsIgnoreCase("card") ? "credit" : type.toLowerCase();
                transaction.last4 = cardLast4;
                transaction.receipt = receipt;
                context.setPaymentCompleted(transaction);
            });
        }

        @Override
        public void onSignatureVerification(Parameters parameters) {
            log("Signature Check Required");
        }

        @Override
        public void onVoiceReferral(Parameters parameters) {
            log("Voice Referral Check Required");
        }

        @Override
        public void onVerifyId(Parameters parameters) {

        }

        @Override
        public void onDeferredAuthorizationListener(Parameters parameters) {

        }

        @Override
        public void onForceAcceptance(Parameters parameters) {

        }

        @Override
        public void onPartialApproval(Parameters parameters) {

        }

        @Override
        public void onApplicationSelection(Parameters parameters) {
            log("onApplicationSelection", parameters);
        }

        @Override
        public void onUserNotification(Parameters parameters) {
            log("onUserNotification", parameters);
            processUserNotificationRequest(parameters);
        }

        @Override
        public void onSignatureCapture(Parameters parameters) {
            log("requestSignatureCapture", parameters);
        }
    }


    private void processUserNotificationRequest(final Parameters parameters) {
        this.context.runOnUiThread(() -> {
            // Notify the cardholder with the user notification using a dialog alert or other means.
            String message = parameters.getValue(ParameterKeys.UserNotification);
            switch (parameters.getValue(ParameterKeys.UserNotification)) {
                case "FallforwardInsertSwipeCard":
                    message = "Please insert or swipe your card.";
                    break;
                case "FallforwardInsertCard":
                    message = "Please insert your card.";
                    break;
                case "FallforwardSwipeCard":
                case "FallbackSwipeCard":
                    message = "Please swipe your card.";
                    break;
            }

            Utils.alert(alertDialog, "Error", message);
            new Handler().postDelayed(() -> {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }, 5000);
        });
    }

    public void RegisterCollectEvents()
    {
        TransactionListener transactionListener = new TransactionListener();
        ChipDnaMobile.getInstance().addTransactionFinishedListener(transactionListener);
        ChipDnaMobile.getInstance().addApplicationSelectionListener(transactionListener);
        ChipDnaMobile.getInstance().addSignatureVerificationListener(transactionListener);
        ChipDnaMobile.getInstance().addUserNotificationListener(transactionListener);

        ChipDnaMobile.getInstance().addTransactionUpdateListener(transactionListener);
        ChipDnaMobile.getInstance().addDeferredAuthorizationListener(transactionListener);
        ChipDnaMobile.getInstance().addVoiceReferralListener(transactionListener);
        ChipDnaMobile.getInstance().addPartialApprovalListener(transactionListener);
        ChipDnaMobile.getInstance().addForceAcceptanceListener(transactionListener);
        ChipDnaMobile.getInstance().addVerifyIdListener(transactionListener);
        ChipDnaMobile.getInstance().addSignatureCaptureListener(transactionListener);
    }


    protected void log(String message)
    {
        Log.w("log", message);
    }

    private void log(String title, Parameters parameters) {
        StringBuilder formattedLogBuilder = new StringBuilder();
        formattedLogBuilder.append(title);

        if(parameters != null) {
            for (Parameter parameter : parameters.toList()) {
                formattedLogBuilder.append(String.format("\t[%s]\n", parameter));
            }
        }

        log(formattedLogBuilder.toString());
    }
}
