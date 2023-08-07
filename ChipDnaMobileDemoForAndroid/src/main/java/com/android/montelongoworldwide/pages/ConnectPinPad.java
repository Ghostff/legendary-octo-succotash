package com.android.montelongoworldwide.pages;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.android.montelongoworldwide.Utils;
import com.creditcall.chipdnamobile.*;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ConnectPinPad extends AbstractToggleable {
    class PinPad {
        public final String name, connectionType, displayName;
        private final String connectionTypeLabelBluetooth = "[BT] ";
        private final String connectionTypeLabelUsb = "[USB] ";
        private final String connectionTypeLabelBluetoothLe = "[BLE] ";

        public PinPad(String name, String connectionType) {
            this.name = name;
            this.connectionType = connectionType;

            if (connectionType.equalsIgnoreCase(ParameterValues.BluetoothConnectionType)) {
                this.displayName = this.connectionTypeLabelBluetooth + this.name;
            } else if (connectionType.equalsIgnoreCase(ParameterValues.UsbConnectionType)) {
                this.displayName = this.connectionTypeLabelUsb + this.name;
            } else if (connectionType.equalsIgnoreCase(ParameterValues.BluetoothLeConnectionType)) {
                this.displayName = this.connectionTypeLabelBluetoothLe + this.name;
            } else {
                this.displayName = connectionType;
            }
        }
    }

    private final PackageSelectionActivity activity;
    private final LinearLayout cardContainer;

    //Replace the APP_ID with your APPLICATION ID
    private static final String APP_ID = "AMPAYMENTCOLLECT";
    private static final String DEFAULT_PASSWORD = "267n*bX$gaPB";

    // Replace the APIKEY with your Gateway API KEY as found in the
    // Gateway Control Panel's options/ security keys page
    private static final String APIKEY = "Rh2ybtc2zH2vv3b9932z3jB5437qqP3z";
//    private static final String APIKEY = "6457Thfj624V5r7WUwc5v6a68Zsd6YEm";

    // Note: for the Miura 810 device, a test device will only work in testEnviornment
    // and a production device will only work in LiveEnvironment
    // Using the device in the wrong environment results in an IncompatibleOSWithAppMode during
    // device configuration,
    private static final String ENVIRONMENT = ParameterValues.LiveEnvironment;
    private final TextView emptyListTextView;


    public ConnectPinPad(PackageSelectionActivity mainActivity)
    {
        ViewGroup parentLayout = mainActivity.findViewById(R.id.mainPageContainer);
        this.layout = (this.activity = mainActivity).getLayout(R.layout.pages_connect_pinpad, parentLayout, false);

        parentLayout.addView(this.layout);
        this.cardContainer = this.layout.findViewById(R.id.pinPadModelContainer);

        this.emptyListTextView = this.layout.findViewById(R.id.emptyList);
        // Find the SwipeRefreshLayout by its ID

        // Initialise ChipDna Mobile before starting to interacting with the API. You can check if ChipDna Mobile has been initialised by using isInitialised()
        // It's possible that android may have cleaned up resources while the application has been in the background and needs to be re-initialised.
        if (!ChipDnaMobile.isInitialized()) {
            Parameters requestParameters = new Parameters();
            requestParameters.add(ParameterKeys.Password, DEFAULT_PASSWORD);
            Parameters response = ChipDnaMobile.initialize(activity.getApplicationContext(), requestParameters);
            String errors = response.getValue(ParameterKeys.Errors);
            mainActivity.toggleRefreshing(true);

            if (errors == null && ChipDnaMobile.isInitialized()) {
                this.setCredentials();


                ChipDnaMobile chipDna = ChipDnaMobile.getInstance();
                chipDna.addConnectAndConfigureFinishedListener(parameters -> {
                    activity.runOnUiThread(() -> {
                        try {
                            activity.setPinPad(parameters);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                });


                activity.toggleRefreshing(true); // this loading is closed on mainActivity render (for previous page)
                final Parameters statusParameters = chipDna.getStatus(null);
                // If bluetooth has been setup, auto connect to it.
                if (statusParameters.getValue(ParameterKeys.PinPadName) != null && statusParameters.getValue(ParameterKeys.PinPadName).length() > 0) {
                    connectToPinPad();
                    return;
                }

                this.refresh();
            } else {
                Utils.alert(this.activity, "error",  String.valueOf(errors));
                mainActivity.toggleRefreshing(false);
            }
        }
    }


    @SuppressLint("SetTextI18n")
    protected void renderSelectablePinPads(List<PinPad> selectablePinPads)
    {
        this.cardContainer.removeAllViews();
        for (PinPad pinPad : selectablePinPads) {
            View cardView = activity.getLayout(R.layout.components_connect_pinpad, null);
            TextView cardText = cardView.findViewById(R.id.textViewTitle);
            cardText.setText(pinPad.displayName);
            cardView.setOnClickListener(v -> onPinPadClicked(pinPad));
            PackageSelectionActivity.addVerticalMargin(cardView, 15);
            this.cardContainer.addView(cardView);
        }
    }

    protected void onPinPadClicked(PinPad pinPad)
    {
        this.activity.toggleRefreshing(true);
        // When this is done, chipDna.addConnectAndConfigureFinishedListener callback registered on current constructor is called
        this.activity.runOnUiThread(() -> {
            Parameters requestParameters = new Parameters();
            requestParameters.add(ParameterKeys.PinPadName, pinPad.name);
            requestParameters.add(ParameterKeys.PinPadConnectionType, pinPad.connectionType);
            ChipDnaMobile chipDna = ChipDnaMobile.getInstance();
            Parameters response = chipDna.setProperties(requestParameters);

            if (!response.containsKey(ParameterKeys.Result) || !response.getValue(ParameterKeys.Result).equalsIgnoreCase("True")) {
                Utils.alert(this.activity, "error", "Failed to select pinpad");
                return;
            }

            connectToPinPad();
        });
    }

    private void connectToPinPad()
    {
        // Use an instance of ChipDnaMobile to begin connectAndConfigure of the device.
        // PINpad checks are completed within connectAndConfigure, deciding whether a TMS update will need to be completed.
        Parameters response = ChipDnaMobile.getInstance().connectAndConfigure(ChipDnaMobile.getInstance().getStatus(null));
        if (response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result).equals(ParameterValues.FALSE)) {
            Utils.alert(this.activity, "error", response.getValue(ParameterKeys.Errors));
        }
    }

    protected void refresh() {
        Parameters parameters = new Parameters();
        parameters.add(ParameterKeys.SearchConnectionTypeBluetooth, ParameterValues.TRUE);
        parameters.add(ParameterKeys.SearchConnectionTypeBluetoothLe, ParameterValues.TRUE);
        parameters.add(ParameterKeys.SearchConnectionTypeUsb, ParameterValues.TRUE);

        ChipDnaMobile.getInstance().clearAllAvailablePinPadsListeners();
        ChipDnaMobile.getInstance().addAvailablePinPadsListener(new IAvailablePinPadsListener() {
            @Override
            public void onAvailablePinPads(Parameters parameters) {
                String availablePinPadsXml = parameters.getValue(ParameterKeys.AvailablePinPads);
                List<PinPad> pinPads = new ArrayList<>();

                activity.runOnUiThread(() -> {
                    try {
                        HashMap<String, ArrayList<String>> availablePinPads = ChipDnaMobileSerializer.deserializeAvailablePinPads(availablePinPadsXml);
                        for(String connectionType: availablePinPads.keySet()) {
                            for (String pinpad : Objects.requireNonNull(availablePinPads.get(connectionType))) {
                                pinPads.add(new PinPad(pinpad, connectionType));
                            }
                        }

                    } catch (XmlPullParserException | IOException e) {
                        throw new RuntimeException(e);
                    }

                    ConnectPinPad.this.renderSelectablePinPads(pinPads);
                    activity.toggleRefreshing(false);
                    emptyListTextView.setVisibility(pinPads.size() > 0 ? View.GONE : View.VISIBLE);
                });
            }
        });
        ChipDnaMobile.getInstance().getAvailablePinPads(parameters);
    }

    protected void setCredentials()
    {
        // Credentials are returned to ChipDnaMobile as a set of Parameters
        Parameters requestParameters = new Parameters();

        // The credentials consist of an APIKEY from your gateway account. This can be created by
        // navigating to the Gateway Options => Security Keys section.
        requestParameters.add(ParameterKeys.ApiKey, APIKEY);

        // Set ChipDna Mobile to test mode. This means ChipDna Mobile is running in it's test environment, can configure test devices and perform test transaction.
        // Use test mode while developing your application.
        requestParameters.add(ParameterKeys.Environment, ENVIRONMENT);


        // Set the Application Identifier value. This is used by the TMS platform to configure TMS properties specifically for an integrating application.
        requestParameters.add(ParameterKeys.ApplicationIdentifier, APP_ID.toUpperCase());

        // Once all changes have been made a call to .setProperties() is required in order for the changes to take effect.
        // Parameters are passed within this method and added to the ChipDna Mobile status object.
        ChipDnaMobile.getInstance().setProperties(requestParameters);

    }

    protected void requestBluetoothPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            final String[] android12BluetoothPermissions = new String[] {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
            };

            if (this.activity.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    &&  this.activity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.activity, android12BluetoothPermissions, 2);
            }
        }
    }

    protected boolean isBluetoothPermissionGranted()  {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }
        return this.activity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && this.activity.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    protected boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this.activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    protected void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this.activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
    }

    @Override
    public void onRefresh(SwipeRefreshLayout swipeRefreshLayout) {
        this.refresh();
    }
}
