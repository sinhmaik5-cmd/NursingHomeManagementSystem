package com.group6.nhms.repository;

import com.group6.nhms.entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedRepository extends JpaRepository<Bed, Integer> {
    // Tìm danh sách tất cả các giường đang trống (Status = 'AVAILABLE')
    List<Bed> findByStatus(String status);

    // Tìm giường trống trong một phòng cụ thể
    List<Bed> findByRoomIdAndStatus(Integer roomId, String status);
}