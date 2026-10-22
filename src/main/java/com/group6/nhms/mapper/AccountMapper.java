package com.group6.nhms.mapper;

import com.group6.nhms.dto.request.AccountCreateRequest;
import com.group6.nhms.dto.response.AccountResponse;
import com.group6.nhms.entity.Account;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    private final ModelMapper modelMapper;

    public AccountMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public AccountResponse toResponse(Account entity) {
        if (entity == null) return null;
        AccountResponse response = modelMapper.map(entity, AccountResponse.class);
        response.setRole(entity.getRole());
        return response;
    }

    public Account toEntity(AccountCreateRequest request) {
        if (request == null) return null;
        Account entity = modelMapper.map(request, Account.class);
        entity.setPasswordHash(request.getPassword());
        return entity;
    }
}