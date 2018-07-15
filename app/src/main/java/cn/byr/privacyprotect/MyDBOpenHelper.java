package cn.byr.privacyprotect;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBOpenHelper extends SQLiteOpenHelper {

    private static final String CREATE_APPSTATE = "create table AppState ("
            + "packageName text primary key, "
            + "appName text, "
            + "state integer)";

    private static final String CREATE_MODULESTATE = "create table ModuleState ("
            + "methodName text primary key, "
            + "moduleName text, "
            + "state integer DEFAULT 1)";

    private static final String CREATE_LOG = "create table Log ("
            + "id integer primary key autoincrement, "
            + "time text DEFAULT (datetime('now','localtime')), "
            + "appName text, "
            + "moduleName text)";

    private static final String INSERT_MODULE = "insert into ModuleState (methodName, moduleName) values (?, ?)";

    public MyDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_APPSTATE);
        db.execSQL(CREATE_MODULESTATE);
        db.execSQL(CREATE_LOG);
        db.execSQL(INSERT_MODULE, new String[] { "android.hardware.Camera.open", "相机" });
        db.execSQL(INSERT_MODULE, new String[] { "android.location.LocationManager.requestLocationUpdates", "定位" });
        db.execSQL(INSERT_MODULE, new String[] { "android.media.AudioRecord.startRecording", "录音" });
        db.execSQL(INSERT_MODULE, new String[] { "通讯录", "通讯录" });
        db.execSQL(INSERT_MODULE, new String[] { "日历", "日历" });
        db.execSQL(INSERT_MODULE, new String[] { "通话记录", "通话记录" });
        db.execSQL(INSERT_MODULE, new String[] { "短信", "短信" });
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVers, int newVers) {
        db.execSQL("drop table if exists AppState");
        db.execSQL("drop table if exists ModuleState");
        db.execSQL("drop table if exists Log");
    }

}
