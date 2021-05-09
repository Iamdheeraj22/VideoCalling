package com.example.videocalling.CallingHistory;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao
{
    @Query("SELECT * FROM MyTodo")
    List<CallHistory> getAll();

    @Insert
    void insertTask(CallHistory task);
}
