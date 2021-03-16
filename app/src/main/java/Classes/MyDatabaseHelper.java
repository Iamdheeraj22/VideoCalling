package Classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;


public class MyDatabaseHelper extends SQLiteOpenHelper
{
    private Context context;
    private static final String DATABASE_NAME="History.db";
    private static final String TABLE_NAME="CallHistory";
    private static final String Caller_Username="caller_Username";
    private static final String Caller_Id="caller_ID";
    private static final String Caller_name="caller_name";
    private static final String Calling_date="call_date";
    private static final String Call_by="Call_by";

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + Call_by + " TEXT PRIMARY KEY, " +
                Caller_Username + " TEXT, " +Caller_name + " TEXT, "+
                Caller_Id + " TEXT, "+
                Calling_date + " DATE);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addCallingHistory(String call_byUser,String userName, String fullName,String caller_Id, String currentDate){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(Call_by,call_byUser);
        cv.put(Caller_Username,userName);
        cv.put(Caller_name,fullName);
        cv.put(Caller_Id,caller_Id);
        cv.put(Calling_date,currentDate);
        long result = db.insert(TABLE_NAME,null, cv);
        if( result== -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "call added", Toast.LENGTH_SHORT).show();
        }
    }
    public Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }
}
