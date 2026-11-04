package com.group6.nhms.service.impl;

import com.group6.nhms.dto.request.RelativeRegisterRequest;
import com.group6.nhms.entity.Account;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.enums.RoleEnum;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.service.RelativeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RelativeServiceImpl implements RelativeService {

    private final RelativeRepository relativeRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public RelativeServiceImpl(
            RelativeRepository relativeRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.relativeRepository = relativeRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Relative> getAllRelatives() {
        return relativeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Relative getRelativeById(Integer id) {
        return relativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người thân ID " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Relative resolveCurrentRelative(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        return accountRepository.findByUsername(authentication.getName())
                .filter(a -> a.getReferenceId() != null)
                .flatMap(a -> relativeRepository.findById(a.getReferenceId()))
                .orElse(null);
    }

    @Override
    @Transactional
    public Relative registerRelative(RelativeRegisterRequest request) {
        // 1. Kiểm tra mật khẩu xác nhận
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp. Vui lòng nhập lại!");
        }

        // 2. Kiểm tra tên đăng nhập đã tồn tại chưa
        String cleanUsername = request.getUsername().trim();
        if (accountRepository.existsByUsername(cleanUsername)) {
            throw new BadRequestException("Tên đăng nhập '" + cleanUsername + "' đã được sử dụng. Vui lòng chọn tên khác!");
        }

        // 3. Khởi tạo và lưu hồ sơ Relative
        Relative relative = new Relative();
        relative.setFullName(request.getFullName().trim());
        relative.setPhoneNumber(request.getPhoneNumber().trim());
        relative.setEmail(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null);
        relative.setAddress(request.getAddress() != null && !request.getAddress().isBlank() ? request.getAddress().trim() : null);
        relative.setRelationship(request.getRelationship() != null && !request.getRelationship().isBlank()
                ? request.getRelationship().trim() : "Người thân");
        Relative savedRelative = relativeRepository.save(relative);

        // 4. Khởi tạo tài khoản Account với quyền duy nhất là RELATIVE
        Account account = new Account();
        account.setUsername(cleanUsername);
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setRole(RoleEnum.RELATIVE.name());
        account.setReferenceId(savedRelative.getId());
        account.setIsActive(true);
        accountRepository.save(account);

        return savedRelative;
    }
}
