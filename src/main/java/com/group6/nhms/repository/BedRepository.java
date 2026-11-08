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

    // Tìm danh sách tất cả các giường theo trạng thái (AVAILABLE, OCCUPIED, MAINTENANCE)
    List<Bed> findByStatus(String status);

    // Tìm danh sách giường thuộc một phòng cụ thể
    List<Bed> findByRoomId(Integer roomId);

    // Tìm giường theo phòng và trạng thái
    List<Bed> findByRoomIdAndStatus(Integer roomId, String status);

    // Đếm số giường trong một phòng (để kiểm tra không vượt quá Capacity)
    long countByRoomId(Integer roomId);

    // Kiểm tra tên giường đã tồn tại trong phòng chưa
    boolean existsByRoomIdAndBedNumber(Integer roomId, String bedNumber);

    // Kiểm tra trùng tên giường khi cập nhật trong cùng một phòng
    boolean existsByRoomIdAndBedNumberAndIdNot(Integer roomId, String bedNumber, Integer id);

    // Xóa các giường thuộc một phòng
    void deleteByRoomId(Integer roomId);

    // Đếm số giường theo trạng thái (phục vụ tính tỷ lệ lấp đầy Occupancy Rate)
    long countByStatus(String status);

    // Khóa bi quan khi thực hiện check-in, chuyển giường, check-out
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bed b where b.id = :id")
    Optional<Bed> findByIdForUpdate(@Param("id") Integer id);
}
