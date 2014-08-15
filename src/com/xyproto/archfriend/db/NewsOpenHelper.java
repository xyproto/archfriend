
package com.xyproto.archfriend.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class NewsOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "news.db";
    private static final int DATABASE_VERSION = 1;

    public static final String NEWS_TABLE_NAME = "news";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_DATE = "date";

    private static final String NEWS_TABLE_CREATE = "CREATE TABLE " + NEWS_TABLE_NAME + " (" + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TEXT + " TEXT NOT NULL, " + COLUMN_TITLE + " TEXT NOT NULL, "
            + COLUMN_AUTHOR + " TEXT NOT NULL, " + COLUMN_URL + " TEXT NOT NULL, " + COLUMN_DATE
            + " LONG NOT NULL DEFAULT (strftime('%s','now')));";

    NewsOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NEWS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(NewsOpenHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + NEWS_TABLE_NAME);
        onCreate(db);
    }

}
