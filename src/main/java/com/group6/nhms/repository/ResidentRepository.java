package com.group6.nhms.repository;

import com.group6.nhms.entity.Resident;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Integer> {
    // Dùng cho Flow Cổng thông tin: Tìm các cư dân thuộc về 1 người bảo lãnh
    List<Resident> findByRelativeId(Integer relativeId);

    // Lấy danh sách cư dân đang lưu trú (Status = 'ACTIVE')
    List<Resident> findByStatus(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Resident r where r.id = :id")
    Optional<Resident> findByIdForUpdate(@Param("id") Integer id);
}