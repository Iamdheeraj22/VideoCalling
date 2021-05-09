package com.example.videocalling.CallingHistory;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = CallHistory.class,exportSchema = false,version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HistoryDao historyDao();
}