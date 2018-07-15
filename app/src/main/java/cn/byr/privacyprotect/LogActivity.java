package cn.byr.privacyprotect;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LogActivity extends AppCompatActivity {
    private Button button;
    private LinkedList<MyLog> logList;
    private LogListAdapter logListAdapter;
    private ListView vList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        vList = (ListView) findViewById(R.id.listview_loglist);
        set();
        logListAdapter = new LogListAdapter(logList, LogActivity.this);
        vList.setAdapter(logListAdapter);
        // 监听button，用来清库
        button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDBOpenHelper myDBHelper = new MyDBOpenHelper(LogActivity.this, "data.db", null , 1);
                SQLiteDatabase db = myDBHelper.getWritableDatabase();
                db.execSQL("delete from Log");  // 清库
                //清库后重新启动
                vList = (ListView) findViewById(R.id.listview_loglist);
                set();
                logListAdapter = new LogListAdapter(logList, LogActivity.this);
                vList.setAdapter(logListAdapter);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        vList = (ListView) findViewById(R.id.listview_loglist);
        set();
        logListAdapter = new LogListAdapter(logList, LogActivity.this);
        vList.setAdapter(logListAdapter);
    }

    private void set() {
//        显示日志
//        从日志中读出所有的记录，保存到列表中
        logList = new LinkedList<MyLog>();
        MyDBOpenHelper myDBHelper = new MyDBOpenHelper(LogActivity.this, "data.db", null, 1);
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
//        Cursor cursor = db.query("Log", new String[]{"id", "time", "appName", "methodName"}, null, null, null, null, null);
        Cursor cursor = db.rawQuery("SELECT * FROM Log", null);
        String aLog;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String time = cursor.getString(1);
            String appName = cursor.getString(2);
            String moduleName = cursor.getString(3);
            MyLog log = new MyLog(id, time, appName, moduleName);
            logList.add(log);
        }
    }

    public class LogListAdapter extends BaseAdapter {
        private LinkedList<MyLog> LogList;
        private Context mContext;

        public LogListAdapter(LinkedList<MyLog> LogList, Context mContext){
            this.LogList = LogList;
            this.mContext = mContext;
        }

        public int getCount(){
            return LogList.size();
        }

        public Object getItem(int position){
            return null;
        }

        public long getItemId(int position){
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.log_content, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.textview_log);
            MyLog log = LogList.get(position);
            String aLog =  log.getTime() + "\t " + log.getAppName() + " 调用 " + log.getModuleName();
            textView.setText(aLog);
            textView.setTextSize(15);
            return convertView;
        }

    }

}
