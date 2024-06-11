package si.uni_lj.fe.tnuv.taskman;

import org.json.JSONArray;

public class ConnectionOutput {
    private String responseCode;
    private JSONArray jsonArray;
    private String responseString;

    public ConnectionOutput(String string, JSONArray jsonArray, String responseString){
        this.responseCode = string;
        this.jsonArray = jsonArray;
        this.responseString = responseString;
    }

    public String getResponseCode(){
        return responseCode;
    }

    public JSONArray getJsonArray(){
        return jsonArray;
    }
    public String getResponseString(){
        return responseString;
    }
    public void setResponseCode(String responseCode){
        this.responseCode = responseCode;
    }
    public void setJsonArray(JSONArray jsonArray){
    this.jsonArray = jsonArray;
    }
    public void setResponseString(String responseString){
        this.responseString = responseString;
    }

}
