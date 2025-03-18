package com.jjangtrio.veteran.ServerApplication.config;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class AESUtil {

    private final AESConfig aesConfig;

    // 생성자 주입 방식
    public AESUtil(AESConfig aesConfig) {
        this.aesConfig = aesConfig;
    }

    // AES 키 길이를 16, 24, 32바이트로 맞추는 유틸리티 함수
    private byte[] fixKeyLength(String key, int length) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] fixedKey = new byte[length];
        System.arraycopy(keyBytes, 0, fixedKey, 0, Math.min(keyBytes.length, length));
        return fixedKey;
    }

    // 암호화 메서드
    public String encrypt(String value) throws Exception {
        String iv = aesConfig.getIv();
        String secretKey = aesConfig.getSecretKey();

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(fixKeyLength(secretKey, 16), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(fixKeyLength(iv, 16));

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted);
    }

    // 복호화 메서드
    public String decrypt(String encrypted) throws Exception {
        String iv = aesConfig.getIv();
        String secretKey = aesConfig.getSecretKey();

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(fixKeyLength(secretKey, 16), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(fixKeyLength(iv, 16));

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
        byte[] decrypted = cipher.doFinal(decodedBytes);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
