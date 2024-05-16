package com.todo.config;

import com.todo.filter.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //authorizeRequests()：开启授权保护
        //anyRequest()：对所有请求开启授权保护
        //authenticated()：已认证请求会自动被授权
        return http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/user/login").anonymous()
                        .requestMatchers("/user/register").anonymous()
                        .requestMatchers("/user/getEmailCodeKey").anonymous()
                        .requestMatchers("/user/modifyPassword").anonymous()
                        .requestMatchers("/doc.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
