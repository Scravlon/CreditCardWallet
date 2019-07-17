package com.example.haoko.creditcardwallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by haoko on 03-Jul-17.
 */

public class cardDatabase extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "cardstorage.db";

    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "cardstorage";

    public static final String KEY_ID = "id";
    public static final String KEY_CARD = "card";
    public static final String KEY_EXPIRED = "expired";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NAME = "namecard";
    private SQLiteDatabase myDB;




    public cardDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "
                + TABLE_NAME + " (" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_CARD + " TEXT NOT NULL, " +
                KEY_EXPIRED + " TEXT NOT NULL, " +
                KEY_TYPE + " TEXT NOT NULL, "  +
                KEY_NAME + " TEXT NOT NULL" + ") "
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void openDB() { myDB = getWritableDatabase(); }

    public void closeDB() {
        if (myDB != null & myDB.isOpen()) {
            myDB.close();
        }
    }

    public long insert (String card, String expired, String type){
        ContentValues values = new ContentValues();
        values.put(KEY_ID, getTaskCount());
        //values.put(KEY_ID, 0);
        values.put(KEY_CARD, card);
        values.put(KEY_EXPIRED, expired);
        values.put(KEY_TYPE, type);
        values.put(KEY_NAME, "");
        return myDB.insert(TABLE_NAME, null, values);
    }

    public long delete(int deletedata){
        String cursor1 = KEY_ID + " = " + deletedata;
        myDB.delete(TABLE_NAME, cursor1, null);
        updateRemove(deletedata);
        return 0;

    }

    public void updateRemove(int updatedate){
        for (int i = updatedate; i <= ((int) getTaskCount() - 1); i++){
            String where = KEY_ID + " = " + (i + 1);
            ContentValues value = new ContentValues();
            value.put(KEY_ID, i);
            myDB.update(TABLE_NAME, value, where, null);
        }
    }

    public void bindTab(int cardlocate, String name){
        String where = KEY_ID + " = " + cardlocate;
        ContentValues value = new ContentValues();
        value.put(KEY_NAME, name);
        myDB.update(TABLE_NAME, value, where, null);
    }

    public long getTaskCount(){
        return DatabaseUtils.queryNumEntries(myDB, TABLE_NAME);
    }

    public Cursor getAllRecords(){
        String query = "SELECT * FROM " + TABLE_NAME;
        return myDB.rawQuery(query, null);
    }


}
