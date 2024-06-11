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
import java.util.Objects;

public class ProjectActivity extends AppCompatActivity {

    private ListView lv;
    private List<Map<String,String>> programTasks;
    private JSONArray tasksArray;

    String userID = null;
    String token = null;
    JSONObject requestInfo;
    private SimpleAdapter adapter;
    String URL;
    String projectID;
    String username;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_activity_v2);

        programTasks = new ArrayList<>();
        lv = findViewById(R.id.task_list);
        Intent startingActivity = getIntent();
        String startActivity = startingActivity.getStringExtra("startingActivity");
        username = startingActivity.getStringExtra("username");
        String projectIme = startingActivity.getStringExtra("projectIme");
        String projectOpis = startingActivity.getStringExtra("projectOpis");

        TextView UsernameField = findViewById(R.id.username);
        UsernameField.setText(username);
        TextView ProjectImeField = findViewById(R.id.project_name);
        ProjectImeField.setText(projectIme);
        TextView ProjectOpisField = findViewById(R.id.project_description);
        ProjectOpisField.setText(projectOpis);

        if (Objects.equals(startActivity, "UserviewActivity") || Objects.equals(startActivity, "TaskActivity")){

            projectID = startingActivity.getStringExtra("projectID");
            URL = getString(R.string.URL_base_storitve) + getString(R.string.projectsAPI) + projectID + getString(R.string.tasksAPI);
            Log.d("ProjectAct L76", URL);
            Log.d("ProjectAct L77", projectID);

        } else if (Objects.equals(startActivity, "ProjectNewActivity")) {

            URL = startingActivity.getStringExtra("projectTasksURL");
            String prefix = "/projects/";
            String suffix = "/tasks";
            int startIndex = URL.indexOf(prefix) + prefix.length();
            int endIndex = URL.indexOf(suffix);
            projectID = URL.substring(startIndex, endIndex);
            Log.d("ProjectAct L87", URL);
            Log.d("ProjectAct L88", projectID);

        } else {
            Log.d("ProjectAct L91", "Napaka pri URL");
        }

        SharedPreferences prefs = ProjectActivity.this.getSharedPreferences("TMan", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", null);
        token = prefs.getString("token", null);
        Log.d("ProjectAct L97", URL);
        tasksArray = new JSONArray();
        requestInfo = new JSONObject();
        try {
            requestInfo.put("token", token);
            requestInfo.put("userID", userID);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        lv.setOnItemClickListener((adapterView, view, i, l) ->{
            try {
                JSONObject task = tasksArray.getJSONObject(i);
                String taskID = task.getString("id");

                Intent intent = new Intent(ProjectActivity.this, TaskActivity.class);
                intent.putExtra("taskID", taskID);
                intent.putExtra("projectID", projectID);
                intent.putExtra("projectIme", projectIme);
                intent.putExtra("username", username);
                startActivity(intent);


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void populateListView() {
        programTasks.clear();
        for (int i = 0; i < tasksArray.length(); i++) {
            try {
                JSONObject task = tasksArray.getJSONObject(i);
                Map<String, String> item = new HashMap<>();
                item.put("name", task.getString("ime"));
                item.put("description", task.getString("opis"));
                programTasks.add(item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d("ProjectAct L145", "Tukaj smo");
        useAPI apiProject = new useAPI("GET", URL, requestInfo, true);
        apiProject.uporabi(output -> {
            Log.d("Project L147 API Resp", "Response code: " + output.getResponseCode());
            int responseCode = Integer.parseInt(output.getResponseCode());
            if(responseCode == 200){
                tasksArray = output.getJsonArray();
                ProjectActivity.this.runOnUiThread(() -> {
                    Log.d("ProjAct L143", "Tukaj smo");
                    populateListView();
                });
                Log.d("ProjAct L146 Vseb spor", "Sporocilo: " + tasksArray);
            }else{
                ProjectActivity.this.runOnUiThread(() -> {
                    Toast.makeText(ProjectActivity.this, "Response code: " + output.getResponseCode(), Toast.LENGTH_LONG).show();
                });
            }
        });


        adapter = new SimpleAdapter(
                this,
                programTasks,
                R.layout.list_item_layout,
                new String[]{"name", "description"},
                new int[]{R.id.projectTask, R.id.projectTaskDescription}
        );
        lv.setAdapter(adapter);
    }

    public void startTaskNewActivity(View v) {
        Intent intent = new Intent(ProjectActivity.this, TaskNewActivity.class);
        intent.putExtra("projectID", projectID);
        startActivity(intent);
    }

    public void removeProjectActivity(View v) {
        URL = getString(R.string.URL_base_storitve) + getString(R.string.projectsAPI) + projectID;
        useAPI projectDelete = new useAPI("DELETE", URL, requestInfo,true);
        projectDelete.uporabi(output -> {
            Log.d("Project L192 API Resp", "Response code: " + output.getResponseCode());
            int responseCode = Integer.parseInt(output.getResponseCode());
            if(responseCode == 204){
                ProjectActivity.this.runOnUiThread(() -> {
                    Toast.makeText(ProjectActivity.this, "Projekt uspešno izbrisan", Toast.LENGTH_LONG).show();
                });

                Intent intent = new Intent(ProjectActivity.this, UserviewActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }else{
                ProjectActivity.this.runOnUiThread(() -> {
                    Toast.makeText(ProjectActivity.this, "Response code: " + output.getResponseCode(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}