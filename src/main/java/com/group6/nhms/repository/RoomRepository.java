package com.group6.nhms.repository;

import com.group6.nhms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    // Tìm phòng theo số phòng
    Optional<Room> findByRoomNumber(String roomNumber);

    // Kiểm tra số phòng đã tồn tại chưa (khi tạo phòng mới)
    boolean existsByRoomNumber(String roomNumber);

    // Kiểm tra trùng số phòng khi cập nhật
    boolean existsByRoomNumberAndIdNot(String roomNumber, Integer id);

    // Lọc phòng theo trạng thái (AVAILABLE, OCCUPIED, MAINTENANCE)
    List<Room> findByStatus(String status);

    // Lọc phòng theo loại phòng (VIP, STANDARD,...)
    List<Room> findByRoomType(String roomType);
}
