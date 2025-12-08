package com.example.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * MD5 加盐加密工具类
 * fyt-豆包生成2025-10-15
 */
public class MD5SaltUtils {

    /**
     * 生成随机盐值
     * @return 随机盐值（Base64编码）
     */
    public static String generateSalt() {
        // 使用安全随机数生成器
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16字节盐值，可根据需要调整
        random.nextBytes(salt);
        // 使用Base64编码方便存储
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * MD5加盐加密
     * @param original 原始字符串
     * @param salt 盐值（Base64编码）
     * @return 加密后的字符串（十六进制）
     */
    public static String encrypt(String original, String salt) {
        try {
            // 解码盐值
            byte[] saltBytes = Base64.getDecoder().decode(salt);

            // 创建MD5消息摘要
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 先更新盐值
            md.update(saltBytes);
            // 再更新原始数据
            md.update(original.getBytes());

            // 计算哈希值
            byte[] digest = md.digest();

            // 转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                // 转换为两位十六进制数
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5算法是Java标准算法，一般不会抛出此异常
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 验证原始字符串与加密字符串是否匹配
     * @param original 原始字符串
     * @param salt 盐值
     * @param encrypted 加密后的字符串
     * @return 是否匹配
     */
    public static boolean verify(String original, String salt, String encrypted) {
        String newEncrypted = encrypt(original, salt);
        return newEncrypted.equals(encrypted);
    }

    // 测试示例
    public static void main(String[] args) {
        // 原始密码
        String password = "123456";
        // 生成盐值
        String salt = generateSalt();
        System.out.println("盐值: " + salt);
        // 加密
        String encryptedPassword = encrypt(password, salt);
        System.out.println("加密后: " + encryptedPassword);
        // 验证
        boolean isMatch = verify(password, salt, encryptedPassword);
        System.out.println("验证结果: " + isMatch); // 应输出true
        // 验证错误密码
        boolean isWrongMatch = verify("wrongPassword", salt, encryptedPassword);
        System.out.println("错误密码验证结果: " + isWrongMatch); // 应输出false
    }

}