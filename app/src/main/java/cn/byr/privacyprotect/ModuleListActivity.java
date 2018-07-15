package cn.byr.privacyprotect;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.ielse.view.SwitchView;

public class ModuleListActivity extends AppCompatActivity {

    private ListView vfunc;

    private ItemListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulelist);
        vfunc = (ListView) findViewById(R.id.listview_modulelist);
        vfunc.setAdapter(adapter = new ItemListAdapter());
        adapter.set();
        // 监听button，重置ModuleState
        Button button = (Button) findViewById(R.id.button_modulelist) ;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDBOpenHelper myDBHelper = new MyDBOpenHelper(ModuleListActivity.this, "data.db", null, 1);
                SQLiteDatabase db = myDBHelper.getWritableDatabase();
                myDBHelper.resetModule(db);
                vfunc = (ListView) findViewById(R.id.listview_modulelist);
                vfunc.setAdapter(adapter = new ItemListAdapter());
                adapter.set();
            }
        });
    }

    private class ItemListAdapter extends BaseAdapter {

        List<ItemObject> mDataList = new ArrayList<ItemObject>();

        void addItem(String name){
            ItemObject itemObject = new ItemObject();
            itemObject.title = name;
            mDataList.add(itemObject);
        }

        public void set() {
            mDataList.clear();
            addItem("相机");
            addItem("定位");
            addItem("录音");
            addItem("通讯录");
            addItem("日历");
            addItem("通话记录");
            addItem("短信");
            notifyDataSetChanged();
        }

        private class ViewHolder implements View.OnClickListener {
            View itemView;
            TextView tTitle;
            SwitchView vSwitch;
            ItemObject itemObject;
            ImageView image;
            int pos;

            ViewHolder(View view) {
                this.itemView = view;
            }

            @Override
            public void onClick(View v) {
                if (v == vSwitch) {
                    itemObject.isOpened = vSwitch.isOpened();
                    String moduleName = tTitle.getText().toString();
                    int state  = MyDBOpenHelper.changeBoolToInt(itemObject.isOpened);
                    // 查询对应的记录
                    MyDBOpenHelper myDBHelper = new MyDBOpenHelper(ModuleListActivity.this, "data.db", null, 1);
                    SQLiteDatabase db = myDBHelper.getWritableDatabase();
                    Cursor cursor = db.rawQuery("SELECT * FROM ModuleState WHERE moduleName = ?", new String[] { moduleName });
//                    Cursor cursor = db.query("ModuleState", new String[]{"moduleName", "state"}, "moduleName=?", new String[]{moduleName}, null, null, null);
                    // 如果存在记录，就修改
                    if (cursor.moveToFirst())
                        db.execSQL("UPDATE ModuleState SET state = ? where moduleName = ?", new String[] { Integer.toString(state), moduleName });
                    cursor.close();
                }
            }
        }

        @Override
        public int getCount() {
            return mDataList.size();
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
            if (viewHolder == null) {
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
                viewHolder.tTitle = (TextView) convertView.findViewById(R.id.textview_title);
                viewHolder.vSwitch = (SwitchView) convertView.findViewById(R.id.switchview_switch);
                viewHolder.vSwitch.setOnClickListener(viewHolder);
            }

            viewHolder.pos = position;
            viewHolder.itemObject = (ItemObject) getItem(position);
            viewHolder.tTitle.setText(viewHolder.itemObject.title);

            // 从数据库中查询是否有对应记录，如果有，就按照查询结果来设置state
            MyDBOpenHelper myDBHelper = new MyDBOpenHelper(ModuleListActivity.this, "data.db", null , 1);
            SQLiteDatabase db = myDBHelper.getReadableDatabase();
            String moduleName = viewHolder.itemObject.title;
            Cursor cursor = db.rawQuery("SELECT * FROM ModuleState WHERE moduleName = ?", new String[] { moduleName });
//            Cursor cursor = db.query("ModuleState", new String[]{"moduleName", "state"}, "moduleName=?", new String[]{moduleName}, null, null, null);
            cursor.moveToFirst();
            int state = cursor.getInt(cursor.getColumnIndex("state"));
            cursor.close();
            viewHolder.vSwitch.setOpened(MyDBOpenHelper.changeIntToBool(state));

            viewHolder.itemView.setBackgroundColor(position % 2 != 0 ? 0xFFFFFFFF : 0xFFEEEFF3);
            return convertView;
        }
    }
}

