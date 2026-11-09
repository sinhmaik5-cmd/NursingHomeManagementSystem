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
    public TaskScheduleResponse createSchedule(TaskScheduleCreateRequest request, Integer roomId) {
        if (request.getWorkDate() == null || request.getWorkDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Không thể phân công ca trực trong quá khứ! Ngày phân công phải từ hôm nay (" + LocalDate.now() + ") trở đi.");
        }


        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nhân viên ID " + request.getStaffId()));


        if ("CENTER_MANAGER".equalsIgnoreCase(staff.getJobTitle())) {
            throw new BadRequestException("Không thể phân công ca trực cho Giám đốc cơ sở (CENTER_MANAGER)!");
        }


        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phòng ID " + roomId));


        String jobTitle = staff.getJobTitle() != null ? staff.getJobTitle().trim().toUpperCase() : "";
        String roomType = room.getRoomType() != null ? room.getRoomType().trim().toUpperCase() : "";


        if ("DOCTOR".equals(jobTitle)) {
            if (!"CLINIC".equals(roomType)) {
                throw new BadRequestException("Bác sĩ chỉ có thể được phân công trực tại Phòng Khám / Y tế (CLINIC), không thể gán vào phòng cư dân hoặc quầy lễ tân!");
            }
        } else if ("RECEPTIONIST".equals(jobTitle)) {
            if (!"RECEPTION".equals(roomType)) {
                throw new BadRequestException("Lễ tân chỉ có thể được phân công trực tại Quầy Lễ tân (RECEPTION), không thể gán vào phòng khám hoặc phòng lưu trú!");
            }
        } else if ("NURSE".equals(jobTitle)) {
            if ("CLINIC".equals(roomType) || "RECEPTION".equals(roomType)) {
                throw new BadRequestException("Điều dưỡng chỉ được phân công phụ trách Phòng Lưu trú của Cư dân (STANDARD, VIP, CARE_SPECIAL), không phụ trách phòng khám hay quầy lễ tân!");
            }
        }


        String shift = normalizeShift(request.getShift());


        boolean exists = taskScheduleRepository.existsByStaffIdAndShiftDateAndShiftType(
                request.getStaffId(),
                request.getWorkDate(),
                shift
        );


        if (exists) {
            throw new BadRequestException("Nhân viên này đã được phân công ca " + shift + " vào ngày " + request.getWorkDate());
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


    @Override
    @Transactional(readOnly = true)
    public List<TaskScheduleResponse> getSchedulesByDate(LocalDate date) {
        return taskScheduleRepository.findByShiftDate(date)
                .stream()
                .map(taskScheduleMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<TaskScheduleResponse> getSchedulesByRoomAndDate(Integer roomId, LocalDate date) {
        return taskScheduleRepository.findByRoomIdAndShiftDate(roomId, date)
                .stream()
                .map(taskScheduleMapper::toResponse)
                .toList();
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

