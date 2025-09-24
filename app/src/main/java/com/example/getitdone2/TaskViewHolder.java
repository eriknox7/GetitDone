package com.example.getitdone2;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    int position;
    TextView title;
    TextView content;
    ImageButton deleteTask;
    Context context;
    ArrayList<TaskDataModel> taskList;
    TaskDataModel task;
    String currentTaskTitle;
    public TaskViewHolder(Context context, View taskView, ArrayList<TaskDataModel> taskList, TaskAdaptor taskAdaptor) {
        super(taskView);
        title = taskView.findViewById(R.id.title);
        content = taskView.findViewById(R.id.content);
        deleteTask = taskView.findViewById(R.id.deleteTask);
        this.context = context;
        this.taskList = taskList;

        taskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = getAdapterPosition();
                task = taskList.get(position);
                CurrentTaskPosition.position = ActualPosition.getActualPosition(task.getTitle());
                if(position != RecyclerView.NO_POSITION) {
                    Intent goToTheTask = new Intent(context, CreateTask.class);
                    goToTheTask.putExtra("updateRequest", true);
                    goToTheTask.putExtra("taskToUpdate", task);
                    context.startActivity(goToTheTask);
                }
            }
        });

        deleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskView.setEnabled(false);
                position = getAdapterPosition();
                task = taskList.get(position);
                currentTaskTitle = task.getTitle();
                CurrentTaskPosition.position = ActualPosition.getActualPosition(currentTaskTitle);
                TaskDeleter.deleteTask(UserCredentials.userCollection, currentTaskTitle);
                taskList.remove(position);
                taskAdaptor.notifyItemRemoved(position);
                if(ActualPosition.taskList.size() > CurrentTaskPosition.position) {
                    if (currentTaskTitle.equals(ActualPosition.taskList.get(CurrentTaskPosition.position).getTitle())) {
                        ActualPosition.taskList.remove(CurrentTaskPosition.position);
                    }
                }
                CurrentTaskPosition.position = -1;
            }
        });
    }
}