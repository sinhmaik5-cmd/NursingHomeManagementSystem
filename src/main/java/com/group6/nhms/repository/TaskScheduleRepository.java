package com.group6.nhms.repository;

import com.group6.nhms.entity.TaskSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskScheduleRepository extends JpaRepository<TaskSchedule, Integer> {

    // Kiểm tra chống trùng lịch: 1 nhân viên đã có ca trực loại này trong ngày này chưa?
    boolean existsByStaffIdAndShiftDateAndShiftType(Integer staffId, LocalDate shiftDate, String shiftType);

    // Lấy danh sách công việc trong ngày của 1 nhân viên (để họ vào xem và xác nhận)
    List<TaskSchedule> findByStaffIdAndShiftDate(Integer staffId, LocalDate shiftDate);

    // Lấy lịch làm việc của tất cả nhân viên theo ngày
    List<TaskSchedule> findByShiftDate(LocalDate shiftDate);

    // Lấy lịch làm việc của 1 phòng theo ngày
    List<TaskSchedule> findByRoomIdAndShiftDate(Integer roomId, LocalDate shiftDate);

    // Lấy toàn bộ lịch phân công của 1 nhân viên
    List<TaskSchedule> findByStaffId(Integer staffId);

    // Xóa lịch làm việc theo phòng khi phòng bị xóa
    void deleteByRoomId(Integer roomId);
}
