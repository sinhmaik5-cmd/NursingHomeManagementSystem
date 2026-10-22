package com.group6.nhms.repository;

import com.group6.nhms.entity.HealthRecordDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface HealthRecordDetailRepository extends JpaRepository<HealthRecordDetail, Integer> {

    // Lấy chi tiết đơn thuốc theo hồ sơ khám bệnh
    List<HealthRecordDetail> findByHealthRecordId(Integer healthRecordId);

    // Lấy tất cả lịch sử kê đơn của 1 cư dân trong khoảng thời gian (phục vụ chốt tiền thuốc cuối tháng)
    @Query("SELECT d FROM HealthRecordDetail d WHERE d.healthRecord.resident.id = :residentId AND d.healthRecord.recordDate BETWEEN :startDate AND :endDate")
    List<HealthRecordDetail> findByResidentIdAndRecordDateBetween(
            @Param("residentId") Integer residentId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Tính tổng tiền thuốc của 1 cư dân trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(d.totalPrice), 0) FROM HealthRecordDetail d WHERE d.healthRecord.resident.id = :residentId AND d.healthRecord.recordDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalPriceByResidentIdAndRecordDateBetween(
            @Param("residentId") Integer residentId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Lấy lịch sử kê đơn của 1 cư dân sắp xếp theo ngày khám mới nhất (cho Bác sĩ & Người thân xem)
    @Query("SELECT d FROM HealthRecordDetail d WHERE d.healthRecord.resident.id = :residentId ORDER BY d.healthRecord.recordDate DESC")
    List<HealthRecordDetail> findByResidentIdOrderByRecordDateDesc(@Param("residentId") Integer residentId);
}
