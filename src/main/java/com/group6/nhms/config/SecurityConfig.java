package com.group6.nhms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF để thuận tiện cho việc phát triển và gọi API
                .csrf(AbstractHttpConfigurer::disable)

                // Phân quyền đường dẫn
                .authorizeHttpRequests(auth -> auth
                        // 1. Cho phép truy cập Swagger UI & OpenAPI Documentation không cần đăng nhập
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // 2. Cho phép truy cập file tĩnh của giao diện (CSS, JS, Fonts, Images)
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**",
                                "/favicon.ico"
                        ).permitAll()

                        // 3. Cho phép truy cập các trang xác thực và trang lỗi
                        .requestMatchers("/login", "/register", "/error").permitAll()

                        // 4. Cho phép gọi các REST API trong giai đoạn phát triển & test
                        .requestMatchers("/api/**").permitAll()

                        // 5. Tất cả các trang Web view Thymeleaf còn lại yêu cầu phải đăng nhập
                        .anyRequest().authenticated()
                )

                // Cấu hình Form Login sử dụng Session (phù hợp cho Thymeleaf)
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                // Hỗ trợ HTTP Basic Authentication cho Postman hoặc cURL
                .httpBasic(Customizer.withDefaults())

                // Cấu hình Đăng xuất
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
