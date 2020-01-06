/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.DataTools;

import static org.apache.commons.codec.binary.Hex.*;
import static org.apache.commons.io.FileUtils.*;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Hex;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

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
            if (!Files.exists(Paths.get(Configuration.getAppConfigDirectory())))
                Files.createDirectories(Paths.get(Configuration.getAppConfigDirectory()));


            if(!Files.exists(configFile)){
                System.out.println("Creating config file at: " + configFile.toString());
                ConfigItem emptyItem = new ConfigItem();
                emptyItem.setToken("");
                saveConfig(emptyItem);
            }

            config = loadConfig();
        }
        catch (IOException e){
            flagError("Error accessing " + fileName, e);
            System.exit(1);
        }
        catch (CryptoException e){

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
        config.setToken(token);
        saveConfig();
    }

    public String getToken(){
        return config.getToken();
    }

    private void saveConfig(){
        try {
            saveConfig(config);
        }
        catch (CryptoException | IOException e){
            flagError("Error storing token in " + fileName, e);
            System.exit(1);
        }
    }

    private void saveConfig(ConfigItem item) throws IOException, CryptoException {
        String serialized = serializer.toJson(item);
        String encrypted = encrypt(serialized);
        writeStringToFile(configFile.toFile(), String.valueOf(encrypted) , StandardCharsets.UTF_8);
    }

    private ConfigItem loadConfig() throws IOException, CryptoException {
        String data = new String(readFileToByteArray(configFile.toFile()));
        String decrypted = decrypt(data);
        return serializer.fromJson(decrypted, ConfigItem.class);
    }

    private String decrypt(String encrypted) throws CryptoException{
        try {
            Key key = KeyStore.getInstance().getKey();
            Cipher cipher = Cipher.getInstance(KeyStore.ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return String.valueOf(cipher.doFinal(encrypted.getBytes()));
        }catch (NoSuchAlgorithmException |
                NoSuchPaddingException |
                InvalidKeyException |
                BadPaddingException |
                IllegalBlockSizeException e){
            throw new CryptoException("Error decrypting " + fileName, e);
        }
    }

    private String encrypt(String plain) throws CryptoException {
        try{
            Key key = KeyStore.getInstance().getKey();
            Cipher cipher = Cipher.getInstance(KeyStore.ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plain.getBytes());
            return String.valueOf(encrypted);
        }catch (NoSuchAlgorithmException |
                NoSuchPaddingException |
                InvalidKeyException |
                BadPaddingException |
                IllegalBlockSizeException e){
            throw new CryptoException("Error encrypting " + fileName, e);
        }
    }
}
