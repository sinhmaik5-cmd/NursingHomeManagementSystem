package com.group6.nhms.config;

import com.group6.nhms.security.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
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

    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF để thuận tiện cho việc submit các form Thymeleaf
                .csrf(AbstractHttpConfigurer::disable)

                // Phân quyền đường dẫn Web MVC Thymeleaf theo vai trò nghiệp vụ
                .authorizeHttpRequests(auth -> auth
                        // 1. Tài nguyên tĩnh giao diện (CSS, JS, Fonts, Images, Icons) và Landing page
                        .requestMatchers(
                                "/",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**",
                                "/favicon.ico"
                        ).permitAll()

                        // 2. Trang công khai không cần đăng nhập
                        .requestMatchers("/login", "/register", "/error", "/403").permitAll()

                        // 3. Phân hệ Quản trị Nhân sự & Phân ca trực: Dành riêng cho Giám đốc cơ sở
                        .requestMatchers(
                                "/management/**",
                                "/schedules/assign"
                        ).hasAnyRole("CENTER_MANAGER", "ADMIN")

                        // 4. Cho phép Người thân (RELATIVE) xem chi tiết hóa đơn của người nhà (GET /billing/orders/*)
                        .requestMatchers(HttpMethod.GET, "/billing/orders/*")
                        .hasAnyRole("RECEPTIONIST", "CENTER_MANAGER", "ADMIN", "ACCOUNTANT", "RELATIVE")

                        // Phân hệ Tài chính & Thu viện phí nội bộ (Chốt hóa đơn, xác nhận thanh toán POST /pay)
                        // Do Lễ tân kiêm thu ngân thực hiện, Giám đốc có quyền xem & duyệt
                        .requestMatchers("/billing/**")
                        .hasAnyRole("RECEPTIONIST", "CENTER_MANAGER", "ADMIN", "ACCOUNTANT")

                        // 5. Phân hệ Tiếp nhận & Quản lý cư dân: Check-in, Chuyển phòng, Xuất viện
                        // Do Lễ tân chính thực hiện, Giám đốc toàn quyền
                        .requestMatchers(
                                "/residents/check-in",
                                "/residents/transfer-bed",
                                "/residents/*/check-out"
                        ).hasAnyRole("RECEPTIONIST", "CENTER_MANAGER", "ADMIN", "MANAGER")

                        // Cho phép Người thân (RELATIVE) xem lịch sử bệnh án & khám bệnh của người nhà mình
                        .requestMatchers(HttpMethod.GET, "/health/history", "/health/history/*")
                        .hasAnyRole("DOCTOR", "NURSE", "CENTER_MANAGER", "ADMIN", "RELATIVE")

                        // Hủy phiếu khám & hoàn trả kho thuốc: Chỉ Bác sĩ, Giám đốc cơ sở & Quản trị viên
                        .requestMatchers("/health/records/*/cancel")
                        .hasAnyRole("DOCTOR", "CENTER_MANAGER", "ADMIN")

                        // 6. Phân hệ Y tế, Bệnh án & Sinh hiệu: Bác sĩ (khám & kê đơn) và Điều dưỡng (đo sinh hiệu)
                        .requestMatchers("/health/**")
                        .hasAnyRole("DOCTOR", "NURSE", "CENTER_MANAGER", "ADMIN")

                        // Xử lý sự cố y tế: Chỉ Bác sĩ, Giám đốc cơ sở & Quản trị viên
                        .requestMatchers("/incidents/*/resolve")
                        .hasAnyRole("DOCTOR", "CENTER_MANAGER", "ADMIN")

                        // 7. Báo cáo & Xử lý Sự cố y tế
                        // Toàn bộ nhân viên y tế, lễ tân và quản lý
                        .requestMatchers("/incidents/**")
                        .hasAnyRole("DOCTOR", "NURSE", "RECEPTIONIST", "CENTER_MANAGER", "ADMIN")

                        // 8. Cổng thông tin dành cho Người thân (Portal)
                        .requestMatchers("/portal/**")
                        .hasAnyRole("RELATIVE", "CENTER_MANAGER", "ADMIN")

                        // 9. Xem danh sách cư dân, sơ đồ phòng giường, ca trực cá nhân
                        // Dành cho toàn bộ nhân viên nội bộ
                        .requestMatchers(
                                "/residents/**",
                                "/rooms/beds/**",
                                "/schedules/my-schedule",
                                "/schedules/*/complete"
                        ).hasAnyRole("RECEPTIONIST", "DOCTOR", "NURSE", "CENTER_MANAGER", "ADMIN", "MANAGER", "STAFF")

                        // 10. Dashboard trang chủ và tất cả các trang giao diện còn lại yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )

                // Cấu hình Form Login thuần Thymeleaf với Session và Handler điều hướng
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // Xử lý khi bị từ chối quyền truy cập (403 Forbidden)
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403")
                )

                // Cấu hình Đăng xuất (Logout)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
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
