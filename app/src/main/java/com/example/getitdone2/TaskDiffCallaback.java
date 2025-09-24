package com.example.getitdone2;
import androidx.recyclerview.widget.DiffUtil;
import java.util.List;

// Custom DiffUtil Callback
class TaskDiffCallback extends DiffUtil.Callback {
    private final List<TaskDataModel> oldList;
    private final List<TaskDataModel> newList;

    TaskDiffCallback(List<TaskDataModel> oldList, List<TaskDataModel> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() { return oldList.size(); }

    @Override
    public int getNewListSize() { return newList.size(); }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}