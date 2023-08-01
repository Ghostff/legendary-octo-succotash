package com.android.montelongoworldwide;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class JsonRequest<T> {
    private final String url;
    private final String bearerToken;

    public JsonRequest(String url, String bearerToken) {
        this.url = url;
        this.bearerToken = bearerToken;
    }

    public void dispatch() {
        new Thread(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) apiUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                if (this.bearerToken != null && !this.bearerToken.isEmpty()) {
                    urlConnection.setRequestProperty("Authorization", "Bearer " + this.bearerToken);
                }

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = readResponse(urlConnection.getInputStream());
                    try {
                        T result = parseResponse(response);
                        handleSuccess(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        handleError("Error parsing response"); // UI update handled on the main thread
                    }
                } else {
                    handleError("Error response code: " + responseCode); // UI update handled on the main thread
                }

                urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                handleError("Error fetching data from the server"); // UI update handled on the main thread
            }
        }).start();
    }

    private String readResponse(InputStream inputStream) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }
        return response.toString();
    }

    private T parseResponse(String response) throws JSONException {
        try {
            // Attempt to parse the response as a JSON Object
            return (T) new JSONObject(response);
        } catch (JSONException e) {
            // If parsing as JSON Object fails, try parsing as a JSON Array
            return (T) new JSONArray(response);
        }
    }

    private void handleSuccess(final T result) {
        // Execute on the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                onSuccess(result);
            }
        });
    }

    private void handleError(final String errorMessage) {
        // Execute on the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                onError(errorMessage);
            }
        });
    }

    public abstract void onSuccess(T result);
    public abstract void onError(String errorMessage);
}
