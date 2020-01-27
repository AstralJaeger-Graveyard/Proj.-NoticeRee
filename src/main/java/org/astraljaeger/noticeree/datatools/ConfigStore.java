/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.datatools;

import com.google.gson.Gson;
import org.astraljaeger.noticeree.Configuration;
import org.astraljaeger.noticeree.Utils;
import org.jasypt.util.text.StrongTextEncryptor;

import javax.sound.sampled.Mixer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Logger;

import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.io.FileUtils.writeStringToFile;

public class ConfigStore {

    private static final Logger logger = Logger.getLogger(ConfigStore.class.getSimpleName());
    private static ConfigStore instance;

    public static ConfigStore getInstance(){
        if(instance == null)
            instance = new ConfigStore();
        return instance;
    }

    private String fileName = "config.ejson";
    private Path configFile;

    private HashMap<String, String> config;
    private Gson serializer;
    private StrongTextEncryptor encryptor;

    private static final String TOKEN_KEY = "token";
    private static final String USERNAME_KEY = "username";
    private static final String CHANNEL_KEY = "channel";
    private static final String DEFAULT_OUTPUT_DEVICE = "default_output_device";

    private ConfigStore(){
        serializer = new Gson();

        encryptor = new StrongTextEncryptor();
        encryptor.setPassword(getHardwareKey());

        try {
            configFile = Paths.get(Configuration.getAppConfigDirectory() + fileName);
            if (!Files.exists(Paths.get(Configuration.getAppConfigDirectory())))
                Files.createDirectories(Paths.get(Configuration.getAppConfigDirectory()));

            if(!Files.exists(configFile)){
                System.out.println("Creating config file at: " + configFile.toString());
                config = new HashMap<>();
                config.put(TOKEN_KEY, "");
                config.put(CHANNEL_KEY, "");
                config.put(USERNAME_KEY, "");
                config.put(DEFAULT_OUTPUT_DEVICE, null);
                saveConfig();
            }

            config = loadConfig();
        }
        catch (IOException e){
            flagError("Error accessing " + fileName, e);
            System.exit(1);
        }
    }

    public HashMap<String, String> getConfigItem(){
        return config;
    }

    public void setToken(String token){
        logger.info("Storing new token: " + "*".repeat(token.length()));
        config.put(TOKEN_KEY, token);
        saveConfig();
    }

    public String getToken(){
        logger.fine("Revealing token: " + "*".repeat(config.get(TOKEN_KEY).length()));
        return config.get(TOKEN_KEY);
    }

    public void setUsername(String username){
        logger.info("Storing new username: " + username);
        config.put(USERNAME_KEY, username);
        saveConfig();
    }

    public String getUsername(){
        return config.get(USERNAME_KEY);
    }

    public void setChannel(String channel){
        logger.info("Storing new channel: " + channel);
        config.put(CHANNEL_KEY, channel);
        saveConfig();
    }

    public String getChannel(){
        String channel = config.get(CHANNEL_KEY);
        logger.fine("Revealing channel name: " + channel);
        return channel;
    }

    public void setDefaultOutputDevice(Mixer.Info device){
        logger.info("Storing new default playback device: " + device.getName());
        String serialized = serializer.toJson(device);
        logger.fine("Serialized: " + serialized);
        config.put(DEFAULT_OUTPUT_DEVICE, serialized);
        saveConfig();
    }

    public Mixer.Info getDefaultOutputDevice(){
        String serialized = config.get(DEFAULT_OUTPUT_DEVICE);
        if(serialized == null){
            return null;
        }
        Mixer.Info info = serializer.fromJson(serialized, Mixer.Info.class);
        logger.fine("Revealing stored playback device: " + info.getName());
        return info;
    }

    // TODO: Add getters and setters for other properties


    // Private methods

    private void flagError(String title, Exception e){
        var dialog = Utils.createErrorDialog(
                e,
                title,
                e.getMessage()
        );
        dialog.showAndWait();
    }

    private String getHardwareKey(){
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            for(NetworkInterface nic: Collections.list(nics)){
                Enumeration<InetAddress> inetAddresses = nic.getInetAddresses();
                for(InetAddress adr : Collections.list(inetAddresses))
                    if(nic.getHardwareAddress() != null && (!adr.isAnyLocalAddress() || !adr.isLoopbackAddress()))
                        return macToString(nic.getHardwareAddress());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        RuntimeMXBean rmx = ManagementFactory.getRuntimeMXBean();
        String jvm = rmx.getName();
        String[] parts = jvm.split("@");
        if (parts.length > 0) {
            String name = parts[1];
            if (name != null && !name.isEmpty()) {
                System.out.println("Name: " + name);
                return name;
            }
        }
        return System.getenv("COMPUTERNAME");
    }

    private String macToString(byte[] bytes){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < bytes.length; i++){
            builder.append(String.format("%02X%s", bytes[i], (i < bytes.length - 1) ? "-": ""));
        }
        return builder.toString();
    }

    private void saveConfig(){
        try {
            saveConfig(config);
        }
        catch (IOException e){
            flagError("Error storing token in " + fileName, e);
            System.exit(1);
        }
    }

    private void saveConfig(HashMap<String, String> item) throws IOException {
        String serialized = serializer.toJson(item);
        String encrypted = encryptor.encrypt(serialized);
        writeStringToFile(configFile.toFile(), encrypted , StandardCharsets.UTF_8);
    }

    private HashMap<String, String> loadConfig() throws IOException {
        String raw = new String(readFileToByteArray(configFile.toFile()));
        String decrypted = encryptor.decrypt(raw);
        return serializer.fromJson(decrypted, HashMap.class);
    }
}
