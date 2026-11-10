package com.group6.nhms.service.impl;


import com.group6.nhms.dto.request.StaffAccountCreateRequest;
import com.group6.nhms.dto.response.StaffResponse;
import com.group6.nhms.entity.Account;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.RoleEnum;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.StaffMapper;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.StaffService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;


@Service
public class StaffServiceImpl implements StaffService {


    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;


    public StaffServiceImpl(
            StaffRepository staffRepository,
            StaffMapper staffMapper,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.staffRepository = staffRepository;
        this.staffMapper = staffMapper;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        return staffRepository.findAll().stream().map(staff -> {
            StaffResponse res = staffMapper.toResponse(staff);
            List<Account> accounts = accountRepository.findByReferenceId(staff.getId()).stream()
                    .filter(acc -> !RoleEnum.RELATIVE.name().equalsIgnoreCase(acc.getRole()))
                    .toList();
            if (!accounts.isEmpty()) {
                Account acc = accounts.stream()
                        .filter(a -> staff.getJobTitle() != null && staff.getJobTitle().equalsIgnoreCase(a.getRole()))
                        .findFirst()
                        .orElse(accounts.get(0));
                res.setUsername(acc.getUsername());
                res.setIsActive(acc.getIsActive());
                res.setAccountId(acc.getId());
            }
            return res;
        }).toList();
    }


    @Override
    @Transactional(readOnly = true)
    public Staff getStaffById(Integer id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên ID " + id));
    }


    @Override
    @Transactional(readOnly = true)
    public Staff resolveCurrentStaff(Authentication authentication) {
        if (authentication == null) {
            return null;
        }


        return accountRepository.findByUsername(authentication.getName())
                .filter(a -> !RoleEnum.RELATIVE.name().equalsIgnoreCase(a.getRole())
                        && !"ROLE_RELATIVE".equalsIgnoreCase(a.getRole()))
                .filter(a -> a.getReferenceId() != null)
                .flatMap(a -> staffRepository.findById(a.getReferenceId()))
                .orElse(null);
    }


    @Override
    @Transactional(readOnly = true)
    public Integer resolveCurrentStaffId(Authentication authentication) {
        Staff staff = resolveCurrentStaff(authentication);
        return staff != null ? staff.getId() : null;
    }


    @Override
    @Transactional
    public Staff createStaffWithAccount(StaffAccountCreateRequest request) {
        // 1. Kiểm tra mật khẩu khớp nhau
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp!");
        }


        // 2. Kiểm tra tên đăng nhập đã tồn tại
        String cleanUsername = request.getUsername().trim();
        if (accountRepository.existsByUsername(cleanUsername)) {
            throw new BadRequestException("Tên đăng nhập '" + cleanUsername + "' đã tồn tại trong hệ thống!");
        }


        // 3. Kiểm tra tính hợp lệ của vai trò nhân sự
        RoleEnum roleEnum;
        try {
            roleEnum = RoleEnum.valueOf(request.getJobTitle().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Vai trò '" + request.getJobTitle() + "' không hợp lệ!");
        }


        if (roleEnum == RoleEnum.RELATIVE) {
            throw new BadRequestException("Không thể cấp vai trò Người thân (RELATIVE) cho nhân viên nội bộ!");
        }


        // 4. Khởi tạo và lưu hồ sơ Staff
        Staff staff = new Staff();
        staff.setFullName(request.getFullName().trim());
        staff.setPhone(request.getPhone().trim());
        staff.setEmail(request.getEmail() != null && !request.getEmail().isBlank()
                ? request.getEmail().trim() : null);
        staff.setCitizenId(request.getCitizenId() != null && !request.getCitizenId().isBlank()
                ? request.getCitizenId().trim() : null);
        staff.setJobTitle(roleEnum.name());
        staff.setSpecialization(request.getSpecialization() != null && !request.getSpecialization().isBlank()
                ? request.getSpecialization().trim() : null);
        Staff savedStaff = staffRepository.save(staff);


        // 5. Khởi tạo tài khoản Account tương ứng cho nhân viên
        Account account = new Account();
        account.setUsername(cleanUsername);
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setRole(roleEnum.name());
        account.setReferenceId(savedStaff.getId());
        account.setIsActive(true);
        accountRepository.save(account);


        return savedStaff;
    }


    @Override
    @Transactional
    public void toggleStaffAccountStatus(Integer staffId, Authentication currentAuth) {
        List<Account> accounts = accountRepository.findByReferenceId(staffId).stream()
                .filter(acc -> !RoleEnum.RELATIVE.name().equalsIgnoreCase(acc.getRole())
                        && !"ROLE_RELATIVE".equalsIgnoreCase(acc.getRole()))
                .toList();
        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy tài khoản gắn liền với nhân viên ID " + staffId);
        }


        for (Account account : accounts) {
            // Ràng buộc bảo mật: Không được tự khóa tài khoản của chính mình
            if (currentAuth != null && account.getUsername().equalsIgnoreCase(currentAuth.getName())) {
                throw new BadRequestException("Bạn không thể tự khóa tài khoản đang đăng nhập của chính mình!");
            }
            account.setIsActive(!Boolean.TRUE.equals(account.getIsActive()));
            accountRepository.save(account);
        }
    }
}

