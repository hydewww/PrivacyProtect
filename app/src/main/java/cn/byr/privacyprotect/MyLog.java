package cn.byr.privacyprotect;

public class MyLog {
    private int id;
    private String time;
    private String appName;
    private String moduleName;

    public MyLog(int id, String time, String appName, String moduleName){
        this.id = id;
        this.time = time;
        this.appName = appName;
        this.moduleName = moduleName;
    }

    public int getId(){
        return id;
    }

    public String getTime(){
        return time;
    }

    public String getAppName(){
        return appName;
    }

    public  String  getModuleName(){
        return moduleName;
    }

}
