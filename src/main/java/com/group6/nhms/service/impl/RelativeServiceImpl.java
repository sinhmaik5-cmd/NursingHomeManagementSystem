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
                .filter(account -> RoleEnum.RELATIVE.name().equals(account.getRole()))
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


        // 3. Validation số điện thoại Việt Nam
        String cleanPhone = request.getPhoneNumber() != null ? request.getPhoneNumber().trim().replaceAll("[\\s.-]", "") : "";
        if (!cleanPhone.matches("^(0|\\+84)[35789]\\d{8}$")) {
            throw new BadRequestException("Số điện thoại không hợp lệ! Vui lòng nhập số điện thoại Việt Nam gồm 10 chữ số (bắt đầu bằng 03, 05, 07, 08, 09 hoặc +84).");
        }


        // 4. Validation Email
        String cleanEmail = request.getEmail() != null ? request.getEmail().trim() : "";
        if (cleanEmail.isBlank()) {
            throw new BadRequestException("Email không được để trống. Vui lòng nhập địa chỉ email hợp lệ!");
        }
        if (!cleanEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("Địa chỉ email không đúng định dạng (VD: example@gmail.com)!");
        }
        // 5. Tìm hồ sơ Relative đã tồn tại theo SĐT (trường hợp Lễ tân đã Check-in cư dân trước) hoặc tạo mới
        Relative targetRelative = relativeRepository.findFirstByPhoneNumber(cleanPhone)
                .map(existing -> {
                    if (accountRepository.findByReferenceIdAndRole(existing.getId(), RoleEnum.RELATIVE.name()).isPresent()) {
                        throw new BadRequestException("Số điện thoại '" + cleanPhone + "' đã được liên kết với một tài khoản Người thân khác. Vui lòng đăng nhập hoặc liên hệ Lễ tân để được hỗ trợ!");
                    }
                    existing.setFullName(request.getFullName().trim());
                    existing.setEmail(cleanEmail);
                    if (request.getAddress() != null && !request.getAddress().isBlank()) {
                        existing.setAddress(request.getAddress().trim());
                    }
                    if (request.getRelationship() != null && !request.getRelationship().isBlank()) {
                        existing.setRelationship(request.getRelationship().trim());
                    } else if (existing.getRelationship() == null || existing.getRelationship().isBlank()) {
                        existing.setRelationship("Người thân");
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    Relative newRel = new Relative();
                    newRel.setFullName(request.getFullName().trim());
                    newRel.setPhoneNumber(cleanPhone);
                    newRel.setEmail(cleanEmail);
                    newRel.setAddress(request.getAddress() != null && !request.getAddress().isBlank() ? request.getAddress().trim() : null);
                    newRel.setRelationship(request.getRelationship() != null && !request.getRelationship().isBlank()
                            ? request.getRelationship().trim() : "Người thân");
                    return newRel;
                });
        Relative savedRelative = relativeRepository.save(targetRelative);


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

