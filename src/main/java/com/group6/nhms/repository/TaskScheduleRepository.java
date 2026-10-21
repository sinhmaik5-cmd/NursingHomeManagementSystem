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

    // Lấy danh sách công việc trong ngày của 1 nhân viên (để họ vào xác nhận)
    List<TaskSchedule> findByStaffIdAndShiftDate(Integer staffId, LocalDate shiftDate);
}