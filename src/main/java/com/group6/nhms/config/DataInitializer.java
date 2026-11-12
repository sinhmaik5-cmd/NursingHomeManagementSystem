package com.group6.nhms.config;


import com.group6.nhms.entity.*;
import com.group6.nhms.enums.BedStatus;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.enums.RoleEnum;
import com.group6.nhms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.time.LocalDate;


@Component
public class DataInitializer implements CommandLineRunner {


   private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);


   private final AccountRepository accountRepository;
   private final StaffRepository staffRepository;
   private final RelativeRepository relativeRepository;
   private final RoomRepository roomRepository;
   private final BedRepository bedRepository;
   private final ResidentRepository residentRepository;
   private final MedicationRepository medicationRepository;
   private final ServiceRepository serviceRepository;
   private final TaskScheduleRepository taskScheduleRepository;
   private final PasswordEncoder passwordEncoder;


   public DataInitializer(
           AccountRepository accountRepository,
           StaffRepository staffRepository,
           RelativeRepository relativeRepository,
           RoomRepository roomRepository,
           BedRepository bedRepository,
           ResidentRepository residentRepository,
           MedicationRepository medicationRepository,
           ServiceRepository serviceRepository,
           TaskScheduleRepository taskScheduleRepository,
           PasswordEncoder passwordEncoder
   ) {
       this.accountRepository = accountRepository;
       this.staffRepository = staffRepository;
       this.relativeRepository = relativeRepository;
       this.roomRepository = roomRepository;
       this.bedRepository = bedRepository;
       this.residentRepository = residentRepository;
       this.medicationRepository = medicationRepository;
       this.serviceRepository = serviceRepository;
       this.taskScheduleRepository = taskScheduleRepository;
       this.passwordEncoder = passwordEncoder;
   }


   @Override
   public void run(String... args) {
       try {
           cleanupLegacyRoles();
           initStaffAndAccounts();
           initRoomsAndBeds();
           initMedications();
           initServices();
           initResidents();
           initSampleSchedules();
           log.info("Data initialization check completed successfully.");
       } catch (Exception e) {
           log.warn("Data initialization encountered a non-fatal error: {}", e.getMessage());
       }
   }


   private void cleanupLegacyRoles() {
       accountRepository.findAll().stream()
               .filter(a -> "ACCOUNTANT".equalsIgnoreCase(a.getRole()) || "MANAGER".equalsIgnoreCase(a.getRole()))
               .forEach(accountRepository::delete);
       staffRepository.findAll().stream()
               .filter(s -> "ACCOUNTANT".equalsIgnoreCase(s.getJobTitle()) || "MANAGER".equalsIgnoreCase(s.getJobTitle()))
               .forEach(s -> {
                   try { staffRepository.delete(s); } catch (Exception ignored) {}
               });
   }


   private void initStaffAndAccounts() {
       // Staff 1: Giám đốc cơ sở (Center Manager)
       Staff managerStaff = staffRepository.findByJobTitle("CENTER_MANAGER").stream().findFirst().orElseGet(() -> {
           Staff s = new Staff();
           s.setFullName("Trần Quang Huy");
           s.setJobTitle("CENTER_MANAGER");
           s.setPhone("0912345678");
           s.setEmail("manager@nhmscare.vn");
           s.setCitizenId("079085001234");
           s.setSpecialization("Quản lý toàn diện & Điều hành");
           return staffRepository.save(s);
       });


       // Staff 2: Lễ tân & Thu phí (Receptionist)
       Staff receptionistStaff = staffRepository.findByJobTitle("RECEPTIONIST").stream().findFirst().orElseGet(() -> {
           Staff s = new Staff();
           s.setFullName("Lê Thu Trang");
           s.setJobTitle("RECEPTIONIST");
           s.setPhone("0987654321");
           s.setEmail("letan.trang@nhmscare.vn");
           s.setCitizenId("079192003456");
           s.setSpecialization("Lễ tân & Hành chính thu phí");
           return staffRepository.save(s);
       });


       // Staff 3: Bác sĩ (Doctor)
       Staff doctorStaff = staffRepository.findByJobTitle("DOCTOR").stream().findFirst().orElseGet(() -> {
           Staff s = new Staff();
           s.setFullName("BS. Nguyễn Văn Minh");
           s.setJobTitle("DOCTOR");
           s.setPhone("0901234567");
           s.setEmail("bs.minh@nhmscare.vn");
           s.setCitizenId("079080005678");
           s.setSpecialization("Nội tổng quát & Lão khoa");
           return staffRepository.save(s);
       });


       // Staff 4: Điều dưỡng (Nurse)
       Staff nurseStaff = staffRepository.findByJobTitle("NURSE").stream().findFirst().orElseGet(() -> {
           Staff s = new Staff();
           s.setFullName("ĐD. Lê Thị Hoa");
           s.setJobTitle("NURSE");
           s.setPhone("0907654321");
           s.setEmail("dd.hoa@nhmscare.vn");
           s.setCitizenId("079190009876");
           s.setSpecialization("Chăm sóc phục hồi & Vệ sinh giường");
           return staffRepository.save(s);
       });


       // Đảm bảo các nhân viên mẫu hiện có nếu chưa có Email / CCCD thì cập nhật bổ sung
       if (managerStaff.getEmail() == null || managerStaff.getCitizenId() == null) {
           managerStaff.setEmail("manager@nhmscare.vn");
           managerStaff.setCitizenId("079085001234");
           managerStaff = staffRepository.save(managerStaff);
       }
       if (receptionistStaff.getEmail() == null || receptionistStaff.getCitizenId() == null) {
           receptionistStaff.setEmail("letan.trang@nhmscare.vn");
           receptionistStaff.setCitizenId("079192003456");
           receptionistStaff = staffRepository.save(receptionistStaff);
       }
       if (doctorStaff.getEmail() == null || doctorStaff.getCitizenId() == null) {
           doctorStaff.setEmail("bs.minh@nhmscare.vn");
           doctorStaff.setCitizenId("079080005678");
           doctorStaff = staffRepository.save(doctorStaff);
       }
       if (nurseStaff.getEmail() == null || nurseStaff.getCitizenId() == null) {
           nurseStaff.setEmail("dd.hoa@nhmscare.vn");
           nurseStaff.setCitizenId("079190009876");
           nurseStaff = staffRepository.save(nurseStaff);
       }


       // Relative 1
       Relative relative = relativeRepository.findAll().stream().findFirst().orElseGet(() -> {
           Relative r = new Relative();
           r.setFullName("Nguyễn Tuấn Dũng");
           r.setPhoneNumber("0911223344");
           r.setEmail("dung.nguyen@gmail.com");
           r.setAddress("123 Phố Huế, Hai Bà Trưng, Hà Nội");
           r.setRelationship("Con trai");
           return relativeRepository.save(r);
       });


       // Chỉ khởi tạo tài khoản mẫu nếu chưa tồn tại trong DB (không ghi đè mật khẩu hay trạng thái khóa đã thay đổi)
       createAccountIfNotExists("manager", "123456", RoleEnum.CENTER_MANAGER.name(), managerStaff.getId());
       createAccountIfNotExists("receptionist", "123456", RoleEnum.RECEPTIONIST.name(), receptionistStaff.getId());
       createAccountIfNotExists("doctor", "123456", RoleEnum.DOCTOR.name(), doctorStaff.getId());
       createAccountIfNotExists("nurse", "123456", RoleEnum.NURSE.name(), nurseStaff.getId());
       createAccountIfNotExists("relative", "123456", RoleEnum.RELATIVE.name(), relative.getId());
   }


   private void createAccountIfNotExists(String username, String rawPassword, String role, Integer refId) {
       accountRepository.findByUsername(username).ifPresentOrElse(
               existing -> {
                   boolean updated = false;
                   if (existing.getRole() == null || !existing.getRole().equals(role)) {
                       existing.setRole(role);
                       updated = true;
                   }
                   if (existing.getReferenceId() == null && refId != null) {
                       existing.setReferenceId(refId);
                       updated = true;
                   }
                   if (updated) {
                       accountRepository.save(existing);
                   }
               },
               () -> {
                   Account account = new Account();
                   account.setUsername(username);
                   account.setPasswordHash(passwordEncoder.encode(rawPassword));
                   account.setRole(role);
                   account.setReferenceId(refId);
                   account.setIsActive(true);
                   accountRepository.save(account);
               }
       );
   }


   private void initRoomsAndBeds() {
       if (roomRepository.count() == 0) {
           createRoomWithBeds("P101", "STANDARD", "5000000", "G101-1", "G101-2", "G101-3", "G101-4");
           createRoomWithBeds("P102", "STANDARD", "5000000", "G102-1", "G102-2", "G102-3", "G102-4");
           createRoomWithBeds("P201", "VIP", "8000000", "G201-1");
           createRoomWithBeds("P202", "VIP", "8000000", "G202-1");
       }
   }

   private void createRoomWithBeds(String roomNumber, String roomType, String basePrice, String... bedNumbers) {
       Room room = new Room();
       room.setRoomNumber(roomNumber);
       room.setRoomType(roomType);
       room.setBasePrice(new BigDecimal(basePrice));
       room.setCapacity(bedNumbers.length);
       Room savedRoom = roomRepository.save(room);

       for (String bedNumber : bedNumbers) {
           Bed bed = new Bed();
           bed.setBedNumber(bedNumber);
           bed.setRoom(savedRoom);
           bed.setStatus(BedStatus.AVAILABLE.name());
           bedRepository.save(bed);
       }


       // Khởi tạo phòng khám Bác sĩ và Quầy Lễ tân nếu chưa tồn tại
       if (!roomRepository.existsByRoomNumber("PK-01")) {
           Room pk01 = new Room();
           pk01.setRoomNumber("PK-01");
           pk01.setRoomType("CLINIC");
           pk01.setBasePrice(BigDecimal.ZERO);
           pk01.setCapacity(2);
           pk01.setStatus("AVAILABLE");
           roomRepository.save(pk01);
       }


       if (!roomRepository.existsByRoomNumber("LT-01")) {
           Room lt01 = new Room();
           lt01.setRoomNumber("LT-01");
           lt01.setRoomType("RECEPTION");
           lt01.setBasePrice(BigDecimal.ZERO);
           lt01.setCapacity(2);
           lt01.setStatus("AVAILABLE");
           roomRepository.save(lt01);
       }
   }


   private void initMedications() {
       if (medicationRepository.count() == 0) {
           saveMed("Amlodipine 5mg", 150, LocalDate.now().plusMonths(18), "Dược Hậu Giang", new BigDecimal("3500"));
           saveMed("Metformin 500mg", 200, LocalDate.now().plusMonths(12), "Stada", new BigDecimal("2500"));
           saveMed("Panadol Extra", 300, LocalDate.now().plusMonths(24), "GSK", new BigDecimal("1800"));
           saveMed("Losartan 50mg", 80, LocalDate.now().plusMonths(15), "Dược Hà Tây", new BigDecimal("4200"));
           saveMed("Atorvastatin 10mg", 90, LocalDate.now().plusMonths(8), "Pfizer", new BigDecimal("8500"));
           saveMed("Ginkgo Biloba 120mg", 45, LocalDate.now().plusMonths(20), "Trunature", new BigDecimal("4500"));
       }
   }


   private void saveMed(String name, int qty, LocalDate exp, String supplier, BigDecimal price) {
       Medication m = new Medication();
       m.setMedName(name);
       m.setQuantityInStock(qty);
       m.setExpiryDate(exp);
       m.setSupplier(supplier);
       m.setUnitPrice(price);
       medicationRepository.save(m);
   }


   private void initServices() {
       if (serviceRepository.count() == 0) {
           saveService("Vật lý trị liệu phục hồi (1 buổi)", "Tập vận động cơ xương khớp, phục hồi chức năng theo buổi", new BigDecimal("250000"));
           saveService("Ngâm chân thảo dược & Massage (1 buổi)", "Ngâm chân thảo dược ấm, xoa bóp huyệt đạo giúp ngủ ngon", new BigDecimal("150000"));
           saveService("Chăm sóc vết thương & Thay băng (1 lần)", "Sát khuẩn, thay băng y tế, chăm sóc vùng da tỳ đè", new BigDecimal("120000"));
           saveService("Tắm gội & Vệ sinh tại giường (1 lần)", "Hỗ trợ tắm gội, vệ sinh chuyên biệt cho cư dân yếu/hạn chế vận động", new BigDecimal("150000"));
           saveService("Hộ tống tái khám bệnh viện (1 lượt)", "Điều dưỡng đi kèm theo dõi, hỗ trợ thủ tục khi tái khám ngoại viện", new BigDecimal("500000"));
       }
   }


   private void saveService(String name, String desc, BigDecimal price) {
       com.group6.nhms.entity.Service s = new com.group6.nhms.entity.Service();
       s.setServiceName(name);
       s.setDescription(desc);
       s.setUnitPrice(price);
       serviceRepository.save(s);
   }


   private void initResidents() {
       if (residentRepository.count() == 0) {
           Relative rel = relativeRepository.findAll().stream().findFirst().orElse(null);
           Bed bed = bedRepository.findByStatus(BedStatus.AVAILABLE.name()).stream().findFirst().orElse(null);


           Resident r = new Resident();
           r.setFullName("Nguyễn Văn An");
           r.setGender("Nam");
           r.setDob(LocalDate.of(1945, 6, 15));
           r.setAdmissionDate(LocalDate.now().minusMonths(2));
           r.setBloodType("O+");
           r.setInitialHealthStatus("Tăng huyết áp nhẹ, suy giảm thính lực, minh mẫn.");
           r.setStatus(ResidentStatus.ACTIVE.name());
           r.setRelative(rel);
           if (bed != null) {
               r.setBed(bed);
               bed.setStatus(BedStatus.OCCUPIED.name());
               bedRepository.save(bed);
           }
           residentRepository.save(r);
       }
   }


   private void initSampleSchedules() {
       LocalDate today = LocalDate.now();
       Staff doctor = staffRepository.findByJobTitle("DOCTOR").stream().findFirst().orElse(null);
       Staff receptionist = staffRepository.findByJobTitle("RECEPTIONIST").stream().findFirst().orElse(null);
       Room clinic = roomRepository.findByRoomNumber("PK-01").orElse(null);
       Room reception = roomRepository.findByRoomNumber("LT-01").orElse(null);


       if (doctor != null && clinic != null) {
           if (!taskScheduleRepository.existsByStaffIdAndShiftDateAndShiftType(doctor.getId(), today, "MORNING")) {
               TaskSchedule s1 = new TaskSchedule();
               s1.setStaff(doctor);
               s1.setRoom(clinic);
               s1.setShiftDate(today);
               s1.setShiftType("MORNING");
               s1.setStatus("ASSIGNED");
               s1.setTask("Trực phòng khám, đo sinh hiệu & khám sức khỏe định kỳ cho cư dân");
               taskScheduleRepository.save(s1);
           }
       }


       if (receptionist != null && reception != null) {
           if (!taskScheduleRepository.existsByStaffIdAndShiftDateAndShiftType(receptionist.getId(), today, "MORNING")) {
               TaskSchedule s2 = new TaskSchedule();
               s2.setStaff(receptionist);
               s2.setRoom(reception);
               s2.setShiftDate(today);
               s2.setShiftType("MORNING");
               s2.setStatus("ASSIGNED");
               s2.setTask("Trực quầy sảnh chính, đón tiếp người thân và hướng dẫn check-in/check-out");
               taskScheduleRepository.save(s2);
           }
       }
   }
}

