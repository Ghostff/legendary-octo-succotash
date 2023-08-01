package com.android.montelongoworldwide;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class Utils
{
//    public static String APP_URL = "https://montelongoworldwide.net";
    public static String APP_URL = "https://ad6b-2600-1700-5005-3120-519a-1062-73e8-29a9.ngrok-free.app";
    @SuppressLint("DefaultLocale")
    public static String formatAmount(int amount)
    {
        return String.format("$%,.2f", amount / 100.0);
    }

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
}
