package com.group6.nhms.service;

import com.group6.nhms.dto.request.RelativeRegisterRequest;
import com.group6.nhms.entity.Relative;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface RelativeService {
    List<Relative> getAllRelatives();

    Relative getRelativeById(Integer id);

    Relative resolveCurrentRelative(Authentication authentication);

    Relative registerRelative(RelativeRegisterRequest request);
}
