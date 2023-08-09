package com.android.montelongoworldwide;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class Utils
{
//    public static String APP_URL = "https://montelongoworldwide.net";
    public static String APP_URL = "https://18c3-2600-1700-5005-3120-3d89-dbad-f612-e1c4.ngrok-free.app";
    @SuppressLint("DefaultLocale")
    public static String formatAmount(int amount)
    {
        return String.format("$%,.2f", amount / 100.0);
    }


    @SuppressLint("SimpleDateFormat")
    public static String formatDate(String dateString) {
        SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat targetDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

        try {
            return targetDateFormat.format(Objects.requireNonNull(sourceDateFormat.parse(dateString)));
        } catch (ParseException e) {
            e.printStackTrace();
            return ""; // Return an empty string in case of parsing error
        }
    }

    public static String url(String uri)
    {
        return Utils.APP_URL + "/api/v1/" + uri;
    }

    public static class AlertAction {
        public final String text;
        public final DialogInterface.OnClickListener callback;

        public AlertAction(String text, DialogInterface.OnClickListener callback) {
            this.text = text;
            this.callback = callback;
        }
    }

    public static void runAfter(Runnable r, long delayMillis) {
        new Handler(Looper.getMainLooper()).postDelayed(r, delayMillis);
    }

    public static AlertDialog alert(
            Context context,
            String title,
            String message,
            AlertAction okAction,
            AlertAction cancelAction
    )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        if (okAction != null) {
            builder.setPositiveButton(okAction.text, okAction.callback);
        }

        if (cancelAction != null) {
            builder.setNegativeButton(cancelAction.text, cancelAction.callback);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    public static AlertDialog alert(Context context, String title, String message) {
        return alert(context, title, message, new AlertAction("OK", (dialog, which) -> dialog.dismiss()), null);
    }

    public static AlertDialog alert(AlertDialog dialog, String title, String message, boolean isStatic)
    {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();

        Button okBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (okBtn != null) okBtn.setEnabled(!isStatic);
        if (cancelBtn != null) cancelBtn.setEnabled(!isStatic);

        return dialog;
    }


    public static AlertDialog alert(AlertDialog dialog, String title, String message)
    {
        return alert(dialog, title, message, false);
    }
}
