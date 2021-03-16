package com.example.videocalling;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

import Classes.MyDatabaseHelper;
import Classes.callHistoryAdapter;

public class videocall_history extends AppCompatActivity {

    RecyclerView recyclerView;
    callHistoryAdapter callHistoryAdapter;
    MyDatabaseHelper myDatabaseHelper;
    ArrayList<String> callById,username,fullname,receiver_id,date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videocall_history);

        recyclerView=findViewById(R.id.call_history_recyclerView);

        myDatabaseHelper=new MyDatabaseHelper(this);
        callById=new ArrayList<>();
        username=new ArrayList<>();
        fullname=new ArrayList<>();
        receiver_id=new ArrayList<>();
        date=new ArrayList<>();

        storeDataInArrays();

        callHistoryAdapter=new callHistoryAdapter(videocall_history.this,this,callById,username,fullname,receiver_id,date);
        recyclerView.setAdapter(callHistoryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(videocall_history.this));


    }

    private void storeDataInArrays()
    {
        Cursor cursor=myDatabaseHelper.readAllData();
        if(cursor.getCount()==0)
        {
            Toast.makeText(this, "No Data!", Toast.LENGTH_SHORT).show();
        }else{
            while (cursor.moveToNext())
            {
                callById.add(cursor.getString(0));
                username.add(cursor.getString(1));
                fullname.add(cursor.getString(2));
                receiver_id.add(cursor.getString(3));
                date.add(cursor.getString(4));
            }
        }
    }
}