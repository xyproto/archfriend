
package com.xyproto.archfriend.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.xyproto.archfriend.model.News;


public class NewsDataSource {

    private SQLiteDatabase database;
    private NewsOpenHelper dbHelper;
    private String[] allColumns = {
            NewsOpenHelper.COLUMN_ID, NewsOpenHelper.COLUMN_TEXT, NewsOpenHelper.COLUMN_TITLE, NewsOpenHelper.COLUMN_AUTHOR,
            NewsOpenHelper.COLUMN_URL, NewsOpenHelper.COLUMN_DATE};

    public NewsDataSource(Context context) {
        dbHelper = new NewsOpenHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public News createNews(News news) {
        return createNews(news.getText(), news.getTitle(), news.getAuthor(), news.getUrl(), news.getDate());
    }

    public News createNews(String text, String title, String author, String url, long date) {
        ContentValues values = new ContentValues();
        values.put(NewsOpenHelper.COLUMN_TEXT, text);
        values.put(NewsOpenHelper.COLUMN_TITLE, title);
        values.put(NewsOpenHelper.COLUMN_AUTHOR, author);
        values.put(NewsOpenHelper.COLUMN_URL, url);
        values.put(NewsOpenHelper.COLUMN_DATE, date);

        long insertId = database.insert(NewsOpenHelper.NEWS_TABLE_NAME, null, values);

        Cursor cursor = database.query(NewsOpenHelper.NEWS_TABLE_NAME, allColumns, NewsOpenHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        News news = cursorToNews(cursor);
        cursor.close();

        return news;
    }

    private News cursorToNews(Cursor cursor) {
        News news = new News();

        news.setId(cursor.getLong(0));
        news.setText(cursor.getString(1));
        news.setTitle(cursor.getString(2));
        news.setAuthor(cursor.getString(3));
        news.setUrl(cursor.getString(4));
        news.setDate(cursor.getLong(5));

        return news;
    }

    public News getLatestNews() {
        Cursor cursor = database.query(NewsOpenHelper.NEWS_TABLE_NAME, allColumns, null, null, null, null, NewsOpenHelper.COLUMN_DATE
                + " desc");

        News news = null;
        if (cursor.moveToFirst())
            news = cursorToNews(cursor);

        cursor.close();

        return news;
    }
}
