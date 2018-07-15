package cn.byr.privacyprotect;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;

public class MyContentProvider extends ContentProvider {

    public static final int APP_STATE = 0;

    public static final int MODULE_STATE = 1;

    public static final int LOG = 2;

    public static final String AUTHORITY = "cn.byr.privacyprotect.provider";

    private static UriMatcher uriMatcher;

    private MyDBOpenHelper dbHelper;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "appstate", APP_STATE);
        uriMatcher.addURI(AUTHORITY, "modulestate", MODULE_STATE);
        uriMatcher.addURI(AUTHORITY, "log", LOG);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new MyDBOpenHelper(getContext(), "data.db", null, 1);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)){
            case APP_STATE:
                cursor = db.query("AppState", projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MODULE_STATE:
                cursor = db.query("ModuleState", projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                break;
        }
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri uriReturn = null;
        switch (uriMatcher.match(uri)) {
            case MODULE_STATE:
                db.insert("ModuleState", null, values);
            case LOG:
                long newLogId = db.insert("Log",null, values);
                uriReturn = Uri.parse("content://" + AUTHORITY + "/log/" + newLogId);
                break;
            default:
                break;
        }
        return uriReturn;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        final String str = "vnd.android.cursor.dir/vnd." + AUTHORITY;
        switch (uriMatcher.match(uri)){
            case APP_STATE:
                return str + ".appstate";
            case MODULE_STATE:
                return str + ".modulestate";
            case LOG:
                return str + ".log";
            default:
                break;
        }
        return null;
    }

}
