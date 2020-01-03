/*
 * Copyright (c) 2020.
 */

package org.astraljaeger.noticeree.DataTools;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.Utils;

public class ConfigStore {

    private static ConfigStore instance;

    public static ConfigStore getInstance(){
        if(instance == null)
            instance = new ConfigStore();
        return instance;
    }

    private Logger log;

    private String file;
    private String dir;

    private String fileName = "config.eson";
    private ConfigItem config;
    private Gson serializer;

    private ConfigStore(){
        // log = Logger.getLogger(this.getClass().getName());
        serializer = new Gson();
        char sep = File.separatorChar;

        String os = System.getProperty("os.name").toLowerCase();
        String workingDir = "";
        if(os.contains("win")){
            workingDir = System.getenv("AppData");
        }
        else {
            workingDir = System.getProperty("user.home");
            if(os.contains("mac"))
                workingDir += sep + "Library" + sep + "Application Support";
        }

        dir = workingDir + sep +
            Configuration.APP_LOCATION + sep +
            Configuration.CONFIG_LOCATION + sep;
        file = dir + fileName;
        if(!Files.exists(Paths.get(file))){
            try {
                Files.createDirectories(Paths.get(dir));
                Files.createFile(Paths.get(file));

                var empty = new ConfigItem("");
                Files.write(
                    Paths.get(file),
                    encrypt(serializer.toJson(empty)).getBytes(),
                    StandardOpenOption.CREATE
                );
            }catch (IOException e){
                // TODO
                System.exit(1);
            }
        }

        try {

            var content = Files.readString(Paths.get(file));
            config = serializer.fromJson(decrypt(content), ConfigItem.class);
        }catch (IOException e){
            // TODO: Add error display
            System.exit(2);
        }
    }

    public ConfigItem getConfigItem(){
        return config;
    }

    public void setToken(String token){
        config.token = token;
        writeFile(encrypt(serializer.toJson(config)));
    }

    private String decrypt(String cipher){
        return cipher;
    }

    private String encrypt(String plain){
        return plain;
    }

    private void writeFile(String content){
        try {
            Files.write(
                Paths.get(file),
                encrypt(content).getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING
            );
        }catch (IOException e){
            var dialog = Utils.createErrorDialog(
              e,
              "Error writing to file",
              "An error occured while trying to write to the config file"
            );
            dialog.showAndWait();
        }
    }
}
