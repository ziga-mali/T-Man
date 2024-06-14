package si.uni_lj.fe.tnuv.taskman;

import android.annotation.SuppressLint;
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
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/** @noinspection ALL*/
public class ProjectNewActivity extends AppCompatActivity {

    private EditText ProjectNameInput;
    private EditText ProjectDescriptionInput;
    private TextView UsernameField;
    private UserAdapter userAdapter;
    private final List<User> userList = new ArrayList<>();

    String username;
    String userID;
    String token;
    SharedPreferences prefs;
    JSONArray usersArrayRaw;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_new_activity);

        UsernameField = findViewById(R.id.username);
        ProjectNameInput = findViewById(R.id.project_name_input);
        ProjectDescriptionInput = findViewById(R.id.project_description_input);
        RecyclerView projectAccessList = findViewById(R.id.project_access_list);
        projectAccessList.setLayoutManager(new LinearLayoutManager(this));

        prefs = ProjectNewActivity.this.getSharedPreferences("TMan", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", null);
        token = prefs.getString("token", null);

        userAdapter = new UserAdapter(userList, this);
        projectAccessList.setAdapter(userAdapter);

    }

    @SuppressLint("NotifyDataSetChanged")
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
                username = userInfo;
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
            if (responseCode.equals("200")) {
                usersArrayRaw = output.getJsonArray();
                for (int i = 0; i < usersArrayRaw.length(); i++) {
                    JSONObject rawObject = usersArrayRaw.getJSONObject(i);
                    String id = rawObject.getString("id");
                    String username = rawObject.getString("vzdevek");

                    if (!id.equals(userID)) {
                        userList.add(new User(id, username));
                    }
                }
                userAdapter.notifyDataSetChanged();
            } else {
                ProjectNewActivity.this.runOnUiThread(Toast.makeText(ProjectNewActivity.this, "Ups, nekaj je šlo narobe \n Response code: " + responseCode, Toast.LENGTH_SHORT)::show);
            }
        });
    }

    public void startAddProject(View v) {

        String projectName = ProjectNameInput.getText().toString();
        String projectDescription = ProjectDescriptionInput.getText().toString();
        JSONArray accessArray = new JSONArray();

        if (projectName.isEmpty() || projectDescription.isEmpty()) {
            Toast.makeText(this, "Prosimo izpolnite vsa polja.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (User user : userList) {
            if (user.isSelected()) {
                accessArray.put(user.getId());
            }
        }

        JSONObject requestInfo = new JSONObject();
        try {
            requestInfo.put("token", token);
            requestInfo.put("userID", userID);
            requestInfo.put("ime", projectName);
            requestInfo.put("opis", projectDescription);
            requestInfo.put("dostop", accessArray);
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
                ProjectNewActivity.this.runOnUiThread(() -> {

                    Toast.makeText(ProjectNewActivity.this, "Projekt uspešno ustvarjen", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProjectNewActivity.this, ProjectActivity_v2.class);
                    startActivity(intent);
                });
            } else {
                ProjectNewActivity.this.runOnUiThread(Toast.makeText(ProjectNewActivity.this, "Ups, nekaj je šlo narobe \n Response code: " + responseCode, Toast.LENGTH_LONG)::show);
            }
        });

    }
    @SuppressLint("NotifyDataSetChanged")
    public void clearProject(View v) {
        ProjectNameInput.setText("");
        ProjectDescriptionInput.setText("");
        for (User user : userList) {
            user.setSelected(false);
        }
        userAdapter.notifyDataSetChanged();
    }
}

