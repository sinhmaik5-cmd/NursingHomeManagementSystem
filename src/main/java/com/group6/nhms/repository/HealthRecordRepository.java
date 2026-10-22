package com.group6.nhms.repository;

import com.group6.nhms.entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Integer> {

    // Lấy toàn bộ lịch sử khám của 1 cư dân, sắp xếp từ mới nhất đến cũ nhất
    List<HealthRecord> findByResidentIdOrderByRecordDateDesc(Integer residentId);

    // Lấy lịch sử khám của 1 cư dân trong khoảng thời gian
    List<HealthRecord> findByResidentIdAndRecordDateBetween(Integer residentId, Instant start, Instant end);

    // Lấy danh sách hồ sơ khám do một nhân viên y tế (Bác sĩ/Điều dưỡng) thực hiện
    List<HealthRecord> findByStaffId(Integer staffId);
}
