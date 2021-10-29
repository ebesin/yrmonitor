package com.dwayne.monitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // 数据库版本号,在更新时系统便是根据version来判断，若version号低于则会启动升级程序
    private static final int version = 10;
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
                "ip TEXT," +
                "intent_class varchar(100)," +
                "type varchar(100)" +
                ");";
        db.execSQL(TABLECONTACTS);
        db.execSQL("insert into robots(name,ip,intent_class,type) Values('履带式喷雾车','192.168.1.103','com.dwayne.monitor.OldBunkerActivity','履带车v1')");
        db.execSQL("insert into robots(name,ip,intent_class,type) Values('阿克曼喷雾车','192.168.1.176','com.dwayne.monitor.HunterActivity','阿克曼车')");
        db.execSQL("insert into robots(name,ip,intent_class,type) Values('新型履带式喷雾车','192.168.1.195','com.dwayne.monitor.NewBunkerActivity','履带车v2')");
        db.execSQL("insert into robots(name,ip,intent_class,type) Values('测试连接','','com.dwayne.monitor.RosBridgeActivity','测试')");

        String CREATE_DEVICE_TYPE = "create table device_type(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT," + // rowID
                "name varchar(100)," +
                "intent_class varchar(100)," +
                "type varchar(100)" +
                ");";
        db.execSQL(CREATE_DEVICE_TYPE);
        db.execSQL("insert into device_type(name,intent_class,type) values('履带车v1','com.dwayne.monitor.OldBunkerActivity','履带车v1')");
        db.execSQL("insert into device_type(name,intent_class,type) values('履带车v2','com.dwayne.monitor.NewBunkerActivity','履带车v2')");
        db.execSQL("insert into device_type(name,intent_class,type) values('阿克曼车','com.dwayne.monitor.HunterActivity','阿克曼车')");
        db.execSQL("insert into device_type(name,intent_class,type) values('测试','com.dwayne.monitor.RosBridgeActivity','测试')");

        //version 8
        db.execSQL("alter table robots add column connectMode varchar(100)");
        String CREATE_CONNECT_MODE = "create table connect_mode(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT," + // rowID
                "connectMode varchar(100)" +
                ");";
        db.execSQL(CREATE_CONNECT_MODE);
        db.execSQL("insert into connect_mode(connectMode) values('局域网连接')");
        db.execSQL("insert into connect_mode(connectMode) values('远程连接')");
        db.execSQL("insert into connect_mode(connectMode) values('界面测试(不连接)')");

        //version 9
        db.execSQL("update robots set connectMode='界面测试(不连接)'");

        //version 10
        db.execSQL("alter table robots add column port varchar(20)");
        db.execSQL("update robots set port='9090'");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Updating table from " + oldVersion + " to " + newVersion);
        if (oldVersion < 8) {
            db.execSQL("alter table robots add column connectMode varchar(100)");
            String CREATE_CONNECT_MODE = "create table connect_mode(" + "ID INTEGER PRIMARY KEY AUTOINCREMENT," + // rowID
                    "connectMode varchar(100)" +
                    ");";
            db.execSQL(CREATE_CONNECT_MODE);
            db.execSQL("insert into connect_mode(connectMode) values('局域网连接')");
            db.execSQL("insert into connect_mode(connectMode) values('远程连接')");
            db.execSQL("insert into connect_mode(connectMode) values('界面测试(不连接)')");
        }
        if (oldVersion < 9) {
            db.execSQL("update robots set connectMode='界面测试(不连接)'");
        }
        if(oldVersion < 10){
            db.execSQL("alter table robots add column port varchar(20)");
            db.execSQL("update robots set port='9090'");
        }
    }
}
