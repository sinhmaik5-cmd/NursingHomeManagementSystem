package com.group6.nhms.repository;

import com.group6.nhms.entity.Medication;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Integer> {

    // Danh sách thuốc sắp hết hạn (ExpiryDate <= ngày chỉ định)
    List<Medication> findByExpiryDateBefore(LocalDate date);

    // Danh sách thuốc đã hết hạn (ExpiryDate <= hôm nay)
    List<Medication> findByExpiryDateLessThanEqual(LocalDate date);

    // Danh sách thuốc sắp hết hạn trong khoảng (ví dụ từ hôm nay đến 30 ngày tới)
    List<Medication> findByExpiryDateBetween(LocalDate startDate, LocalDate endDate);

    // Danh sách thuốc sắp hết hàng (tồn kho < ngưỡng cảnh báo, vd 10)
    List<Medication> findByQuantityInStockLessThan(Integer threshold);

    // Tìm kiếm thuốc theo tên
    List<Medication> findByMedNameContainingIgnoreCase(String medName);

    // Tìm các thuốc còn hạn sử dụng và còn tồn kho (phục vụ danh sách chọn kê đơn cho Bác sĩ)
    @Query("SELECT m FROM Medication m WHERE (m.expiryDate IS NULL OR m.expiryDate > :currentDate) AND m.quantityInStock > 0")
    List<Medication> findAvailableForPrescription(@Param("currentDate") LocalDate currentDate);

    // Khóa bi quan khi trừ kho thuốc
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medication m where m.id = :id")
    Optional<Medication> findByIdForUpdate(@Param("id") Integer id);
}
