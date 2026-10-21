package com.group6.nhms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                // Tắt CSRF tạm thời để dễ test (khi nào làm form HTML chuẩn thì bật lại sau)
                .csrf(AbstractHttpConfigurer::disable)
                // Phân quyền các đường dẫn
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập file tĩnh (CSS, JS, Hình ảnh) của Thymeleaf không cần đăng nhập
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        // Cho phép truy cập trang đăng nhập, đăng ký
                        .requestMatchers("/login", "/register").permitAll()
                        // Tất cả các trang còn lại bắt buộc phải đăng nhập
                        .anyRequest().authenticated()
                )

                // Cấu hình Form Login sử dụng Session (Phù hợp với Thymeleaf)
                .formLogin(form -> form
                        // .loginPage("/login") // Mở comment dòng này khi bạn đã tự thiết kế xong file login.html trong Thymeleaf
                        .defaultSuccessUrl("/", true) // Đăng nhập thành công thì chuyển về trang chủ
                        .permitAll()
                )

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
}