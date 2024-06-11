package si.uni_lj.fe.tnuv.taskman;

import org.json.JSONObject;

public class getUserInfo {
    private final String method = "GET";
    private final String URL = "";
    private final JSONObject inputData = new JSONObject();
    private final Boolean IdenAndAuth = true;

    interface APIResponseCallback {
        void onResponse(connectionOutput output);
    }
}
