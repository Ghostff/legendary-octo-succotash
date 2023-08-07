package com.android.montelongoworldwide;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Objects;

public class Utils
{
//    public static String APP_URL = "https://montelongoworldwide.net";
    public static String APP_URL = "https://dac6-2600-1700-5005-3120-c9b1-eef5-55c0-5c65.ngrok-free.app";
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

    public static AlertDialog alert(
            Context context,
            String title,
            String message,
            final DialogInterface.OnClickListener onOk,
            final DialogInterface.OnClickListener onCancel
    )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("OK", onOk);
        if (onCancel != null) {
            builder.setNegativeButton("Cancel", onCancel);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    public static AlertDialog alert(Context context, String title, String message) {
        return alert(context, title, message, (dialog, which) -> dialog.dismiss(), null);
    }

    public static AlertDialog alert(AlertDialog dialog, String title, String message)
    {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();

        return dialog;
    }
}
