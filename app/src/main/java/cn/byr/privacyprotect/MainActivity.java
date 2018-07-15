package cn.byr.privacyprotect;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;

public class MainActivity extends ActivityGroup {

    private TabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏状态栏部分（电池、信号等）
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initTabs();
    }

    private void initTabs() {
        mTabHost = (TabHost) findViewById(R.id.tabhost);
        mTabHost.setup(this.getLocalActivityManager());
        // 添加功能列表的tab,注意下面的setContent中的代码.是这个需求实现的关键
        mTabHost.addTab(mTabHost.newTabSpec("tab_setting")
                .setIndicator("功能开关",getResources().getDrawable(R.drawable.btn_choose))
                .setContent(new Intent(this, ModuleListActivity.class)));
        mTabHost.addTab(mTabHost.newTabSpec("tab_applist")
                .setIndicator("应用列表",getResources().getDrawable(R.drawable.btn_choose))
                .setContent(new Intent(this, AppListActivity.class)));
        mTabHost.addTab(mTabHost.newTabSpec("tab_log")
                .setIndicator("查看日志",getResources().getDrawable(R.drawable.btn_choose))
                .setContent(new Intent(this, LogActivity.class)));

        mTabHost.setCurrentTab(1);
    }

}
