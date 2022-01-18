package com.example.covid19app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.Iterator;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String TABLE_NAME = "AGRAWAL";
    private static final String col1 = "heart_rate";
    private static final String col2 = "respiratory_rate";
    public static final String col3 = "nausea";
    public static final String col4 = "headache";
    public static final String col5 = "diarrhea";
    public static final String col6 = "sore_throat";
    public static final String col7 = "fever";
    public static final String col8 = "muscle_ache";
    public static final String col9 = "loss_of_smell_or_taste";
    public static final String col10 = "cough";
    public static final String col11 = "shortness_of_breath";
    public static final String col12 = "tired";

    SQLiteDatabase sqLite_database;

    public DatabaseHelper(Context context){
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + col1 + " TEXT, " + col2 + " TEXT, " + col3 + " TEXT, " + col4 + " TEXT, " + col5 + " TEXT, " + col6 + " TEXT, "  + col7 + " TEXT, " + col8 + " TEXT, " + col9 + " TEXT, " + col10 + " TEXT, " + col11 + " TEXT, " + col12 + " TEXT)";
        sqLiteDatabase.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void update_symptoms(Map<String, Float> symptom_value) {

        ContentValues contentValues = new ContentValues();
        sqLite_database = this.getWritableDatabase();
        sqLite_database.beginTransaction();
        try {
           Cursor cursor = sqLite_database.rawQuery("SELECT MAX(ID) FROM "+TABLE_NAME, null);
           int id = 1;
           if(cursor.getCount() > 0){
               int i = 0;
               while(cursor.moveToNext()){
                   id = cursor.getInt(0);
                   i++;
               }
           }

            Log.d("helper class", "id value = "+id);
            Iterator<Map.Entry<String, Float>> iter = symptom_value.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry<String, Float> entry = iter.next();
                String column_name = entry.getKey();
                float rating = entry.getValue();
                contentValues.put(column_name, String.valueOf(rating));
            }
            int result = sqLite_database.update(TABLE_NAME, contentValues, "ID=?", new String[]{String.valueOf(id)});
            sqLite_database.setTransactionSuccessful();
            if(result == -1){
                Log.d("upload signs", "data not updated");
            }else {
                Log.d("upload signs", "data updated");
            }
        }catch (SQLiteException e){
            sqLite_database.endTransaction();
        }finally {
            sqLite_database.endTransaction();
        }

    }

    public void insert_hrate_resprate(int heart_rate, int resp_rate) {
        ContentValues contentValues = new ContentValues();
        sqLite_database = this.getWritableDatabase();
        sqLite_database.beginTransaction();
        try {
            contentValues.put("heart_rate", String.valueOf(heart_rate));
            contentValues.put("respiratory_rate", String.valueOf(resp_rate));
            long result = sqLite_database.insert(TABLE_NAME, null, contentValues);
            sqLite_database.setTransactionSuccessful();
            if(result == -1){
                Log.d("upload signs", "data not inserted");
            }else{
                Log.d("upload signs", "data inserted");
            }
        }catch (SQLiteException e){
            sqLite_database.endTransaction();
        }finally {
            sqLite_database.endTransaction();
        }
    }
}
