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

    var mStateAndName = StateAndName()

    // 产生调用时发起Toast
    fun myToast(context: Context, newStateAndName: StateAndName) {
        val alog = "${newStateAndName.appName} 调用 ${newStateAndName.moduleName}"
        try {
            Toast.makeText(context, alog, Toast.LENGTH_SHORT).show()
        // 当context为线程时
        } catch(e: Exception) {
            object: Thread(){
                override fun run() {
                    super.run()
                    Looper.prepare()
                    Toast.makeText(context, alog, Toast.LENGTH_SHORT).show()
                    Looper.loop()
                }
            }.start()
        }
    }

    // 日志：Xposed+数据库
    fun myLog(context: Context, newStateAndName: StateAndName) {
        log("${newStateAndName.appName} 调用 ${newStateAndName.moduleName}")
        val values = ContentValues()
        values.put("appName", newStateAndName.appName)
        values.put("moduleName", newStateAndName.moduleName)
        context.contentResolver.insert(log_URI, values)
    }

    // 获取ContentResolver访问的模块
    fun getUriName(uri: String): String {
        var uriName = ""
        if ("content://com.android.contacts/contacts"  in uri){
            uriName = "通讯录"
        } else if("content://com.android.calendar/calendars" in uri){
            uriName = "日历"
        } else if("content://call_log/calls" in uri){
            uriName = "通话记录"
        } else if("content://sms" in uri){
            uriName = "短信"
        }
        return uriName
    }

    // 应用及模块监测开关状态及名字
    inner class StateAndName {
        var state = false
        var moduleName = ""
        var appName = ""
        fun isNull() : Boolean {
            return !state && moduleName == "" && appName == ""
        }
    }

    // 获取当前应用及模块监测开关状态及名字
    fun getStateAndName(context: Context?, methodName: String, packageName: String, param: XC_MethodHook.MethodHookParam): StateAndName {
        val newStateAndName = StateAndName()
        // 单独操作内容提供器
        var _methodName = methodName
        if (_methodName == "android.content.ContentResolver.query") {
            _methodName = getUriName(uri = param.args[0].toString())
            if (_methodName == "")
                return newStateAndName // 防止下面query死循环
        }
        // kotlin 确定类型非空
        if (context == null)
            return newStateAndName
        // query
        val cursor_module = context.contentResolver.query(moduleState_URI, null, "methodName = ?", Array<String>(1){_methodName}, null)
        val cursor_app = context.contentResolver.query(appState_URI,  null, "packageName = ?", Array<String>(1){packageName}, null)
        if (cursor_module == null || cursor_app == null)    // app未启动
            return newStateAndName
        if (!cursor_module.moveToFirst() || !cursor_app.moveToFirst()) {  // app或module不在列表内
            cursor_module.close();cursor_app.close()
            return newStateAndName
        }
        newStateAndName.state = cursor_module.getInt(cursor_module.getColumnIndex("state")) == 1 && cursor_app.getInt(cursor_app.getColumnIndex("state")) == 1
        newStateAndName.moduleName = cursor_module.getString(cursor_module.getColumnIndex("moduleName"))
        newStateAndName.appName = cursor_app.getString(cursor_app.getColumnIndex("appName"))
        cursor_module.close();cursor_app.close()
        return newStateAndName
    }

    // 统一的hook操作
    fun addHook(lpparam: XC_LoadPackage.LoadPackageParam, className: String, methodName: String){
        hookAllMethods(Class.forName(className), methodName, object: XC_MethodHook(){
            override fun beforeHookedMethod(param: MethodHookParam) {
                val context = AndroidAppHelper.currentApplication() // 获取当前应用上下文
                val newStateAndName = getStateAndName(context, "$className.$methodName", lpparam.packageName, param)
                if (newStateAndName.state) {
                    // 不重复提醒
                    if (!mStateAndName.state // 禁用后启用 会有提醒
                            || newStateAndName.moduleName != mStateAndName.moduleName
                            || newStateAndName.appName != mStateAndName.appName) {
                        myLog(context, newStateAndName)
                        myToast(context, newStateAndName)
                    }
                }
                if (!newStateAndName.isNull())  // 会一直产生isNull的数据
                    mStateAndName = newStateAndName
            }
        })
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if ("com.android" in lpparam.packageName || "android" == lpparam.packageName) // 不监控系统应用
            return
        addHook(lpparam, "android.hardware.Camera", "open")
        addHook(lpparam, "android.location.LocationManager", "requestLocationUpdates")
        addHook(lpparam, "android.media.AudioRecord", "startRecording")
        addHook(lpparam, "android.content.ContentResolver", "query")
    }

}
