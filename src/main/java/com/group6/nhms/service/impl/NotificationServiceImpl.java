package com.group6.nhms.service.impl;

import com.group6.nhms.dto.response.NotificationItemDto;
import com.group6.nhms.dto.response.NotificationSummaryDto;
import com.group6.nhms.dto.response.RelativeNotificationSummary;
import com.group6.nhms.entity.HealthRecord;
import com.group6.nhms.entity.IncidentReport;
import com.group6.nhms.entity.Order;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.repository.*;
import com.group6.nhms.service.NotificationService;
import com.group6.nhms.service.RelativeNotificationService;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.StaffService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final IncidentReportRepository incidentReportRepository;
    private final BedRepository bedRepository;
    private final OrderRepository orderRepository;
    private final MedicationRepository medicationRepository;
    private final TaskScheduleRepository taskScheduleRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final ResidentRepository residentRepository;
    private final StaffService staffService;
    private final RelativeService relativeService;
    private final RelativeNotificationService relativeNotificationService;

    public NotificationServiceImpl(
            IncidentReportRepository incidentReportRepository,
            BedRepository bedRepository,
            OrderRepository orderRepository,
            MedicationRepository medicationRepository,
            TaskScheduleRepository taskScheduleRepository,
            HealthRecordRepository healthRecordRepository,
            ResidentRepository residentRepository,
            StaffService staffService,
            RelativeService relativeService,
            RelativeNotificationService relativeNotificationService
    ) {
        this.incidentReportRepository = incidentReportRepository;
        this.bedRepository = bedRepository;
        this.orderRepository = orderRepository;
        this.medicationRepository = medicationRepository;
        this.taskScheduleRepository = taskScheduleRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.residentRepository = residentRepository;
        this.staffService = staffService;
        this.relativeService = relativeService;
        this.relativeNotificationService = relativeNotificationService;
    }

    @Override
    public NotificationSummaryDto getNotificationsForUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return new NotificationSummaryDto(false, 0, List.of());
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        List<NotificationItemDto> items = new ArrayList<>();

        if (roles.contains("ROLE_CENTER_MANAGER")) {
            populateManagerNotifications(items);
        } else if (roles.contains("ROLE_DOCTOR")) {
            populateDoctorNotifications(authentication, items);
        } else if (roles.contains("ROLE_NURSE")) {
            populateNurseNotifications(authentication, items);
        } else if (roles.contains("ROLE_RECEPTIONIST")) {
            populateReceptionistNotifications(items);
        } else if (roles.contains("ROLE_RELATIVE")) {
            populateRelativeNotifications(authentication, items);
        }

        int totalNewCount = (int) items.stream().filter(NotificationItemDto::isHasNew).count();
        boolean hasNew = totalNewCount > 0;

        return new NotificationSummaryDto(hasNew, totalNewCount, items);
    }

    private void populateManagerNotifications(List<NotificationItemDto> items) {
        long pendingIncidents = incidentReportRepository.countByStatus("PENDING");
        items.add(new NotificationItemDto(
                "mgr_incidents",
                "Báo cáo sự cố cần xử lý",
                pendingIncidents > 0 ? (pendingIncidents + " sự cố đang chờ chỉ đạo giải quyết") : "Không có sự cố nào tồn đọng",
                "/incidents/report",
                "bi bi-exclamation-triangle-fill text-warning",
                (int) pendingIncidents,
                pendingIncidents > 0
        ));

        long maintBeds = bedRepository.countByStatus("MAINTENANCE");
        items.add(new NotificationItemDto(
                "mgr_beds",
                "Giường cần dọn dẹp & bảo trì",
                maintBeds > 0 ? (maintBeds + " giường đang bảo trì cần hoàn tất") : "Tất cả giường đều sẵn sàng",
                "/rooms/beds",
                "bi bi-tools text-info",
                (int) maintBeds,
                maintBeds > 0
        ));

        long unpaidBills = orderRepository.countByPaymentStatus("PENDING");
        items.add(new NotificationItemDto(
                "mgr_bills",
                "Hóa đơn viện phí chưa thu",
                unpaidBills > 0 ? (unpaidBills + " hóa đơn tháng chưa thanh toán") : "Toàn bộ viện phí đã thanh toán",
                "/billing/monthly?status=PENDING",
                "bi bi-receipt text-danger",
                (int) unpaidBills,
                unpaidBills > 0
        ));

        int lowStockMeds = medicationRepository.findByQuantityInStockLessThan(10).size();
        items.add(new NotificationItemDto(
                "mgr_meds",
                "Cảnh báo tồn kho thuốc thấp",
                lowStockMeds > 0 ? (lowStockMeds + " loại thuốc dưới định mức tồn (<10)") : "Kho dược đảm bảo đủ số lượng",
                "/medications",
                "bi bi-capsule text-danger",
                lowStockMeds,
                lowStockMeds > 0
        ));
    }

    private void populateDoctorNotifications(Authentication authentication, List<NotificationItemDto> items) {
        long pendingIncidents = incidentReportRepository.countByStatus("PENDING");
        items.add(new NotificationItemDto(
                "doc_incidents",
                "Sự cố y tế chờ Bác sĩ xử lý",
                pendingIncidents > 0 ? (pendingIncidents + " sự cố lâm sàng đang chờ xử lý") : "Không có sự cố y tế nào cần xử lý",
                "/incidents/report",
                "bi bi-exclamation-octagon-fill text-danger",
                (int) pendingIncidents,
                pendingIncidents > 0
        ));

        Staff staff = staffService.resolveCurrentStaff(authentication);
        long todayShifts = (staff != null) ? taskScheduleRepository.findByStaffIdAndShiftDate(staff.getId(), LocalDate.now())
                .stream()
                .filter(s -> !"CANCELLED".equalsIgnoreCase(s.getStatus()) && !"COMPLETED".equalsIgnoreCase(s.getStatus()))
                .count() : 0;
        items.add(new NotificationItemDto(
                "doc_shifts",
                "Ca trực & Lịch khám hôm nay",
                todayShifts > 0 ? ("Bác sĩ có " + todayShifts + " ca trực phân công hôm nay") : "Hôm nay không có ca trực phân công",
                "/schedules/my-schedule",
                "bi bi-calendar2-heart text-primary",
                (int) todayShifts,
                todayShifts > 0
        ));

        int lowStockMeds = medicationRepository.findByQuantityInStockLessThan(10).size();
        items.add(new NotificationItemDto(
                "doc_meds",
                "Thuốc kê đơn sắp hết hàng",
                lowStockMeds > 0 ? (lowStockMeds + " loại thuốc sắp hết, lưu ý khi kê đơn") : "Kho thuốc kê đơn dồi dào",
                "/health/records/create",
                "bi bi-capsule-pill text-warning",
                lowStockMeds,
                lowStockMeds > 0
        ));
    }

    private void populateNurseNotifications(Authentication authentication, List<NotificationItemDto> items) {
        Staff staff = staffService.resolveCurrentStaff(authentication);
        long todayShifts = (staff != null) ? taskScheduleRepository.findByStaffIdAndShiftDate(staff.getId(), LocalDate.now())
                .stream()
                .filter(s -> !"CANCELLED".equalsIgnoreCase(s.getStatus()) && !"COMPLETED".equalsIgnoreCase(s.getStatus()))
                .count() : 0;
        items.add(new NotificationItemDto(
                "nurse_shifts",
                "Ca trực chăm sóc hôm nay",
                todayShifts > 0 ? ("Điều dưỡng có " + todayShifts + " ca trực phân công cần làm") : "Hôm nay không có ca trực phân công",
                "/schedules/my-schedule",
                "bi bi-calendar-check-fill text-primary",
                (int) todayShifts,
                todayShifts > 0
        ));

        long pendingIncidents = incidentReportRepository.countByStatus("PENDING");
        items.add(new NotificationItemDto(
                "nurse_incidents",
                "Sự cố y tế cần theo dõi",
                pendingIncidents > 0 ? (pendingIncidents + " sự cố lâm sàng cần theo dõi sát sao") : "Tất cả cư dân đều an toàn ổn định",
                "/incidents/report",
                "bi bi-heart-pulse-fill text-danger",
                (int) pendingIncidents,
                pendingIncidents > 0
        ));

        long maintBeds = bedRepository.countByStatus("MAINTENANCE");
        items.add(new NotificationItemDto(
                "nurse_beds",
                "Giường cần dọn dẹp / Chuẩn bị",
                maintBeds > 0 ? (maintBeds + " giường bảo trì cần chuẩn bị") : "Tất cả giường đều sẵn sàng",
                "/rooms/beds",
                "bi bi-hospital text-info",
                (int) maintBeds,
                maintBeds > 0
        ));
    }

    private void populateReceptionistNotifications(List<NotificationItemDto> items) {
        long unpaidBills = orderRepository.countByPaymentStatus("PENDING");
        items.add(new NotificationItemDto(
                "rec_bills",
                "Hóa đơn viện phí cần thu",
                unpaidBills > 0 ? (unpaidBills + " hóa đơn viện phí tháng cần thu tiền") : "Toàn bộ hóa đơn đã được thanh toán",
                "/billing/monthly?status=PENDING",
                "bi bi-cash-coin text-danger",
                (int) unpaidBills,
                unpaidBills > 0
        ));

        long pendingNotifs = (relativeNotificationService != null)
                ? relativeNotificationService.getAllNotificationSummaries().stream().filter(RelativeNotificationSummary::isHasPendingItems).count()
                : 0;
        items.add(new NotificationItemDto(
                "rec_notifs",
                "Thân nhân cần gửi thông báo",
                pendingNotifs > 0 ? (pendingNotifs + " thân nhân cần gửi email thông báo") : "Đã hoàn tất gửi email cho thân nhân",
                "/billing/notifications",
                "bi bi-envelope-paper-heart-fill text-primary",
                (int) pendingNotifs,
                pendingNotifs > 0
        ));

        long pendingIncidents = incidentReportRepository.countByStatus("PENDING");
        items.add(new NotificationItemDto(
                "rec_incidents",
                "Sự cố CSVC chờ xử lý",
                pendingIncidents > 0 ? (pendingIncidents + " sự cố cơ sở vật chất chờ khắc phục") : "Cơ sở vật chất hoạt động tốt",
                "/incidents/report",
                "bi bi-wrench-adjustable-circle-fill text-warning",
                (int) pendingIncidents,
                pendingIncidents > 0
        ));
    }

    private void populateRelativeNotifications(Authentication authentication, List<NotificationItemDto> items) {
        Relative relative = relativeService.resolveCurrentRelative(authentication);
        if (relative == null) {
            return;
        }

        List<Resident> residents = residentRepository.findByRelativeId(relative.getId());
        if (residents.isEmpty()) {
            items.add(new NotificationItemDto(
                    "rel_no_res",
                    "Cổng thông tin gia đình",
                    "Chưa có hồ sơ cư dân liên kết với tài khoản",
                    "/portal/dashboard",
                    "bi bi-people-fill text-muted",
                    0,
                    false
            ));
            return;
        }

        List<Integer> residentIds = residents.stream().map(Resident::getId).toList();

        // 1. Sự cố phát sinh của người thân (chưa giải quyết)
        List<IncidentReport> incidents = incidentReportRepository.findByResidentIdIn(residentIds);
        long pendingIncidents = incidents.stream()
                .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()))
                .count();
        items.add(new NotificationItemDto(
                "rel_incidents",
                "Sự cố & An toàn của người thân",
                pendingIncidents > 0 ? ("Có " + pendingIncidents + " sự việc đang được viện theo dõi xử lý") : "Cụ đang an toàn, không có sự cố nào",
                "/portal/incidents",
                "bi bi-shield-exclamation text-warning",
                (int) pendingIncidents,
                pendingIncidents > 0
        ));

        // 2. Phiếu khám & Đo sinh hiệu mới trong 48h
        List<HealthRecord> records = healthRecordRepository.findByResidentIdInOrderByRecordDateDesc(residentIds);
        Instant since48h = Instant.now().minus(48, ChronoUnit.HOURS);
        long recentRecords = records.stream()
                .filter(r -> r.getRecordDate() != null && r.getRecordDate().isAfter(since48h))
                .count();
        items.add(new NotificationItemDto(
                "rel_health",
                "Cập nhật sức khỏe & Sinh hiệu mới",
                recentRecords > 0 ? ("Có " + recentRecords + " phiếu khám & sinh hiệu mới trong 48h") : "Chưa có phiếu khám mới trong hôm nay",
                "/health/history",
                "bi bi-activity text-danger",
                (int) recentRecords,
                recentRecords > 0
        ));

        // 3. Viện phí tháng cần thanh toán
        List<Order> orders = orderRepository.findByResidentIdInOrderByIssueDateDesc(residentIds);
        long unpaidOrders = orders.stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.getPaymentStatus()) || "UNPAID".equalsIgnoreCase(o.getPaymentStatus()))
                .count();
        items.add(new NotificationItemDto(
                "rel_bills",
                "Hóa đơn viện phí tháng",
                unpaidOrders > 0 ? ("Có " + unpaidOrders + " hóa đơn viện phí cần thanh toán") : "Viện phí của cụ đã thanh toán đủ",
                "/portal/bills",
                "bi bi-receipt text-primary",
                (int) unpaidOrders,
                unpaidOrders > 0
        ));
    }
}
