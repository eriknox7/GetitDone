package com.example.getitdone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.Objects;

public class LoginPage extends AppCompatActivity {
    EditText loginEmail, loginPassword;
    TextView goToSignUp;
    Button login;
    LinearLayout googleSignIn;
    String email, password;
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
        setContentView(R.layout.login_page);
        customActivityBars.setCustomActivityBars(this);

        loginEmail    = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        login         = findViewById(R.id.login);
        goToSignUp    = findViewById(R.id.goToSignUp);
        googleSignIn  = findViewById(R.id.googleSignIn);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        login.setOnClickListener(v -> {
            setButtonsEnabled(false);
            authenticate();
        });

        googleSignIn.setOnClickListener(v -> {
            setButtonsEnabled(false);
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
        });
    }


    void authenticate() {
        email    = loginEmail.getText().toString().trim();
        password = loginPassword.getText().toString();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Snackbar.make(findViewById(android.R.id.content), "Please enter email and password", Snackbar.LENGTH_SHORT).show();
            setButtonsEnabled(true);
            return;
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onAuthSuccess();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Login failed. Check credentials.", Snackbar.LENGTH_SHORT).show();
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
        startActivity(new Intent(this, HomePage.class));
        finish();
    }

    void saveLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("loginStatus", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userCollection", UserCredentials.userCollection)
                .apply();
    }

    public void goToSignUpPage(View view) {
        startActivity(new Intent(this, SignUpPage.class));
    }

    void setButtonsEnabled(boolean enabled) {
        login.setClickable(enabled);
        googleSignIn.setClickable(enabled);
    }
}
