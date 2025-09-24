package com.example.getitdone2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.Objects;

public class SignUpPage extends AppCompatActivity {
    EditText emailSignUp, passwordSignUp;
    Button signUp;
    String email, password;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    CustomizedActivityBars customActivityBars = new CustomizedActivityBars();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);
        customActivityBars.setCustomActivityBars(this);
        emailSignUp = findViewById(R.id.emailSignUp);
        passwordSignUp = findViewById(R.id.passwordSignUp);
        signUp = findViewById(R.id.signUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp.setClickable(false);
                createUser();
            }
        });
    }

    // Create user.
    void createUser() {
        email = emailSignUp.getText().toString();
        password = passwordSignUp.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(SignUpPage.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            signUp.setClickable(true);
        } else {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        UserCredentials.userCollection = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
                        saveLoginStatus();
                        Toast.makeText(SignUpPage.this, "Account created", Toast.LENGTH_SHORT).show();
                        goToHomepage();
                    } else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    UserCredentials.userCollection = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
                                    saveLoginStatus();
                                    Toast.makeText(SignUpPage.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    goToHomepage();
                                } else {
                                    Toast.makeText(SignUpPage.this, "Email already in use", Toast.LENGTH_SHORT).show();
                                    signUp.setClickable(true);
                                }
                            }
                        });
                    } else {
                        Toast.makeText(SignUpPage.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        signUp.setClickable(true);
                    }
                }
            });
        }
    }

    // Save login status in shared preferences.
    void saveLoginStatus() {
        SharedPreferences preferences = getSharedPreferences("loginStatus", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userCollection", UserCredentials.userCollection);
        editor.apply();
    }

    // Go to Homepage.
    void goToHomepage() {
        Intent gotoHomePage = new Intent(SignUpPage.this, HomePage.class);
        gotoHomePage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(gotoHomePage);
        finishAffinity();
    }
}