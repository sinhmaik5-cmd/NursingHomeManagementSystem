package com.group6.nhms.service;

import com.group6.nhms.dto.request.StaffAccountCreateRequest;
import com.group6.nhms.dto.response.StaffResponse;
import com.group6.nhms.entity.Staff;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface StaffService {
    List<StaffResponse> getAllStaff();

    Staff getStaffById(Integer id);

    Staff resolveCurrentStaff(Authentication authentication);

    Integer resolveCurrentStaffId(Authentication authentication);

    Staff createStaffWithAccount(StaffAccountCreateRequest request);

    void toggleStaffAccountStatus(Integer staffId, Authentication currentAuth);

    void resetStaffPassword(Integer staffId, String newPassword);
}
