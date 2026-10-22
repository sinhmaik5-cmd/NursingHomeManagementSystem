package com.group6.nhms.repository;

import com.group6.nhms.entity.Relative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelativeRepository extends JpaRepository<Relative, Integer> {

    // Tìm người thân theo số điện thoại
    List<Relative> findByPhoneNumber(String phoneNumber);

    // Tìm người thân đầu tiên theo số điện thoại (dùng khi check-in kiểm tra người thân đã có trong hệ thống)
    Optional<Relative> findFirstByPhoneNumber(String phoneNumber);

    // Tìm kiếm người thân theo tên
    List<Relative> findByFullNameContainingIgnoreCase(String fullName);
}
