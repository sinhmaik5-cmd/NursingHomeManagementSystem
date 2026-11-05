package com.group6.nhms.repository;

import com.group6.nhms.entity.Resident;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Integer> {

    // Tìm các cư dân do 1 người thân bảo lãnh (dùng cho Relative Portal)
    List<Resident> findByRelativeId(Integer relativeId);

    List<Resident> findByRelativeIdAndIdIn(Integer relativeId, Collection<Integer> ids);

    // Lấy danh sách cư dân theo trạng thái (ACTIVE, DISCHARGED, DECEASED)
    List<Resident> findByStatus(String status);

    // Đếm số lượng cư dân theo trạng thái (Dashboard thống kê)
    long countByStatus(String status);

    // Tìm kiếm cư dân theo họ tên
    List<Resident> findByFullNameContainingIgnoreCase(String fullName);

    // Tìm cư dân theo giường đang nằm
    Optional<Resident> findByBedId(Integer bedId);

    // Khóa bi quan khi tiếp nhận, đổi phòng, xuất viện
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Resident r where r.id = :id")
    Optional<Resident> findByIdForUpdate(@Param("id") Integer id);
}
