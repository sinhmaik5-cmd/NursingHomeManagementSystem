package com.group6.nhms;

import com.group6.nhms.dto.request.RelativeRegisterRequest;
import com.group6.nhms.dto.request.StaffAccountCreateRequest;
import com.group6.nhms.dto.response.StaffResponse;
import com.group6.nhms.entity.Account;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.RoleEnum;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.service.RelativeService;
import com.group6.nhms.service.StaffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NursingHomeManagementSystemApplicationTests {

    @Autowired
    private RelativeService relativeService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void testRegisterRelativeSuccess() {
        String uniqueUser = "testrel_" + System.currentTimeMillis();

        RelativeRegisterRequest request = new RelativeRegisterRequest();
        request.setFullName("Phạm Thị Mai");
        request.setPhoneNumber("0988776655");
        request.setEmail("mai.pham@test.com");
        request.setAddress("Hà Nội");
        request.setRelationship("Con gái");
        request.setUsername(uniqueUser);
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        Relative saved = relativeService.registerRelative(request);

        assertNotNull(saved.getId(), "Relative ID must not be null after saving");
        assertEquals("Phạm Thị Mai", saved.getFullName());

        Account account = accountRepository.findByUsername(uniqueUser).orElse(null);
        assertNotNull(account, "Account must be created in database");
        assertEquals(RoleEnum.RELATIVE.name(), account.getRole(), "Role must be RELATIVE");
        assertEquals(saved.getId(), account.getReferenceId(), "Reference ID must match Relative ID");
        assertTrue(account.getIsActive(), "Account must be active");
        assertTrue(passwordEncoder.matches("password123", account.getPasswordHash()), "Password must be BCrypt encoded");
    }

    @Test
    @Transactional
    void testRegisterRelativePasswordMismatchThrows() {
        RelativeRegisterRequest request = new RelativeRegisterRequest();
        request.setFullName("Phạm Thị Mai");
        request.setPhoneNumber("0988776655");
        request.setEmail("mai.pham@test.com");
        request.setUsername("testuser_mismatch");
        request.setPassword("password123");
        request.setConfirmPassword("different123");

        assertThrows(BadRequestException.class, () -> relativeService.registerRelative(request));
    }

    @Test
    @Transactional
    void testRegisterRelativeInvalidVietnamPhoneThrows() {
        RelativeRegisterRequest request = new RelativeRegisterRequest();
        request.setFullName("Trần Văn Test");
        request.setPhoneNumber("0123456789");
        request.setEmail("test@gmail.com");
        request.setUsername("testuser_invalid_phone");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        assertThrows(BadRequestException.class, () -> relativeService.registerRelative(request));
    }

    @Test
    @Transactional
    void testRegisterRelativeInvalidEmailThrows() {
        RelativeRegisterRequest request = new RelativeRegisterRequest();
        request.setFullName("Trần Văn Test");
        request.setPhoneNumber("0912345678");
        request.setEmail("not-an-email");
        request.setUsername("testuser_invalid_email");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        assertThrows(BadRequestException.class, () -> relativeService.registerRelative(request));
    }

    @Test
    @Transactional
    void testRegisterRelativeInternationalVNPhoneSucceeds() {
        String uniqueUser = "rel_plus84_" + System.currentTimeMillis();
        RelativeRegisterRequest request = new RelativeRegisterRequest();
        request.setFullName("Lê Thị Hương");
        request.setPhoneNumber("+84912345678");
        request.setEmail("huong.le@example.com");
        request.setUsername(uniqueUser);
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        Relative saved = relativeService.registerRelative(request);
        assertNotNull(saved.getId());
        assertEquals("+84912345678", saved.getPhoneNumber());
        assertEquals("huong.le@example.com", saved.getEmail());
    }

    @Test
    @Transactional
    void testCreateStaffWithAccountSuccess() {
        String uniqueDoctor = "doctor_" + System.currentTimeMillis();

        StaffAccountCreateRequest request = new StaffAccountCreateRequest();
        request.setFullName("BS. Vũ Quốc Thái");
        request.setPhone("0919998888");
        request.setJobTitle("DOCTOR");
        request.setSpecialization("Tim mạch lão khoa");
        request.setUsername(uniqueDoctor);
        request.setPassword("docpass123");
        request.setConfirmPassword("docpass123");

        Staff createdStaff = staffService.createStaffWithAccount(request);

        assertNotNull(createdStaff.getId(), "Staff ID must not be null");
        assertEquals("BS. Vũ Quốc Thái", createdStaff.getFullName());
        assertEquals("DOCTOR", createdStaff.getJobTitle());

        Account account = accountRepository.findByUsername(uniqueDoctor).orElse(null);
        assertNotNull(account, "Account for staff must exist");
        assertEquals("DOCTOR", account.getRole());
        assertEquals(createdStaff.getId(), account.getReferenceId());
        assertTrue(account.getIsActive());

        // Test getAllStaff has enriched account information
        List<StaffResponse> staffList = staffService.getAllStaff();
        StaffResponse found = staffList.stream().filter(s -> s.getId().equals(createdStaff.getId())).findFirst().orElse(null);
        assertNotNull(found);
        assertEquals(uniqueDoctor, found.getUsername());
        assertTrue(found.getIsActive());
    }

    @Test
    @Transactional
    void testToggleStaffAccountStatusAndPreventSelfLock() {
        String uniqueNurse = "nurse_" + System.currentTimeMillis();

        StaffAccountCreateRequest request = new StaffAccountCreateRequest();
        request.setFullName("ĐD. Trần Thùy Linh");
        request.setPhone("0903334444");
        request.setJobTitle("NURSE");
        request.setUsername(uniqueNurse);
        request.setPassword("nursepass123");
        request.setConfirmPassword("nursepass123");

        Staff nurse = staffService.createStaffWithAccount(request);

        // Center Manager auth
        Authentication managerAuth = new UsernamePasswordAuthenticationToken("manager", "n/a", Collections.emptyList());

        // Lock nurse account
        staffService.toggleStaffAccountStatus(nurse.getId(), managerAuth);
        Account lockedAccount = accountRepository.findByUsername(uniqueNurse).orElse(null);
        assertNotNull(lockedAccount);
        assertFalse(lockedAccount.getIsActive(), "Account should now be locked (isActive = false)");

        // Unlock nurse account
        staffService.toggleStaffAccountStatus(nurse.getId(), managerAuth);
        Account unlockedAccount = accountRepository.findByUsername(uniqueNurse).orElse(null);
        assertNotNull(unlockedAccount);
        assertTrue(unlockedAccount.getIsActive(), "Account should now be active (isActive = true)");

        // Test preventing self-lock
        Authentication nurseSelfAuth = new UsernamePasswordAuthenticationToken(uniqueNurse, "n/a", Collections.emptyList());
        assertThrows(BadRequestException.class, () -> staffService.toggleStaffAccountStatus(nurse.getId(), nurseSelfAuth));
    }

    @Test
    @Transactional
    void testCreateStaffInvalidRoleThrows() {
        StaffAccountCreateRequest request = new StaffAccountCreateRequest();
        request.setFullName("Nguyen Van Test");
        request.setPhone("0901112222");
        request.setJobTitle("INVALID_ROLE");
        request.setUsername("test_invalid_role");
        request.setPassword("123456");
        request.setConfirmPassword("123456");

        assertThrows(BadRequestException.class, () -> staffService.createStaffWithAccount(request));
    }

    @Test
    @Transactional
    void testCreateStaffRelativeRoleThrows() {
        StaffAccountCreateRequest request = new StaffAccountCreateRequest();
        request.setFullName("Nguyen Van Test");
        request.setPhone("0901112222");
        request.setJobTitle("RELATIVE");
        request.setUsername("test_relative_staff");
        request.setPassword("123456");
        request.setConfirmPassword("123456");

        assertThrows(BadRequestException.class, () -> staffService.createStaffWithAccount(request));
    }
}
