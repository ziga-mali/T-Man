package si.uni_lj.fe.tnuv.taskman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskActivity extends AppCompatActivity {

    String taskID;
    String projectID;
    String projectIme;
    String userID;
    String token;
    String URL;
    JSONObject requestInfo;
    TextView NalogaIme;
    TextView OpisNaloge;
    TextView KoncajDo;
    TextView ProjectIme;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);

        ProjectIme = findViewById(R.id.project_name);
        NalogaIme = findViewById(R.id.task_name);
        OpisNaloge = findViewById(R.id.task_description);
        KoncajDo = findViewById(R.id.task_finish_time);

        Intent intent = getIntent();
        taskID = intent.getStringExtra("taskID");

        SharedPreferences prefs = TaskActivity.this.getSharedPreferences("TMan", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", null);
        token = prefs.getString("token", null);
        projectID = prefs.getString("projectID", null);

        URL = getString(R.string.URL_base_storitve) + getString(R.string.projectsAPI) + projectID + getString(R.string.tasksAPI) + taskID;

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
                projectIme = projectInfo.getString("ime");
                ProjectIme.setText(projectIme);
            }
            @Override
            public void onError(String error) {
                Log.e("UserInfoError", "Error: " + error);}
        });
        useAPI apiTask = new useAPI("GET", URL, requestInfo, true);

        apiTask.uporabi(output -> {
            int responseCode = Integer.parseInt(output.getResponseCode());

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
                        TaskActivity.this.runOnUiThread(Toast.makeText(TaskActivity.this, "Prišlo je do napake", Toast.LENGTH_SHORT)::show);
                    }

                }

                NalogaIme.setText(ime);
                OpisNaloge.setText(opis);
                if(koncaniCas.equals("null")){
                    koncaniCas = "Hitro končaj";
                }
                KoncajDo.setText(koncaniCas);
                if(koncano.equals("1")){
                    String koncanoText = getString(R.string.ze_koncano);
                    KoncajDo.setText(koncanoText + koncaniCas);
                }

            } else {
                TaskActivity.this.runOnUiThread(() -> {
                    Toast.makeText(TaskActivity.this, "Ups, nekaj je šlo narobe \n Response code: " + responseCode, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void finishTask(View v) throws JSONException {
        long currentTimeMillis = System.currentTimeMillis();
        useAPI apiTaskFinish = getUseAPI(currentTimeMillis);

        apiTaskFinish.uporabi(output -> {
            int responseCode = Integer.parseInt(output.getResponseCode());
            if (responseCode == 204){
                TaskActivity.this.runOnUiThread(Toast.makeText(TaskActivity.this, "Naloga je zaključena", Toast.LENGTH_LONG)::show);

                Intent intent = new Intent(TaskActivity.this, ProjectActivity_v2.class);
                startActivity(intent);

            }
        });
    }

    @NonNull
    private useAPI getUseAPI(long currentTimeMillis) throws JSONException {
        Date currentDate = new Date(currentTimeMillis);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(currentDate);

        JSONObject requestInfoFinish = new JSONObject();
        requestInfoFinish.put("token", token);
        requestInfoFinish.put("userID", userID);
        requestInfoFinish.put("koncano", 1);
        requestInfoFinish.put("kon_cas", formattedDate);

        return new useAPI("PUT", URL, requestInfoFinish, true);
    }

    public void deleteTask(View v) {

        useAPI apiTaskDelete = new useAPI("DELETE", URL, requestInfo, true);

        apiTaskDelete.uporabi(output -> {
            int responseCode = Integer.parseInt(output.getResponseCode());
            if (responseCode == 204){

                String sporocilo = "Naloga je izbrisana";
                TaskActivity.this.runOnUiThread(()->{
                        Toast.makeText(TaskActivity.this, sporocilo, Toast.LENGTH_SHORT).show();
                });

                Intent intent = new Intent(TaskActivity.this, ProjectActivity_v2.class);
                startActivity(intent);

            }
        });
    }
}