package com.android.montelongoworldwide.pages;

import android.app.AlertDialog;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.android.montelongoworldwide.Request;
import com.android.montelongoworldwide.Utils;
import com.bumptech.glide.Glide;
import com.creditcall.chipdnamobile.*;
import com.google.android.material.tabs.TabLayout;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Objects;

public class PaymentCollect extends AbstractToggleable {
    protected final TextView paymentAmountView;
    protected final ConstraintLayout paymentSwipeContainer;
    protected final ConstraintLayout paymentKeyInContainer;
    protected final TabLayout paymentTabNavLayout;
    protected final Button submitPaymentButton;
    private final PackageSelectionActivity context;
    private final EditText cardNumber;
    private final EditText cardExpiry;
    private final EditText cardCVV;
    private final EditText cardName;
    private Transaction transaction;
    private AlertDialog alertDialog;
    private boolean allowPinPad;

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


        this.cardName = this.layout.findViewById(R.id.cardName);
        this.cardNumber = this.layout.findViewById(R.id.cardNumber);
        this.cardExpiry = this.layout.findViewById(R.id.cardExpiry);
        this.cardCVV = this.layout.findViewById(R.id.cardCVV);

        this.cardNumber.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            private static final String CREDIT_CARD_REGEX = "([0-9]{1,4})";

            @Override
            public void afterTextChanged(Editable editable) {
                String formattedText = editable.toString().replaceAll(" ", "").replaceAll(CREDIT_CARD_REGEX, "$1 ").trim();
                cardNumber.removeTextChangedListener(this);
                cardNumber.setText(formattedText);
                cardNumber.setSelection(formattedText.length());
                cardNumber.addTextChangedListener(this);
            }
        });

        this.cardExpiry.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String rawText = editable.toString().replaceAll("/", "");

                if (rawText.length() > 2) {
                    String formattedText = rawText.substring(0, 2) + "/" + rawText.substring(2);
                    cardExpiry.removeTextChangedListener(this);
                    cardExpiry.setText(formattedText);
                    cardExpiry.setSelection(formattedText.length());
                    cardExpiry.addTextChangedListener(this);
                }
            }
        });

        // we should process api payment here (this is just for demo)
        this.submitPaymentButton.setOnClickListener(this::startManualTransaction);
        this.setVisibility(false);
    }

    public void startTransaction(Transaction transaction)
    {
        this.paymentAmountView.setText(Utils.formatAmount((this.transaction = transaction).amount));
        if (this.allowPinPad) {
            this.startPinPadTransaction();
        } else {
            Objects.requireNonNull(this.paymentTabNavLayout.getTabAt(1)).select();
            this.paymentTabNavLayout.removeTabAt(0);
        }
    }

    protected void startManualTransaction(View view)
    {
        Button btn = (Button) view;
        btn.setEnabled(false);

        String[] names = this.cardName.getText().toString().split(" ");
        String cardNumber = this.cardNumber.getText().toString().replaceAll(" ", "");
        String cardExpiry = this.cardExpiry.getText().toString();
        String cardCVV = this.cardCVV.getText().toString();

        StringBuilder lastName = new StringBuilder();
        for (int i = 1; i < names.length; i++) {
            lastName.append(" ").append(names[i]);
        }

        new Request<HashMap<String, String>>("https://secure.nmi.com/api/transact.php") {
            @Override
            public void onSuccess(HashMap<String, String> result) {
                btn.setEnabled(true);
                if (!result.get("response").equalsIgnoreCase("1")) {
                    alertDialog = Utils.alert(context, "Error", result.get("responsetext"));
                    return;
                }

                transaction.id = result.get("transactionid");
                transaction.firstName = names[0];
                transaction.lastName = lastName.toString();
                transaction.type = "credit";
                transaction.last4 = cardNumber.substring(cardNumber.length() - 4);
                context.setPaymentCompleted(transaction);
            }

            @Override
            public void onError(String errorMessage) {
                btn.setEnabled(true);
                alertDialog = Utils.alert(context, "Error", errorMessage);
            }

            @Override
            public HashMap<String, String> onResponse(String response) throws UnsupportedEncodingException {
                return (HashMap<String, String>) Request.decodeQueryString(response);
            }
        }.post(new HashMap<String, String>() {{
            put("amount", (new DecimalFormat("#.00")).format(transaction.amount / 100));
            put("type", "sale");
            put("firstname", names[0]);
            put("lastname", lastName.toString());
            put("ccnumber", cardNumber);
            put("ccexp", cardExpiry);
            put("cvv", cardCVV);
            put("orderid", transaction.refId);
            put("security_key", ConnectPinPad.APIKEY);
        }});
    }

    protected void startPinPadTransaction()
    {
        // Request Parameters are used as to communicate - with ChipDna Mobile - the parameters needed to complete a given command.
        // They are sent with the method call to ChipDna Mobile.
        Parameters requestParameters = new Parameters();

        // The following parameters are essential for the completion of a transaction.
        // In the current example the parameters are initialised as constants. They will need to be dynamically collected and initialised.
        requestParameters.add(ParameterKeys.Amount, String.valueOf(this.transaction.amount));
        requestParameters.add(ParameterKeys.AmountType, ParameterValues.AmountTypeActual);
        requestParameters.add(ParameterKeys.Currency, "USD");

        // The user reference is needed to be to able to access the transaction on WEBMis.
        // The reference should be unique to a transaction, so it is suggested that the reference is generated, similar to the example below.
        requestParameters.add(ParameterKeys.UserReference, this.transaction.refId);

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

            // [] to allow mutability in closure
            final String[] errorMsg = {parameters.getValue(ParameterKeys.ErrorDescription)};
            String error = parameters.getValue(ParameterKeys.Errors);
            String receipt = parameters.getValue(ParameterKeys.PreformattedCustomerReceipt);
            String type = parameters.getValue(ParameterKeys.PaymentMethod);
            String transactionId = parameters.getValue(ParameterKeys.TransactionId);
            String cardLast4 = parameters.getValue(ParameterKeys.MaskedPan);

            context.runOnUiThread(() -> {
                context.toggleRefreshing(false);
                if (errorMsg[0] != null || error != null) {
                    // Restart transaction after ok is clicked.
                    alertDialog.dismiss();

                    String okBtnMessage = "Ok";
                    Utils.AlertAction cancel;
                    // maybe "TimeOutError"
                    if (errorMsg[0] == null) {
                        okBtnMessage = "Retry";
                        errorMsg[0] = error;
                        cancel = new Utils.AlertAction("Go Back", (dialog, which) -> context.moveBackward());
                    } else {
                        cancel = null;
                    }

                    Utils.AlertAction ok = new Utils.AlertAction(okBtnMessage, (dialog, which) -> startTransaction(transaction));
                    ChipDnaMobile.getInstance().terminateTransaction(null);
                    context.runOnUiThread(() -> Utils.alert(context, "Error", errorMsg[0], ok, cancel));
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

    public void RegisterCollectEvents(boolean isUsingPinPad)
    {
        if (!(this.allowPinPad = isUsingPinPad)) {
            return;
        }

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
        StringBuilder formattedLogBuilder = new StringBuilder(title);

        if(parameters != null) {
            for (Parameter parameter : parameters.toList()) {
                formattedLogBuilder.append(String.format("\t[%s]\n", String.valueOf(parameter)));
            }
        }

        log(formattedLogBuilder.toString());
    }
}
