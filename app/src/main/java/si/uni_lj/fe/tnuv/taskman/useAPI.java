package si.uni_lj.fe.tnuv.taskman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.Log;

class useAPI {
    private final String method;
    private final String URL;
    private final JSONObject inputData;
    private final Boolean IdenAndAuth;

    interface APIResponseCallback {
        void onResponse(ConnectionOutput output);
    }

    public useAPI(String method, String URL, JSONObject inputData, Boolean IdenAndAuth){
        this.method = method;
        this.URL = URL;
        this.inputData = inputData;
        this.IdenAndAuth = IdenAndAuth;
    }

    public ConnectionOutput uporabi (APIResponseCallback callback) {

        JSONArray placeholderJsonArray = new JSONArray();
        String placeholderString = "";
        String placeholderResponseString = "";
        ConnectionOutput connectionOutput = new ConnectionOutput(placeholderString, placeholderJsonArray, placeholderResponseString);

        new Thread(() -> {
            try {
                java.net.URL url = new URL(URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(5000 /* milliseconds */);
                connection.setConnectTimeout(10000 /* milliseconds */);
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoInput(true);
                connection.setRequestMethod(method);
                if (IdenAndAuth) {
                    connection.setRequestProperty("Authorization", "Bearer " + inputData.getString("token"));
                    connection.setRequestProperty("Identification", "UserID " + inputData.getString("userID"));
                    inputData.remove("Authorization");
                    inputData.remove("Identification");
                }
                if (Objects.equals(method, "POST") || Objects.equals(method, "PUT")){
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(inputData.toString());
                    Log.d("useAPI L72", inputData.toString());
                    writer.flush();
                    writer.close();
                    os.close();
                }


                int responseCode = connection.getResponseCode();
                connectionOutput.setResponseCode(String.valueOf(responseCode));
                Log.d("UseAPI L81", String.valueOf(responseCode));
                if (responseCode >= 200 && responseCode < 300) {
                    int contentLength = connection.getContentLength();
                    Log.d("UseAPI L84 CONTENT_LEN", String.valueOf(contentLength));
                    if (contentLength > 0) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        try{
                            JSONArray jsonResponse = new JSONArray(response.toString());
                            connectionOutput.setJsonArray(jsonResponse);
                            connectionOutput.setResponseString(null);
                            Log.d("UseAPI L97 JSN response", String.valueOf(jsonResponse));
                        } catch (JSONException e) {
                            connectionOutput.setJsonArray(null); // Set JSON array to null
                            Log.d("UseAPI L99 Str resp", response.toString());
                            connectionOutput.setResponseString(response.toString());
                            if (response.toString().startsWith("\"http")){
                                String cleanedResponse = response.toString().replace("\\/","/").replace("\"","");
                                connectionOutput.setResponseString(cleanedResponse);
                                Log.d("UseAPI L103 Str URL res", cleanedResponse);
                            }
                        }
                    }
                    if (callback != null) {
                        callback.onResponse(connectionOutput);
                    }
                }else{
                    if (callback != null) {
                        Log.d("UseAPI L111", "Response code: " + connectionOutput.getResponseCode());
                        callback.onResponse(connectionOutput);
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.d("UseAPI L119", "Response code: " + connectionOutput.getResponseCode());
            }
        }).start();
        return connectionOutput;
    }
}