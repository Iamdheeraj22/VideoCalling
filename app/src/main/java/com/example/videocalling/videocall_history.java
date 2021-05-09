package com.example.videocalling;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.List;

import com.example.videocalling.CallingHistory.CallHistory;
import com.example.videocalling.CallingHistory.CallingDatabase;
import com.example.videocalling.CallingHistory.CallingHistoryAdapter;

public class videocall_history extends AppCompatActivity {

    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videocall_history);

        recyclerView = findViewById(R.id.call_history_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        getTasks();
    }

    private void getTasks()
    {
        @SuppressLint("StaticFieldLeak")
        class GetTasks extends AsyncTask<Void, Void, List<CallHistory>> {

            @Override
            protected List<CallHistory> doInBackground(Void... voids) {
                List<CallHistory> historyList = CallingDatabase
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .historyDao()
                        .getAll();
                return historyList;
            }

            @Override
            protected void onPostExecute(List<CallHistory> histories) {
                super.onPostExecute(histories);
                CallingHistoryAdapter adapter = new CallingHistoryAdapter(getApplicationContext(), histories);
                recyclerView.setAdapter(adapter);
            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }
}