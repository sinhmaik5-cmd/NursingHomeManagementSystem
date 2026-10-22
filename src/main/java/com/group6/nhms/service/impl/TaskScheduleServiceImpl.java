package com.group6.nhms.service.impl;

import com.group6.nhms.dto.request.TaskScheduleCreateRequest;
import com.group6.nhms.dto.response.TaskScheduleResponse;
import com.group6.nhms.entity.Room;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.entity.TaskSchedule;
import com.group6.nhms.enums.ShiftType;
import com.group6.nhms.enums.TaskStatus;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.TaskScheduleMapper;
import com.group6.nhms.repository.RoomRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.repository.TaskScheduleRepository;
import com.group6.nhms.service.TaskScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskScheduleServiceImpl implements TaskScheduleService {

    private final TaskScheduleRepository taskScheduleRepository;
    private final StaffRepository staffRepository;
    private final RoomRepository roomRepository;
    private final TaskScheduleMapper taskScheduleMapper;

    public TaskScheduleServiceImpl(
            TaskScheduleRepository taskScheduleRepository,
            StaffRepository staffRepository,
            RoomRepository roomRepository,
            TaskScheduleMapper taskScheduleMapper
    ) {
        this.taskScheduleRepository = taskScheduleRepository;
        this.staffRepository = staffRepository;
        this.roomRepository = roomRepository;
        this.taskScheduleMapper = taskScheduleMapper;
    }

    @Override
    @Transactional
    public TaskScheduleResponse createSchedule(
            TaskScheduleCreateRequest request,
            Integer roomId
    ) {
        Integer targetRoomId = (roomId != null) ? roomId : request.getRoomId();
        if (targetRoomId == null) {
            throw new BadRequestException("Vui lòng chọn phòng được phân công");
        }

        String shift = normalizeShift(request.getShift());
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nhân viên ID " + request.getStaffId()));
        Room room = roomRepository.findById(targetRoomId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phòng ID " + targetRoomId));

        // Nghiệp vụ: Chặn xếp trùng 2 ca trực khác nhau cho 1 nhân viên trong cùng khung giờ/ngày
        boolean alreadyAssigned = taskScheduleRepository
                .existsByStaffIdAndShiftDateAndShiftType(
                        staff.getId(), request.getWorkDate(), shift);

        if (alreadyAssigned) {
            throw new BadRequestException(
                    "Nhân viên đã được phân công ca " + shift + " trong ngày " + request.getWorkDate());
        }

        TaskSchedule schedule = new TaskSchedule();
        schedule.setStaff(staff);
        schedule.setRoom(room);
        schedule.setShiftDate(request.getWorkDate());
        schedule.setShiftType(shift);
        schedule.setTask(request.getNote());
        schedule.setStatus(TaskStatus.ASSIGNED.name());

        return taskScheduleMapper.toResponse(taskScheduleRepository.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskScheduleResponse> getStaffSchedule(Integer staffId, LocalDate date) {
        return taskScheduleRepository.findByStaffIdAndShiftDate(staffId, date)
                .stream()
                .map(taskScheduleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TaskScheduleResponse completeSchedule(Integer scheduleId, Integer loggedInStaffId) {
        TaskSchedule schedule = taskScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lịch trực ID " + scheduleId));

        if (!schedule.getStaff().getId().equals(loggedInStaffId)) {
            throw new BadRequestException("Bạn chỉ có thể xác nhận lịch trực của chính mình");
        }

        if (!TaskStatus.ASSIGNED.name().equals(schedule.getStatus())) {
            throw new BadRequestException("Chỉ có thể hoàn thành lịch đang ở trạng thái ASSIGNED");
        }

        schedule.setStatus(TaskStatus.COMPLETED.name());
        return taskScheduleMapper.toResponse(taskScheduleRepository.save(schedule));
    }

    private String normalizeShift(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Vui lòng chọn ca trực");
        }

        try {
            return ShiftType.valueOf(value.trim().toUpperCase()).name();
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(
                    "Ca trực không hợp lệ. Giá trị hợp lệ: MORNING, AFTERNOON, NIGHT");
        }
    }
}
