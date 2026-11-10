package com.group6.nhms;

import com.group6.nhms.entity.Bed;
import com.group6.nhms.entity.IncidentReport;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Room;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.repository.BedRepository;
import com.group6.nhms.repository.IncidentReportRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.RoomRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.repository.TaskScheduleRepository;
import com.group6.nhms.service.IncidentReportService;
import com.group6.nhms.service.RelativeNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ViewRenderIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TaskScheduleRepository taskScheduleRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private IncidentReportRepository incidentReportRepository;

    @Autowired
    private IncidentReportService incidentReportService;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private RelativeRepository relativeRepository;

    @Autowired
    private RelativeNotificationService relativeNotificationService;

    private MockMvc createMockMvc() {
        return MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void testLoginViewRender() throws Exception {
        createMockMvc().perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Chọn nhanh tài khoản thử nghiệm"))))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("quickFill"))));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerIndexRender() throws Exception {
        createMockMvc().perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "relative", roles = {"RELATIVE"})
    void testRelativePortalDashboardRender() throws Exception {
        createMockMvc().perform(get("/portal/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "relative", roles = {"RELATIVE"})
    void testRelativePortalIncidentsRender() throws Exception {
        createMockMvc().perform(get("/portal/incidents"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "relative", roles = {"RELATIVE"})
    void testRelativeHealthHistoryRender() throws Exception {
        createMockMvc().perform(get("/health/history"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "relative", roles = {"RELATIVE"})
    void testRelativePortalBillsRender() throws Exception {
        createMockMvc().perform(get("/portal/bills"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testProfileRenderAsStaff() throws Exception {
        createMockMvc().perform(get("/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "relative", roles = {"RELATIVE"})
    void testProfileRenderAsRelative() throws Exception {
        createMockMvc().perform(get("/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerRoomBedManagementRender() throws Exception {
        createMockMvc().perform(get("/rooms/beds"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseCannotPostCreateRoom() throws Exception {
        createMockMvc().perform(post("/rooms/beds/rooms/create")
                        .param("roomNumber", "P999")
                        .param("roomType", "STANDARD")
                        .param("capacity", "2")
                        .param("basePrice", "5000000"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCannotAccessResidentCheckIn() throws Exception {
        createMockMvc().perform(get("/residents/check-in"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCannotAccessHealthRecordsCreate() throws Exception {
        createMockMvc().perform(get("/health/records/create"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCannotAccessRelativePortal() throws Exception {
        createMockMvc().perform(get("/portal/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCannotAccessHealthRecordsCreate() throws Exception {
        createMockMvc().perform(get("/health/records/create"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testDoctorCannotAccessStaffManagement() throws Exception {
        createMockMvc().perform(get("/management/staff"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testDoctorCanAccessHealthRecordsCreate() throws Exception {
        createMockMvc().perform(get("/health/records/create"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCanAccessResidentCheckIn() throws Exception {
        createMockMvc().perform(get("/residents/check-in"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCanAccessBillingMonthlyView() throws Exception {
        createMockMvc().perform(get("/billing/monthly"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCannotPostBillingGenerate() throws Exception {
        createMockMvc().perform(post("/billing/generate")
                        .param("residentId", "1")
                        .param("year", "2026")
                        .param("month", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCannotPostBillingPay() throws Exception {
        createMockMvc().perform(post("/billing/orders/1/pay"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseCannotPostEditRoom() throws Exception {
        createMockMvc().perform(post("/rooms/beds/rooms/1/edit")
                        .param("roomNumber", "P101-NEW")
                        .param("roomType", "STANDARD")
                        .param("capacity", "3")
                        .param("basePrice", "6000000"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseCannotPostDeleteRoom() throws Exception {
        createMockMvc().perform(post("/rooms/beds/rooms/1/delete"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseCannotPostEditBed() throws Exception {
        createMockMvc().perform(post("/rooms/beds/1/edit")
                        .param("bedNumber", "G101-1-NEW")
                        .param("status", "AVAILABLE"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseCannotPostDeleteBed() throws Exception {
        createMockMvc().perform(post("/rooms/beds/1/delete"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCanPostEditRoom() throws Exception {
        Room anyRoom = roomRepository.findAll().stream().findFirst().orElseThrow();
        createMockMvc().perform(post("/rooms/beds/rooms/" + anyRoom.getId() + "/edit")
                        .param("roomNumber", anyRoom.getRoomNumber())
                        .param("roomType", anyRoom.getRoomType())
                        .param("capacity", String.valueOf(Math.max(anyRoom.getCapacity(), 2)))
                        .param("basePrice", anyRoom.getBasePrice().toString()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCannotDeleteOccupiedRoom() throws Exception {
        createMockMvc().perform(post("/rooms/beds/rooms/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCannotDeleteOccupiedBed() throws Exception {
        createMockMvc().perform(post("/rooms/beds/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testScheduleAssignRejectsPastDate() throws Exception {
        Staff nurse = staffRepository.findAll().stream()
                .filter(s -> "NURSE".equalsIgnoreCase(s.getJobTitle()))
                .findFirst().orElseThrow();
        Room residentRoom = roomRepository.findAll().stream()
                .filter(r -> "STANDARD".equalsIgnoreCase(r.getRoomType()))
                .findFirst().orElseThrow();

        createMockMvc().perform(post("/schedules/assign")
                        .param("workDate", "2020-01-01")
                        .param("shift", "MORNING")
                        .param("staffId", nurse.getId().toString())
                        .param("roomId", residentRoom.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testScheduleAssignRejectsCenterManager() throws Exception {
        Staff manager = staffRepository.findAll().stream()
                .filter(s -> "CENTER_MANAGER".equalsIgnoreCase(s.getJobTitle()))
                .findFirst().orElseThrow();
        Room anyRoom = roomRepository.findAll().get(0);

        createMockMvc().perform(post("/schedules/assign")
                        .param("workDate", LocalDate.now().plusDays(1).toString())
                        .param("shift", "MORNING")
                        .param("staffId", manager.getId().toString())
                        .param("roomId", anyRoom.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testScheduleAssignDoctorToResidentRoomFails() throws Exception {
        Staff doctor = staffRepository.findAll().stream()
                .filter(s -> "DOCTOR".equalsIgnoreCase(s.getJobTitle()))
                .findFirst().orElseThrow();
        Room residentRoom = roomRepository.findAll().stream()
                .filter(r -> "STANDARD".equalsIgnoreCase(r.getRoomType()))
                .findFirst().orElseThrow();

        createMockMvc().perform(post("/schedules/assign")
                        .param("workDate", LocalDate.now().plusDays(2).toString())
                        .param("shift", "MORNING")
                        .param("staffId", doctor.getId().toString())
                        .param("roomId", residentRoom.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testScheduleAssignDoctorToClinicSucceeds() throws Exception {
        Staff doctor = staffRepository.findAll().stream()
                .filter(s -> "DOCTOR".equalsIgnoreCase(s.getJobTitle()))
                .findFirst().orElseThrow();
        Room clinic = roomRepository.findAll().stream()
                .filter(r -> "CLINIC".equalsIgnoreCase(r.getRoomType()))
                .findFirst().orElseThrow();

        LocalDate testDate = LocalDate.now().plusDays(25);
        taskScheduleRepository.deleteAll(taskScheduleRepository.findByStaffIdAndShiftDate(doctor.getId(), testDate));

        createMockMvc().perform(post("/schedules/assign")
                        .param("workDate", testDate.toString())
                        .param("shift", "MORNING")
                        .param("staffId", doctor.getId().toString())
                        .param("roomId", clinic.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testScheduleAssignReceptionistToReceptionSucceeds() throws Exception {
        Staff receptionist = staffRepository.findAll().stream()
                .filter(s -> "RECEPTIONIST".equalsIgnoreCase(s.getJobTitle()))
                .findFirst().orElseThrow();
        Room reception = roomRepository.findAll().stream()
                .filter(r -> "RECEPTION".equalsIgnoreCase(r.getRoomType()))
                .findFirst().orElseThrow();

        LocalDate testDate = LocalDate.now().plusDays(26);
        taskScheduleRepository.deleteAll(taskScheduleRepository.findByStaffIdAndShiftDate(receptionist.getId(), testDate));

        createMockMvc().perform(post("/schedules/assign")
                        .param("workDate", testDate.toString())
                        .param("shift", "MORNING")
                        .param("staffId", receptionist.getId().toString())
                        .param("roomId", reception.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testStaffListWithSearchKeyword() throws Exception {
        createMockMvc().perform(get("/management/staff").param("keyword", "Minh"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerIncidentReportPageRender() throws Exception {
        createMockMvc().perform(get("/incidents/report"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCannotReportIncident() throws Exception {
        createMockMvc().perform(post("/incidents/report")
                        .param("residentId", "1")
                        .param("title", "Test Incident")
                        .param("severityLevel", "LOW")
                        .param("description", "Test Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseCanReportLowIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        if (resident != null) {
            createMockMvc().perform(post("/incidents/report")
                            .param("residentId", String.valueOf(resident.getId()))
                            .param("title", "Nurse Low Incident")
                            .param("severityLevel", "LOW")
                            .param("description", "Vết trầy xước nhẹ"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));
        }
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseCannotReportHighOrCriticalIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        if (resident != null) {
            createMockMvc().perform(post("/incidents/report")
                            .param("residentId", String.valueOf(resident.getId()))
                            .param("title", "Nurse High Incident")
                            .param("severityLevel", "HIGH")
                            .param("description", "Gãy xương"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
        }
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testDoctorCanReportHighIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        if (resident != null) {
            createMockMvc().perform(post("/incidents/report")
                            .param("residentId", String.valueOf(resident.getId()))
                            .param("title", "Doctor High Incident")
                            .param("severityLevel", "HIGH")
                            .param("description", "Cấp cứu y tế"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));
        }
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testDoctorCannotReportLowIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        if (resident != null) {
            createMockMvc().perform(post("/incidents/report")
                            .param("residentId", String.valueOf(resident.getId()))
                            .param("title", "Doctor Low Incident")
                            .param("severityLevel", "LOW")
                            .param("description", "Trầy xước da"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
        }
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCanReportFacilityIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        if (resident != null) {
            createMockMvc().perform(post("/incidents/report")
                            .param("residentId", String.valueOf(resident.getId()))
                            .param("title", "Hỏng điều hòa phòng P101")
                            .param("severityLevel", "MEDIUM")
                            .param("description", "Điều hòa kêu to và không mát"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));
        }
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testDoctorCanResolveNurseIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        Staff nurse = staffRepository.findByJobTitle("NURSE").stream().findFirst().orElse(null);
        if (resident != null && nurse != null) {
            com.group6.nhms.dto.request.IncidentReportCreateRequest req = new com.group6.nhms.dto.request.IncidentReportCreateRequest();
            req.setResidentId(resident.getId());
            req.setTitle("Sốt nhẹ cần theo dõi");
            req.setDescription("Nhiệt độ 37.9 độ C");
            req.setSeverityLevel("MEDIUM");
            var inc = incidentReportService.createIncident(req, nurse.getId());

            createMockMvc().perform(post("/incidents/" + inc.getId() + "/resolve")
                            .param("actionTaken", "Đã cho uống thuốc hạ sốt và theo dõi sinh hiệu"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));
        }
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testDoctorCanResolveHighIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        Staff doctor = staffRepository.findByJobTitle("DOCTOR").stream().findFirst().orElse(null);
        if (resident != null && doctor != null) {
            com.group6.nhms.dto.request.IncidentReportCreateRequest req = new com.group6.nhms.dto.request.IncidentReportCreateRequest();
            req.setResidentId(resident.getId());
            req.setTitle("Khó thở cấp tính");
            req.setDescription("SpO2 giảm xuống 88%");
            req.setSeverityLevel("HIGH");
            var inc = incidentReportService.createIncident(req, doctor.getId());

            createMockMvc().perform(post("/incidents/" + inc.getId() + "/resolve")
                            .param("actionTaken", "Bác sĩ đã cho thở oxy dòng cao và chỉ định khí dung"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));

            var resolved = incidentReportRepository.findById(inc.getId()).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals("RESOLVED", resolved.getStatus());
        }
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testDoctorCannotResolveCriticalIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        Staff doctor = staffRepository.findByJobTitle("DOCTOR").stream().findFirst().orElse(null);
        if (resident != null && doctor != null) {
            com.group6.nhms.dto.request.IncidentReportCreateRequest req = new com.group6.nhms.dto.request.IncidentReportCreateRequest();
            req.setResidentId(resident.getId());
            req.setTitle("Ngừng tuần hoàn hô hấp");
            req.setDescription("Cấp cứu khẩn cấp nguy kịch");
            req.setSeverityLevel("CRITICAL");
            var inc = incidentReportService.createIncident(req, doctor.getId());

            createMockMvc().perform(post("/incidents/" + inc.getId() + "/resolve")
                            .param("actionTaken", "Bác sĩ thử tự hoàn tất"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
        }
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testDoctorCannotResolveReceptionistIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        Staff receptionist = staffRepository.findByJobTitle("RECEPTIONIST").stream().findFirst().orElse(null);
        if (resident != null && receptionist != null) {
            com.group6.nhms.dto.request.IncidentReportCreateRequest req = new com.group6.nhms.dto.request.IncidentReportCreateRequest();
            req.setResidentId(resident.getId());
            req.setTitle("Hỏng khóa cửa");
            req.setDescription("Kẹt ổ khóa");
            req.setSeverityLevel("LOW");
            var inc = incidentReportService.createIncident(req, receptionist.getId());

            createMockMvc().perform(post("/incidents/" + inc.getId() + "/resolve")
                            .param("actionTaken", "Bác sĩ thử xử lý cửa"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
        }
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCannotResolveNurseIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        Staff nurse = staffRepository.findByJobTitle("NURSE").stream().findFirst().orElse(null);
        if (resident != null && nurse != null) {
            com.group6.nhms.dto.request.IncidentReportCreateRequest req = new com.group6.nhms.dto.request.IncidentReportCreateRequest();
            req.setResidentId(resident.getId());
            req.setTitle("Trầy da ngón tay");
            req.setDescription("Xước nhẹ");
            req.setSeverityLevel("LOW");
            var inc = incidentReportService.createIncident(req, nurse.getId());

            createMockMvc().perform(post("/incidents/" + inc.getId() + "/resolve")
                            .param("actionTaken", "Manager can thiệp y tế"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
        }
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCanResolveReceptionistIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        Staff receptionist = staffRepository.findByJobTitle("RECEPTIONIST").stream().findFirst().orElse(null);
        if (resident != null && receptionist != null) {
            com.group6.nhms.dto.request.IncidentReportCreateRequest req = new com.group6.nhms.dto.request.IncidentReportCreateRequest();
            req.setResidentId(resident.getId());
            req.setTitle("Hỏng vòi nước");
            req.setDescription("Rò rỉ nước phòng tắm");
            req.setSeverityLevel("MEDIUM");
            var inc = incidentReportService.createIncident(req, receptionist.getId());

            createMockMvc().perform(post("/incidents/" + inc.getId() + "/resolve")
                            .param("actionTaken", "Đã chỉ đạo tổ bảo trì thay van mới"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));
        }
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCanResolveFacilityIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        Staff receptionist = staffRepository.findByJobTitle("RECEPTIONIST").stream().findFirst().orElse(null);
        if (resident != null && receptionist != null) {
            com.group6.nhms.dto.request.IncidentReportCreateRequest req = new com.group6.nhms.dto.request.IncidentReportCreateRequest();
            req.setResidentId(resident.getId());
            req.setTitle("Kẹt khóa phòng P102");
            req.setDescription("Nhân viên kỹ thuật đã tra dầu và sửa xong");
            req.setSeverityLevel("LOW");
            var inc = incidentReportService.createIncident(req, receptionist.getId());

            createMockMvc().perform(post("/incidents/" + inc.getId() + "/resolve")
                            .param("actionTaken", "Nhân viên kỹ thuật đã thay ổ khóa mới"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));

            var resolvedIncident = incidentReportRepository.findById(inc.getId()).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals("RESOLVED", resolvedIncident.getStatus());
        }
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCannotResolveNurseClinicalIncident() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        Staff nurse = staffRepository.findByJobTitle("NURSE").stream().findFirst().orElse(null);
        if (resident != null && nurse != null) {
            com.group6.nhms.dto.request.IncidentReportCreateRequest req = new com.group6.nhms.dto.request.IncidentReportCreateRequest();
            req.setResidentId(resident.getId());
            req.setTitle("Sốt nhẹ cần theo dõi");
            req.setDescription("Nhiệt độ 38 độ C");
            req.setSeverityLevel("MEDIUM");
            var inc = incidentReportService.createIncident(req, nurse.getId());

            createMockMvc().perform(post("/incidents/" + inc.getId() + "/resolve")
                            .param("actionTaken", "Lễ tân thử xử lý y tế"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("errorMessage"));
        }
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCanReportCommonAreaWithoutResident() throws Exception {
        createMockMvc().perform(post("/incidents/report")
                        .param("location", "Hành lang Tầng 2")
                        .param("title", "Cháy bóng đèn chiếu sáng")
                        .param("severityLevel", "LOW")
                        .param("description", "Bóng đèn nhấp nháy rồi tắt"))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistReportBedMaintenanceAndManagerRestore() throws Exception {
        Bed bed = bedRepository.findAll().stream()
                .filter(b -> !"MAINTENANCE".equalsIgnoreCase(b.getStatus()))
                .findFirst().orElse(null);
        if (bed != null) {
            String initialStatus = bed.getStatus();
            createMockMvc().perform(post("/incidents/report")
                            .param("bedId", String.valueOf(bed.getId()))
                            .param("setMaintenance", "true")
                            .param("title", "Gãy dát giường cần sửa")
                            .param("severityLevel", "HIGH")
                            .param("description", "Thanh dát giường bị nứt gãy"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));

            Bed afterReportBed = bedRepository.findById(bed.getId()).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals("MAINTENANCE", afterReportBed.getStatus());

            var pendingList = incidentReportService.getPendingIncidents();
            var incident = pendingList.stream()
                    .filter(i -> "Gãy dát giường cần sửa".equals(i.getTitle()))
                    .findFirst().orElseThrow();

            createMockMvc().perform(post("/incidents/" + incident.getId() + "/resolve")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("manager").roles("CENTER_MANAGER"))
                            .param("actionTaken", "Đã thay thanh dát giường mới chắc chắn"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));

            Bed afterResolveBed = bedRepository.findById(bed.getId()).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals(initialStatus, afterResolveBed.getStatus());
        }
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testHealthHistoryRenderForDoctor() throws Exception {
        createMockMvc().perform(get("/health/history"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerHealthHistoryRedirectsToResidents() throws Exception {
        createMockMvc().perform(get("/health/history"))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/residents"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerResidentsListContainsMedicalHistoryModal() throws Exception {
        createMockMvc().perform(get("/residents"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("residentMedicalRecordModal")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("openMedicalRecordModal")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("/health/history"))));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerViewResidentMedicalHistoryModalAndJson() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        if (resident != null) {
            createMockMvc().perform(get("/residents/" + resident.getId() + "/medical-history"))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("TIỀN SỬ BỆNH LÝ")));

            createMockMvc().perform(get("/residents/" + resident.getId() + "/medical-history-data"))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.resident.fullName").value(resident.getFullName()));
        }
    }

    @Test
    void testRegisterRelativeRejectsInvalidPhone() throws Exception {
        createMockMvc().perform(post("/register")
                        .param("fullName", "Nguyễn Văn Test")
                        .param("phoneNumber", "0123456789") // Invalid prefix 01
                        .param("email", "test@gmail.com")
                        .param("username", "testuser_invalid_phone")
                        .param("password", "123456")
                        .param("confirmPassword", "123456"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasErrors());
    }

    @Test
    void testRegisterRelativeRejectsInvalidEmail() throws Exception {
        createMockMvc().perform(post("/register")
                        .param("fullName", "Nguyễn Văn Test")
                        .param("phoneNumber", "0912345678")
                        .param("email", "not-a-valid-email") // Invalid email
                        .param("username", "testuser_invalid_email")
                        .param("password", "123456")
                        .param("confirmPassword", "123456"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasErrors());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCannotPerformTransferBedOrCheckout() throws Exception {
        createMockMvc().perform(post("/residents/transfer-bed")
                        .param("residentId", "1")
                        .param("newBedId", "2")
                        .param("reason", "Manager đổi phòng"))
                .andExpect(status().isForbidden());

        createMockMvc().perform(post("/residents/1/check-out"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCannotPerformHospitalTransfer() throws Exception {
        createMockMvc().perform(post("/residents/1/hospital-transfer")
                        .param("hospitalName", "Bệnh viện 115")
                        .param("emergencyReason", "Cấp cứu"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerResidentListActions() throws Exception {
        createMockMvc().perform(get("/residents"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Chuyển viện cấp cứu (Nguy cấp)")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("hospitalTransferModal")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Chuyển giường (cuối tháng)"))))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Làm thủ tục Xuất viện"))));
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistResidentListActions() throws Exception {
        createMockMvc().perform(get("/residents"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Chuyển giường (cuối tháng)")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Làm thủ tục Xuất viện")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Chuyển viện cấp cứu (Nguy cấp)"))));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerPerformsHospitalTransferAndReturn() throws Exception {
        Resident resident = residentRepository.findByStatus("ACTIVE").stream()
                .filter(r -> r.getBed() != null)
                .findFirst().orElse(null);

        if (resident != null) {
            Integer bedId = resident.getBed().getId();

            createMockMvc().perform(post("/residents/" + resident.getId() + "/hospital-transfer")
                            .param("hospitalName", "Bệnh viện Chợ Rẫy")
                            .param("emergencyReason", "Đột quỵ não cấp tính nguy kịch")
                            .param("transportInfo", "Xe 115 - BS. Minh đi kèm"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/residents"))
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));

            Resident afterTransfer = residentRepository.findById(resident.getId()).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals("HOSPITALIZED", afterTransfer.getStatus());

            Bed afterBed = bedRepository.findById(bedId).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals("MAINTENANCE", afterBed.getStatus());

            // Sau đó tiếp nhận lại
            createMockMvc().perform(post("/residents/" + resident.getId() + "/return-from-hospital"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/residents"))
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));

            Resident afterReturn = residentRepository.findById(resident.getId()).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", afterReturn.getStatus());

            Bed afterReturnBed = bedRepository.findById(bedId).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals("OCCUPIED", afterReturnBed.getStatus());
        }
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerIncidentReportSeparatesReceptionistAndClinicalTabs() throws Exception {
        createMockMvc().perform(get("/incidents/report"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("managerIncidentTabs")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Mảng Cơ Sở Vật Chất (Lễ Tân Báo Cáo)")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Mảng Y Tế & Chăm Sóc (Bác Sĩ & Điều Dưỡng)")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("tab-receptionist")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("tab-clinical")));
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistIncidentReportContainsTwoDomainTabsAndNoResidentOption() throws Exception {
        createMockMvc().perform(get("/incidents/report"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("receptionistIncidentTabs")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Mảng Cơ Sở Vật Chất (Lễ Tân Báo Cáo)")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Mảng Y Tế & Chăm Sóc (Bác Sĩ & Điều Dưỡng)")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("rec-tab-facility")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("rec-tab-clinical")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Báo cáo Giường")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Báo cáo Phòng")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Khu sinh hoạt chung")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Mới nhất")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Cũ nhất")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Nhân viên xử lý")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Báo cáo đã giải quyết")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Chờ Giám đốc xử lý"))))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("id=\"targetResident\""))))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Cư dân gặp sự cố y tế / lâm sàng"))));
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCanReportRoomFacility() throws Exception {
        Room room = roomRepository.findAll().stream().findFirst().orElse(null);
        if (room != null) {
            createMockMvc().perform(post("/incidents/report")
                            .param("roomId", String.valueOf(room.getId()))
                            .param("title", "Hỏng khóa cửa phòng")
                            .param("severityLevel", "LOW")
                            .param("description", "Kẹt ổ khóa không mở được"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.flash().attributeExists("successMessage"));
        }
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerIncidentReportTimeSorting() throws Exception {
        createMockMvc().perform(get("/incidents/report").param("sort", "oldest"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("currentSort", "oldest"))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Mới nhất")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Cũ nhất")));
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseIncidentReportDoesNotContainDomainTabs() throws Exception {
        createMockMvc().perform(get("/incidents/report"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("rec-tab-facility"))))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("rec-tab-clinical"))))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("tab-receptionist"))))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("tab-clinical"))));
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testDoctorAndNurseIncidentReportOnlyShowClinicalIncidents() throws Exception {
        createMockMvc().perform(get("/incidents/report"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Danh Sách Sự Cố Y Tế & Chăm Sóc")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Lễ tân (CSVC)"))))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Hỏng điều hòa phòng P101"))))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Rò rỉ nước phòng tắm"))))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Xử lý y tế")));
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCheckInRendersVisualRoomBedModal() throws Exception {
        createMockMvc().perform(get("/residents/check-in"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("visualRoomBedModal")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Sơ Đồ Phòng & Giường Trực Quan")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Cư dân đang lưu trú:")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Thêm cư dân mới vào giường này")));
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCheckInWithBedIdPreselected() throws Exception {
        createMockMvc().perform(get("/residents/check-in").param("bedId", "4"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("selectedBedId", 4))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("visualRoomBedModal")));
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistRoomBedMapContainsDirectAddResidentButton() throws Exception {
        createMockMvc().perform(get("/rooms/beds"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("/residents/check-in?bedId=")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Tiếp nhận cư dân mới")));
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCanAccessRelativeNotificationsPage() throws Exception {
        createMockMvc().perform(get("/billing/notifications"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Gửi Thông Báo Viện Phí &amp; Sự Cố Cho Thân Nhân")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Gửi Email Cho Tất Cả Thân Nhân")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Bảng Tổng Hợp Thân Nhân Cần Gửi Thông Báo")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("emailPreviewModal")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("confirmSendAllModal")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("confirmSendSingleModal")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Lịch Sử Gửi Email Gần Nhất")));
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseForbiddenFromRelativeNotifications() throws Exception {
        createMockMvc().perform(get("/billing/notifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "relative", roles = {"RELATIVE"})
    void testRelativeForbiddenFromRelativeNotifications() throws Exception {
        createMockMvc().perform(get("/billing/notifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCanSendSingleRelativeNotification() throws Exception {
        Relative relative = relativeRepository.findAll().stream()
                .filter(r -> r.getEmail() != null && !r.getEmail().isBlank())
                .findFirst().orElse(null);

        if (relative != null) {
            createMockMvc().perform(post("/billing/notifications/send/" + relative.getId()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/billing/notifications"));
        }
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testReceptionistCanSendAllRelativeNotifications() throws Exception {
        createMockMvc().perform(post("/billing/notifications/send-all"))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/billing/notifications"));
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testSidebarAndMonthlyContainsRelativeNotificationsForReceptionist() throws Exception {
        createMockMvc().perform(get("/billing/monthly"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Gửi Thông Báo Thân Nhân")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("/billing/notifications")));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCanAccessMedicationInventory() throws Exception {
        createMockMvc().perform(get("/medications"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("addMedicationModal")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("editStockModal")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("/medications/create")));
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseForbiddenFromMedicationInventory() throws Exception {
        createMockMvc().perform(get("/medications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"CENTER_MANAGER"})
    void testManagerCanCreateMedication() throws Exception {
        createMockMvc().perform(post("/medications/create")
                        .param("medName", "Paracetamol 500mg Test")
                        .param("quantityInStock", "100")
                        .param("unitPrice", "2000")
                        .param("supplier", "Dược Hậu Giang")
                        .param("expiryDate", "2027-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/medications"));
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseHealthHistoryUIEnhancements() throws Exception {
        createMockMvc().perform(get("/health/history"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Lịch Sử Bệnh Án")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Chỉnh sửa tiền sử bệnh lý")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Xem theo ngày:")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("toggle-record-btn")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("onclick=\"clearDateFilter()\""))))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Xem Hồ Sơ Bệnh Án"))));
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testDoctorRecordCreateShowsNurseVitals() throws Exception {
        createMockMvc().perform(get("/health/records/create"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Chỉ Số Sinh Hiệu (Điều Dưỡng Ghi Nhận)")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Kê Đơn Thuốc (Tự động trừ kho)")));
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testDoctorCanUpdateMedicalHistory() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        if (resident != null) {
            createMockMvc().perform(post("/health/residents/" + resident.getId() + "/medical-history")
                            .param("medicalHistory", "Tiền sử tăng huyết áp nhẹ, suy tim độ 1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(MockMvcResultMatchers.redirectedUrl("/health/history?residentId=" + resident.getId()));
        }
    }

    @Test
    @WithMockUser(username = "nurse", roles = {"NURSE"})
    void testNurseIncidentReportDoesNotContainSotNhe() throws Exception {
        createMockMvc().perform(get("/incidents/report"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Chóng mặt té ngã, xây xát, bầm tím")))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Sốt nhẹ, xây xát, bầm tím"))));
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void testHealthHistoryRecordViewModalRender() throws Exception {
        Resident resident = residentRepository.findAll().stream().findFirst().orElse(null);
        if (resident != null) {
            createMockMvc().perform(get("/health/history").param("residentId", String.valueOf(resident.getId())))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("data-bs-target=\"#recordModal_")))
                    .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("recordModalsContainer")))
                    .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Chi Tiết Phiếu Khám & Đơn Thuốc")))
                    .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Chỉ Số Sinh Hiệu")))
                    .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Chẩn Đoán Lâm Sàng & Dặn Dò")));
        }
    }
}
