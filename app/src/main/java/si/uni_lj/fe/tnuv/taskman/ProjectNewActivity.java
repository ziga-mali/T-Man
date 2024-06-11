package si.uni_lj.fe.tnuv.taskman;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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



public class ProjectNewActivity extends AppCompatActivity {

    private EditText ProjectNameInput;
    private EditText ProjectDescriptionInput;
    private EditText ProjectAccessInput;
    String username;
    String userID;
    String token;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_new_activity);

        ProjectNameInput = findViewById(R.id.project_name_input);
        ProjectDescriptionInput = findViewById(R.id.project_description_input);
        // ProjectAccessInput = findViewById(R.id.project_access_input);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        SharedPreferences prefs = ProjectNewActivity.this.getSharedPreferences("TMan", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", null);
        token = prefs.getString("token", null);

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
            Log.d("ProjectNewActivity L91", responseCode);

            if (responseCode.equals("201")) {

                ProjectNewActivity.this.runOnUiThread(() -> {
                    Toast.makeText(ProjectNewActivity.this, "Projekt uspešno ustvarjen", Toast.LENGTH_SHORT).show();
                });

                Intent intent = new Intent(ProjectNewActivity.this, ProjectActivity_v2.class);
                intent.putExtra("projectTasksURL", projectURL);
                Log.d("ProjectNewActivity L100", projectURL);
                intent.putExtra("startingActivity", "ProjectNewActivity");
                intent.putExtra("username", username);
                intent.putExtra("projectIme", projectName);
                intent.putExtra("projectOpis", projectDescription);
                startActivity(intent);
            } else {
                ProjectNewActivity.this.runOnUiThread(() -> {
                    Toast.makeText(ProjectNewActivity.this, "Ups, nekaj je šlo narobe \n Response code: " + responseCode, Toast.LENGTH_LONG).show();
                });
            }
        });

    }
    public void clearProject(View v) {
        ProjectNameInput.setText("");
        ProjectDescriptionInput.setText("");
        ProjectAccessInput.setText("");
    }
}

