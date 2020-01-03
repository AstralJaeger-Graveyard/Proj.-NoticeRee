/*
 * Copyright (c) 2020.
 */

package org.astraljaeger.noticeree.DataTools;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.Utils;

public class ConfigStore {

    private static ConfigStore instance;

    public static ConfigStore getInstance(){
        if(instance == null)
            instance = new ConfigStore();
        return instance;
    }

    private String location = Configuration.CONFIG_LOCATION;
    private String fileName = "config.eson";
    private Path configPath = Path.of(location + fileName);
    private Config config;
    private File configFile;

    private ConfigStore(){

        try {
            if (!Files.exists(configPath))
                Files.createFile(configPath);
            else {
                var content = decrypt(Files.readString(configPath));
                var serializer = new Gson();
                config = serializer.fromJson(content, Config.class);
            }
        }catch (IOException e){
            var dialog = Utils.createErrorDialog(
                e,
                "Error",
                "Creating " + this.getClass().getName() + "caused an error!"
            );
            dialog.show();
        }
    }

    public Config getConfig(){
        return config;
    }

    private String decrypt(String cipher){



        return cipher;
    }

    private String encrypt(String plain){



        return plain;
    }

    private void writeFile(String content){
        try {
            Files.write(configPath, encrypt(content).getBytes());
        }catch (IOException e){
            var dialog = Utils.createErrorDialog(
              e,
              "Error writing to file",
              "An error occured while trying to write to the config file"
            );
            dialog.show();
        }
    }
}
