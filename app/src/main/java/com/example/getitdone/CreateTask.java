package com.example.getitdone;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class CreateTask extends AppCompatActivity {
    TextView taskMetaDataLength;
    TextView taskMetaDataDate;
    TextView taskMetaDataTime;
    TextView taskMetaDataSeparator;
    TextView taskMetaDataCharacters;
    EditText content;
    EditText title;
    ImageButton backToHomePageButton;
    ImageButton saveTask;
    CustomizedActivityBars customActivityBars = new CustomizedActivityBars();
    FirebaseFirestore dataBase = FirebaseFirestore.getInstance();
    Date currentDateAndTime;
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy", Locale.getDefault());
    String lastEditedTime;
    String lastEditedDate;
    String taskTitle;
    String previousTaskTitle;
    String taskDate;
    String taskTime;
    String taskLength;
    String taskContent;
    HashMap<String, Object> task;
    TaskDataModel taskToUpdate;
    boolean revisitingTask;
    boolean reWritingTheSameTask = false;
    long length;
    long titleLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_task);

        Intent sourceIntent = getIntent();
        revisitingTask = sourceIntent.getBooleanExtra("updateRequest", false);
        if (revisitingTask) {
            reWritingTheSameTask = true;
            taskToUpdate = (TaskDataModel) sourceIntent.getSerializableExtra("taskToUpdate");
        }

        customActivityBars.setCustomActivityBars(this);
        findViews();
        setSaveTaskActivityStatus(false);
        initializeTaskMetaDataAsPerMode(revisitingTask);

        title.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                keepUpdatingTimeAndDate(revisitingTask);
            }
            @Override public void afterTextChanged(Editable s) {
                setSaveTaskActivityStatus((titleLength = s.length()) > 0 || length > 0);
            }
        });

        content.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContentLength();
                keepUpdatingTimeAndDate(revisitingTask);
            }
            @Override public void afterTextChanged(Editable s) {
                setSaveTaskActivityStatus(titleLength > 0 || length > 0);
            }
        });

        backToHomePageButton.setOnClickListener(v -> finish());

        saveTask.setOnClickListener(v -> {
            getTaskMetaData();
            if (ActualPosition.getActualPosition(taskTitle) < 0
                    || (reWritingTheSameTask && previousTaskTitle.equals(taskTitle))) {
                previousTaskTitle = taskTitle;
                task = getTheTaskDataReady();
                createAndUploadTask(task);
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Title already used", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    void findViews() {
        title                  = findViewById(R.id.title);
        content                = findViewById(R.id.content);
        taskMetaDataDate       = findViewById(R.id.taskMetaDataDate);
        taskMetaDataTime       = findViewById(R.id.taskMetaDataTime);
        taskMetaDataSeparator  = findViewById(R.id.taskMetaDataSeparator);
        taskMetaDataLength     = findViewById(R.id.taskMetaDataLength);
        taskMetaDataCharacters = findViewById(R.id.taskMetaDataCharacters);
        backToHomePageButton   = findViewById(R.id.back_to_home_page);
        saveTask               = findViewById(R.id.saveTask);
    }

    void keepUpdatingTimeAndDate(boolean revisitingTask) {
        currentDateAndTime = new Date();
        String currentTime = timeFormat.format(currentDateAndTime);
        if (!lastEditedTime.equals(currentTime)) {
            lastEditedTime = currentTime;
            taskMetaDataTime.setText(String.format(getString(R.string.time), currentTime));
            String currentDate = dateFormat.format(currentDateAndTime);
            if (!lastEditedDate.equals(currentDate)) {
                lastEditedDate = currentDate;
                taskMetaDataDate.setText(String.format(getString(R.string.date), currentDate));
            }
            return;
        }
        if (revisitingTask) {
            taskMetaDataDate.setText(String.format(getString(R.string.date), dateFormat.format(currentDateAndTime)));
            this.revisitingTask = false;
        }
    }

    void updateContentLength() {
        length = content.length();
        taskMetaDataLength.setText(String.format(getString(R.string.length), length));
    }

    void initializeTaskMetaData(boolean revisitingTask) {
        if (revisitingTask) {
            title.setText(taskToUpdate.getTitle());
            taskMetaDataDate.setText(String.format("%s ", lastEditedDate));
            taskMetaDataTime.setText(String.format("%s ", lastEditedTime));
            taskMetaDataSeparator.setText(R.string.pipe);
            taskMetaDataLength.setText(String.format(getString(R.string.length), Long.parseLong(taskToUpdate.getLength())));
            taskMetaDataCharacters.setText(R.string.characters);
            content.setText(taskToUpdate.getContent());
        } else {
            taskMetaDataDate.setText(String.format(getString(R.string.date), lastEditedDate));
            taskMetaDataTime.setText(String.format(getString(R.string.time), lastEditedTime));
            taskMetaDataSeparator.setText(R.string.pipe);
            updateContentLength();
            taskMetaDataCharacters.setText(R.string.characters);
        }
        content.requestFocus();
    }

    void initializeTimeAndDate(boolean revisitingTask) {
        if (revisitingTask) {
            lastEditedTime = taskToUpdate.getTime();
            lastEditedDate = taskToUpdate.getDate();
        } else {
            currentDateAndTime = new Date();
            lastEditedTime = timeFormat.format(currentDateAndTime);
            lastEditedDate = dateFormat.format(currentDateAndTime);
        }
    }

    void getTaskMetaData() {
        taskDate    = taskMetaDataDate.getText().toString();
        taskTime    = taskMetaDataTime.getText().toString();
        taskLength  = String.valueOf(length);
        taskContent = content.getText().toString();
        if (title.length() > 0) {
            taskTitle = title.getText().toString();
        } else {
            taskTitle = extractTaskTitle(taskContent);
        }
    }

    HashMap<String, Object> getTheTaskDataReady() {
        HashMap<String, Object> task = new HashMap<>();
        task.put("title",     taskTitle);
        task.put("date",      taskDate);
        task.put("time",      taskTime);
        task.put("length",    taskLength);
        task.put("content",   taskContent);
        task.put("createdAt", FieldValue.serverTimestamp());
        return task;
    }

    // FIX: infinite loop removed — no more mutual recursion
    void createAndUploadTask(HashMap<String, Object> task) {
        // If title changed while editing, delete old document first
        if (reWritingTheSameTask && !taskTitle.equals(previousTaskTitle)) {
            deletePreviousDocInSameTask(previousTaskTitle);
        }
        dataBase.collection(UserCredentials.userCollection)
                .document(taskTitle)
                .set(task);
        revokeFocusAndCloseSoftKeyboard();
        setSaveTaskActivityStatus(false);
        previousTaskTitle  = taskTitle;
        reWritingTheSameTask = true;
        HomePage.taskAltered = true;
    }

    void revokeFocusAndCloseSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        if (title.hasFocus()) {
            imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
            title.clearFocus();
            return;
        }
        imm.hideSoftInputFromWindow(content.getWindowToken(), 0);
        content.clearFocus();
    }

    void setSaveTaskActivityStatus(boolean enablement) {
        saveTask.setEnabled(enablement);
        saveTask.setAlpha(enablement ? 1.0f : 0.45f);
    }

    void deletePreviousDocInSameTask(String prevTitle) {
        dataBase.collection(UserCredentials.userCollection).document(prevTitle).delete();
    }

    String extractTaskTitle(String taskContent) {
        String[] extractedWords = taskContent.split(" ", 2);
        return extractedWords[0];
    }

    void initializeTaskMetaDataAsPerMode(boolean revisitingTask) {
        if (revisitingTask) {
            initializeTimeAndDate(true);
            initializeTaskMetaData(true);
            previousTaskTitle = taskToUpdate.getTitle();
            length            = Long.parseLong(taskToUpdate.getLength());
            titleLength       = taskToUpdate.getTitle().length();
        } else {
            initializeTimeAndDate(false);
            initializeTaskMetaData(false);
        }
    }
}