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
        void onResponse(JSONObject projectInfo);
        void onError(String error);
    }

    public void getProjectName(String projectID, JSONObject idenAuth, GetProjectNameCallback callback) {
        String URL = context.getString(R.string.URL_base_storitve) + context.getString(R.string.projectsAPI) + projectID;
        useAPI apiRequest = new useAPI("GET", URL, idenAuth, true);
        apiRequest.uporabi(output -> {
            try {
                if (output.getResponseCode().startsWith("2")) { // Check for successful response code (2xx)
                    if (output.getJsonArray() != null) {
                        JSONObject projectNameJSON = output.getJsonArray().getJSONObject(0);
                        String ime = projectNameJSON.getString("ime");
                        String opis = projectNameJSON.getString("opis");

                        JSONObject result = new JSONObject();
                        result.put("ime", ime);
                        result.put("opis", opis);

                        callback.onResponse(result);
                    } else if (output.getResponseString() != null) {
                        callback.onResponse(new JSONObject().put("response", output.getResponseString()));
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
