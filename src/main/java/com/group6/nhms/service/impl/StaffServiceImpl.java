package com.group6.nhms.service.impl;

import com.group6.nhms.dto.response.StaffResponse;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.StaffMapper;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.StaffService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;
    private final AccountRepository accountRepository;

    public StaffServiceImpl(
            StaffRepository staffRepository,
            StaffMapper staffMapper,
            AccountRepository accountRepository
    ) {
        this.staffRepository = staffRepository;
        this.staffMapper = staffMapper;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        return staffRepository.findAll().stream().map(staffMapper::toResponse).toList();
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
            return staffRepository.findAll().stream().findFirst().orElse(null);
        }

        return accountRepository.findByUsername(authentication.getName())
                .filter(a -> a.getReferenceId() != null)
                .flatMap(a -> staffRepository.findById(a.getReferenceId()))
                .orElseGet(() -> staffRepository.findAll().stream().findFirst().orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public Integer resolveCurrentStaffId(Authentication authentication) {
        Staff staff = resolveCurrentStaff(authentication);
        return staff != null ? staff.getId() : 1;
    }
}
