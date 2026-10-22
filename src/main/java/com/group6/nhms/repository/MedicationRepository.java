package com.group6.nhms.repository;

import com.group6.nhms.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Integer> {
    // Lấy danh sách thuốc sắp hết hạn (cảnh báo tồn kho)
    List<Medication> findByExpiryDateBefore(LocalDate date);

    // Lấy danh sách thuốc sắp hết (số lượng < ngưỡng)
    List<Medication> findByQuantityInStockLessThan(Integer threshold);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medication m where m.id = :id")
    Optional<Medication> findByIdForUpdate(@Param("id") Integer id);
}