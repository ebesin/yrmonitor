package com.dwayne.monitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // 数据库版本号,在更新时系统便是根据version来判断，若version号低于则会启动升级程序
    private static final int version = 5;
    private static final String TAG = DatabaseHelper.class.getName();
    //设置你自己的数据库名称
    public static final String DATABASE_NAME = "Monitor.db";
    //这里是构造方法，主要实现SQLiteOpenHelper的初始化工作，注意：若SQLiteOpenHelper没有初始化，则在使用时会报空指针异常
    //即，需要明确指出Context即环境，否则会报NULLPOINT错误
    //这是系统提供的初始化构造方法，你也可以使用下面的构造方法来实现数据库自己命名
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //自定义初始化方法，这样你就可以动态修改本地的数据库名称，一目了然。
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String TABLECONTACTS = "create table robots(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT," + // rowID
                "name TEXT," +
                "ip TEXT" +
                ");";
        db.execSQL(TABLECONTACTS);
        db.execSQL("insert into robots(name,ip) Values('农业机器人（001）','192.168.1.103')");
        db.execSQL("insert into robots(name,ip) Values('农业机器人（002）','192.168.1.103')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG,"Updating table from " + oldVersion + " to " + newVersion);
        db.execSQL("insert into robots(name,ip,intent_class) Values('测试连接','','com.jiyouliang.monitor.RosBridgeActivity')");
    }
}
