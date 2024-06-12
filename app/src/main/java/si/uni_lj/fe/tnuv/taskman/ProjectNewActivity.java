package si.uni_lj.fe.tnuv.taskman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.appcompat.app.AppCompatActivity;



public class ProjectNewActivity extends AppCompatActivity {

    private EditText ProjectNameInput;
    private EditText ProjectDescriptionInput;
    private EditText ProjectAccessInput;
    private TextView UsernameField;
    String username;
    String userID;
    String token;
    SharedPreferences prefs;
    JSONArray usersArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_new_activity);

        UsernameField = findViewById(R.id.username);
        ProjectNameInput = findViewById(R.id.project_name_input);
        ProjectDescriptionInput = findViewById(R.id.project_description_input);
        // ProjectAccessInput = findViewById(R.id.project_access_input);


        prefs = ProjectNewActivity.this.getSharedPreferences("TMan", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", null);
        token = prefs.getString("token", null);

    }

    @Override
    public void onStart(){
        super.onStart();

        GetUserNick getUserNick = new GetUserNick(this);
        JSONObject requestGetInfo = new JSONObject();
        try {
            requestGetInfo.put("token", token);
            requestGetInfo.put("userID", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getUserNick.getUserNick(userID, requestGetInfo, new GetUserNick.GetUserNickCallback() {
            @Override
            public void onResponse(String userInfo) {
                Log.d("UserInfo", userInfo);
                username = userInfo;
                Log.d("Project L140 ","userNick: " + username);
                UsernameField.setText(username);
            }
            @Override
            public void onError(String error) {
                Log.e("UserInfoError", "Error: " + error);}
        });

        String usersURL = this.getString(R.string.URL_base_storitve) + this.getString(R.string.usersAPI);
        useAPI api = new useAPI("GET", usersURL, requestGetInfo, true);

        api.uporabi(output -> {
            String responseCode = output.getResponseCode();
            usersArray = output.getJsonArray();
            Log.d("Userview L131 Vseb. spo", "Sporocilo: " + usersArray);
            if (responseCode.equals("200")) {
            } else {
                ProjectNewActivity.this.runOnUiThread(Toast.makeText(ProjectNewActivity.this, "Ups, nekaj je šlo narobe \n Response code: " + responseCode, Toast.LENGTH_SHORT)::show);
            }
        });

    }

    public void startAddProject(View v) {

        String projectName = ProjectNameInput.getText().toString();
        String projectDescription = ProjectDescriptionInput.getText().toString();
        //String projectAccessInput = ProjectAccessInput.getText().toString();

        if (projectName.isEmpty() || projectDescription.isEmpty() /* || projectAccessInput.isEmpty()*/) {
            Toast.makeText(this, "Prosimo izpolnite vsa polja.", Toast.LENGTH_SHORT).show();
        }

        JSONObject requestInfo = new JSONObject();
        try {
            requestInfo.put("token", token);
            requestInfo.put("userID", userID);
            requestInfo.put("ime", projectName);
            requestInfo.put("opis", projectDescription);
            // requestInfo.put("dostop", projectAccessInput);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String URL = this.getString(R.string.URL_base_storitve) + this.getString(R.string.projectsAPI);
        useAPI api = new useAPI("POST", URL, requestInfo, true);

        api.uporabi(output -> {
            String responseCode = output.getResponseCode();
            String projectURL = output.getResponseString();
            String projectID;
            String prefix = "/projects/";
            String suffix = "/tasks";
            int startIndex = projectURL.indexOf(prefix) + prefix.length();
            int endIndex = projectURL.indexOf(suffix);
            projectID = projectURL.substring(startIndex, endIndex);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("projectID", projectID);
            editor.apply();

            if (responseCode.equals("201")) {

                ProjectNewActivity.this.runOnUiThread(Toast.makeText(ProjectNewActivity.this, "Projekt uspešno ustvarjen", Toast.LENGTH_SHORT)::show);

                Intent intent = new Intent(ProjectNewActivity.this, ProjectActivity_v2.class);
                startActivity(intent);
            } else {
                ProjectNewActivity.this.runOnUiThread(Toast.makeText(ProjectNewActivity.this, "Ups, nekaj je šlo narobe \n Response code: " + responseCode, Toast.LENGTH_LONG)::show);
            }
        });

    }
    public void clearProject(View v) {
        ProjectNameInput.setText("");
        ProjectDescriptionInput.setText("");
        ProjectAccessInput.setText("");
    }
}

