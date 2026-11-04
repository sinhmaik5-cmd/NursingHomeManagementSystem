package com.group6.nhms.controller.web;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.MedicationResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.service.HealthRecordService;
import com.group6.nhms.service.ResidentService;
import com.group6.nhms.service.StaffService;
import com.group6.nhms.exception.BadRequestException;
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

    public WebHealthController(
            HealthRecordService healthRecordService,
            ResidentService residentService,
            StaffService staffService
    ) {
        this.healthRecordService = healthRecordService;
        this.residentService = residentService;
        this.staffService = staffService;
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
        try {
            boolean isNurse = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_NURSE"));

            if (isNurse) {
                if (request.getMedications() != null && !request.getMedications().isEmpty()) {
                    throw new BadRequestException("Điều dưỡng không được kê đơn hoặc xuất kho thuốc");
                }
                request.setMedications(new ArrayList<>());
                request.setDiagnosis(null);
            } else if (request.getMedications() != null) {
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

    // 3. Giao diện xem lịch sử bệnh án & Đơn thuốc
    @GetMapping("/history")
    public String showHealthHistory(
            @RequestParam(required = false) Integer residentId,
            Model model
    ) {
        List<ResidentResponse> residents = residentService.getAllResidents(null, null);

        ResidentResponse selectedResident = null;
        List<HealthRecordResponse> records = List.of();

        if (residentId != null) {
            try {
                selectedResident = residentService.getResidentById(residentId);
            } catch (Exception ignored) {
            }
        } else if (!residents.isEmpty()) {
            selectedResident = residents.get(0);
        }

        if (selectedResident != null) {
            records = healthRecordService.getRecordsByResident(selectedResident.getId());
        }

        model.addAttribute("residents", residents);
        model.addAttribute("selectedResident", selectedResident);
        model.addAttribute("records", records);
        model.addAttribute("activeMenu", "health-history");

        return "health/history";
    }

    // 4. Xem lịch sử bệnh án theo PathVariable ID
    @GetMapping("/history/{residentId}")
    public String showResidentHistoryPathVariable(@PathVariable Integer residentId) {
        return "redirect:/health/history?residentId=" + residentId;
    }

    // 5. Hủy phiếu khám & Hoàn trả tồn kho
    @PostMapping("/records/{id}/cancel")
    public String cancelHealthRecord(
            @PathVariable Integer id,
            @RequestParam Integer residentId,
            RedirectAttributes redirectAttributes
    ) {
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
