package cn.byr.privacyprotect

import android.app.AndroidAppHelper
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Looper
import android.widget.Toast
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedInit : IXposedHookLoadPackage {

    val appState_URI = Uri.parse("content://cn.byr.privacyprotect.provider/appstate")

    val moduleState_URI = Uri.parse("content://cn.byr.privacyprotect.provider/modulestate")

    val log_URI = Uri.parse("content://cn.byr.privacyprotect.provider/log")

    private var moduleName = ""

    private var appName = ""

    // 产生调用时发起Toast
    fun myToast(context: Context) {
        try {
            Toast.makeText(context, "$appName 调用 $moduleName", Toast.LENGTH_SHORT).show()
        // 当context为线程时
        } catch(e: Exception) {
            object: Thread(){
                override fun run() {
                    super.run()
                    Looper.prepare()
                    Toast.makeText(context, "$appName 调用 $moduleName", Toast.LENGTH_SHORT).show()
                    Looper.loop()
                }
            }.start()
        }
    }

    // 日志：Xposed+数据库
    fun myLog(context: Context) {
        log("$appName 调用 $moduleName")
        val values = ContentValues()
        values.put("appName", appName)
        values.put("moduleName", moduleName)
        context.contentResolver.insert(log_URI, values)
    }

    // 获取当前应用及模块监测开关状态并返回，应用及模块名称赋给类中的私有变量
    fun getStateAndName(context: Context, methodName: String, packageName: String): Boolean {
        var state = false
        val cursor_module = context.contentResolver.query(moduleState_URI, null, "methodName = ?", Array<String>(1){methodName}, null)
        val cursor_app = context.contentResolver.query(appState_URI,  null, "packageName = ?", Array<String>(1){packageName}, null)
        if (cursor_module == null || cursor_app == null)    // app未启动
            return false
        if (cursor_module.moveToFirst() && cursor_app.moveToFirst()) {
            state = cursor_module.getInt(cursor_module.getColumnIndex("state")) == 1 && cursor_app.getInt(cursor_app.getColumnIndex("state")) == 1
            moduleName = cursor_module.getString(cursor_module.getColumnIndex("moduleName"))
            appName = cursor_app.getString(cursor_app.getColumnIndex("appName"))
        } else {
            moduleName = cursor_module.getString(cursor_module.getColumnIndex("moduleName"))
            appName = packageName
        }
        cursor_module.close();cursor_app.close()
        return state
    }

    // 统一的hook操作
    fun addHook(lpparam: XC_LoadPackage.LoadPackageParam, className: String, methodName: String){
        hookAllMethods(Class.forName(className), methodName, object: XC_MethodHook(){
            override fun afterHookedMethod(param: MethodHookParam) {
                val context = AndroidAppHelper.currentApplication() // 获取当前应用上下文
                if (!getStateAndName(context, "$className.$methodName", lpparam.packageName))
                    return
                myLog(context)
                myToast(context)
            }
        })
    }

    // 获取ContentProvider访问的模块
    fun getModuleName(uri: String): String {
        var moduleName = ""
        if ("content://com.android.contacts/contacts"  in uri){
            moduleName = "通讯录"
        } else if("content://com.android.calendar/calendars" in uri){
            moduleName = "日历"
        } else if("content://call_log/calls" in uri){
            moduleName = "通话记录"
        } else if("content://sms" in uri){
            moduleName = "短信"
        }
        return moduleName
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 不监控系统应用
        if ("com.android" in lpparam.packageName || "android" == lpparam.packageName)
            return

        addHook(lpparam, "android.hardware.Camera", "open")
        addHook(lpparam, "android.location.LocationManager", "requestLocationUpdates")
        addHook(lpparam, "android.media.AudioRecord", "startRecording")

        // 单独操作ContentResolver.query()
        hookAllMethods(Class.forName("android.content.ContentResolver"), "query", object: XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val moduleName = getModuleName(uri = param.args[0].toString())
                if (moduleName == "")
                    return  // 防止getStateAndName死循环
                val context =  AndroidAppHelper.currentApplication()    // 获取当前应用上下文
                if (!getStateAndName(context, moduleName, lpparam.packageName))
                    return
                myLog(context)
                myToast(context)
            }
        })
    }

}
