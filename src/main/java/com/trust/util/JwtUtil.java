package com.trust.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.trust.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    public static boolean verify(String token, String userName) {
        try {
            // 设置加密算法
            Algorithm algorithm = Algorithm.HMAC256("落花有情");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim("userName", userName)
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
        Algorithm algorithm = Algorithm.HMAC256("落花有情");
        // 附带username信息
        return JWT.create()
                .withClaim("userName", user.getUserName())
                .withExpiresAt(date)
                .sign(algorithm);

    }

    /* *
     * @Author lsc
     * <p> 获得用户名 </p>
     * @Param [request]
     * @Return java.lang.String
     */
    public static String getUserNameByToken(HttpServletRequest request)  {
        String token = request.getHeader("token");
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("userName")
                .asString();
    }
}
