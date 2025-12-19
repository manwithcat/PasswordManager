package com.passwordmanager.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncryptionManager {
    
    public enum EncryptionType {
        BASE64("BASE64"),
        MD5("MD5"),
        AES_WITH_SALT("AES_WITH_SALT"),
        FEISTEL("FEISTEL"),
        PLAINTEXT("PLAINTEXT");
        
        private final String displayName;
        
        EncryptionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private static final byte[] SECRET_KEY = generateSecretKey();
    private static final String SALT_PREFIX = "PM_SALT_";
    
    private static byte[] generateSecretKey() {
        String hardcodedKey = "PasswordManagerSecretKey2024_JDK17_v1";
        return hardcodedKey.getBytes();
    }
    
    public static String encryptBase64(String plaintext) {
        return Base64.getEncoder().encodeToString(plaintext.getBytes());
    }
    
    public static String decryptBase64(String encrypted) {
        try {
            return new String(Base64.getDecoder().decode(encrypted));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Base64 decryption error: " + e.getMessage());
        }
    }
    
    public static String encryptMD5(String plaintext) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(plaintext.getBytes());
            return bytesToHex(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not supported: " + e.getMessage());
        }
    }
    
    public static String encryptAESWithSalt(String plaintext) {
        String salt = generateSalt();
        String saltedPassword = SALT_PREFIX + salt + plaintext;
        
        try {
            byte[] encrypted = xorEncrypt(saltedPassword.getBytes(), SECRET_KEY);
            String result = Base64.getEncoder().encodeToString(encrypted);
            return salt + ":" + result;
        } catch (Exception e) {
            throw new RuntimeException("AES encryption error: " + e.getMessage());
        }
    }
    
    public static String decryptAESWithSalt(String encrypted) {
        try {
            String[] parts = encrypted.split(":", 2);
            if (parts.length != 2) {
                throw new RuntimeException("Invalid encrypted data format");
            }
            
            String salt = parts[0];
            String encryptedData = parts[1];
            
            byte[] decrypted = xorDecrypt(Base64.getDecoder().decode(encryptedData), SECRET_KEY);
            String result = new String(decrypted);
            
            if (result.startsWith(SALT_PREFIX + salt)) {
                return result.substring((SALT_PREFIX + salt).length());
            } else {
                throw new RuntimeException("Salt mismatch");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("AES decryption error: " + e.getMessage());
        }
    }
    
    public static String encryptFeistel(String plaintext) {
        byte[] data = plaintext.getBytes();
        
        if (data.length % 2 != 0) {
            byte[] padded = new byte[data.length + 1];
            System.arraycopy(data, 0, padded, 0, data.length);
            padded[data.length] = 0;
            data = padded;
        }
        
        byte[] encrypted = new byte[data.length];
        
        for (int round = 0; round < 16; round++) {
            for (int i = 0; i < data.length; i += 2) {
                int left = data[i] & 0xFF;
                int right = data[i + 1] & 0xFF;
                
                int f = feistelFunction(right, round);
                
                int newLeft = right;
                int newRight = left ^ f;
                
                encrypted[i] = (byte) newLeft;
                encrypted[i + 1] = (byte) newRight;
            }
            System.arraycopy(encrypted, 0, data, 0, encrypted.length);
        }
        
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decryptFeistel(String encrypted) {
        byte[] data = Base64.getDecoder().decode(encrypted);
        byte[] decrypted = new byte[data.length];
        
        for (int round = 15; round >= 0; round--) {
            for (int i = 0; i < data.length; i += 2) {
                int left = data[i] & 0xFF;
                int right = data[i + 1] & 0xFF;
                
                int f = feistelFunction(left, round);
                
                int newLeft = right ^ f;
                int newRight = left;
                
                decrypted[i] = (byte) newLeft;
                decrypted[i + 1] = (byte) newRight;
            }
            System.arraycopy(decrypted, 0, data, 0, decrypted.length);
        }
        
        return new String(decrypted);
    }
    
    private static int feistelFunction(int input, int round) {
        int key = SECRET_KEY[round % SECRET_KEY.length] & 0xFF;
        return (input ^ key ^ round) & 0x0F;
    }
    
    private static byte[] xorEncrypt(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }
    
    private static byte[] xorDecrypt(byte[] data, byte[] key) {
        return xorEncrypt(data, key);
    }
    
    private static String generateSalt() {
        return String.format("%016x", System.nanoTime());
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    public static String encrypt(String plaintext, EncryptionType type) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        
        return switch (type) {
            case BASE64 -> encryptBase64(plaintext);
            case MD5 -> encryptMD5(plaintext);
            case AES_WITH_SALT -> encryptAESWithSalt(plaintext);
            case FEISTEL -> encryptFeistel(plaintext);
            case PLAINTEXT -> plaintext;
        };
    }
    
    public static String decrypt(String encrypted, EncryptionType type) {
        if (encrypted == null || encrypted.isEmpty()) {
            return encrypted;
        }
        
        return switch (type) {
            case BASE64 -> decryptBase64(encrypted);
            case MD5 -> encrypted;
            case AES_WITH_SALT -> decryptAESWithSalt(encrypted);
            case FEISTEL -> decryptFeistel(encrypted);
            case PLAINTEXT -> encrypted;
        };
    }
    
    public static boolean isReversible(EncryptionType type) {
        return type != EncryptionType.MD5;
    }
}
