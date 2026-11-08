package com.group6.nhms.controller;

import com.group6.nhms.dto.request.BedTransferRequest;
import com.group6.nhms.dto.request.HospitalTransferRequest;
import com.group6.nhms.dto.request.ResidentCheckInRequest;
import com.group6.nhms.dto.response.BedResponse;
import com.group6.nhms.dto.response.HealthRecordResponse;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.dto.response.RoomResponse;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.service.BedService;
import com.group6.nhms.service.HealthRecordService;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.ResidentService;
import com.group6.nhms.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/residents")
public class WebResidentController {

    private final ResidentService residentService;
    private final BedService bedService;
    private final RoomService roomService;
    private final RelativeService relativeService;
    private final HealthRecordService healthRecordService;

    public WebResidentController(
            ResidentService residentService,
            BedService bedService,
            RoomService roomService,
            RelativeService relativeService,
            HealthRecordService healthRecordService
    ) {
        this.residentService = residentService;
        this.bedService = bedService;
        this.roomService = roomService;
        this.relativeService = relativeService;
        this.healthRecordService = healthRecordService;
    }

    // 1. Danh sách cư dân
    @GetMapping
    public String listResidents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            Model model
    ) {
        List<ResidentResponse> residents = residentService.getAllResidents(name, status);
        List<BedResponse> availableBeds = bedService.getAvailableBeds();

        model.addAttribute("residents", residents);
        model.addAttribute("availableBeds", availableBeds);
        model.addAttribute("activeMenu", "resident-list");
        return "resident/list";
    }

    // 2. Form tiếp nhận cư dân mới (Hỗ trợ mở trực tiếp hoặc chọn giường từ sơ đồ trực quan)
    @GetMapping("/check-in")
    public String showCheckInForm(
            @RequestParam(required = false) Integer bedId,
            Model model
    ) {
        ResidentCheckInRequest checkInRequest = new ResidentCheckInRequest();
        if (bedId != null) {
            checkInRequest.setBedId(bedId);
        }

        List<BedResponse> availableBeds = bedService.getAvailableBeds();
        List<Relative> relatives = relativeService.getAllRelatives();

        // Dữ liệu sơ đồ phòng & giường trực quan
        List<RoomResponse> rooms = roomService.getAllRooms();
        List<RoomResponse> residentRooms = rooms.stream()
                .filter(r -> !"CLINIC".equalsIgnoreCase(r.getRoomType()) && !"RECEPTION".equalsIgnoreCase(r.getRoomType()))
                .toList();

        Map<Integer, List<BedResponse>> bedsByRoomId = new HashMap<>();
        for (RoomResponse room : residentRooms) {
            bedsByRoomId.put(room.getId(), bedService.getBedsByRoom(room.getId()));
        }

        List<ResidentResponse> allResidents = residentService.getAllResidents(null, null);
        Map<Integer, ResidentResponse> residentByBedId = new HashMap<>();
        for (ResidentResponse r : allResidents) {
            if (r.getBedId() != null && ("ACTIVE".equalsIgnoreCase(r.getStatus()) || "HOSPITALIZED".equalsIgnoreCase(r.getStatus()))) {
                residentByBedId.put(r.getBedId(), r);
            }
        }

        model.addAttribute("checkInRequest", checkInRequest);
        model.addAttribute("selectedBedId", bedId);
        model.addAttribute("availableBeds", availableBeds);
        model.addAttribute("relatives", relatives);
        model.addAttribute("residentRooms", residentRooms);
        model.addAttribute("bedsByRoomId", bedsByRoomId);
        model.addAttribute("residentByBedId", residentByBedId);
        model.addAttribute("activeMenu", "check-in");
        return "resident/check-in";
    }

    // 3. Xử lý tiếp nhận cư dân mới
    @PostMapping("/check-in")
    public String processCheckIn(
            @ModelAttribute("checkInRequest") ResidentCheckInRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ResidentResponse res = residentService.checkIn(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tiếp nhận thành công cư dân " + res.getFullName() + "! Đã xếp phòng " + res.getRoomNumber() + " - Giường " + res.getBedNumber() + ".");
            return "redirect:/residents";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tiếp nhận: " + e.getMessage());
            return "redirect:/residents/check-in" + (request != null && request.getBedId() != null ? "?bedId=" + request.getBedId() : "");
        }
    }

    // 4. Xử lý đổi phòng / Chuyển giường cuối tháng (Chỉ Lễ tân)
    @PostMapping("/transfer-bed")
    public String processTransferBed(
            @ModelAttribute BedTransferRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_RECEPTIONIST"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ Lễ tân mới có thẩm quyền thực hiện đổi phòng / chuyển giường!");
            return "redirect:/residents";
        }

        try {
            ResidentResponse res = residentService.transferBed(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Chuyển giường thành công cho cư dân " + res.getFullName() + " sang Phòng " + res.getRoomNumber() + " - Giường " + res.getBedNumber() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi chuyển giường: " + e.getMessage());
        }
        return "redirect:/residents";
    }

    // 5. Xử lý thủ tục xuất viện (Check-out) (Chỉ Lễ tân)
    @PostMapping("/{id}/check-out")
    public String processCheckOut(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "DISCHARGED") String finalStatus,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_RECEPTIONIST"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ Lễ tân mới có thẩm quyền làm thủ tục xuất viện (Check-out)!");
            return "redirect:/residents";
        }

        try {
            residentService.checkOut(id, finalStatus);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hoàn tất thủ tục xuất viện cho cư dân ID " + id + ". Giường đã chuyển sang bảo trì/dọn dẹp.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xuất viện: " + e.getMessage());
        }
        return "redirect:/residents";
    }

    // 6. Thủ tục chuyển đến bệnh viện khi có tình trạng nguy cấp (Chỉ Giám đốc cơ sở)
    @PostMapping("/{id}/hospital-transfer")
    public String processHospitalTransfer(
            @PathVariable Integer id,
            @ModelAttribute HospitalTransferRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_CENTER_MANAGER"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ Giám đốc cơ sở mới có thẩm quyền quyết định chuyển viện cấp cứu!");
            return "redirect:/residents";
        }

        try {
            ResidentResponse res = residentService.emergencyHospitalTransfer(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hoàn tất thủ tục chuyển viện cấp cứu cho cư dân " + res.getFullName() + 
                    " sang " + (request.getHospitalName() != null ? request.getHospitalName() : "Bệnh viện tuyến trên") + 
                    "! Giường hiện tại đã được tạm khóa bảo dưỡng (MAINTENANCE).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi chuyển viện cấp cứu: " + e.getMessage());
        }
        return "redirect:/residents";
    }

    // 7. Tiếp nhận lại cư dân sau điều trị tại bệnh viện
    @PostMapping("/{id}/return-from-hospital")
    public String processReturnFromHospital(
            @PathVariable Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication != null && authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_CENTER_MANAGER") || a.getAuthority().equals("ROLE_RECEPTIONIST"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện tiếp nhận lại cư dân!");
            return "redirect:/residents";
        }

        try {
            ResidentResponse res = residentService.returnFromHospital(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã tiếp nhận thành công cư dân " + res.getFullName() + " trở lại viện dưỡng lão sau điều trị tại bệnh viện!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tiếp nhận: " + e.getMessage());
        }
        return "redirect:/residents";
    }

    // 8. Tích hợp xem Lịch sử Bệnh án & Đơn thuốc trực tiếp cho Cư dân (Modal)
    @GetMapping("/{id}/medical-history")
    public String getResidentMedicalHistory(
            @PathVariable Integer id,
            Model model
    ) {
        ResidentResponse resident = residentService.getResidentById(id);
        List<HealthRecordResponse> records = healthRecordService.getRecordsByResident(id);

        model.addAttribute("resident", resident);
        model.addAttribute("records", records);
        return "resident/medical-history-modal :: medicalHistoryContent";
    }

    // 9. API JSON trả về dữ liệu bệnh án & thông tin cư dân
    @GetMapping("/{id}/medical-history-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getResidentMedicalHistoryData(@PathVariable Integer id) {
        ResidentResponse resident = residentService.getResidentById(id);
        List<HealthRecordResponse> records = healthRecordService.getRecordsByResident(id);
        return ResponseEntity.ok(Map.of(
                "resident", resident,
                "records", records
        ));
    }
}
