package com.group6.nhms.repository;

import com.group6.nhms.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Integer> {
    // Dùng cho Flow Cổng thông tin: Tìm các cư dân thuộc về 1 người bảo lãnh
    List<Resident> findByRelativeId(Integer relativeId);

    // Lấy danh sách cư dân đang lưu trú (Status = 'ACTIVE')
    List<Resident> findByStatus(String status);
}