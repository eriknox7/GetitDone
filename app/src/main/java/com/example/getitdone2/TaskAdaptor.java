package com.example.getitdone2;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdaptor extends RecyclerView.Adapter <TaskViewHolder> {
    Context context;
    ArrayList<TaskDataModel> taskList;

    public TaskAdaptor(Context context) {
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    void setList(ArrayList<TaskDataModel> taskList) {
        this.taskList = taskList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View taskView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new TaskViewHolder(context, taskView, taskList, this);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskDataModel currentTask = taskList.get(position);
        holder.title.setText(MetaDataFormatter.titleFormatter(currentTask.getTitle()));
        holder.content.setText(MetaDataFormatter.contentFormatter(currentTask.getContent()));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}