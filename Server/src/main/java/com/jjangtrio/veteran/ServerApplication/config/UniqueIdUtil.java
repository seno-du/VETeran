package com.jjangtrio.veteran.ServerApplication.config;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.security.MessageDigest;

public class UniqueIdUtil {
    public static String generateOrderId() {
        return UUID.randomUUID().toString().replaceAll("-", ""); // 하이픈 제거 후 32자리
    }

    public static String generatePaymentId() {
        return UUID.randomUUID().toString().replaceAll("-", ""); // 하이픈 제거 후 32자리
    }

    // SHA-256 해시 함수 (자동 고유 값 생성)
    public static String generateEncryptedOrderId() throws NoSuchAlgorithmException {
        String input = UUID.randomUUID().toString() + System.currentTimeMillis(); // UUID와 현재 시간으로 고유한 입력 값 생성
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedHash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString().substring(0, 32); // 32자
    }

    // SHA-256을 통한 paymentId 생성 (자동 고유 값 생성)
    public static String generateEncryptedPaymentId() throws NoSuchAlgorithmException {
        String input = UUID.randomUUID().toString() + System.currentTimeMillis(); // UUID와 현재 시간으로 고유한 입력 값 생성
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedHash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString().substring(0, 32); // 32자
    }
}
