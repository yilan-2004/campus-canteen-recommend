package com.example.shitang.config;

import com.example.shitang.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/health",
                                // 第四阶段:推荐接口允许匿名访问(冷启动场景)
                                "/api/recommend/hot",
                                "/api/recommend/high-score",
                                "/api/recommend/scenarios",
                                // 第五阶段:推荐评估接口(给老师演示)
                                "/api/recommend/eval/**",
                                // 第五阶段:API 文档
                                "/doc.html",
                                "/doc.html/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/uploads/**"
                        ).permitAll()
                        // 菜品匿名可读,但写操作需要 ADMIN/MERCHANT
                        .requestMatchers(HttpMethod.GET, "/api/dishes", "/api/dishes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/dishes").hasAnyRole("ADMIN", "MERCHANT")
                        .requestMatchers(HttpMethod.PUT, "/api/dishes/**").hasAnyRole("ADMIN", "MERCHANT")
                        .requestMatchers(HttpMethod.DELETE, "/api/dishes/**").hasAnyRole("ADMIN", "MERCHANT")
                        // 管理员/商户专属接口
                        .requestMatchers("/api/orders/admin", "/api/orders/*/status").hasAnyRole("ADMIN", "MERCHANT")
                        .requestMatchers("/api/tags/**", "/api/dish-categories/**", "/api/canteens/**", "/api/canteen-windows/**")
                                .hasAnyRole("ADMIN", "MERCHANT")
                        .requestMatchers("/api/comments/admin", "/api/comments/*/reply").hasAnyRole("ADMIN", "MERCHANT")
                        .requestMatchers("/api/stats").hasAnyRole("ADMIN", "MERCHANT")
                        .requestMatchers("/api/export/**").hasAnyRole("ADMIN", "MERCHANT")
                        // 用户管理:列表/状态/角色仅管理员;个人信息/密码任意登录用户
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/status", "/api/users/*/role").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/profile", "/api/users/password").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":403,\"message\":\"没有访问权限\",\"data\":null}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
