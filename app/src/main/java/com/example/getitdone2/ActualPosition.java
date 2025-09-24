package com.example.getitdone2;
import java.util.ArrayList;
public class ActualPosition {
    public static ArrayList<TaskDataModel> taskList;
    public static int getActualPosition(String title) {
        for(TaskDataModel task: taskList) {
            if(title.equals(task.getTitle())) {
                return taskList.indexOf(task);
            }
        }
        return -1;
    }
}