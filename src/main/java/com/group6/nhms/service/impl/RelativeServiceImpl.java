package com.group6.nhms.service.impl;

import com.group6.nhms.entity.Relative;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.repository.AccountRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.service.RelativeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RelativeServiceImpl implements RelativeService {

    private final RelativeRepository relativeRepository;
    private final AccountRepository accountRepository;

    public RelativeServiceImpl(RelativeRepository relativeRepository, AccountRepository accountRepository) {
        this.relativeRepository = relativeRepository;
        this.accountRepository = accountRepository;
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
            return relativeRepository.findAll().stream().findFirst().orElse(null);
        }

        return accountRepository.findByUsername(authentication.getName())
                .filter(a -> a.getReferenceId() != null)
                .flatMap(a -> relativeRepository.findById(a.getReferenceId()))
                .orElseGet(() -> relativeRepository.findAll().stream().findFirst().orElse(null));
    }
}
