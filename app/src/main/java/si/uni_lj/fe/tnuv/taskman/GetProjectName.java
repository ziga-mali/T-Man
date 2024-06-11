package si.uni_lj.fe.tnuv.taskman;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;

public class GetProjectName {

    private final Context context;

    public GetProjectName(Context context) {
        this.context = context;
    }

    public interface GetProjectNameCallback {
        void onResponse(String userNick);
        void onError(String error);
    }

    public void getProjectName(int userID, JSONObject idenAuth, GetProjectNameCallback callback) {
        String URL = context.getString(R.string.URL_base_storitve) + context.getString(R.string.projectsAPI) + userID;
        useAPI apiRequest = new useAPI("GET", URL, idenAuth, true);
        apiRequest.uporabi(output -> {
            try {
                if (output.getResponseCode().startsWith("2")) { // Check for successful response code (2xx)
                    if (output.getJsonArray() != null) {
                        JSONObject projectNameJSON = output.getJsonArray().getJSONObject(0);
                        String userNick = projectNameJSON.toString();
                        callback.onResponse(userNick);
                    } else if (output.getResponseString() != null) {
                        callback.onResponse(output.getResponseString());
                    } else {
                        callback.onError("Empty response from server");
                    }
                } else {
                    callback.onError("Server returned response code: " + output.getResponseCode());
                }
            } catch (JSONException e) {
                callback.onError("Error parsing response: " + e.getMessage());
            }
        });
    }
}
