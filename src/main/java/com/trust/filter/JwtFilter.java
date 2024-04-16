package com.trust.filter;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trust.user.entity.User;
import com.trust.user.service.impl.UserServiceImpl;
import com.trust.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        if (matcher.match("/trust/user/login", requestURI) ||
                matcher.match("/trust/user/register", requestURI) ||
                request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 从 http 请求头中取出 token
        String token = request.getHeader("token");
        if (token != null){
            String userName = JwtUtil.getUserNameByToken(request);
            // 这边拿到的 用户名 应该去数据库查询获得密码，简略，步骤在service直接获取密码
            boolean result = JwtUtil.verify(token, userName);
            if(result){
                UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.authenticated(userName, null, AuthorityUtils.NO_AUTHORITIES);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request, response);;
            }
        }
        filterChain.doFilter(request, response);

    }
}
