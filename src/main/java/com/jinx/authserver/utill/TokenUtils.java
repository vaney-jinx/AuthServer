package com.jinx.authserver.utill;

import com.jinx.authserver.bean.AuthResponseData;
import com.jinx.authserver.bean.GetTokenParam;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class TokenUtils {
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    private int EXPIRATION_TIME = 30;
    private String secret = "secret";
    private String scopeKey = "scopeKey";
    private String clientIdKey = "clientIdKey";
    private String clientSecretKey = "clientSecret";
    private String redisTokenKey = "redisTokenKey";

    public String createTokenReturnCode(String clientId, String clientSecret, String scope) {
        Map<String, Object> claims = new HashMap<>(16);
        claims.put(scopeKey, scope);
        claims.put(clientIdKey, clientId);
        claims.put(clientSecretKey, clientSecret);
        JwtBuilder jwtBuilder = Jwts.builder().setClaims(claims);
        String token = jwtBuilder.signWith(SignatureAlgorithm.HS512, secret).compact();
        String code = getRedisTokenKey();
        refreshTokenExpirationTime(code, token);
        return code;
    }


    /**
     * 获取token
     */
    public AuthResponseData getToken(GetTokenParam getTokenParam) throws Exception {
        try {
            String code = getTokenParam.getCode();
            String token = redisTemplate.opsForValue().get(code);
            if(StringUtils.isEmpty(token))
                throw new Exception("code错误");
            Claims claims = getClaimsFromToken(token);
            String clientId = getTokenParam.getClientId();
            String clientSecret = getTokenParam.getClientSecret();
            String scope = getTokenParam.getScope();
            String clientIdToken = claims.get(clientIdKey).toString();
            String clientSecretToken = claims.get(clientSecretKey).toString();
            String scopeToken = claims.get(scopeKey).toString();
            if (clientId.equals(clientIdToken) && clientSecret.equals(clientSecretToken) && scope.equals(scopeToken)){
                return AuthResponseData.build(token,
                        "refreshToken:" + token,
                        redisTemplate.opsForValue().getOperations().getExpire(code));
            }
            throw new Exception("认证出错");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("认证出错");
        }
    }

    private Claims getClaimsFromToken(String token) throws Exception {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 设置token 过期时间
     *
     * @param tokenKey key
     * @param token    value
     */
    private void refreshTokenExpirationTime(String tokenKey, String token) {
        if (EXPIRATION_TIME > 0) {
            redisTemplate.opsForValue().set(tokenKey, token, EXPIRATION_TIME, TimeUnit.MINUTES);
        }
    }

    /**
     * 创建redis token key
     *
     * @return tokenkey
     */
    private String getRedisTokenKey() {
        return UUID.randomUUID().toString() + System.currentTimeMillis();
    }
}
