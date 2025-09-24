package com.example.getitdone2;

import com.google.firebase.firestore.FirebaseFirestore;

public class TaskDeleter {
    public static void deleteTask(String collection, String document) {
        FirebaseFirestore dataBase = FirebaseFirestore.getInstance();
        dataBase.collection(collection).document(document)
        .delete();
    }
}