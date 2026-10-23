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
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            initStaffAndAccounts();
            initRoomsAndBeds();
            initMedications();
            initServices();
            initResidents();
            log.info("Data initialization check completed successfully.");
        } catch (Exception e) {
            log.warn("Data initialization encountered a non-fatal error: {}", e.getMessage());
        }
    }

    private void initStaffAndAccounts() {
        // Staff 1: Manager
        Staff managerStaff = staffRepository.findByJobTitle("MANAGER").stream().findFirst().orElseGet(() -> {
            Staff s = new Staff();
            s.setFullName("Trần Quang Huy");
            s.setJobTitle("MANAGER");
            s.setPhone("0912345678");
            s.setSpecialization("Quản lý viện");
            return staffRepository.save(s);
        });

        // Staff 2: Doctor
        Staff doctorStaff = staffRepository.findByJobTitle("DOCTOR").stream().findFirst().orElseGet(() -> {
            Staff s = new Staff();
            s.setFullName("BS. Nguyễn Văn Minh");
            s.setJobTitle("DOCTOR");
            s.setPhone("0901234567");
            s.setSpecialization("Nội tổng quát & Lão khoa");
            return staffRepository.save(s);
        });

        // Staff 3: Nurse
        Staff nurseStaff = staffRepository.findByJobTitle("NURSE").stream().findFirst().orElseGet(() -> {
            Staff s = new Staff();
            s.setFullName("ĐD. Lê Thị Hoa");
            s.setJobTitle("NURSE");
            s.setPhone("0907654321");
            s.setSpecialization("Chăm sóc phục hồi");
            return staffRepository.save(s);
        });

        // Staff 4: Accountant
        Staff accountantStaff = staffRepository.findByJobTitle("ACCOUNTANT").stream().findFirst().orElseGet(() -> {
            Staff s = new Staff();
            s.setFullName("KT. Phạm Thanh Thảo");
            s.setJobTitle("ACCOUNTANT");
            s.setPhone("0987654321");
            s.setSpecialization("Kế toán tài chính");
            return staffRepository.save(s);
        });

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

        // Create Accounts if not exists
        createAccountIfNotExists("admin", "123456", RoleEnum.ADMIN.name(), null);
        createAccountIfNotExists("manager", "123456", RoleEnum.MANAGER.name(), managerStaff.getId());
        createAccountIfNotExists("doctor", "123456", RoleEnum.DOCTOR.name(), doctorStaff.getId());
        createAccountIfNotExists("nurse", "123456", RoleEnum.NURSE.name(), nurseStaff.getId());
        createAccountIfNotExists("accountant", "123456", RoleEnum.ACCOUNTANT.name(), accountantStaff.getId());
        createAccountIfNotExists("relative", "123456", RoleEnum.RELATIVE.name(), relative.getId());
    }

    private void createAccountIfNotExists(String username, String rawPassword, String role, Integer refId) {
        if (!accountRepository.existsByUsername(username)) {
            Account account = new Account();
            account.setUsername(username);
            account.setPasswordHash(passwordEncoder.encode(rawPassword));
            account.setRole(role);
            account.setReferenceId(refId);
            account.setIsActive(true);
            accountRepository.save(account);
        }
    }

    private void initRoomsAndBeds() {
        if (roomRepository.count() == 0) {
            Room r101 = new Room();
            r101.setRoomNumber("P101");
            r101.setRoomType("STANDARD");
            r101.setCapacity(2);
            r101.setBasePrice(new BigDecimal("5000000"));
            r101.setStatus("AVAILABLE");
            r101 = roomRepository.save(r101);

            Room r102 = new Room();
            r102.setRoomNumber("P102");
            r102.setRoomType("STANDARD");
            r102.setCapacity(2);
            r102.setBasePrice(new BigDecimal("5000000"));
            r102.setStatus("AVAILABLE");
            r102 = roomRepository.save(r102);

            Room r201 = new Room();
            r201.setRoomNumber("P201");
            r201.setRoomType("VIP");
            r201.setCapacity(1);
            r201.setBasePrice(new BigDecimal("8000000"));
            r201.setStatus("AVAILABLE");
            r201 = roomRepository.save(r201);

            Room r202 = new Room();
            r202.setRoomNumber("P202");
            r202.setRoomType("VIP");
            r202.setCapacity(1);
            r202.setBasePrice(new BigDecimal("8000000"));
            r202.setStatus("AVAILABLE");
            r202 = roomRepository.save(r202);

            // Beds
            createBed(r101, "G101-1", BedStatus.AVAILABLE.name());
            createBed(r101, "G101-2", BedStatus.AVAILABLE.name());
            createBed(r102, "G102-1", BedStatus.AVAILABLE.name());
            createBed(r102, "G102-2", BedStatus.MAINTENANCE.name());
            createBed(r201, "G201-1", BedStatus.AVAILABLE.name());
            createBed(r202, "G202-1", BedStatus.MAINTENANCE.name());
        }
    }

    private void createBed(Room room, String bedNumber, String status) {
        Bed bed = new Bed();
        bed.setRoom(room);
        bed.setBedNumber(bedNumber);
        bed.setStatus(status);
        bedRepository.save(bed);
    }

    private void initMedications() {
        if (medicationRepository.count() == 0) {
            saveMed("Amlodipine 5mg", 120, LocalDate.now().plusMonths(18), "Dược Hậu Giang", new BigDecimal("3500"));
            saveMed("Metformin 500mg", 90, LocalDate.now().plusMonths(12), "Domesco", new BigDecimal("2800"));
            saveMed("Panadol Extra", 250, LocalDate.now().plusMonths(24), "GSK", new BigDecimal("2000"));
            saveMed("Berberin 100mg", 80, LocalDate.now().plusMonths(10), "OPC", new BigDecimal("1500"));
            saveMed("Glucosamine 500mg", 60, LocalDate.now().plusMonths(15), "Nature Bounty", new BigDecimal("6500"));
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
            saveService("Vật lý trị liệu chuyên sâu", "Bài tập phục hồi chức năng vận động cơ xương khớp", new BigDecimal("1500000"));
            saveService("Chế độ dinh dưỡng đặc biệt", "Suất ăn chia theo bệnh lý tiểu đường, tim mạch, xay nhuyễn", new BigDecimal("2000000"));
            saveService("Ngâm chân thảo dược & Massage", "Thư giãn huyệt đạo, giúp ngủ ngon và lưu thông khí huyết", new BigDecimal("800000"));
            saveService("Chăm sóc y tế 24/7", "Theo dõi sinh hiệu, hỗ trợ vệ sinh và vận động hàng ngày", new BigDecimal("3000000"));
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
}
