package si.uni_lj.fe.tnuv.taskman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @noinspection ALL*/
public class UserviewActivity extends AppCompatActivity {

    private ListView lv;
    private JSONArray projectsArray;
    private List<Map<String,String>> userProgram;
    private SimpleAdapter adapter;
    String userID = null;
    String token = null;
    JSONObject requestInfo;
    String URL;
    String username;
    TextView UsernameField;
    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userview_activity);

        userProgram = new ArrayList<>();

        lv = findViewById(R.id.program_list);
        UsernameField = findViewById(R.id.username);

        prefs = UserviewActivity.this.getSharedPreferences("TMan", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", null);
        token = prefs.getString("token", null);

        URL = getString(R.string.URL_base_storitve) + getString(R.string.userviewAPI) + userID;
        projectsArray = new JSONArray();
        requestInfo = new JSONObject();
        try {
            requestInfo.put("token", token);
            requestInfo.put("userID", userID);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        lv.setOnItemClickListener((adapterView, view, i, l) ->{
            try {

                JSONObject project = projectsArray.getJSONObject(i);
                String projectID = project.getString("id");

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("projectID", projectID);
                editor.apply();

                Intent intent = new Intent(UserviewActivity.this, ProjectActivity_v2.class);
                startActivity(intent);


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void populateListView() {
        userProgram.clear();
        for (int i = 0; i < projectsArray.length(); i++) {
            try {
                JSONObject project = projectsArray.getJSONObject(i);
                Map<String, String> item = new HashMap<>();
                item.put("name", project.getString("ime"));
                item.put("description", project.getString("opis"));
                userProgram.add(item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        GetUserNick getUserNick = new GetUserNick(this);
        getUserNick.getUserNick(userID, requestInfo, new GetUserNick.GetUserNickCallback() {
            @Override
            public void onResponse(String userInfo) {
                username = userInfo;
                UsernameField.setText(username);
            }
            @Override
            public void onError(String error) {
                Log.e("UserInfoError", "Error: " + error);}
        });

        useAPI apiUserview = new useAPI("GET", URL, requestInfo, true);

        apiUserview.uporabi(output -> {
            int responseCode = Integer.parseInt(output.getResponseCode());
            if(responseCode == 200){
                projectsArray = output.getJsonArray();
                UserviewActivity.this.runOnUiThread(() -> {populateListView();
                });
            }else{
                UserviewActivity.this.runOnUiThread(Toast.makeText(UserviewActivity.this, "Response code: " + output.getResponseCode(), Toast.LENGTH_LONG)::show);
            }
        });

        adapter = new SimpleAdapter(
                this,
                userProgram,
                R.layout.list_item_layout,
                new String[]{"name", "description"},
                new int[]{R.id.projectTask, R.id.projectTaskDescription}
        );
        lv.setAdapter(adapter);

    }

    public void startProjectNewActivity(View v) {
        Intent intent = new Intent(UserviewActivity.this, ProjectNewActivity.class);
        startActivity(intent);
    }

    public void startLogoutActivity(View v) {
        SharedPreferences prefs = getSharedPreferences("TMan", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(UserviewActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}

