package com.android.montelongoworldwide;

import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static java.net.URLDecoder.decode;

public abstract class Request<T> {
    private final String url;
    private final String bearerToken;
    private final String contentType;
    private final String method;

    public Request(String url, String bearerToken) {
        this(url, "GET", "application/json", bearerToken);
    }

    public Request(String url) {
        this(url, "POST", "application/x-www-form-urlencoded; charset=UTF-8", null);
    }

    public Request(String url, String method, String contentType, String bearerToken) {
        this.url = url;
        this.method = method;
        this.contentType = contentType;
        this.bearerToken = bearerToken;
    }

    public void get() {
        new Thread(() -> {
            try {
                HttpURLConnection conn = getHttpURLConnection();
                extracted(conn);
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                handleError("Error fetching data from the server"); // UI update handled on the main thread
            }
        }).start();
    }

    public void post(HashMap<String, String> formData)
    {
        new Thread(() -> {
            try {
                HttpURLConnection conn = getHttpURLConnection();
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(encodeParams(formData).getBytes());
                }
                extracted(conn);
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                handleError("Error fetching data from the server"); // UI update handled on the main thread
            }
        }).start();
    }

    @NotNull
    private HttpURLConnection getHttpURLConnection() throws IOException {
        URL apiUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", contentType);
        if (this.bearerToken != null && !this.bearerToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + this.bearerToken);
        }

        return conn;
    }

    private void extracted(HttpURLConnection conn) throws IOException
    {
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try {
                T result = onResponse(readResponse(conn.getInputStream()));
                handleSuccess(result);
            } catch (JSONException e) {
                e.printStackTrace();
                handleError("Error parsing response"); // UI update handled on the main thread
            }
        } else {
            handleError("Error response code: " + responseCode); // UI update handled on the main thread
        }
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

    private void handleSuccess(final T result) {
        // Execute on the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> onSuccess(result));
    }

    private void handleError(final String errorMessage) {
        // Execute on the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> onError(errorMessage));
    }

    public abstract void onSuccess(T result);
    public abstract void onError(String errorMessage);

    public static String encodeParams(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return builder.toString();
    }

    public T onResponse(String response) throws JSONException, UnsupportedEncodingException {
        try {
            // Attempt to parse the response as a JSON Object
            return (T) new JSONObject(response);
        } catch (JSONException e) {
            // If parsing as JSON Object fails, try parsing as a JSON Array
            return (T) new JSONArray(response);
        }
    }

    public static Map<String, String> decodeQueryString(String encodedParams) throws UnsupportedEncodingException {
        Map<String, String> decodedParams = new HashMap<>();
        for (String param : encodedParams.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                decodedParams.put(decode(keyValue[0], "UTF-8"), decode(keyValue[1], "UTF-8"));
            }
        }
        return decodedParams;
    }
}
