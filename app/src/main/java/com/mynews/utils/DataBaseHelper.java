package com.mynews.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.mynews.model.Collect;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Junova on 2017/12/26.
 */

public class DataBaseHelper extends OrmLiteSqliteOpenHelper {

    private final static String DATABASE_NAME = "news.db";  //数据库名称
    private final static int VERSION = 1;  //数据库版本号
    private Map<String, Dao> maps = new HashMap<>();
    private static DataBaseHelper instance;  //单例模式

    public static synchronized DataBaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DataBaseHelper.class) {
                if (instance == null) {
                    instance = new DataBaseHelper(context);
                }
            }
        }
        return instance;
    }

    /**
     * 获得数据库的访问对象
     *
     * @param cls
     * @return
     * @throws SQLException
     */
    public synchronized Dao getDao(Class cls) throws SQLException {
        Dao dao = null;
        String className = cls.getSimpleName();//通过反射获得类名
        if (maps.containsKey(className)) {
            dao = maps.get(className);
        }
        if (dao == null) {
            dao = super.getDao(cls);
            maps.put(className, dao);
        }
        return dao;
    }

    /**
     * 关闭所有操作
     */
    public void close() {
        super.close();
        for (String key : maps.keySet()) {
            Dao dao = maps.get(key);
            dao = null;
        }
    }

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        //完成对数据库的创建，以及表的建立
        try {
            TableUtils.createTable(connectionSource, Collect.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
        try {
            TableUtils.dropTable(connectionSource, Collect.class, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
