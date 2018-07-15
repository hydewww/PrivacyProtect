package cn.byr.privacyprotect;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import ch.ielse.view.SwitchView;


public class AppListActivity extends AppCompatActivity {
    private ListView vList;
    private ItemListAdapter adapter;
    public Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applist);
        vList = (ListView) findViewById(R.id.listview_applist);
        vList.setAdapter(adapter = new ItemListAdapter());
        initAppList();
        // 监听button，用来清库
        Button button = (Button) findViewById(R.id.button_applist) ;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDBOpenHelper myDBHelper = new MyDBOpenHelper(AppListActivity.this, "data.db", null , 1);
                SQLiteDatabase db = myDBHelper.getWritableDatabase();
                db.execSQL("delete from AppState");
                vList = (ListView) findViewById(R.id.listview_applist);
                vList.setAdapter(adapter = new ItemListAdapter());
                initAppList();
            }
        });
    }

    private void initAppList() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                //扫描得到APP列表
                final List<MyAppInfo> appInfos = ApkTool.scanLocalInstallAppList(AppListActivity.this.getPackageManager());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setData(appInfos);
                    }
                });
            }
        }.start();
    }

    private class ItemListAdapter extends BaseAdapter {
        List<ItemObject> mDataList = new ArrayList<ItemObject>();
        List<MyAppInfo> myAppInfos = new ArrayList<MyAppInfo>();

        public void setData(List<MyAppInfo> myAppInfos) {
            mDataList.clear();
            for (int i = 0; i < myAppInfos.size(); i++) {
                ItemObject itemObject = new ItemObject();
                itemObject.title = "item[" + i + "]";
                mDataList.add(itemObject);
            }
            this.myAppInfos = myAppInfos;
            notifyDataSetChanged();
        }


        private class ViewHolder implements View.OnClickListener {
            View itemView;
            ImageView image;
            TextView tTitle;
            SwitchView vSwitch;
            ItemObject itemObject;
            String packageName;
            int pos;

            ViewHolder(View view) {
                this.itemView = view;
            }

            @Override
            public void onClick(View v) {
                if (v == vSwitch) {
                    itemObject.isOpened = vSwitch.isOpened();

                    String appName = tTitle.getText().toString();
                    int state  = MyDBOpenHelper.changeBoolToInt(itemObject.isOpened);
                    //查询对应的记录
                    MyDBOpenHelper myDBHelper = new MyDBOpenHelper(AppListActivity.this, "data.db", null, 1);
                    SQLiteDatabase db = myDBHelper.getWritableDatabase();
                    Cursor cursor = db.rawQuery("SELECT * FROM AppState WHERE appName = ?", new String[] { appName });
//                    Cursor cursor = db.query("AppState", new String[]{"packageName", "appName", "state"}, "appName=?", new String[]{appName}, null, null, null);
                    // 如果存在记录，就修改，否则插入
                    ContentValues values =  new ContentValues();
                    values.put("packageName", packageName);
                    values.put("appName", appName);
                    values.put("state", state);
                    if (cursor.moveToFirst())
                        db.execSQL("UPDATE AppState SET state = ? where appName = ?", new String[] { Integer.toString(state), appName });
                    cursor.close();
                }
            }
        }

        @Override
        public int getCount() {
            return myAppInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_content, parent, false);
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            MyAppInfo myAppInfo = myAppInfos.get(position);
            if (viewHolder == null) {
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.imageview_icon);
                viewHolder.tTitle = (TextView) convertView.findViewById(R.id.textview_title);
                viewHolder.vSwitch = (SwitchView) convertView.findViewById(R.id.switchview_switch);
                viewHolder.vSwitch.setOnClickListener(viewHolder);
            }

            viewHolder.pos = position;
            viewHolder.image.setImageDrawable(myAppInfo.getImage());
            viewHolder.itemObject = (ItemObject) getItem(position);
            viewHolder.tTitle.setText(myAppInfo.getAppName());
            viewHolder.packageName = myAppInfo.getPackageName();

            // 从数据库中查询是否有对应记录，如果有，就按照查询结果来设置state，否则就按默认值设置并插入一条记录到数据库中
            MyDBOpenHelper myDBHelper = new MyDBOpenHelper(AppListActivity.this, "data.db", null , 1);
            SQLiteDatabase db = myDBHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM AppState WHERE appName = ?", new String[] { myAppInfo.getAppName() });
//            Cursor cursor = db.query("AppState", new String[]{"packageName", "appName", "state"}, "appName=?", new String[]{myAppInfo.getAppName()}, null, null, null);
            int state;
            if (cursor.moveToFirst()) {
                state = cursor.getInt(cursor.getColumnIndex("state"));
            }else{
                state = 1;
                String appName = myAppInfo.getAppName();
                String packageName = myAppInfo.getPackageName();
                ContentValues values =  new ContentValues();
                values.put("packageName", packageName);
                values.put("appName", appName);
                values.put("state", state);
                db.insert("AppState", null, values);
            }
            cursor.close();
            viewHolder.vSwitch.setOpened(MyDBOpenHelper.changeIntToBool(state));

            viewHolder.itemView.setBackgroundColor(position % 2 != 0 ? 0xFFFFFFFF : 0xFFEEEFF3);
            return convertView;
        }
    }
}
