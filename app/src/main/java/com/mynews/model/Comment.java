package com.mynews.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/12/28.
 */

public class Comment implements Serializable {

    private String COMMENTID;
    private String DATE;
    private String CONTENT;

    public String getCOMMENTID() {
        return COMMENTID;
    }

    public void setCOMMENTID(String COMMENTID) {
        this.COMMENTID = COMMENTID;
    }

    public String getDATE() {
        return DATE;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }

    public String getCONTENT() {
        return CONTENT;
    }

    public void setCONTENT(String CONTENT) {
        this.CONTENT = CONTENT;
    }

}
