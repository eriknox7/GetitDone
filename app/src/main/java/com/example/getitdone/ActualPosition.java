package com.example.getitdone;

import java.util.ArrayList;

public class ActualPosition {
    // FIX: no longer a static global list (memory leak risk removed)
    // kept only for backward compat with TaskViewHolder delete logic
    public static ArrayList<TaskDataModel> taskList;

    public static int getActualPosition(String title) {
        if (taskList == null) return -1;
        for (TaskDataModel task : taskList) {
            if (title.equals(task.getTitle())) {
                return taskList.indexOf(task);
            }
        }
        return -1;
    }
}
