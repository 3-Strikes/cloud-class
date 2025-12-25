package com.example.utils;


import com.example.exceptions.BusinessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 工具类
 * fyt-豆包生成-2025-10-20
 */
public class JwtTokenUtils {
    private static Logger logger = LoggerFactory.getLogger(JwtTokenUtils.class);


    // 自定义签名密钥（生产环境建议从配置文件读取，且长度至少 256 位（32 字节）以支持 HS256 算法）
    private static final String SECRET_KEY = "NJqttMcvJpcQWrxYMBQBHZ6ZXKnR9NJS6DJ38QRB/MQ=";

    /**
     * 将 SecretKey 转换为 Base64 字符串（方便存储和传输）
     * @param secretKey 密钥对象
     * @return Base64 编码的密钥字符串
     */
    public static String encodeKeyToBase64(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * 生成签名密钥（基于自定义密钥字符串，转换为 JJWT 所需的 SecretKey）
     */
    private static SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Token（不过期）
     *
     * @param subject 主题（通常为用户唯一标识，如用户 ID）
     * @return 生成的 Token 字符串
     */
    public static String generateToken(String subject) {
        return generateToken(subject, new HashMap<>(), null);
    }

    /**
     * 生成 Token（指定过期时间）
     *
     * @param subject      主题（用户唯一标识）
     * @param expireMillis 过期时间（毫秒）
     * @return 生成的 Token 字符串
     */
    public static String generateToken(String subject, Long expireMillis) {
        return generateToken(subject, new HashMap<>(), expireMillis);
    }

    /**
     * 生成 Token（携带自定义声明 + 指定过期时间）
     *
     * @param subject      主题（用户唯一标识）
     * @param claims       自定义声明（如角色、权限等附加信息）
     * @param expireMillis 过期时间（毫秒）
     * @return 生成的 Token 字符串
     */
    public static String generateToken(String subject, Map<String, Object> claims, Long expireMillis) {
        // 构建 JWT
        JwtBuilder builder = Jwts.builder();
        if(claims!=null&&!claims.isEmpty()) builder.setClaims(claims);
        builder.setSubject(subject);
        builder.setIssuedAt(new Date());
        if (expireMillis != null && expireMillis > 0) {
            // 计算过期时间（当前时间 + 过期毫秒数）
            Date expireDate = new Date(System.currentTimeMillis() + expireMillis);
            builder.setExpiration(expireDate);
        }

        builder.signWith(getSecretKey(), SignatureAlgorithm.HS256);
        builder.compressWith(CompressionCodecs.GZIP);
        String compact = builder.compact();
        return compact;
    }


    public static String generateToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims,null);
    }

    /**
     * 从 Token 中获取主题（用户 ID）
     *
     * @param token Token 字符串
     * @return 主题（用户 ID）
     */
    public static String getSubject(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从 Token 中获取指定的自定义声明
     *
     * @param token     Token 字符串
     * @param claimName 声明名称
     * @return 声明值
     */
    public static Object getClaim(String token, String claimName) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(claimName);
    }

    /**
     * 从 Token 中获取签发时间
     *
     * @param token Token 字符串
     * @return 签发时间
     */
    public static Date getIssuedAt(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * 从 Token 中获取过期时间
     *
     * @param token Token 字符串
     * @return 过期时间
     */
    public static Date getExpiration(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 验证 Token 是否有效
     * 验证逻辑：1. 签名是否正确；2. 是否未过期
     *
     * @param token Token 字符串
     * @return true-有效，false-无效
     */
    public static Claims validateToken(String token) {
        try {
            // 解析 Token 并验证签名（若签名错误或过期，会抛出异常）
            Claims body = Jwts.parser()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token).getBody();

            return body;
        } catch (Exception e) {
            throw new BusinessException("请重新登录");
        }
    }

    /**
     * 检查 Token 是否已过期
     *
     * @param token Token 字符串
     * @return true-已过期，false-未过期
     */
    public static boolean isTokenExpired(String token) {
        Date expiration = getExpiration(token);
        return expiration.before(new Date());
    }

    /**
     * 从 Token 中获取所有声明
     */
    private static Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中获取指定类型的声明
     */
    private static <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

}