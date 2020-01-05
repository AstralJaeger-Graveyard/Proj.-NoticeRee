/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree;

import java.io.File;

public class Configuration {

    public static String APP_LOCATION = "NoticeRee";
    public static String CONFIG_LOCATION = "config";
    public static String DATA_LOCATION = "data";

    private static String workingDirectory = "";

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
