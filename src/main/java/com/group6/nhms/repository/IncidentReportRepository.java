package com.group6.nhms.repository;

import com.group6.nhms.entity.IncidentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Integer> {

    // 1. Phục vụ Cổng thông tin người nhà: Tìm tất cả sự cố liên quan đến 1 cư dân
    List<IncidentReport> findByResidentId(Integer residentId);

    // 2. Phục vụ Admin/Quản lý: Tìm các sự cố chưa được xử lý (Status = 'PENDING') để giải quyết gấp
    List<IncidentReport> findByStatus(String status);

    // 3. Phục vụ Thống kê/Cảnh báo: Lọc các sự cố theo mức độ nghiêm trọng (VD: 'CRITICAL', 'HIGH')
    List<IncidentReport> findBySeverityLevel(String severityLevel);

    // 4. (Nâng cao): Lấy danh sách sự cố chưa xử lý, sắp xếp theo thời gian mới nhất
    List<IncidentReport> findByStatusOrderByIncidentTimeDesc(String status);
}