package com.group6.nhms.repository;

import com.group6.nhms.entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Integer> {

    // Lấy toàn bộ lịch sử khám của 1 cư dân, sắp xếp từ mới nhất đến cũ nhất
    List<HealthRecord> findByResidentIdOrderByRecordDateDesc(Integer residentId);
}