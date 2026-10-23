package com.group6.nhms.controller.web;

import com.group6.nhms.dto.request.HealthRecordCreateRequest;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.MedicationResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Medication;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.mapper.MedicationMapper;
import com.group6.nhms.mapper.ResidentMapper;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.MedicationRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.HealthRecordService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/health")
public class WebHealthController {

    private final HealthRecordService healthRecordService;
    private final ResidentRepository residentRepository;
    private final MedicationRepository medicationRepository;
    private final StaffRepository staffRepository;
    private final AccountRepository accountRepository;
    private final ResidentMapper residentMapper;
    private final MedicationMapper medicationMapper;

    public WebHealthController(
            HealthRecordService healthRecordService,
            ResidentRepository residentRepository,
            MedicationRepository medicationRepository,
            StaffRepository staffRepository,
            AccountRepository accountRepository,
            ResidentMapper residentMapper,
            MedicationMapper medicationMapper
    ) {
        this.healthRecordService = healthRecordService;
        this.residentRepository = residentRepository;
        this.medicationRepository = medicationRepository;
        this.staffRepository = staffRepository;
        this.accountRepository = accountRepository;
        this.residentMapper = residentMapper;
        this.medicationMapper = medicationMapper;
    }

    // 1. Giao diện khám bệnh & Kê đơn thuốc
    @GetMapping("/records/create")
    public String showCreateHealthRecord(
            @RequestParam(required = false) Integer residentId,
            Model model
    ) {
        List<ResidentResponse> residents = residentRepository.findByStatus(ResidentStatus.ACTIVE.name())
                .stream()
                .map(residentMapper::toResponse)
                .toList();

        List<MedicationResponse> availableMeds = medicationRepository.findAvailableForPrescription(LocalDate.now())
                .stream()
                .map(medicationMapper::toResponse)
                .toList();

        List<Medication> lowStockMeds = medicationRepository.findByQuantityInStockLessThan(10);

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
            // Filter out empty medication rows if any
            if (request.getMedications() != null) {
                request.setMedications(request.getMedications().stream()
                        .filter(m -> m.getMedicationId() != null && m.getQuantityRequested() != null && m.getQuantityRequested() > 0)
                        .toList());
            }

            Integer staffId = resolveStaffId(authentication);
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
        List<ResidentResponse> residents = residentRepository.findAll()
                .stream()
                .map(residentMapper::toResponse)
                .toList();

        ResidentResponse selectedResident = null;
        List<HealthRecordResponse> records = List.of();

        if (residentId != null) {
            selectedResident = residentRepository.findById(residentId)
                    .map(residentMapper::toResponse)
                    .orElse(null);
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

    private Integer resolveStaffId(Authentication authentication) {
        if (authentication == null) {
            return staffRepository.findAll().stream().findFirst().map(Staff::getId).orElse(1);
        }

        return accountRepository.findByUsername(authentication.getName())
                .filter(a -> a.getReferenceId() != null)
                .map(a -> a.getReferenceId())
                .orElseGet(() -> staffRepository.findAll().stream().findFirst().map(Staff::getId).orElse(1));
    }
}
