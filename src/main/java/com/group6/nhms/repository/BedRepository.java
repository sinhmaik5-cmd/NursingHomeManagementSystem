package com.group6.nhms.repository;

import com.group6.nhms.entity.Bed;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedRepository extends JpaRepository<Bed, Integer> {
    // Tìm danh sách tất cả các giường đang trống (Status = 'AVAILABLE')
    List<Bed> findByStatus(String status);

    // Tìm giường trống trong một phòng cụ thể
    List<Bed> findByRoomIdAndStatus(Integer roomId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bed b where b.id = :id")
    Optional<Bed> findByIdForUpdate(@Param("id") Integer id);
}