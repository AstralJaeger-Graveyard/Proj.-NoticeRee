package org.astraljaeger.noticeree.DataTools;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;

@Deprecated
public class AESCryptoUtil {

    private static final int ITERATIONS = 65536;
    private static final Charset STRING_ENCODING = StandardCharsets.UTF_8;
    private static final String CRYPT_PREFIX = "CRYPT:";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEYSIZE = 128;
    private static final byte[] SALT = {(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0};
    private SecretKeySpec secret;
    private Cipher cipher;
    private Base64 base64Encoder;

    public AESCryptoUtil() throws CryptoException {

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec;
            spec = new PBEKeySpec(getHardwareKey(), SALT, ITERATIONS, KEYSIZE);
            SecretKey tmp = factory.generateSecret(spec);
            secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            cipher = Cipher.getInstance(ALGORITHM);
            base64Encoder = new Base64();
        } catch (Exception e) {
            throw new CryptoException("Unable to initialize " + this.getClass().getSimpleName(), e);
        }
    }

    public char[] getHardwareKey() throws CryptoException {

        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface nic = NetworkInterface.getByInetAddress(address);
            if (nic != null) {
                byte[] mac = nic.getHardwareAddress();
                if (mac != null && mac.length > 0) {
                    return new String(mac, STRING_ENCODING).toCharArray();
                }
            }
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }

        RuntimeMXBean rmx = ManagementFactory.getRuntimeMXBean();
        String jvm = rmx.getName();
        String[] parts = jvm.split("@");
        if (parts.length > 0) {
            String name = parts[1];
            if (name != null && !name.isEmpty()) {
                return name.toCharArray();
            }
        }

        String name = System.getenv("COMPUTERNAME");
        if (name != null && !name.isEmpty()) {
            return name.toCharArray();
        }

        throw new CryptoException("Unable to obtain secure Key");
    }

    public String encrypt(String input) throws CryptoException {
        try{
            byte[] inputBytes = input.getBytes();
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] ciphertext = cipher.doFinal(inputBytes);
            byte[] out = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ciphertext, 0, out, iv.length, ciphertext.length);
            return CRYPT_PREFIX + base64Encoder.encode(out);
        }
        catch (Exception e){
            throw new CryptoException("Unable to encrypt", e);
        }
    }

    public String decrypt(String input) throws CryptoException {

        if (!input.startsWith(CRYPT_PREFIX))
            throw new CryptoException("Unable to decrypt, input string does not start with " + CRYPT_PREFIX);

        try {
            byte[] data = base64Encoder.decode(input.substring(CRYPT_PREFIX.length()));
            int keylen = KEYSIZE / 8;
            byte[] iv = new byte[keylen];
            System.arraycopy(data, 0, iv, 0, keylen);
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            return getString(cipher.doFinal(data, keylen, data.length - keylen));
        } catch (Exception e) {
            throw new CryptoException("Unable to decrypt", e);
        }
    }

    public String getString(byte[] data){
        StringBuilder builder = new StringBuilder();
        for(byte b : data){
            builder.append((char)b);
        }
        return builder.toString();
    }

}