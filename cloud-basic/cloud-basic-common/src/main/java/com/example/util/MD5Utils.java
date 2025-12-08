
package com.example.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 无盐值MD5加密工具类
 * 说明：纯MD5加密安全性较低（易被彩虹表破解），仅建议用于兼容性场景，新系统推荐使用SHA-256/BCrypt等算法
 */
public class MD5Utils {

    // 加密算法名称常量
    private static final String MD5_ALGORITHM = "MD5";
    // 字符编码（固定UTF-8，避免系统默认编码差异）
    private static final String CHARSET = StandardCharsets.UTF_8.name();

    /**
     * 私有化构造方法，禁止实例化工具类
     */
    private MD5Utils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 标准32位MD5加密（小写）
     * @param original 原始字符串（不可为null）
     * @return 32位小写MD5加密结果
     */
    public static String encrypt32(String original) {
        // 空值校验
        if (original == null) {
            throw new IllegalArgumentException("Original string cannot be null");
        }

        try {
            // 初始化MD5消息摘要
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            // 将字符串转为UTF-8字节数组并更新摘要
            byte[] digest = md.digest(original.getBytes(CHARSET));
            // 转换为32位十六进制字符串
            return bytesToHex(digest);
        } catch (Exception e) {
            // MD5是JDK内置算法，理论上不会抛出此异常，此处转为运行时异常
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 16位MD5加密（小写，截取32位结果的第8-24位）
     * @param original 原始字符串（不可为null）
     * @return 16位小写MD5加密结果
     */
    public static String encrypt16(String original) {
        String md532 = encrypt32(original);
        // 截取16位结果（常用的8-24位区间）
        return md532.substring(8, 24);
    }

    /**
     * 验证原始字符串与MD5加密结果是否匹配
     * @param original 原始字符串
     * @param encrypted MD5加密结果（32位/16位均可）
     * @return 是否匹配
     */
    public static boolean verify(String original, String encrypted) {
        if (original == null || encrypted == null) {
            return false;
        }
        // 根据加密结果长度匹配对应验证逻辑
        if (encrypted.length() == 32) {
            return encrypt32(original).equals(encrypted);
        } else if (encrypted.length() == 16) {
            return encrypt16(original).equals(encrypted);
        }
        return false;
    }

    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 加密后的字节数组
     * @return 小写十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            // 转换为两位十六进制数（补零，避免单字符）
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // 测试示例
    public static void main(String[] args) {
        // 原始字符串
        String original = "123456";
        
        // 32位MD5加密
        String md532 = encrypt32(original);
        System.out.println("32位MD5结果：" + md532); // 输出：e10adc3949ba59abbe56e057f20f883e
        
        // 16位MD5加密
        String md516 = encrypt16(original);
        System.out.println("16位MD5结果：" + md516); // 输出：49ba59abbe56e057
        
        // 验证匹配
        boolean match32 = verify(original, md532);
        System.out.println("32位验证结果：" + match32); // true
        
        // 验证不匹配
        boolean mismatch = verify("654321", md532);
        System.out.println("错误字符串验证结果：" + mismatch); // false
    }
}