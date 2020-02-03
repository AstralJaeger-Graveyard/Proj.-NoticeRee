/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree;

import java.io.File;

public class Configuration {


    public static final String CLIENT_ID = "i76h7g9dys23tnsp4q5qbc9vezpwfb";
    public static final String APP_LOCATION = "NoticeRee";
    public static final String CONFIG_LOCATION = "config";
    public static final String DATA_LOCATION = "data";
    public static final String LOG_LOCATION = "logs";

    public static final boolean USE_PERSISTANCE = true;

    private static String workingDirectory = "";

    private Configuration(){

    }

    public static String getAppWorkingDirectory(){
        if(!workingDirectory.equals(""))
            return workingDirectory;

        String os = System.getProperty("os.name").toLowerCase();
        String workingDir = "";
        if(os.contains("win")){
            workingDir = System.getenv("AppData");
        }
        else {
            workingDir = System.getProperty("user.home");
            if(os.contains("mac"))
                workingDir += File.separator + "Library" + File.separator + "Application Support";
        }
        workingDir += File.separator + APP_LOCATION + File.separator;
        workingDirectory = workingDir;
        return workingDir;
    }

    public static String getAppConfigDirectory(){
        return getAppWorkingDirectory() + CONFIG_LOCATION + File.separator;
    }

    public static String getAppDataDirectory(){
        return getAppWorkingDirectory() + DATA_LOCATION  + File.separator;
    }
}
