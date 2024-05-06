package com.todo.filter;

import com.todo.entity.User;
import com.todo.exception.CustomAuthenticationEntryPoint;
import com.todo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        if (matcher.match("/user/login", requestURI) ||
                matcher.match("/user/register", requestURI) ||
                matcher.match("/user/getEmailCodeKey", requestURI) ||
                matcher.match("/user/modifyPassword", requestURI) ||
                matcher.match("/doc.html", requestURI) ||
                matcher.match("/swagger-ui/**", requestURI) ||
                matcher.match("/swagger-ui.html", requestURI) ||
                matcher.match("/v3/**", requestURI) ||
                matcher.match("/webjars/**", requestURI) ||
                request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 从 http 请求头中取出 token
        String token = request.getHeader("token");
        if (StringUtils.hasText(token)){
            User user = JwtUtil.getUserByToken(token);
            if(JwtUtil.verify(token, user)){
                UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.authenticated(user, null, AuthorityUtils.NO_AUTHORITIES);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request, response);
            }else {
                new CustomAuthenticationEntryPoint().commence(request, response, new AuthenticationException("认证失败") {});
            }
        }else {
            new CustomAuthenticationEntryPoint().commence(request, response, new AuthenticationException("认证失败") {});
        }
    }
}
