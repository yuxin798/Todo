package com.todo.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.todo.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;

public class JwtUtil {

    // Token过期时间30天
    public static final long EXPIRE_TIME = 30L * 60 * 1000 * 48 * 30;

    /* *
     * @Author lsc
     * <p> 校验token是否正确 </p>
     * @Param token
     * @Param username
     * @Param secret
     * @Return boolean
     */
    public static boolean verify(String token, User user) {
        try {
            // 设置加密算法
            Algorithm algorithm = Algorithm.HMAC256(user.getUserId() + "Todo" + user.getEmail());
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim("userId", user.getUserId())
                    .withClaim("userName", user.getUserName())
                    .withClaim("email", user.getEmail())
                    .build();
            // 效验TOKEN
            DecodedJWT jwt = verifier.verify(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }



    /* *
     * @Author lsc
     * <p>生成签名,30天后过期 </p>
     * @Param [username, secret]
     * @Return java.lang.String
     */
    public static String sign(User user) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        Algorithm algorithm = Algorithm.HMAC256(user.getUserId() + "Todo" + user.getEmail());
        // 附带username信息
        return JWT.create()
                .withClaim("userId", user.getUserId())
                .withClaim("userName", user.getUserName())
                .withClaim("email", user.getEmail())
                .withExpiresAt(date)
                .sign(algorithm);
    }

    /* *
     * @Author lsc
     * <p> 获得用户名 </p>
     * @Param [request]
     * @Return java.lang.String
     */
    public static User getUserByToken(String token)  {
        DecodedJWT jwt = JWT.decode(token);
        Long userId = jwt.getClaim("userId").asLong();
        String userName = jwt.getClaim("userName").asString();
        String email = jwt.getClaim("email").asString();
        return new User(userId, userName, email);
    }
}
