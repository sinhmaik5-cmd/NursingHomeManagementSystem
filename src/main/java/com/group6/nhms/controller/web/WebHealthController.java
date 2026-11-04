package com.group6.nhms.controller.web;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.MedicationResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.service.HealthRecordService;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.ResidentService;
import com.group6.nhms.service.StaffService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/health")
public class WebHealthController {

    private final HealthRecordService healthRecordService;
    private final ResidentService residentService;
    private final StaffService staffService;
    private final RelativeService relativeService;

    public WebHealthController(
            HealthRecordService healthRecordService,
            ResidentService residentService,
            StaffService staffService,
            RelativeService relativeService
    ) {
        this.healthRecordService = healthRecordService;
        this.residentService = residentService;
        this.staffService = staffService;
        this.relativeService = relativeService;
    }

    // 1. Giao diện khám bệnh & Kê đơn thuốc
    @GetMapping("/records/create")
    public String showCreateHealthRecord(
            @RequestParam(required = false) Integer residentId,
            Authentication authentication,
            Model model
    ) {
        List<ResidentResponse> residents = residentService.getAllResidents(null, "ACTIVE");
        List<MedicationResponse> availableMeds = healthRecordService.getAvailableMedications();
        List<MedicationResponse> lowStockMeds = healthRecordService.getLowStockMedications();

        boolean isNurse = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_NURSE"));

        HealthRecordCreateRequest recordRequest = new HealthRecordCreateRequest();
        if (residentId != null) {
            recordRequest.setResidentId(residentId);
        }

        if (!isNurse) {
            // Bác sĩ: khởi tạo sẵn 1 dòng chọn thuốc mẫu
            List<HealthRecordCreateRequest.MedicationItemRequest> meds = new ArrayList<>();
            meds.add(new HealthRecordCreateRequest.MedicationItemRequest());
            recordRequest.setMedications(meds);
        } else {
            // Điều dưỡng: không yêu cầu kê thuốc
            recordRequest.setMedications(new ArrayList<>());
        }

        model.addAttribute("recordRequest", recordRequest);
        model.addAttribute("residents", residents);
        model.addAttribute("availableMeds", availableMeds);
        model.addAttribute("lowStockMeds", lowStockMeds);
        model.addAttribute("isNurse", isNurse);
        model.addAttribute("activeMenu", "record-create");

        return "health/record-create";
    }

    // 2. Xử lý tạo phiếu khám bệnh & Trừ kho thuốc
    @PostMapping("/records/create")
    public String processCreateHealthRecord(
            @ModelAttribute HealthRecordCreateRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        boolean isNurse = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_NURSE"));

        // Điều dưỡng cố tình gửi kèm danh sách thuốc sẽ bị từ chối ngay trước khi tạo bệnh án
        if (isNurse && request.getMedications() != null && !request.getMedications().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Điều dưỡng không được kê đơn hoặc xuất kho thuốc");
            return "redirect:/health/records/create" + (request.getResidentId() != null ? "?residentId=" + request.getResidentId() : "");
        }

        try {
            if (request.getMedications() != null) {
                request.setMedications(request.getMedications().stream()
                        .filter(m -> m.getMedicationId() != null && m.getQuantityRequested() != null && m.getQuantityRequested() > 0)
                        .toList());
            }

            Integer staffId = staffService.resolveCurrentStaffId(authentication);
            HealthRecordResponse res = healthRecordService.createHealthRecord(request, staffId);
            String successMsg = isNurse
                    ? "Đã lưu chỉ số sinh tồn và theo dõi chăm sóc thành công cho cư dân " + res.getResidentName() + "!"
                    : "Đã lưu phiếu khám và xuất kho đơn thuốc thành công cho cư dân " + res.getResidentName() + "!";

            redirectAttributes.addFlashAttribute("successMessage", successMsg);
            return "redirect:/health/history?residentId=" + request.getResidentId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tạo hồ sơ sức khỏe: " + e.getMessage());
            return "redirect:/health/records/create" + (request.getResidentId() != null ? "?residentId=" + request.getResidentId() : "");
        }
    }

    // 3. Giao diện xem lịch sử bệnh án & Đơn thuốc (Có kiểm soát IDOR cho Người thân)
    @GetMapping("/history")
    public String showHealthHistory(
            @RequestParam(required = false) Integer residentId,
            Authentication authentication,
            Model model
    ) {
        boolean isRelative = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RELATIVE"));

        List<ResidentResponse> residents;
        if (isRelative) {
            Relative relative = relativeService.resolveCurrentRelative(authentication);
            if (relative != null) {
                residents = residentService.getResidentsByRelative(relative.getId());
            } else {
                residents = List.of();
            }
        } else {
            residents = residentService.getAllResidents(null, null);
        }

        ResidentResponse selectedResident = null;
        List<HealthRecordResponse> records = List.of();

        if (residentId != null) {
            try {
                ResidentResponse r = residentService.getResidentById(residentId);
                // IDOR Protection: Người thân chỉ xem được lịch sử của cư dân mình bảo lãnh
                if (isRelative) {
                    if (residents.stream().anyMatch(myRes -> myRes.getId().equals(r.getId()))) {
                        selectedResident = r;
                    }
                } else {
                    selectedResident = r;
                }
            } catch (Exception ignored) {
            }
        }

        if (selectedResident == null && !residents.isEmpty()) {
            selectedResident = residents.get(0);
        }

        if (selectedResident != null) {
            records = healthRecordService.getRecordsByResident(selectedResident.getId());
        }

        model.addAttribute("residents", residents);
        model.addAttribute("selectedResident", selectedResident);
        model.addAttribute("records", records);
        model.addAttribute("isRelative", isRelative);
        model.addAttribute("activeMenu", "health-history");

        return "health/history";
    }

    // 4. Xem lịch sử bệnh án theo PathVariable ID
    @GetMapping("/history/{residentId}")
    public String showResidentHistoryPathVariable(@PathVariable Integer residentId) {
        return "redirect:/health/history?residentId=" + residentId;
    }

    // 5. Hủy phiếu khám & Hoàn trả tồn kho (Chỉ Bác sĩ, Giám đốc cơ sở & Quản trị viên)
    @PostMapping("/records/{id}/cancel")
    public String cancelHealthRecord(
            @PathVariable Integer id,
            @RequestParam Integer residentId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_DOCTOR") || a.getAuthority().equals("ROLE_CENTER_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền hủy phiếu khám và hoàn kho thuốc.");
            return "redirect:/health/history?residentId=" + residentId;
        }

        try {
            healthRecordService.cancelHealthRecord(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hủy phiếu khám và hoàn trả số lượng thuốc về kho thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hủy phiếu khám: " + e.getMessage());
        }
        return "redirect:/health/history?residentId=" + residentId;
    }
}
