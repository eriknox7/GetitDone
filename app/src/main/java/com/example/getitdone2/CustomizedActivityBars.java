package com.example.getitdone2;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowInsetsController;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Objects;

public class CustomizedActivityBars {
    public void setCustomActivityBars(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11 or later
                Objects.requireNonNull(activity.getWindow().getInsetsController()).setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            } else { // Android 8.0 to Android 10
                activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                );
            }
            // Android 6.0 to Android 7.1
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        // Set the status bar and navigation bar colors to white
        activity.getWindow().setStatusBarColor(activity.getColor(android.R.color.white));
        activity.getWindow().setNavigationBarColor(activity.getColor(android.R.color.white));
    }
}