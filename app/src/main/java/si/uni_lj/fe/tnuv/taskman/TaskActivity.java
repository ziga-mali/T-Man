package si.uni_lj.fe.tnuv.taskman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskActivity extends AppCompatActivity {

    String taskID;
    String projectID;
    String projectIme;
    String projectOpis;
    String userID;
    String token;
    String URL;
    JSONObject requestInfo;
    TextView NalogaIme;
    TextView OpisNaloge;
    TextView KoncajDo;
    TextView ProjectIme;
    String username;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);

        Intent intent = getIntent();
        taskID = intent.getStringExtra("taskID");
        projectID = intent.getStringExtra("projectID");
        projectIme = intent.getStringExtra("projectIme");
        projectOpis = intent.getStringExtra("projectOpis");
        username = intent.getStringExtra("username");


        ProjectIme = findViewById(R.id.project_name);
        NalogaIme = findViewById(R.id.task_name);
        OpisNaloge = findViewById(R.id.task_description);
        KoncajDo = findViewById(R.id.task_finish_time);



        SharedPreferences prefs = TaskActivity.this.getSharedPreferences("TMan", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", null);
        token = prefs.getString("token", null);

        URL = getString(R.string.URL_base_storitve) + getString(R.string.projectsAPI) + projectID + getString(R.string.tasksAPI) + taskID;
        Log.d("TaskActivity L58", URL);

        requestInfo = new JSONObject();
        try {
            requestInfo.put("token", token);
            requestInfo.put("userID", userID);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        GetProjectInfo getProjectInfo = new GetProjectInfo(this);
        getProjectInfo.getProjectInfo(projectID, requestInfo, new GetProjectInfo.GetProjectInfoCallback() {
            @Override
            public void onResponse(JSONObject projectInfo) throws JSONException {
                Log.d("ProjectInfo", projectInfo.toString());
                projectIme = projectInfo.getString("ime");
                ProjectIme.setText(projectIme);
            }
            @Override
            public void onError(String error) {
                Log.e("UserInfoError", "Error: " + error);}
        });

        Log.d("TaskActivity L88", "Tukaj smo");
        useAPI apiTask = new useAPI("GET", URL, requestInfo, true);

        apiTask.uporabi(output -> {
            int responseCode = Integer.parseInt(output.getResponseCode());
            Log.d("Task L82 API Resp", "Response code: " + output.getResponseCode());

            if (responseCode == 200) {
                String ime = "";
                String opis = "";
                String koncaniCas = "";
                String koncano = "";

                JSONArray jsonResponse = output.getJsonArray();

                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject jsonObject = jsonResponse.optJSONObject(i);
                    if (jsonObject != null) {
                        ime = jsonObject.optString("ime");
                        opis = jsonObject.optString("opis");
                        koncaniCas = jsonObject.optString("kon_cas");
                        koncano = jsonObject.optString("koncano");
                    }else{
                        TaskActivity.this.runOnUiThread(() -> {
                            Toast.makeText(TaskActivity.this, "Prišlo je do napake", Toast.LENGTH_LONG).show();
                        });
                    }

                }

                NalogaIme.setText(ime);
                OpisNaloge.setText(opis);
                if(koncaniCas.equals("null")){
                    koncaniCas = "Hitro končaj";
                }
                KoncajDo.setText(koncaniCas);
                if(koncano.equals("1")){
                    KoncajDo.setText("Že končano: " + koncaniCas);
                }

            } else {
                TaskActivity.this.runOnUiThread(() -> {
                    Toast.makeText(TaskActivity.this, "Prišlo je do napake", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    public void finishTask(View v) throws JSONException {
        long currentTimeMillis = System.currentTimeMillis();
        Date currentDate = new Date(currentTimeMillis);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(currentDate);
        Log.d("Task L133", formattedDate);

        JSONObject requestInfoFinish = new JSONObject();
        requestInfoFinish.put("token", token);
        requestInfoFinish.put("userID", userID);
        requestInfoFinish.put("koncano", 1);
        requestInfoFinish.put("kon_cas", formattedDate);

        String modifiedJsonStr = requestInfoFinish.toString();
        Log.d("L148 RequestInfo", modifiedJsonStr);

        useAPI apiTaskFinish = new useAPI("PUT", URL, requestInfoFinish, true);

        apiTaskFinish.uporabi(output -> {
            int responseCode = Integer.parseInt(output.getResponseCode());
            if (responseCode == 204){
                TaskActivity.this.runOnUiThread(() -> {
                    Toast.makeText(TaskActivity.this, "Naloga je zaključena", Toast.LENGTH_LONG).show();
                });

                Intent intent = new Intent(TaskActivity.this, ProjectActivity_v2.class);
                intent.putExtra("username", username);
                intent.putExtra("projectIme",projectIme);
                intent.putExtra("projectID",projectID);
                intent.putExtra("projectOpis", projectOpis);
                intent.putExtra("startingActivity", "TaskActivity");
                startActivity(intent);

            }
        });
    }

    public void deleteTask(View v) {

        useAPI apiTaskDelete = new useAPI("DELETE", URL, requestInfo, true);

        apiTaskDelete.uporabi(output -> {
            int responseCode = Integer.parseInt(output.getResponseCode());
            if (responseCode == 204){
                TaskActivity.this.runOnUiThread(() -> {
                    Toast.makeText(TaskActivity.this, "Naloga je izbrisana", Toast.LENGTH_LONG).show();
                });

                Intent intent = new Intent(TaskActivity.this, ProjectActivity_v2.class);
                intent.putExtra("username", username);
                intent.putExtra("projectIme",projectIme);
                intent.putExtra("projectID",projectID);
                intent.putExtra("projectOpis", projectOpis);
                intent.putExtra("startingActivity", "TaskActivity");
                startActivity(intent);

            }
        });
    }
}