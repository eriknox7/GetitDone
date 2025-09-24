package com.example.getitdone2;

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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomePage extends AppCompatActivity {
    int position = 0;
    ImageButton newTask, logout;
    EditText searchBar;
    RecyclerView taskRecyclerView;
    CustomizedActivityBars customActivityBars = new CustomizedActivityBars();
    FirebaseFirestore dataBase = FirebaseFirestore.getInstance();
    ArrayList<TaskDataModel> taskList = new ArrayList<TaskDataModel>();
    ArrayList<TaskDataModel> filteredList = new ArrayList<TaskDataModel>();
    TaskAdaptor taskAdaptor = new TaskAdaptor(HomePage.this);
    TaskDataModel currentTask;
    public static boolean taskAltered = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        customActivityBars.setCustomActivityBars(this);
        findViews();
        CustomHintSize.set(searchBar, "Search...", 17);
        getAdaptorAndRecyclerViewReady();
        loadEntireList();

        newTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentTaskPosition.position = -1;
                Intent gotoNewTask = new Intent(HomePage.this, CreateTask.class);
                startActivity(gotoNewTask);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("loginStatus", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.putString("userCollection", null);
                editor.apply();
                Toast.makeText(HomePage.this, "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomePage.this, LoginPage.class));
                finish();
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used in this implementation.
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0) {
                    filter(s.toString());
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() < 1) {
                    taskAdaptor.setList(taskList);
                    taskRecyclerView.setAdapter(taskAdaptor);
                }
            }
        });
        getOnBackPressedDispatcher().addCallback(this, getCustomizedSearchBarActions());
    }

    // OnResume
    @Override
    protected void onResume() {
        super.onResume();
        if(searchBar.hasFocus()) {
            revokeFocusAndCloseKeyboard();
        }
        if(taskAltered) {
            if(CurrentTaskPosition.position > -1) {
                taskList.remove(CurrentTaskPosition.position);
                taskAdaptor.notifyItemRemoved(CurrentTaskPosition.position);
                CurrentTaskPosition.position = -1;
            }
            retrieveMostRecentTask();
            taskAltered = false;
        }
    }

    // Method to find views.
    void findViews() {
        searchBar = findViewById(R.id.searchBar);
        newTask = findViewById(R.id.newTask);
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        logout = findViewById(R.id.logout);
    }

    // Custom actions the search bar should perform.
    OnBackPressedCallback getCustomizedSearchBarActions() {
        return new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(searchBar.hasFocus()) {
                    revokeFocusAndCloseKeyboard();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        };
    }

    // Set layout, set adaptor, set global static variables. (Get ready to function)
    void getAdaptorAndRecyclerViewReady() {
        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        taskAdaptor.setList(taskList);
        ActualPosition.taskList = taskList;
        taskRecyclerView.setLayoutManager(linearLayout);
        taskRecyclerView.setAdapter(taskAdaptor);
    }

    // Load the entire list once the app relaunches.
    public void loadEntireList() {
        dataBase.collection(UserCredentials.userCollection)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
            List<TaskDataModel> newList = new ArrayList<>();
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                newList.add(document.toObject(TaskDataModel.class));
            }

            // Run DiffUtil on a background thread
            executorService.execute(() -> {
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TaskDiffCallback(taskList, newList));

                // Apply changes on the main thread
                mainHandler.post(() -> {
                    taskList.clear();
                    taskList.addAll(newList);
                    diffResult.dispatchUpdatesTo(taskAdaptor);
                });
            });
        });
    }


    //Get the most recent data from Firebase Firestore.
    public void retrieveMostRecentTask() {
        dataBase.collection(UserCredentials.userCollection)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(1)
        .get()
        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                    currentTask = document.toObject(TaskDataModel.class);
                    taskList.add(0, currentTask);
                    taskAdaptor.notifyItemInserted(0);
                    taskRecyclerView.scrollToPosition(0);
                }
            }
        });
    }

    // Get search results.
    void filter(String searchQuery) {
        filteredList.clear();
        for(TaskDataModel task: taskList) {
            if(task.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) {
                filteredList.add(task);
            }
        }
        taskAdaptor.setList(filteredList);
        taskRecyclerView.setAdapter(taskAdaptor);
    }

    // Clear focus, reset text, close soft keyboard.
    void revokeFocusAndCloseKeyboard() {

        searchBar.setText("");
        CustomHintSize.set(searchBar, "Search...", 17);
        searchBar.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null)
            imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
    }
}