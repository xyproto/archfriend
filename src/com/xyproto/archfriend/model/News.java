
package com.xyproto.archfriend.model;

import java.text.DateFormat;
import java.util.Date;


public class News {

    private Long id;
    private String text;
    private String title;
    private String author;
    private String url;
    private long date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return id + ") " + text;
    }

    public String formatArticle(String title) {
        Date date = new Date(getDate());
        String text = title + ", by " + getAuthor() + ",\n";
        text += DateFormat.getDateInstance().format(date) + "\n\n:";
        text += getTitle() + "\n\n" + getText() + "\n\n";
        return text;
    }

}
