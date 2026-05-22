package com.example.getitdone;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;

// FIX: class name matches filename (TaskDiffCallaback) — typo retained intentionally
// FIX: null-safe id comparison
class TaskDiffCallaback extends DiffUtil.Callback {
    private final List<TaskDataModel> oldList;
    private final List<TaskDataModel> newList;

    TaskDiffCallaback(List<TaskDataModel> oldList, List<TaskDataModel> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override public int getOldListSize() { return oldList.size(); }
    @Override public int getNewListSize() { return newList.size(); }

    @Override
    public boolean areItemsTheSame(int oldPos, int newPos) {
        String oldId = oldList.get(oldPos).getId();
        String newId = newList.get(newPos).getId();
        if (oldId == null || newId == null) return false;
        return oldId.equals(newId);
    }

    @Override
    public boolean areContentsTheSame(int oldPos, int newPos) {
        return oldList.get(oldPos).equals(newList.get(newPos));
    }
}
