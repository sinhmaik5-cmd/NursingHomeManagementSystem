package com.group6.nhms.controller.web;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.MedicationResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.service.HealthRecordService;
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
            Model model
    ) {
        List<ResidentResponse> residents = residentService.getAllResidents(null, "ACTIVE");
        List<MedicationResponse> availableMeds = healthRecordService.getAvailableMedications();
        List<MedicationResponse> lowStockMeds = healthRecordService.getLowStockMedications();

        HealthRecordCreateRequest recordRequest = new HealthRecordCreateRequest();
        if (residentId != null) {
            recordRequest.setResidentId(residentId);
        }

        // Initialize 1 empty medication item
        List<HealthRecordCreateRequest.MedicationItemRequest> meds = new ArrayList<>();
        meds.add(new HealthRecordCreateRequest.MedicationItemRequest());
        recordRequest.setMedications(meds);

        model.addAttribute("recordRequest", recordRequest);
        model.addAttribute("residents", residents);
        model.addAttribute("availableMeds", availableMeds);
        model.addAttribute("lowStockMeds", lowStockMeds);
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
            if (request.getMedications() != null) {
                request.setMedications(request.getMedications().stream()
                        .filter(m -> m.getMedicationId() != null && m.getQuantityRequested() != null && m.getQuantityRequested() > 0)
                        .toList());
            }

            Integer staffId = staffService.resolveCurrentStaffId(authentication);
            HealthRecordResponse res = healthRecordService.createHealthRecord(request, staffId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã lưu phiếu khám và xuất kho đơn thuốc thành công cho cư dân " + res.getResidentName() + "!");
            return "redirect:/health/history?residentId=" + request.getResidentId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tạo hồ sơ khám bệnh: " + e.getMessage());
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
