package com.group6.nhms.service;

import com.group6.nhms.dto.request.RelativeRegisterRequest;
import com.group6.nhms.entity.Account;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.enums.RoleEnum;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.service.impl.RelativeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelativeServiceImplTest {

    @Mock
    private RelativeRepository relativeRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private RelativeServiceImpl relativeService;

    @BeforeEach
    void setUp() {
        relativeService = new RelativeServiceImpl(relativeRepository, accountRepository, passwordEncoder);
    }

    @Test
    void registerRelative_reusesExistingRelativeRecordMatchedByPhoneNumber() {
        Relative existingFromCheckIn = new Relative();
        existingFromCheckIn.setId(42);
        existingFromCheckIn.setFullName("Trần Thị Hoa");
        existingFromCheckIn.setPhoneNumber("0912345678");
        existingFromCheckIn.setRelationship("Con gái");

        RelativeRegisterRequest request = validRequest("thantrang", "0912345678", "hoa.tran@example.com");

        when(accountRepository.existsByUsername("thantrang")).thenReturn(false);
        when(relativeRepository.findFirstByPhoneNumber("0912345678")).thenReturn(Optional.of(existingFromCheckIn));
        when(accountRepository.findByReferenceIdAndRole(42, RoleEnum.RELATIVE.name())).thenReturn(Optional.empty());
        when(relativeRepository.save(any(Relative.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("Secret123!")).thenReturn("encoded");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Relative saved = relativeService.registerRelative(request);

        assertEquals(42, saved.getId());
        assertEquals("hoa.tran@example.com", saved.getEmail());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertEquals(42, accountCaptor.getValue().getReferenceId());
        assertEquals(RoleEnum.RELATIVE.name(), accountCaptor.getValue().getRole());
    }

    @Test
    void registerRelative_rejectsWhenPhoneNumberAlreadyLinkedToRelativeAccount() {
        Relative existing = new Relative();
        existing.setId(42);
        existing.setPhoneNumber("0912345678");

        Account existingAccount = new Account();
        existingAccount.setId(7);
        existingAccount.setReferenceId(42);
        existingAccount.setRole(RoleEnum.RELATIVE.name());

        RelativeRegisterRequest request = validRequest("nguoidungmoi", "0912345678", "new@example.com");

        when(accountRepository.existsByUsername("nguoidungmoi")).thenReturn(false);
        when(relativeRepository.findFirstByPhoneNumber("0912345678")).thenReturn(Optional.of(existing));
        when(accountRepository.findByReferenceIdAndRole(42, RoleEnum.RELATIVE.name()))
                .thenReturn(Optional.of(existingAccount));

        assertThrows(BadRequestException.class, () -> relativeService.registerRelative(request));
    }

    private RelativeRegisterRequest validRequest(String username, String phone, String email) {
        RelativeRegisterRequest req = new RelativeRegisterRequest();
        req.setUsername(username);
        req.setPassword("Secret123!");
        req.setConfirmPassword("Secret123!");
        req.setFullName("Trần Thị Hoa");
        req.setPhoneNumber(phone);
        req.setEmail(email);
        req.setAddress("Hà Nội");
        req.setRelationship("Con gái");
        return req;
    }
}
