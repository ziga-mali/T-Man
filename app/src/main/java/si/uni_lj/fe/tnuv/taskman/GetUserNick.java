package si.uni_lj.fe.tnuv.taskman;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.util.Log;

public class GetUserNick {

    private final Context context;

    public GetUserNick(Context context) {
        this.context = context;
    }

    public interface GetUserNickCallback {
        void onResponse(String userNick);
        void onError(String error);
    }

    public void getUserNick(String userID, JSONObject idenAuth, GetUserNickCallback callback) {
        String URL = context.getString(R.string.URL_base_storitve) + context.getString(R.string.usersAPI) + userID;
        useAPI apiRequest = new useAPI("GET", URL, idenAuth, true);
        apiRequest.uporabi(output -> {
            try {
                if (output != null) {
                    Log.d("Tag", output.getResponseString());
                    JSONObject userNickJSON = new JSONObject(output.getResponseString());
                    String userNick = userNickJSON.getString("vzdevek");
                    callback.onResponse(userNick);
                } else {
                    callback.onError("Empty response from server");
                }
            } catch (JSONException e) {
                callback.onError("Error parsing response: " + e.getMessage());
            }
        });
    }
}
