package com.android.montelongoworldwide.pages;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class Transaction {
    public final int amount;
    public final String id, type, last4;
    public int marketId, eventId, eventDatetimeId, userId;
    public String packageId;

    public Transaction(int amount, String id, String type, String last4) {
        this.amount = amount;
        this.id = id;
        this.type = id;
        this.last4 = id;
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
