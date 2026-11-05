package com.group6.nhms.repository;

import com.group6.nhms.entity.IncidentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Integer> {

    // 1. Phục vụ Cổng thông tin người nhà: Tìm tất cả sự cố liên quan đến 1 cư dân
    List<IncidentReport> findByResidentId(Integer residentId);

    List<IncidentReport> findByResidentIdIn(List<Integer> residentIds);

    // 2. Phục vụ Admin/Quản lý: Tìm các sự cố chưa được xử lý (Status = 'PENDING') để giải quyết gấp
    List<IncidentReport> findByStatus(String status);

    // 3. Phục vụ Thống kê/Cảnh báo: Lọc các sự cố theo mức độ nghiêm trọng (VD: 'CRITICAL', 'HIGH')
    List<IncidentReport> findBySeverityLevel(String severityLevel);

    // 4. Lấy danh sách sự cố chưa xử lý, sắp xếp theo thời gian mới nhất
    List<IncidentReport> findByStatusOrderByIncidentTimeDesc(String status);

    // 5. Đếm số sự cố theo trạng thái
    long countByStatus(String status);

    // 6. Thống kê số lượng sự cố theo mức độ nghiêm trọng trong khoảng thời gian (Dashboard Admin)
    @Query("SELECT COUNT(i) FROM IncidentReport i WHERE i.severityLevel = :severityLevel AND i.incidentTime BETWEEN :startDate AND :endDate")
    long countBySeverityLevelAndIncidentTimeBetween(
            @Param("severityLevel") String severityLevel,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}
