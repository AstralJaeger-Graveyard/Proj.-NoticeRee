/*
 * Copyright(c) AstralJaeger 2020.
 */

package org.astraljaeger.noticeree.DataTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import org.apache.commons.codec.DecoderException;
import org.astraljaeger.noticeree.Configuration;

import static org.apache.commons.codec.binary.Hex.encodeHex;
import static org.apache.commons.codec.binary.Hex.decodeHex;

public class KeyStore {

    private static KeyStore instance;

    public static KeyStore getInstance(){
        if(instance == null)
            instance = new KeyStore();
        return  instance;
    }

    private final String filename = "Key.bson";
    public static final String ALGORITHM = "AES";
    public static final int KEYSIZE = 128;

    @Getter
    private SecretKey key;
    private Path file;

    private KeyStore(){
        try {
            file = Paths.get(Configuration.getAppConfigDirectory() + filename);
            if(!Files.exists(Paths.get(Configuration.getAppConfigDirectory())))
                Files.createDirectories(Paths.get(Configuration.getAppConfigDirectory()));

            if(!Files.exists(file)) {
                Files.createFile(file);
                key = generateKey();
                saveKey(key);
            }else {
                key = loadKey();
            }
        }catch (IOException e){
            // TODO
        }catch (NoSuchAlgorithmException e){
            // TODO
        }
    }

    private SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEYSIZE);
        return keyGenerator.generateKey();
    }

    private void saveKey(SecretKey key) throws IOException {
        byte[] encoded = key.getEncoded();
        char[] hex = encodeHex(encoded);
        String data = String.valueOf(hex);
        Files.write(file, data.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    private SecretKey loadKey() throws IOException{
        String data = Files.readString(file);
        char[] hex = data.toCharArray();
        byte[] encoded;
        try {
            encoded = decodeHex(hex);
        }catch (DecoderException e){
            // TODO: Better error handling
            e.printStackTrace();
            return null;
        }
        return new SecretKeySpec(encoded, ALGORITHM);
    }
}
