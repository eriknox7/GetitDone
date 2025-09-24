package com.example.getitdone2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class DecisiveLauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("loginStatus", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        UserCredentials.userCollection = preferences.getString("userCollection", null);

        if(isLoggedIn) {
            startActivity(new Intent(DecisiveLauncherActivity.this, HomePage.class));
        } else {
            startActivity(new Intent(DecisiveLauncherActivity.this, LoginPage.class));
        }
        finish();
    }
}