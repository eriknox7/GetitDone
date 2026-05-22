package com.example.getitdone;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomePage extends AppCompatActivity {
    ImageButton newTask, logout;
    EditText searchBar;
    RecyclerView taskRecyclerView;
    CustomizedActivityBars customActivityBars = new CustomizedActivityBars();
    FirebaseFirestore dataBase = FirebaseFirestore.getInstance();
    ArrayList<TaskDataModel> taskList = new ArrayList<>();
    ArrayList<TaskDataModel> filteredList = new ArrayList<>();
    TaskAdaptor taskAdaptor = new TaskAdaptor(HomePage.this);
    public static boolean taskAltered = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // FIX: SnapshotListener — works offline + real-time
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        customActivityBars.setCustomActivityBars(this);
        findViews();
        CustomHintSize.set(searchBar, "Search...", 17);
        getAdaptorAndRecyclerViewReady();
        loadEntireList();

        newTask.setOnClickListener(v -> {
            CurrentTaskPosition.position = -1;
            startActivity(new Intent(HomePage.this, CreateTask.class));
        });

        logout.setOnClickListener(v -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

            googleSignInClient.signOut().addOnCompleteListener(task -> {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences preferences = getSharedPreferences("loginStatus", MODE_PRIVATE);
                preferences.edit()
                        .putBoolean("isLoggedIn", false)
                        .putString("userCollection", null)
                        .apply();
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Logged out", Snackbar.LENGTH_SHORT);
                snackbar.show();
                startActivity(new Intent(HomePage.this, LoginPage.class));
                finish();
            });
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @SuppressLint("NotifyDataSetChanged")
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {
                if (s.length() < 1) {
                    taskAdaptor.setList(taskList);
                    taskRecyclerView.setAdapter(taskAdaptor);
                }
            }
        });

        getOnBackPressedDispatcher().addCallback(this, getCustomizedSearchBarActions());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // FIX: remove listener to prevent memory leak
        if (listenerRegistration != null) listenerRegistration.remove();
    }

    void findViews() {
        searchBar        = findViewById(R.id.searchBar);
        newTask          = findViewById(R.id.newTask);
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        logout           = findViewById(R.id.logout);
    }

    OnBackPressedCallback getCustomizedSearchBarActions() {
        return new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchBar.hasFocus()) {
                    revokeFocusAndCloseKeyboard();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        };
    }

    void getAdaptorAndRecyclerViewReady() {
        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        taskAdaptor.setList(taskList);
        ActualPosition.taskList = taskList;
        taskRecyclerView.setLayoutManager(linearLayout);
        taskRecyclerView.setAdapter(taskAdaptor);
    }

    // FIX: .get() replaced with addSnapshotListener — offline + real-time support
    public void loadEntireList() {
        listenerRegistration = dataBase
                .collection(UserCredentials.userCollection)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    List<TaskDataModel> newList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        TaskDataModel task = doc.toObject(TaskDataModel.class);
                        if (task != null) {
                            task.setId(doc.getId());
                            newList.add(task);
                        }
                    }

                    executorService.execute(() -> {
                        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                                new TaskDiffCallaback(taskList, newList));
                        mainHandler.post(() -> {
                            taskList.clear();
                            taskList.addAll(newList);
                            ActualPosition.taskList = taskList;
                            diffResult.dispatchUpdatesTo(taskAdaptor);
                        });
                    });
                });
    }

    void filter(String searchQuery) {
        filteredList.clear();
        for (TaskDataModel task : taskList) {
            if (task.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) {
                filteredList.add(task);
            }
        }
        taskAdaptor.setList(filteredList);
        taskRecyclerView.setAdapter(taskAdaptor);
    }

    void revokeFocusAndCloseKeyboard() {
        searchBar.setText("");
        CustomHintSize.set(searchBar, "Search...", 17);
        searchBar.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
    }
}
