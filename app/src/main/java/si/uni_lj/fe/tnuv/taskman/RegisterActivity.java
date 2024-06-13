package si.uni_lj.fe.tnuv.taskman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.appcompat.app.AppCompatActivity;

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


    public void startAddUser(View v) {

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

        String newPassHash = HashCalculation.calculateMD5(newPass);

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

                if (responseCode.equals("201")) {

                    RegisterActivity.this.runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Uspešna registracija", Toast.LENGTH_LONG).show());

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    RegisterActivity.this.runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Ups, nekaj je šlo narobe", Toast.LENGTH_LONG).show());
                }
        });
    }

    public void startLoginActivity (View v){
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}