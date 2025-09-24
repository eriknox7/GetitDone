package com.example.getitdone2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Objects;

public class LoginPage extends AppCompatActivity {
    EditText loginEmail, loginPassword;
    TextView goToSignUp;
    Button login;
    String email, password;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    CustomizedActivityBars customActivityBars = new CustomizedActivityBars();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        customActivityBars.setCustomActivityBars(this);
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        login = findViewById(R.id.login);
        goToSignUp = findViewById(R.id.goToSignUp);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setClickable(false);
                authenticate();
            }
        });
    }

    // Login.
    void authenticate() {
        email = loginEmail.getText().toString();
        password = loginPassword.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginPage.this,"Something went wrong", Toast.LENGTH_SHORT).show();
            login.setClickable(true);
        } else {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        UserCredentials.userCollection = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
                        saveLoginStatus();
                        Toast.makeText(LoginPage.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginPage.this, HomePage.class));
                        finish();
                    } else {
                        Toast.makeText(LoginPage.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        login.setClickable(true);
                    }
                }
            });
        }
    }

    // Go to sign up page.
    public void goToSignUpPage(View view) {
        startActivity(new Intent(LoginPage.this, SignUpPage.class));
    }
    // Save login status in shared preferences.
    void saveLoginStatus() {
        SharedPreferences preferences = getSharedPreferences("loginStatus", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userCollection", UserCredentials.userCollection);
        editor.apply();
    }
}