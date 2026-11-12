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


               // Phân quyền đường dẫn Web MVC Thymeleaf theo 5 vai trò nghiệp vụ (RoleEnum)
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
                       ).hasRole("CENTER_MANAGER")


                       // 4. Cho phép Người thân (RELATIVE) xem chi tiết hóa đơn của người nhà (GET /billing/orders/*)
                       .requestMatchers(HttpMethod.GET, "/billing/orders/*")
                       .hasAnyRole("RECEPTIONIST", "CENTER_MANAGER", "RELATIVE")


                       // Chốt hóa đơn, xác nhận thanh toán & hủy hóa đơn chưa thanh toán (POST): Dành riêng cho Lễ tân
                       .requestMatchers(HttpMethod.POST, "/billing/generate", "/billing/orders/*/pay", "/billing/orders/*/cancel")
                       .hasRole("RECEPTIONIST")


                       // Gửi thông báo viện phí & sự cố cho thân nhân: Dành riêng cho Lễ tân
                       .requestMatchers("/billing/notifications/**")
                       .hasRole("RECEPTIONIST")


                       // Phân hệ Tài chính & Thu viện phí nội bộ (Xem bảng kê, xem chi tiết hóa đơn)
                       .requestMatchers("/billing/**")
                       .hasAnyRole("RECEPTIONIST", "CENTER_MANAGER")


                       // 5. Phân hệ Tiếp nhận: Check-in chỉ dành cho Lễ tân
                       .requestMatchers("/residents/check-in")
                       .hasRole("RECEPTIONIST")


                       // Chuyển phòng, Xuất viện: Dành riêng cho Lễ tân (Manager không làm chuyển giường và xuất viện)
                       .requestMatchers(
                               "/residents/transfer-bed",
                               "/residents/*/check-out"
                       ).hasRole("RECEPTIONIST")


                       // Thủ tục chuyển viện cấp cứu khi có tình trạng nguy cấp: Dành riêng cho Giám đốc cơ sở
                       .requestMatchers(
                               "/residents/*/hospital-transfer"
                       ).hasRole("CENTER_MANAGER")


                       // Tiếp nhận lại sau khi điều trị bệnh viện: Lễ tân & Giám đốc cơ sở
                       .requestMatchers(
                               "/residents/*/return-from-hospital"
                       ).hasAnyRole("RECEPTIONIST", "CENTER_MANAGER")


                       // Quản trị phòng & giường (Thêm, Sửa, Xóa): Dành riêng cho Giám đốc cơ sở
                       .requestMatchers(
                               "/rooms/beds/rooms/create",
                               "/rooms/beds/rooms/*/edit",
                               "/rooms/beds/rooms/*/delete",
                               "/rooms/beds/create",
                               "/rooms/beds/*/edit",
                               "/rooms/beds/*/delete"
                       ).hasRole("CENTER_MANAGER")


                       // Xác nhận hoàn tất dọn dẹp giường (MAINTENANCE -> AVAILABLE): Chỉ Điều dưỡng và Quản lý
                       .requestMatchers(HttpMethod.POST, "/rooms/beds/*/status")
                       .hasAnyRole("NURSE", "CENTER_MANAGER")


                       // Cho phép Người thân (RELATIVE) xem lịch sử bệnh án & khám bệnh của người nhà mình
                       .requestMatchers(HttpMethod.GET, "/health/history", "/health/history/*")
                       .hasAnyRole("DOCTOR", "NURSE", "CENTER_MANAGER", "RELATIVE")


                       // Tạo phiếu khám & Đo sinh hiệu: Chỉ Bác sĩ và Điều dưỡng
                       .requestMatchers("/health/records/create")
                       .hasAnyRole("DOCTOR", "NURSE")


                       // Dịch vụ chăm sóc phát sinh cho cư dân: Chỉ Điều dưỡng
                       .requestMatchers("/health/services/**")
                       .hasRole("NURSE")


                       // 6. Phân hệ Y tế, Bệnh án & Sinh hiệu: Bác sĩ, Điều dưỡng và Giám đốc (giám sát)
                       .requestMatchers("/health/**")
                       .hasAnyRole("DOCTOR", "NURSE", "CENTER_MANAGER")


                       // Quản lý kho dược phẩm & tồn kho thuốc: Giám đốc cơ sở & Bác sĩ
                       .requestMatchers("/medications/**")
                       .hasAnyRole("CENTER_MANAGER", "DOCTOR")


                       // Giải quyết sự cố y tế / cơ sở vật chất: Bác sĩ, Giám đốc cơ sở và Lễ tân
                       .requestMatchers(HttpMethod.POST, "/incidents/*/resolve")
                       .hasAnyRole("DOCTOR", "CENTER_MANAGER", "RECEPTIONIST")


                       // 7. Báo cáo Sự cố y tế: Toàn bộ nhân viên y tế, lễ tân và quản lý
                       .requestMatchers("/incidents/**")
                       .hasAnyRole("DOCTOR", "NURSE", "RECEPTIONIST", "CENTER_MANAGER")


                       // 8. Cổng thông tin dành cho Người thân (Portal): Dành riêng cho Người thân
                       .requestMatchers("/portal/**")
                       .hasRole("RELATIVE")


                       // Ca trực cá nhân: Chỉ nhân viên có ca trực (Bác sĩ, Điều dưỡng, Lễ tân)
                       .requestMatchers(
                               "/schedules/my-schedule",
                               "/schedules/*/complete"
                       ).hasAnyRole("RECEPTIONIST", "DOCTOR", "NURSE")


                       // 9. Xem danh sách cư dân, sơ đồ phòng giường
                       .requestMatchers(
                               "/residents/**",
                               "/rooms/beds/**"
                       ).hasAnyRole("RECEPTIONIST", "DOCTOR", "NURSE", "CENTER_MANAGER")


                       // 10. Hồ sơ cá nhân (Profile) dành cho tất cả các vai trò sau khi đăng nhập
                       .requestMatchers("/profile/**").authenticated()


                       // 11. Dashboard trang chủ và tất cả các trang giao diện còn lại yêu cầu đăng nhập
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

