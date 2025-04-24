package com.cq.RssHub.utils;
import com.cq.RssHub.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Redis中存储黑名单token的前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";

    // 生成签名密钥
    private Key getSigningKey() {
        // 确保秘钥长度至少为256位（32字节）
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);

        // 如果秘钥太短，可以使用以下方法生成安全的秘钥
        return Keys.hmacShaKeyFor(ensureKeyLength(keyBytes));
    }
    // 确保密钥长度至少为32字节的辅助方法
    private byte[] ensureKeyLength(byte[] originalKey) {
        if (originalKey.length >= 32) {
            return originalKey;
        }

        // 如果原始密钥太短，使用填充或重复方法扩展
        byte[] extendedKey = new byte[32];
        for (int i = 0; i < 32; i++) {
            extendedKey[i] = originalKey[i % originalKey.length];
        }
        return extendedKey;
    }

    // 从token中获取用户名
    public String getUsernameFromToken(String token) {
        // 先检查token是否在黑名单中
        if (isTokenInBlacklist(token)) {
            return null;
        }
        return getClaimFromToken(token, Claims::getSubject);
    }

    // 从token中获取map

    // 获取token过期时间
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // 通用的获取Claim的方法
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 解析token的所有声明
    private Claims getAllClaimsFromToken(String token) {
        // 如果token以"Bearer "开头，则去掉这部分
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 检查token是否已过期
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // 生成token
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, username);
    }

    // 生成token的具体实现
    public String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    // 根据map创建token
    public String mapGenerateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 验证token是否有效
    public Boolean validateToken(String token, String username) {
        // 检查token是否在黑名单中
        if (isTokenInBlacklist(token)) {
            return false;
        }

        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token));
    }

    // 刷新token
    public String refreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        claims.setIssuedAt(new Date());
        claims.setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()));

        return Jwts.builder()
                .setClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // === 新增的方法，用于实现退出登录功能 ===

    /**
     * 使令牌失效（加入黑名单）
     * @param token JWT令牌
     * @return 是否成功使令牌失效
     */
    public boolean invalidateToken(String token) {
        try {
            // 处理Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 从token中获取过期时间
            Date expiration = getExpirationDateFromToken(token);

            if (expiration != null) {
                // 计算剩余有效时间（毫秒）
                long remainingTime = expiration.getTime() - System.currentTimeMillis();

                if (remainingTime > 0) {
                    // 将token加入Redis黑名单，过期时间设置为token的剩余有效期
                    String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
                    redisTemplate.opsForValue().set(blacklistKey, "1", remainingTime, TimeUnit.MILLISECONDS);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查token是否在黑名单中
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
    public boolean isTokenInBlacklist(String token) {
        try {
            // 处理Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            return false;
        }
    }
}