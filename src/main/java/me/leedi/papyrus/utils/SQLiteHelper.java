package me.leedi.papyrus.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "papyrus";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CONTENTID = "contentId";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_ATTACHMENT = "attachment";

    public static final String DATABASE_NAME = "papyrus.db";
    public static final int DATABASE_VERSION = 3;

    public static final String DATABASE_CREATE = "create table " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY autoincrement, " + COLUMN_CONTENTID + " text not null, "  + COLUMN_TITLE + " text not null, " + COLUMN_CONTENT + " text not null, " + COLUMN_DATE + " text not null, " + COLUMN_ATTACHMENT + " text not null);";
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
