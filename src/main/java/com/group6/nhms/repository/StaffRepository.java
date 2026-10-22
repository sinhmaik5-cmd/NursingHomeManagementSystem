package com.group6.nhms.repository;

import com.group6.nhms.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {

    // Lọc danh sách nhân viên theo chức vụ (DOCTOR, NURSE, MANAGER, ACCOUNTANT...)
    List<Staff> findByJobTitle(String jobTitle);

    // Tìm kiếm nhân viên theo họ tên
    List<Staff> findByFullNameContainingIgnoreCase(String fullName);
}
