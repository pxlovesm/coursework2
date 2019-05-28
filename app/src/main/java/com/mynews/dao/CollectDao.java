package com.mynews.dao;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.mynews.model.Collect;
import com.mynews.utils.DataBaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Junova on 2017/12/26.
 */

public class CollectDao {

    private Context context;
    private Dao<Collect, String> collectDao;
    private DataBaseHelper helper;

    public CollectDao(Context context) {
        this.context = context;
        helper = DataBaseHelper.getInstance(context);
        try {
            collectDao = helper.getDao(Collect.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCollect(Collect collect) {
        try {
            collectDao.create(collect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateCollect(Collect collect) {
        try {
            collectDao.update(collect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateCollectByNewsDetailId(Collect collect, String newsdetailId) {
        try {
            collectDao.updateId(collect, newsdetailId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateCollectByBuilder(Collect collect) {
        try {
            UpdateBuilder builder = collectDao.updateBuilder();
            builder.updateColumnValue("title", collect.getTitle());
            builder.updateColumnValue("date", collect.getDate());
            builder.where().eq("newsdetailId", 1);
            builder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteCollect(Collect collect) {
        try {
            collectDao.delete(collect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMulCollect(List<Collect> collects) {
        try {
            collectDao.delete(collects);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteCollectByIDs(List<String> ids) {
        try {
            collectDao.deleteIds(ids);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<Collect> listAll() {
        try {
            return collectDao.queryForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Collect> queryBuilder1(String newsDetailId) {
        List<Collect> list = null;
        QueryBuilder<Collect, String> queryBuilder = collectDao.queryBuilder();
        //声明的是一个where 条件
        Where<Collect, String> where = queryBuilder.where();
        try {
            where.eq("newsdetailId", newsDetailId);
            where.prepare();
            list = queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
