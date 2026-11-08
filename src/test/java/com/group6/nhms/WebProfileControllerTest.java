package com.group6.nhms;

import com.group6.nhms.controller.WebProfileController;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Account;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.ResidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebProfileControllerTest {

    private AccountRepository accountRepository;
    private StaffRepository staffRepository;
    private RelativeRepository relativeRepository;
    private ResidentService residentService;
    private PasswordEncoder passwordEncoder;
    private WebProfileController controller;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        staffRepository = mock(StaffRepository.class);
        relativeRepository = mock(RelativeRepository.class);
        residentService = mock(ResidentService.class);
        passwordEncoder = mock(PasswordEncoder.class);

        controller = new WebProfileController(
                accountRepository,
                staffRepository,
                relativeRepository,
                residentService,
                passwordEncoder
        );
    }

    @Test
    void testViewProfileAsStaff() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("doctor1");

        Account account = new Account();
        account.setUsername("doctor1");
        account.setRole("DOCTOR");
        account.setReferenceId(10);

        Staff staff = new Staff();
        staff.setId(10);
        staff.setFullName("BS. Nguyễn Văn Minh");

        when(accountRepository.findByUsername("doctor1")).thenReturn(Optional.of(account));
        when(staffRepository.findById(10)).thenReturn(Optional.of(staff));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.viewProfile(auth, model);

        assertEquals("profile/index", view);
        assertEquals(account, model.get("account"));
        assertEquals(staff, model.get("staff"));
        assertEquals(false, model.get("isRelative"));
    }

    @Test
    void testViewProfileAsRelative() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("relative1");

        Account account = new Account();
        account.setUsername("relative1");
        account.setRole("RELATIVE");
        account.setReferenceId(20);

        Relative relative = new Relative();
        relative.setId(20);
        relative.setFullName("Nguyễn Tuấn Dũng");

        ResidentResponse resident = new ResidentResponse();
        resident.setId(1);
        resident.setFullName("Nguyễn Văn An");

        when(accountRepository.findByUsername("relative1")).thenReturn(Optional.of(account));
        when(relativeRepository.findById(20)).thenReturn(Optional.of(relative));
        when(residentService.getResidentsByRelative(20)).thenReturn(List.of(resident));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.viewProfile(auth, model);

        assertEquals("profile/index", view);
        assertEquals(account, model.get("account"));
        assertEquals(relative, model.get("relative"));
        assertEquals(true, model.get("isRelative"));
        List<?> residents = (List<?>) model.get("sponsoredResidents");
        assertEquals(1, residents.size());
    }

    @Test
    void testUpdateProfileInfoStaff() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("doctor1");

        Account account = new Account();
        account.setUsername("doctor1");
        account.setRole("DOCTOR");
        account.setReferenceId(10);

        Staff staff = new Staff();
        staff.setId(10);
        staff.setFullName("Cũ");

        when(accountRepository.findByUsername("doctor1")).thenReturn(Optional.of(account));
        when(staffRepository.findById(10)).thenReturn(Optional.of(staff));

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String redirect = controller.updateProfileInfo("BS. Mới", "0988776655", "minh@nhmscare.vn", "079080005678", null, "Lão khoa", null, auth, redirectAttributes);

        assertEquals("redirect:/profile", redirect);
        assertEquals("BS. Mới", staff.getFullName());
        assertEquals("0988776655", staff.getPhone());
        assertEquals("minh@nhmscare.vn", staff.getEmail());
        assertEquals("079080005678", staff.getCitizenId());
        assertEquals("Lão khoa", staff.getSpecialization());
        verify(staffRepository).save(staff);
    }

    @Test
    void testUpdateProfileInfoRelative() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("relative1");

        Account account = new Account();
        account.setUsername("relative1");
        account.setRole("RELATIVE");
        account.setReferenceId(20);

        Relative relative = new Relative();
        relative.setId(20);
        relative.setFullName("Cũ");

        when(accountRepository.findByUsername("relative1")).thenReturn(Optional.of(account));
        when(relativeRepository.findById(20)).thenReturn(Optional.of(relative));

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String redirect = controller.updateProfileInfo("Người Thân Mới", "0911223344", "test@email.com", null, "Hà Nội", null, "Con trai", auth, redirectAttributes);

        assertEquals("redirect:/profile", redirect);
        assertEquals("Người Thân Mới", relative.getFullName());
        assertEquals("0911223344", relative.getPhoneNumber());
        assertEquals("test@email.com", relative.getEmail());
        assertEquals("Hà Nội", relative.getAddress());
        assertEquals("Con trai", relative.getRelationship());
        verify(relativeRepository).save(relative);
    }

    @Test
    void testChangePasswordSuccess() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user1");

        Account account = new Account();
        account.setUsername("user1");
        account.setPasswordHash("encodedOld");

        when(accountRepository.findByUsername("user1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("oldPass123", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedNew");

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String redirect = controller.changePassword("oldPass123", "newPass123", "newPass123", auth, redirectAttributes);

        assertEquals("redirect:/profile", redirect);
        assertEquals("encodedNew", account.getPasswordHash());
        verify(accountRepository).save(account);
        assertNotNull(redirectAttributes.getFlashAttributes().get("successMessage"));
    }

    @Test
    void testChangePasswordWrongCurrent() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user1");

        Account account = new Account();
        account.setUsername("user1");
        account.setPasswordHash("encodedOld");

        when(accountRepository.findByUsername("user1")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrongPass", "encodedOld")).thenReturn(false);

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String redirect = controller.changePassword("wrongPass", "newPass123", "newPass123", auth, redirectAttributes);

        assertEquals("redirect:/profile", redirect);
        verify(accountRepository, never()).save(any());
        assertNotNull(redirectAttributes.getFlashAttributes().get("errorMessage"));
    }
}
