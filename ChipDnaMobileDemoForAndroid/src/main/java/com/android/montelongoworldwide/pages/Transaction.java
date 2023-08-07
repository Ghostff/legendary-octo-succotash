package com.android.montelongoworldwide.pages;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class Transaction {
    public final int amount;

    public String id;
    public String type;
    public String last4;
    public final String packageId;
    public final int marketId;
    public final int eventId;
    public final int eventDatetimeId;
    public final int userId;

    public Transaction(int amount, int marketId, int eventId, int eventDatetimeId, int userId, String packageId) {
        this.amount = amount;
        this.marketId = marketId;
        this.eventId = eventId;
        this.eventDatetimeId = eventDatetimeId;
        this.userId = userId;
        this.packageId = packageId;
    }


    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAccessible()) continue;
            try {
                jsonObject.put(field.getName(), field.get(this));
            } catch (JSONException|IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return jsonObject;
    }
}
