package com.dipen.sqlite_recview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ListSqliteOpenHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "check_in.db";
    public static final String TABLE_NAME = "check_in";

    public static final String COL_1_ID = "_id";
    public static final String COL_2_TITLE = "title";
    public static final String COL_3_PLACE = "place";
    public static final String COL_4_DETAILS = "details";
    public static final String COL_5_DATE = "unix_time";
    public static final String COL_6_LOCATION = "location";
    public static final String COL_7_IMAGE = "image";


    public ListSqliteOpenHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + COL_1_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_2_TITLE + " TEXT, "
                + COL_3_PLACE + " TEXT, "
                + COL_4_DETAILS + " TEXT, "
                + COL_5_DATE + " INTEGER, "
                + COL_6_LOCATION + " TEXT, "
                + COL_7_IMAGE + " BLOB"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(CheckIn data) {

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2_TITLE, data.title);
        contentValues.put(COL_3_PLACE, data.place);
        contentValues.put(COL_4_DETAILS, data.details);
        contentValues.put(COL_5_DATE, data.date);
        contentValues.put(COL_6_LOCATION, data.location);
        contentValues.put(COL_7_IMAGE, data.image);

        long insertedRows = database.insert(TABLE_NAME, null, contentValues);

        return insertedRows != -1;
    }

    public Cursor getAllData() {

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(
                TABLE_NAME,
                null, null,
                null, null,
                null, null);

        return cursor;
    }

    public CheckIn getDataWithId(String id) {

        SQLiteDatabase database = this.getReadableDatabase();
        String[] selectionArgs = {id};

        Cursor cursor = database.query(
                TABLE_NAME,
                null,
                COL_1_ID + "= ?",
                selectionArgs,
                null, null, null);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        int titleIndex = cursor.getColumnIndex(COL_2_TITLE);
        int placeIndex = cursor.getColumnIndex(COL_3_PLACE);
        int detailsIndex = cursor.getColumnIndex(COL_4_DETAILS);
        int dateIndex = cursor.getColumnIndex(COL_5_DATE);
        int locationIndex = cursor.getColumnIndex(COL_6_LOCATION);
        int imageIndex = cursor.getColumnIndex(COL_7_IMAGE);


        CheckIn checkIn = new CheckIn(
                cursor.getString(titleIndex),
                cursor.getString(placeIndex),
                cursor.getString(detailsIndex),
                cursor.getLong(dateIndex),
                cursor.getString(locationIndex),
                cursor.getBlob(imageIndex)
        );
        cursor.close();

        return checkIn;
    }

    public boolean deleteDataWithID(String rowId) {

        SQLiteDatabase database = this.getWritableDatabase();
        String[] whereArgs = {rowId};

        int rowsDeleted = database.delete(TABLE_NAME, COL_1_ID + "= ?", whereArgs);

        return rowsDeleted > 0;
    }

    public boolean updateRowWithId(String rowId, CheckIn data) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2_TITLE, data.title);
        contentValues.put(COL_3_PLACE, data.place);
        contentValues.put(COL_4_DETAILS, data.details);
        contentValues.put(COL_5_DATE, data.date);
        contentValues.put(COL_6_LOCATION, data.location);
        contentValues.put(COL_7_IMAGE, data.image);

        String[] whereArgs = {rowId};

        int rowsUpdated = database.update(TABLE_NAME, contentValues, COL_1_ID + "= ?", whereArgs);

        return rowsUpdated > 0;
    }

    public String formatDate(long unixTime) {
        DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy"); // "EEE, d MMM yyyy HH:mm:ss"
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(unixTime);
        return formatter.format(calendar.getTime());
    }
}
