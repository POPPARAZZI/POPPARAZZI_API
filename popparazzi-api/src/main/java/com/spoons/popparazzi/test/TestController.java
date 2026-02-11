package com.spoons.popparazzi.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() {
        return "Hello World!";
    }
    public static void main(String[] args) {
        try {
            // AES-256 Key 생성
            String aesKey = generateAesKey(256);
            System.out.println("=== AES Encryption Key (256bit) ===");
            System.out.println(aesKey);
            System.out.println();

            // IV (Initialization Vector) 생성 - AES는 16바이트 고정
            String iv = generateIV(16);
            System.out.println("=== IV (Initialization Vector) ===");
            System.out.println(iv);
            System.out.println();

            // application.yml 형식 출력
            System.out.println("=== application.yml에 추가 ===");
            System.out.println("encryption:");
            System.out.println("  aes:");
            System.out.println("    key: " + aesKey);
            System.out.println("    iv: " + iv);
            System.out.println();

            // 정보 출력
            System.out.println("=== 정보 ===");
            System.out.println("Key Length: " + Base64.getDecoder().decode(aesKey).length + " bytes");
            System.out.println("IV Length: " + Base64.getDecoder().decode(iv).length + " bytes");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * AES Key 생성
     * @param keySize 키 사이즈 (128, 192, 256)
     * @return Base64 인코딩된 AES Key
     */
    public static String generateAesKey(int keySize) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize, new SecureRandom());
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * IV (Initialization Vector) 생성
     * @param length IV 길이 (AES는 16바이트 고정)
     * @return Base64 인코딩된 IV
     */
    public static String generateIV(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[length];
        secureRandom.nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }
}
