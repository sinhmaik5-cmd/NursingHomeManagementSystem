package com.group6.nhms.repository;

import com.group6.nhms.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Integer> {

    // Tìm kiếm dịch vụ theo tên
    List<Service> findByServiceNameContainingIgnoreCase(String serviceName);

    // Kiểm tra tên dịch vụ đã tồn tại chưa
    boolean existsByServiceName(String serviceName);
}
