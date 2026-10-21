package com.group6.nhms.repository;

import com.group6.nhms.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    // Tìm tài khoản theo username để xử lý Đăng nhập
    Optional<Account> findByUsername(String username);

    // Kiểm tra xem username đã tồn tại chưa (khi tạo mới)
    boolean existsByUsername(String username);
}