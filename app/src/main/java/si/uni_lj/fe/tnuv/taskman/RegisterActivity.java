package si.uni_lj.fe.tnuv.taskman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private EditText Username;
    private EditText Name;
    private EditText Surname;
    private EditText Email;
    private EditText NewPass;
    private EditText ConfirmPass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        Username = findViewById(R.id.Username);
        Name = findViewById(R.id.Name);
        Surname = findViewById(R.id.Surname);
        Email = findViewById(R.id.Email);
        NewPass = findViewById(R.id.NewPassword);
        ConfirmPass = findViewById(R.id.ConfirmPassword);


    }


    public void startAddUser(View v) throws JSONException {

        String username = Username.getText().toString();
        String name = Name.getText().toString();
        String surname = Surname.getText().toString();
        String email = Email.getText().toString();
        String newPass = NewPass.getText().toString();
        String confirmPass = ConfirmPass.getText().toString();

        if (username.isEmpty() || name.isEmpty() || surname.isEmpty() || email.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Prosimo izpolnite vsa polja.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Vnešeni gesli se ne ujemata.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newPassHash = hashCalculation.calculateMD5(newPass);

        JSONObject registerData = new JSONObject();
        try {
            registerData.put("vzdevek", username);
            registerData.put("geslo", newPassHash);
            registerData.put("ime", name);
            registerData.put("priimek", surname);
            registerData.put("mail", email);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String URL = this.getString(R.string.URL_base_storitve) + this.getString(R.string.registerAPI);
        useAPI api = new useAPI("POST", URL, registerData, false);

        api.uporabi(output -> {
                String responseCode = output.getResponseCode();

                // Handle the response according to your needs
                if (responseCode.equals("201")) {

                    RegisterActivity.this.runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Uspešna registracija", Toast.LENGTH_LONG).show();
                    });

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    RegisterActivity.this.runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Ups, nekaj je šlo narobe", Toast.LENGTH_LONG).show();
                    });
                }
        });
    }

    public void startLoginActivity (View v){
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}