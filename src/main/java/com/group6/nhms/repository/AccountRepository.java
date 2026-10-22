package com.group6.nhms.repository;

import com.group6.nhms.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    // Tìm tài khoản theo username để xử lý Đăng nhập
    Optional<Account> findByUsername(String username);

    // Kiểm tra xem username đã tồn tại chưa (khi tạo mới)
    boolean existsByUsername(String username);

    // Tìm tài khoản theo referenceId và vai trò (phục vụ lấy thông tin đăng nhập của Staff/Relative)
    Optional<Account> findByReferenceIdAndRole(Integer referenceId, String role);

    // Lọc danh sách tài khoản theo vai trò (ADMIN, DOCTOR, NURSE, MANAGER, ACCOUNTANT, RELATIVE)
    List<Account> findByRole(String role);
}
