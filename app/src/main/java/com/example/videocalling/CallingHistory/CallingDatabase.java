package com.example.videocalling.CallingHistory;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.room.Dao;
import androidx.room.Room;
@Dao
public class CallingDatabase
{
    private static final String Database_name="My_TodoList";
    @SuppressLint("StaticFieldLeak")
    private static CallingDatabase instance;
    Context context;

    private final AppDatabase appDatabase;

    private CallingDatabase(Context mCtx) {
        this.context = mCtx;

        //creating the app database with Room database builder
        //MyToDos is the name of the database
        appDatabase = Room.databaseBuilder(mCtx, AppDatabase.class, Database_name).build();
    }

    public static synchronized CallingDatabase getInstance(Context mCtx) {
        if (instance == null) {
            instance = new CallingDatabase(mCtx);
        }
        return instance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
