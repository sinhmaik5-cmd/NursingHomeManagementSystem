package com.group6.nhms.service.impl;


import com.group6.nhms.dto.response.*;
import com.group6.nhms.entity.IncidentReport;
import com.group6.nhms.entity.Order;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.enums.PaymentStatus;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.IncidentReportMapper;
import com.group6.nhms.mapper.OrderMapper;
import com.group6.nhms.repository.IncidentReportRepository;
import com.group6.nhms.repository.OrderRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.service.EmailService;
import com.group6.nhms.service.RelativeNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class RelativeNotificationServiceImpl implements RelativeNotificationService {


    private static final Logger log = LoggerFactory.getLogger(RelativeNotificationServiceImpl.class);


    private final RelativeRepository relativeRepository;
    private final ResidentRepository residentRepository;
    private final OrderRepository orderRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final OrderMapper orderMapper;
    private final IncidentReportMapper incidentReportMapper;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;


    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


    public RelativeNotificationServiceImpl(
            RelativeRepository relativeRepository,
            ResidentRepository residentRepository,
            OrderRepository orderRepository,
            IncidentReportRepository incidentReportRepository,
            OrderMapper orderMapper,
            IncidentReportMapper incidentReportMapper,
            EmailService emailService,
            TemplateEngine templateEngine
    ) {
        this.relativeRepository = relativeRepository;
        this.residentRepository = residentRepository;
        this.orderRepository = orderRepository;
        this.incidentReportRepository = incidentReportRepository;
        this.orderMapper = orderMapper;
        this.incidentReportMapper = incidentReportMapper;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
    }


    @Override
    @Transactional(readOnly = true)
    public List<RelativeNotificationSummary> getAllNotificationSummaries() {
        List<Relative> relatives = relativeRepository.findAll().stream()
                .sorted(Comparator.comparing(Relative::getId))
                .toList();


        List<RelativeNotificationSummary> summaries = new ArrayList<>();
        for (Relative rel : relatives) {
            summaries.add(buildSummaryForRelative(rel));
        }


        // Ưu tiên hiển thị thân nhân có khoản chưa thanh toán hoặc sự cố chưa xử lý lên trước
        summaries.sort((a, b) -> {
            if (a.isHasPendingItems() && !b.isHasPendingItems()) return -1;
            if (!a.isHasPendingItems() && b.isHasPendingItems()) return 1;
            return a.getRelativeName().compareToIgnoreCase(b.getRelativeName());
        });


        return summaries;
    }


    @Override
    @Transactional(readOnly = true)
    public RelativeNotificationSummary getNotificationSummaryForRelative(Integer relativeId) {
        Relative relative = relativeRepository.findById(relativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thân nhân ID " + relativeId));
        return buildSummaryForRelative(relative);
    }


    @Override
    @Transactional(readOnly = true)
    public boolean sendNotificationToRelative(Integer relativeId, String senderStaffName) {
        RelativeNotificationSummary summary = getNotificationSummaryForRelative(relativeId);


        if (!summary.isHasEmail()) {
            throw new BadRequestException("Thân nhân '" + summary.getRelativeName() + "' chưa có địa chỉ email. Vui lòng cập nhật email trước khi gửi!");
        }


        if (!summary.isHasPendingItems()) {
            throw new BadRequestException("Cư dân của thân nhân '" + summary.getRelativeName() + "' hiện không có hóa đơn chưa thanh toán và không có sự cố tồn đọng nào cần thông báo!");
        }


        String residentNames = summary.getResidents().stream()
                .map(r -> r.getResidentName() + (r.getRoomNumber() != null ? " (P." + r.getRoomNumber() + ")" : ""))
                .collect(Collectors.joining(", "));


        return emailService.sendNotificationEmail(
                summary.getRelativeId(),
                summary.getRelativeEmail(),
                summary.getRelativeName(),
                summary.getPreviewEmailSubject(),
                summary.getPreviewEmailHtml(),
                summary.getPreviewEmailText(),
                senderStaffName != null ? senderStaffName : "Lễ tân cơ sở",
                summary.getTotalUnpaidOrdersCount(),
                summary.getTotalUnpaidAmount(),
                summary.getTotalPendingIncidentsCount(),
                residentNames
        );
    }


    @Override
    @Transactional(readOnly = true)
    public BatchSendResult sendNotificationsToAll(String senderStaffName) {
        List<RelativeNotificationSummary> all = getAllNotificationSummaries();


        List<RelativeNotificationSummary> eligible = all.stream()
                .filter(s -> s.isHasPendingItems() && s.isHasEmail())
                .toList();


        BatchSendResult result = new BatchSendResult();
        result.setTotalEligible(eligible.size());


        int success = 0;
        int failed = 0;
        List<String> emails = new ArrayList<>();
        BigDecimal totalUnpaid = BigDecimal.ZERO;
        int totalIncidents = 0;


        for (RelativeNotificationSummary summary : eligible) {
            try {
                boolean ok = sendNotificationToRelative(summary.getRelativeId(), senderStaffName);
                if (ok) {
                    success++;
                    emails.add(summary.getRelativeEmail());
                    totalUnpaid = totalUnpaid.add(summary.getTotalUnpaidAmount());
                    totalIncidents += summary.getTotalPendingIncidentsCount();
                } else {
                    failed++;
                }
            } catch (Exception e) {
                failed++;
            }
        }


        result.setSuccessCount(success);
        result.setFailedCount(failed);
        result.setSkippedCount(all.size() - eligible.size());
        result.setRecipientEmails(emails);
        result.setTotalUnpaidReminded(totalUnpaid);
        result.setTotalIncidentsReported(totalIncidents);


        return result;
    }


    @Override
    public List<EmailSendLog> getRecentLogs() {
        return emailService.getRecentLogs();
    }


    private RelativeNotificationSummary buildSummaryForRelative(Relative rel) {
        RelativeNotificationSummary summary = new RelativeNotificationSummary(
                rel.getId(),
                rel.getFullName(),
                rel.getPhoneNumber(),
                rel.getEmail(),
                rel.getRelationship() != null ? rel.getRelationship() : "Người thân"
        );


        List<Resident> residents = residentRepository.findByRelativeId(rel.getId());
        for (Resident resident : residents) {
            ResidentPendingNotification resNotif = new ResidentPendingNotification(
                    resident.getId(),
                    resident.getFullName(),
                    resident.getBed() != null && resident.getBed().getRoom() != null ? resident.getBed().getRoom().getRoomNumber() : null,
                    resident.getBed() != null ? resident.getBed().getBedNumber() : null
            );


            // 1. Hóa đơn chưa thanh toán (PENDING)
            List<Order> unpaidOrders = orderRepository.findByResidentIdAndPaymentStatus(
                    resident.getId(), PaymentStatus.PENDING.name());
            List<OrderResponse> unpaidOrderResponses = unpaidOrders.stream()
                    .map(orderMapper::toResponse)
                    .toList();
            resNotif.setUnpaidOrders(unpaidOrderResponses);


            // 2. Sự cố chưa xử lý của cư dân (PENDING)
            List<IncidentReport> residentIncidents = incidentReportRepository.findByResidentId(resident.getId()).stream()
                    .filter(i -> "PENDING".equalsIgnoreCase(i.getStatus()))
                    .toList();
            List<IncidentReportResponse> pendingIncidentResponses = residentIncidents.stream()
                    .map(incidentReportMapper::toResponse)
                    .toList();
            resNotif.setPendingIncidents(pendingIncidentResponses);


            summary.addResident(resNotif);
        }


        summary.setLastSentTime(emailService.getLastSentTime(rel.getId()));


        // Xây dựng nội dung Email Template (Subject, HTML, Plain text)
        buildEmailTemplates(summary);


        return summary;
    }


    private void buildEmailTemplates(RelativeNotificationSummary summary) {
        String residentNames = summary.getResidents().isEmpty()
                ? "Cư dân"
                : summary.getResidents().stream()
                .map(r -> r.getResidentName() + (r.getRoomNumber() != null ? " (P." + r.getRoomNumber() + ")" : ""))
                .collect(Collectors.joining(", "));


        String subject = "[NHMS CARE] Thông Báo Viện Phí & Sự Cố Chưa Xử Lý Của Cư Dân - " + residentNames;
        summary.setPreviewEmailSubject(subject);


        String relName = summary.getRelativeName() != null ? summary.getRelativeName() : "Quý Thân Nhân";
        String relRelation = summary.getRelationship() != null ? summary.getRelationship() : "Thân nhân";


        // 1. Render HTML Email Template qua Thymeleaf Engine
        try {
            Context context = new Context();
            context.setVariable("summary", summary);
            String html = templateEngine.process("email/relative-notification", context);
            summary.setPreviewEmailHtml(html);
        } catch (Exception e) {
            log.error("Lỗi khi render email template qua Thymeleaf: {}", e.getMessage(), e);
            summary.setPreviewEmailHtml("<div style='padding: 20px; font-family: sans-serif; color: #333;'><h3>VIỆN DƯỠNG LÃO NHMS CARE</h3><p>Không thể kết xuất mẫu email: " + e.getMessage() + "</p></div>");
        }


        // 2. Plain Text Email
        StringBuilder text = new StringBuilder();
        text.append("VIỆN DƯỠNG LÃO NHMS CARE\n");
        text.append("THÔNG BÁO TỔNG HỢP VIỆN PHÍ & SỰ CỐ CƯ DÂN\n");
        text.append("--------------------------------------------------\n\n");
        text.append("Kính gửi Quý thân nhân: ").append(relName).append(" (").append(relRelation).append("),\n\n");
        text.append("Bàn Lễ tân & Thu ngân NHMS CARE xin thông báo tình hình viện phí và sự cố của cư dân:\n\n");


        for (ResidentPendingNotification res : summary.getResidents()) {
            text.append("👤 CƯ DÂN: ").append(res.getResidentName())
                    .append(" (Phòng ").append(res.getRoomNumber() != null ? res.getRoomNumber() : "-")
                    .append(" - Giường ").append(res.getBedNumber() != null ? res.getBedNumber() : "-").append(")\n");


            text.append("1. VIỆN PHÍ CHƯA THANH TOÁN:\n");
            if (res.getUnpaidOrders().isEmpty()) {
                text.append("   - Không có hóa đơn viện phí tồn đọng.\n");
            } else {
                for (OrderResponse ord : res.getUnpaidOrders()) {
                    text.append("   - #HĐ-").append(ord.getId())
                            .append(" (Ngày lập: ").append(ord.getIssueDate() != null ? ord.getIssueDate().format(DATE_FORMAT) : "-").append(")")
                            .append(" -> Tổng tiền: ").append(ord.getTotalAmount() != null ? CURRENCY_FORMAT.format(ord.getTotalAmount()) + " VNĐ" : "0 VNĐ")
                            .append(" [CHƯA THANH TOÁN]\n");
                }
                text.append("   => TỔNG CỘNG: ").append(CURRENCY_FORMAT.format(res.getTotalUnpaidAmount())).append(" VNĐ\n");
            }


            text.append("2. SỰ CỐ CHƯA XỬ LÝ:\n");
            if (res.getPendingIncidents().isEmpty()) {
                text.append("   - Không có sự cố y tế / sinh hoạt tồn đọng.\n");
            } else {
                for (IncidentReportResponse inc : res.getPendingIncidents()) {
                    text.append("   - [").append(inc.getSeverityLevel()).append("] ")
                            .append(inc.getTitle())
                            .append(" (").append(inc.getCreatedDate() != null ? inc.getCreatedDate().format(DATETIME_FORMAT) : "-").append("): ")
                            .append(inc.getDescription()).append("\n");
                }
            }
            text.append("\n");
        }


        text.append("--------------------------------------------------\n");
        text.append("Quý thân nhân có thể tra cứu chi tiết tại Cổng Người Thân (Portal NHMS CARE) hoặc liên hệ Hotline 1900-NHMSCARE.\n");
        text.append("Bàn Lễ tân & Thu ngân - Viện Dưỡng Lão NHMS CARE\n");


        summary.setPreviewEmailText(text.toString());
    }
}



