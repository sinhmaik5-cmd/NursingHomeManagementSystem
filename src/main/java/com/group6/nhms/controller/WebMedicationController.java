package com.group6.nhms.controller;

import com.group6.nhms.dto.response.MedicationResponse;
import com.group6.nhms.entity.Medication;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.mapper.MedicationMapper;
import com.group6.nhms.repository.MedicationRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/medications")
public class WebMedicationController {

    private final MedicationRepository medicationRepository;
    private final MedicationMapper medicationMapper;

    public WebMedicationController(MedicationRepository medicationRepository, MedicationMapper medicationMapper) {
        this.medicationRepository = medicationRepository;
        this.medicationMapper = medicationMapper;
    }

    @GetMapping
    public String listMedications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") String filter,
            Model model
    ) {
        List<Medication> allMeds = (search != null && !search.isBlank())
                ? medicationRepository.findByMedNameContainingIgnoreCase(search.trim())
                : medicationRepository.findAll();

        LocalDate today = LocalDate.now();

        // Calculate KPI stats from entire inventory
        List<Medication> fullList = medicationRepository.findAll();
        long totalTypes = fullList.size();
        long lowStockCount = fullList.stream()
                .filter(m -> m.getQuantityInStock() != null && m.getQuantityInStock() < 10 && m.getQuantityInStock() > 0)
                .count();
        long outOfStockCount = fullList.stream()
                .filter(m -> m.getQuantityInStock() == null || m.getQuantityInStock() <= 0)
                .count();
        long expiredCount = fullList.stream()
                .filter(m -> m.getExpiryDate() != null && m.getExpiryDate().isBefore(today))
                .count();
        BigDecimal totalValue = fullList.stream()
                .filter(m -> m.getQuantityInStock() != null && m.getUnitPrice() != null)
                .map(m -> m.getUnitPrice().multiply(BigDecimal.valueOf(m.getQuantityInStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply tab filter
        List<MedicationResponse> filteredMeds = allMeds.stream()
                .filter(m -> {
                    switch (filter.toUpperCase()) {
                        case "LOW_STOCK":
                            return m.getQuantityInStock() != null && m.getQuantityInStock() < 10 && m.getQuantityInStock() > 0;
                        case "OUT_OF_STOCK":
                            return m.getQuantityInStock() == null || m.getQuantityInStock() <= 0;
                        case "EXPIRED":
                            return m.getExpiryDate() != null && m.getExpiryDate().isBefore(today);
                        case "EXPIRING_SOON":
                            return m.getExpiryDate() != null && !m.getExpiryDate().isBefore(today) && m.getExpiryDate().isBefore(today.plusDays(30));
                        default:
                            return true;
                    }
                })
                .map(medicationMapper::toResponse)
                .collect(Collectors.toList());

        model.addAttribute("medications", filteredMeds);
        model.addAttribute("search", search);
        model.addAttribute("filter", filter);
        model.addAttribute("totalTypes", totalTypes);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("expiredCount", expiredCount);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("activeMenu", "medications");

        return "management/medication-list";
    }

    @PostMapping("/create")
    public String createMedication(
            @RequestParam String medName,
            @RequestParam(defaultValue = "0") Integer quantityInStock,
            @RequestParam BigDecimal unitPrice,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (medName == null || medName.isBlank()) {
                throw new BadRequestException("Tên thuốc không được để trống");
            }
            if (quantityInStock < 0) {
                throw new BadRequestException("Số lượng tồn kho không được âm");
            }
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Đơn giá thuốc không được âm");
            }

            Medication medication = new Medication();
            medication.setMedName(medName.trim());
            medication.setQuantityInStock(quantityInStock);
            medication.setUnitPrice(unitPrice);
            medication.setSupplier(supplier != null && !supplier.isBlank() ? supplier.trim() : null);
            medication.setExpiryDate(expiryDate);

            medicationRepository.save(medication);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm mới thuốc '" + medication.getMedName() + "' vào kho thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thêm thuốc: " + e.getMessage());
        }
        return "redirect:/medications";
    }

    @PostMapping("/{id}/update-stock")
    public String updateStock(
            @PathVariable Integer id,
            @RequestParam Integer quantityInStock,
            @RequestParam BigDecimal unitPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestParam(required = false) String supplier,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Medication medication = medicationRepository.findById(id)
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy thuốc ID " + id));

            if (quantityInStock < 0) {
                throw new BadRequestException("Số lượng tồn kho không được âm");
            }
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Đơn giá không được âm");
            }

            medication.setQuantityInStock(quantityInStock);
            medication.setUnitPrice(unitPrice);
            medication.setExpiryDate(expiryDate);
            if (supplier != null && !supplier.isBlank()) {
                medication.setSupplier(supplier.trim());
            }

            medicationRepository.save(medication);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tồn kho thuốc '" + medication.getMedName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật tồn kho: " + e.getMessage());
        }
        return "redirect:/medications";
    }

    @PostMapping("/{id}/delete")
    public String deleteMedication(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Medication medication = medicationRepository.findById(id)
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy thuốc ID " + id));

            medicationRepository.delete(medication);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thuốc '" + medication.getMedName() + "' khỏi danh mục kho.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa thuốc này vì đã có dữ liệu kê đơn trong lịch sử khám bệnh.");
        }
        return "redirect:/medications";
    }
}
