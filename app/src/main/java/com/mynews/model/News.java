package com.mynews.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/12/28.
 */

public class News implements Serializable {

    private String NEWSDETAIL_ID; //编号
    private String UNIQUEKEY;  //新闻编号
    private String TITLE;  //标题
    private String PIC1;  //图片
    private String URL;  //路径
    private String DATE;  //时间
    private String CATEGORY;  //类别
    private String COMMENT;  //跟帖数

    public String getNEWSDETAIL_ID() {
        return NEWSDETAIL_ID;
    }

    public void setNEWSDETAIL_ID(String NEWSDETAIL_ID) {
        this.NEWSDETAIL_ID = NEWSDETAIL_ID;
    }

    public String getUNIQUEKEY() {
        return UNIQUEKEY;
    }

    public void setUNIQUEKEY(String UNIQUEKEY) {
        this.UNIQUEKEY = UNIQUEKEY;
    }

    public String getTITLE() {
        return TITLE;
    }

    public void setTITLE(String TITLE) {
        this.TITLE = TITLE;
    }

    public String getPIC1() {
        return PIC1;
    }

    public void setPIC1(String PIC1) {
        this.PIC1 = PIC1;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getDATE() {
        return DATE;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }

    public String getCATEGORY() {
        return CATEGORY;
    }

    public void setCATEGORY(String CATEGORY) {
        this.CATEGORY = CATEGORY;
    }

    public String getCOMMENT() {
        return COMMENT;
    }

    public void setCOMMENT(String COMMENT) {
        this.COMMENT = COMMENT;
    }
}
