package com.example.getitdone;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.Objects;

public class SignUpPage extends AppCompatActivity {
    EditText emailSignUp, passwordSignUp;
    Button signUp;
    LinearLayout googleSignUp;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    GoogleSignInClient googleSignInClient;
    CustomizedActivityBars customActivityBars = new CustomizedActivityBars();

    ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    Snackbar.make(findViewById(android.R.id.content), "Google sign-in failed", Snackbar.LENGTH_SHORT).show();
                    setButtonsEnabled(true);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);
        customActivityBars.setCustomActivityBars(this);

        emailSignUp    = findViewById(R.id.emailSignUp);
        passwordSignUp = findViewById(R.id.passwordSignUp);
        signUp         = findViewById(R.id.signUp);
        googleSignUp   = findViewById(R.id.googleSignUp);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signUp.setOnClickListener(v -> {
            setButtonsEnabled(false);
            createUser();
        });

        googleSignUp.setOnClickListener(v -> {
            setButtonsEnabled(false);
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
        });
    }

    void createUser() {
        String email    = emailSignUp.getText().toString().trim();
        String password = passwordSignUp.getText().toString();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Snackbar.make(findViewById(android.R.id.content), "Please enter email and password", Snackbar.LENGTH_SHORT).show();
            setButtonsEnabled(true);
            return;
        }
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onAuthSuccess();
            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                // Already exists — try login
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        onAuthSuccess();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Email already in use", Snackbar.LENGTH_SHORT).show();
                        setButtonsEnabled(true);
                    }
                });
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Something went wrong", Snackbar.LENGTH_SHORT).show();
                setButtonsEnabled(true);
            }
        });
    }

    void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onAuthSuccess();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Authentication failed", Snackbar.LENGTH_SHORT).show();
                setButtonsEnabled(true);
            }
        });
    }

    void onAuthSuccess() {
        UserCredentials.userCollection = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
        saveLoginStatus();
        goToHomepage();
    }

    void saveLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("loginStatus", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userCollection", UserCredentials.userCollection)
                .apply();
    }

    void goToHomepage() {
        Intent intent = new Intent(this, HomePage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }


    public void goToLoginPage(android.view.View view) {
        finish();
    }
    void setButtonsEnabled(boolean enabled) {
        signUp.setClickable(enabled);
        googleSignUp.setClickable(enabled);
    }
}