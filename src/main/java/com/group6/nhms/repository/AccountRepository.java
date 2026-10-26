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

    // Tìm tài khoản theo referenceId và vai trò
    Optional<Account> findByReferenceIdAndRole(Integer referenceId, String role);

    // Tìm danh sách tài khoản theo referenceId (1 nhân sự có thể có alias)
    List<Account> findByReferenceId(Integer referenceId);

    // Lọc danh sách tài khoản theo vai trò
    List<Account> findByRole(String role);
}
