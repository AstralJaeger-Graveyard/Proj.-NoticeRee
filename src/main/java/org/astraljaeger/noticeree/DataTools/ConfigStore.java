/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.DataTools;

import static org.apache.commons.codec.binary.Hex.*;
import static org.apache.commons.io.FileUtils.*;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.apache.commons.codec.DecoderException;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.Utils;

public class ConfigStore {

    private static ConfigStore instance;

    public static ConfigStore getInstance(){
        if(instance == null)
            instance = new ConfigStore();
        return instance;
    }

    private String fileName = "config.ebson";
    private Path configFile;

    private ConfigItem config;
    private Gson serializer;

    private ConfigStore(){
        serializer = new Gson();

        try {
            configFile = Paths.get(Configuration.getAppConfigDirectory() + fileName);
            if (!Files.exists(Paths.get(Configuration.getAppConfigDirectory()))) {
                Files.createDirectories(Paths.get(Configuration.getAppConfigDirectory()));
            }

            if(!Files.exists(configFile)){
                ConfigItem emptyItem = new ConfigItem();
                saveConfig(emptyItem);
            }

            config = loadConfig();
        }
        catch (IOException e){
            flagError("Error accessing " + fileName, e);
            System.exit(1);
        }
        catch (DecoderException e){
            flagError("Error decoding " + fileName, e);
            System.exit(1);
        }
    }

    private void flagError(String title, Exception e){
        var dialog = Utils.createErrorDialog(
            e,
            title,
            e.getMessage()
        );
        dialog.showAndWait();
    }

    public ConfigItem getConfigItem(){
        return config;
    }

    public void setToken(String token){
        config.token = token;
        try {
            saveConfig(config);
        }
        catch (IOException e){
            flagError("Error storing token in " + fileName, e);
            System.exit(1);
        }
    }

    private void saveConfig(ConfigItem item) throws IOException{
        String serialized = serializer.toJson(item);
        String encrypted = encrypt(serialized);
        char[] encoded = encodeHex(encrypted.getBytes());
        writeStringToFile(configFile.toFile(), encrypted, StandardCharsets.UTF_8);
    }

    private ConfigItem loadConfig() throws IOException, DecoderException {
        String data = new String(readFileToByteArray(configFile.toFile()));
        byte[] decoded = decodeHex(data);
        String plain = decrypt(String.valueOf(decoded));
        return serializer.fromJson(plain, ConfigItem.class);
    }

    private String decrypt(String cipher){
        return cipher;
    }

    private String encrypt(String plain){
        return plain;
    }
}
