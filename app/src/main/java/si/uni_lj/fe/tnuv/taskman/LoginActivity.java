package si.uni_lj.fe.tnuv.taskman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    private EditText Username;
    private EditText Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        Username = findViewById(R.id.Username);
        Password = findViewById(R.id.Password);
    }

    public void startUserviewActivity(View v) {
        String username = Username.getText().toString();
        String password = Password.getText().toString();
        String passwordHash = HashCalculation.calculateMD5(password);

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vnesite tako uporabniško ime kot geslo", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject loginData = new JSONObject();
        try {
            loginData.put("vzdevek", username);
            loginData.put("geslo", passwordHash);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        useAPI apiLogin = new useAPI("POST", getString(R.string.URL_base_storitve) + getString(R.string.loginAPI),
                loginData, false);

        apiLogin.uporabi(output -> {
            int responseCode = Integer.parseInt(output.getResponseCode());
            if (responseCode == 200) {
                JSONArray jsonResponse = output.getJsonArray();

                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject jsonObject = jsonResponse.optJSONObject(i);
                    if (jsonObject != null) {
                        String token = jsonObject.optString("token");
                        String userID = jsonObject.optString("user_id");

                        SharedPreferences prefs = LoginActivity.this.getSharedPreferences("TMan", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("token", token);
                        editor.putString("userID", userID);
                        editor.putString("username", username);
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, UserviewActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    }
                }
            } else {
                LoginActivity.this.runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Napačno vnešeno ime ali geslo", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    public void startRegisterActivity(View v) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}