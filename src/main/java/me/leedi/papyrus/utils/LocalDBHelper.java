package me.leedi.papyrus.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class LocalDBHelper {
    private SQLiteDatabase database;
    private SQLiteHelper DBHelper;
    private String[] allColumns = {SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_CONTENTID, SQLiteHelper.COLUMN_CONTENT, SQLiteHelper.COLUMN_DATE, SQLiteHelper.COLUMN_ATTACHMENT};

    public LocalDBHelper(Context context) {
        DBHelper = new SQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = DBHelper.getWritableDatabase();
    }

    public void close() throws SQLException {
        DBHelper.close();
    }

    public Papyrus newPapyrus(String contentId, String content, String date, String attachment) {
        Cursor cursor = null;
        try {
            ContentValues values = new ContentValues();
            values.put(SQLiteHelper.COLUMN_CONTENTID, contentId);
            values.put(SQLiteHelper.COLUMN_CONTENT, content);
            values.put(SQLiteHelper.COLUMN_DATE, date);
            values.put(SQLiteHelper.COLUMN_ATTACHMENT, attachment);
            long insertId = database.insert(SQLiteHelper.TABLE_NAME, null, values);
            cursor = database.query(SQLiteHelper.TABLE_NAME, allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
            cursor.moveToFirst();
            return cursorToKeyword(cursor);
        }
        finally {
            closeCursor(cursor);
        }
    }

    public void deleteKeyword(Papyrus papyrus) {
        long id = papyrus.getId();
        database.delete(SQLiteHelper.TABLE_NAME, SQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    public List<Papyrus> getAllPapyrus() {
        List<Papyrus> papyrusList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(SQLiteHelper.TABLE_NAME, allColumns, null, null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Papyrus papyrus = cursorToKeyword(cursor);
                papyrusList.add(papyrus);
                cursor.moveToNext();
            }
            return papyrusList;
        }
        finally {
            closeCursor(cursor);
        }
    }

    private void closeCursor(Cursor cursor) {
        try {
            if(cursor != null) {
                cursor.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Papyrus cursorToKeyword(Cursor cursor) {
        Papyrus papyrus = new Papyrus();
        int idIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_ID);
        int contentIdIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_CONTENTID);
        int titleIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_TITLE);
        int contentIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_CONTENT);
        int dateIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_DATE);
        papyrus.setId(cursor.getLong(idIndex));
        papyrus.setContentId(cursor.getString(contentIdIndex));
        papyrus.setTitle(cursor.getString(titleIndex));
        papyrus.setContent(cursor.getString(contentIndex));
        papyrus.setDate(cursor.getString(dateIndex));
        return papyrus;
    }
}
