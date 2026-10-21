package com.group6.nhms.repository;

import com.group6.nhms.entity.Relative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelativeRepository extends JpaRepository<Relative, Integer> {
}