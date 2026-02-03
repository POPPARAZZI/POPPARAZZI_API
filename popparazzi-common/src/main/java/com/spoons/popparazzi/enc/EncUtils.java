package com.spoons.popparazzi.enc;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class EncUtils {

    private static Map<String, String> keys = Map.of();
    private static Map<String, String> ivs = Map.of();


    public EncUtils(Map<String, String> keys, Map<String, String> ivs) {
        this.keys = keys;
        this.ivs = ivs;
    }

    public static String encrypt(String text, String section) throws Exception {
        String key = keys.get(section);
        String iv = ivs.get(section);

        if (key == null || iv == null) {
            throw new IllegalArgumentException("Invalid section provided for encryption");
        }
        return AES256.encrypt(text, key, iv);
    }

    public static String decrypt(String cipherText, String section) throws Exception {
        String key = keys.get(section);
        String iv = ivs.get(section);
        if (key == null || iv == null) {
            throw new IllegalArgumentException("Invalid section provided for decryption");
        }
        return AES256.decrypt(cipherText, key, iv);
    }


    public static class AES256 {

        /**
         * Modes of Operation:
         * ECB (Electronic Codebook)
         * CBC (Cipher Block Chaining)
         * CFB (Cipher Feedback)
         * OFB (Output Feedback)
         * CTR (Counter)
         * GCM (Galois/Counter Mode)
         * Padding Schemes:
         * NoPadding
         * PKCS5Padding (often used interchangeably with PKCS7Padding in Java)
         * ISO10126Padding
         * ZeroBytePadding
         */
        // 사용할 알고리즘/블록암호화 운용 모드/패딩 선택(패딩사용안함, AES기본패딩방식 = PKCS#7)
//        public static String alg = "AES/CTR/NoPadding";
//        public static String alg = "AES/CBC/PKCS5Padding";
        public static String alg = "AES/CBC/PKCS5Padding";

        // 암호화 기능 수행 : 입력받은 문자열을 AES256 알고리즘으로 암호화한 뒤, Base64 인코딩하여 암호화된 문자열을 반환
        public static String encrypt(String text, String key, String iv) throws Exception {

            // Base64로 인코딩된 문자열을 바이트 배열로 디코딩하는 부분
            byte[] key_byte = Base64.getDecoder().decode(key);
            byte[] iv_byte = Base64.getDecoder().decode(iv);

            // 암호화 과정에서 사용되는 암호화 인스턴스 얻음
            Cipher cipher = Cipher.getInstance(alg);

            // 바이트배열로부터 SecretKey 생성
            SecretKeySpec keySpec = new SecretKeySpec(key_byte, "AES");

            // 바이트배열로부터 초기화벡터 생성
            IvParameterSpec ivParamSpec = new IvParameterSpec(iv_byte);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec); // 인스턴스 초기화 (암호화 모드 : Cipher.ENCRYPT_MODE)

            // 입력받은 문자열을 'UTF-8'인코딩으로 변환한 후, doFinal()메서드를 통해 데이터를 암,복호화
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

            // byte[] 타입의 암호화된 데이터를 Base64 인코딩을 수행한 후, String 형태로 반환
            return Base64.getEncoder().encodeToString(encrypted);

        }

        // 복호화 기능 수행 : 암호화된 문자열을 입력받아, Base64 디코딩 및 AES256 알고리즘으로 복호화한 후, 원래 문자열을 반환
        public static String decrypt(String cipherText, String key, String iv) throws Exception {

            byte[] key_byte = Base64.getDecoder().decode(key);
            byte[] iv_byte = Base64.getDecoder().decode(iv);

            Cipher cipher = Cipher.getInstance(alg);
            SecretKeySpec keySpec = new SecretKeySpec(key_byte, "AES");
            IvParameterSpec ivParamSpec = new IvParameterSpec(iv_byte);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);

            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decodedBytes);

            return new String(decrypted, StandardCharsets.UTF_8);

        }
    }
}
