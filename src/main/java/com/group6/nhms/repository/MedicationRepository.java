package com.group6.nhms.repository;

import com.group6.nhms.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Integer> {
    // Lấy danh sách thuốc sắp hết hạn (cảnh báo tồn kho)
    List<Medication> findByExpiryDateBefore(LocalDate date);

    // Lấy danh sách thuốc sắp hết (số lượng < ngưỡng)
    List<Medication> findByQuantityInStockLessThan(Integer threshold);
}