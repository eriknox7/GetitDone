package com.example.getitdone2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
        if(revisitingTask) {
            reWritingTheSameTask = true;
            taskToUpdate = (TaskDataModel) sourceIntent.getSerializableExtra("taskToUpdate");
        }

        customActivityBars.setCustomActivityBars(this);
        findViews();
        setSaveTaskActivityStatus(false);
        initializeTaskMetaDataAsPerMode(revisitingTask);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used in this implementation.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                keepUpdatingTimeAndDate(revisitingTask);
            }

            @Override
            public void afterTextChanged(Editable s) {
                setSaveTaskActivityStatus((titleLength = s.length()) > 0 || length > 0);
            }
        });

        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used in this implementation.
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContentLength();
                keepUpdatingTimeAndDate(revisitingTask);
            }
            @Override
            public void afterTextChanged(Editable s) {
                setSaveTaskActivityStatus(titleLength > 0 || length > 0);
            }
        });

        backToHomePageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTaskMetaData();
                if(ActualPosition.getActualPosition(taskTitle) < 0 || (reWritingTheSameTask && previousTaskTitle.equals(taskTitle))) {
                    previousTaskTitle = taskTitle;
                    task = getTheTaskDataReady();
                    createAndUploadTask(task);
                } else {
                    Toast.makeText(CreateTask.this, "Title already used", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Find all the views.
    void findViews() {
        title = findViewById(R.id.title);
        content = findViewById(R.id.content);
        taskMetaDataDate = findViewById(R.id.taskMetaDataDate);
        taskMetaDataTime = findViewById(R.id.taskMetaDataTime);
        taskMetaDataSeparator = findViewById(R.id.taskMetaDataSeparator);
        taskMetaDataLength = findViewById(R.id.taskMetaDataLength);
        taskMetaDataCharacters = findViewById(R.id.taskMetaDataCharacters);
        backToHomePageButton = findViewById(R.id.back_to_home_page);
        saveTask = findViewById(R.id.saveTask);
    }

    // Keep updating Time and Date as the text is being changed continuously.
    void keepUpdatingTimeAndDate(boolean revisitingTask) {
        currentDateAndTime = new Date();
        String currentTime = timeFormat.format(currentDateAndTime);
        if(!(lastEditedTime.equals(currentTime))) {
            lastEditedTime = currentTime;
            taskMetaDataTime.setText(String.format(getString(R.string.time), currentTime));
            String currentDate = dateFormat.format(currentDateAndTime);
            if(!(lastEditedDate.equals(currentDate))) {
                lastEditedDate = currentDate;
                taskMetaDataDate.setText(String.format(getString(R.string.date), currentDate));
            }
            return;
        }
        if(revisitingTask) {
            taskMetaDataDate.setText(String.format(getString(R.string.date), dateFormat.format(currentDateAndTime)));
            this.revisitingTask = false;
        }
    }

    // Update length of the content as a new input has been made.
    void updateContentLength() {
        length = content.length();
        taskMetaDataLength.setText(String.format(getString(R.string.length), length));
    }

    // Set the task MetaData as the CreateTask activity is launched for a new task or for updating a task.
    void initializeTaskMetaData(boolean revisitingTask) {
        if(revisitingTask) {
            title.setText(taskToUpdate.getTitle());
            taskMetaDataDate.setText(String.format("%s ",lastEditedDate));
            taskMetaDataTime.setText(String.format("%s ",lastEditedTime));
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

    // Get Time and Date for the first time on launching the activity.
    void initializeTimeAndDate(boolean revisitingTask) {
        if(revisitingTask) {
            lastEditedTime = taskToUpdate.getTime();
            lastEditedDate = taskToUpdate.getDate();
        } else {
            currentDateAndTime = new Date();
            lastEditedTime = timeFormat.format(currentDateAndTime);
            lastEditedDate = dateFormat.format(currentDateAndTime);
        }
    }

    // Get taskMetaData.
    void getTaskMetaData() {
        taskDate = taskMetaDataDate.getText().toString();
        taskTime = taskMetaDataTime.getText().toString();
        taskLength = String.valueOf(length);
        taskContent = content.getText().toString();
        if(title.length() > 0) {
            taskTitle = title.getText().toString();
        } else {
            taskTitle = extractTaskTitle(taskContent);
        }
    }

    // Get the task data ready to upload in a map format.
    HashMap<String, Object> getTheTaskDataReady() {
       HashMap<String, Object> task = new HashMap<String, Object>();
       task.put("title", taskTitle);
       task.put("date", taskDate);
       task.put("time", taskTime);
       task.put("length", taskLength);
       task.put("content", taskContent);
       task.put("createdAt", FieldValue.serverTimestamp());
       return task;
    }

    // Create and upload task to cloud.
    void createAndUploadTask(HashMap<String, Object> task) {
        if(reWritingTheSameTask) {
            manageReWritingTheSameTask(task);
        }
        dataBase.collection(UserCredentials.userCollection).document(taskTitle)
        .set(task);
        revokeFocusAndCloseSoftKeyboard();
        setSaveTaskActivityStatus(false);
        previousTaskTitle = taskTitle;
        reWritingTheSameTask = true;
        HomePage.taskAltered = true;
    }

    // Clear focus from all the focusables and hide soft input method.
    void revokeFocusAndCloseSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(title.hasFocus()) {
            imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
            title.clearFocus();
            return;
        }
        imm.hideSoftInputFromWindow(content.getWindowToken(), 0);
        content.clearFocus();
    }

    // Enable and disable the saveTask button depending upon whether content is present or not.
    void setSaveTaskActivityStatus(boolean enablement) {
        saveTask.setEnabled(enablement);
        if(enablement) {
            saveTask.setAlpha(1.0f);
            return;
        }
        saveTask.setAlpha(0.45f);
    }

    // Manage the case where the same task is being re-written after uploading.
    void manageReWritingTheSameTask(HashMap<String, Object> task) {
        if(!taskTitle.equals(previousTaskTitle)) {
            deletePreviousDocInSameTask(previousTaskTitle);
        }
        reWritingTheSameTask = false;
        createAndUploadTask(task);
    }

    // Delete the previous document written in the same task from the collection.
    void deletePreviousDocInSameTask(String previousTaskTitle) {
        dataBase.collection(UserCredentials.userCollection).document(previousTaskTitle)
        .delete();
    }

    // Extract the task title if not entered by the user.
    String extractTaskTitle(String taskContent) {
        String[] extractedWords = taskContent.split(" ", 2);
        return extractedWords[0];
    }

    // If the task has been opened to edit.
    void initializeTaskMetaDataAsPerMode(boolean revisitingTask) {
        if(revisitingTask) {
            initializeTimeAndDate(true);
            initializeTaskMetaData(true);
            previousTaskTitle = taskToUpdate.getTitle();
            length = Long.parseLong(taskToUpdate.getLength());
            titleLength = taskToUpdate.getTitle().length();
        } else {
            initializeTimeAndDate(false);
            initializeTaskMetaData(false);
        }
    }
}