package com.mynews.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Junova on 2017/12/26.
 */

@DatabaseTable(tableName = "collect")
public class Collect {

//    @DatabaseField(generatedId = true)
//    private int id;//数据库的主键 primary key

    @DatabaseField(columnName = "newsdetailId")
    private String newsdetailId;  //文章编号

    @DatabaseField(columnName = "title")
    private String title;  //文章标题

    @DatabaseField(columnName = "date")
    private String date;  //收藏时间

    public Collect() {
    }

    public Collect(String newsdetailId, String title, String date) {
        this.newsdetailId = newsdetailId;
        this.title = title;
        this.date = date;
    }

    public String getNewsdetailId() {
        return newsdetailId;
    }

    public void setNewsdetailId(String newsdetailId) {
        this.newsdetailId = newsdetailId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
