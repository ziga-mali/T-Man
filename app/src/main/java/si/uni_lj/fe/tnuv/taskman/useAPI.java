package si.uni_lj.fe.tnuv.taskman;

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
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** @noinspection ALL*/
class useAPI {
    private final String method;
    private final String URL;
    private final JSONObject inputData;
    private final Boolean IdenAndAuth;

    interface APIResponseCallback {
        void onResponse(ConnectionOutput output) throws JSONException;
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
                }
                if (Objects.equals(method, "POST") || Objects.equals(method, "PUT")){
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                    writer.write(inputData.toString());
                    writer.flush();
                    writer.close();
                    os.close();
                }


                int responseCode = connection.getResponseCode();
                connectionOutput.setResponseCode(String.valueOf(responseCode));
                if (responseCode >= 200 && responseCode < 300) {
                    int contentLength = connection.getContentLength();
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
                        } catch (JSONException e) {
                            connectionOutput.setJsonArray(null);
                            connectionOutput.setResponseString(response.toString());
                            if (response.toString().startsWith("\"http")){
                                String cleanedResponse = response.toString().replace("\\/","/").replace("\"","");
                                connectionOutput.setResponseString(cleanedResponse);
                            }
                        }
                    }
                    if (callback != null) {
                        callback.onResponse(connectionOutput);
                    }
                }else{
                    if (callback != null) {
                        callback.onResponse(connectionOutput);
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
        return connectionOutput;
    }
}