package com.group6.nhms.service.impl;


import com.group6.nhms.dto.request.RoomCreateRequest;
import com.group6.nhms.dto.request.RoomUpdateRequest;
import com.group6.nhms.dto.response.RoomResponse;
import com.group6.nhms.entity.Bed;
import com.group6.nhms.entity.Room;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.RoomMapper;
import com.group6.nhms.repository.BedRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.RoomRepository;
import com.group6.nhms.repository.TaskScheduleRepository;
import com.group6.nhms.service.RoomService;
import com.group6.nhms.util.NaturalSortUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;


@Service
public class RoomServiceImpl implements RoomService {


    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final ResidentRepository residentRepository;
    private final TaskScheduleRepository taskScheduleRepository;
    private final RoomMapper roomMapper;


    public RoomServiceImpl(
            RoomRepository roomRepository,
            BedRepository bedRepository,
            ResidentRepository residentRepository,
            TaskScheduleRepository taskScheduleRepository,
            RoomMapper roomMapper
    ) {
        this.roomRepository = roomRepository;
        this.bedRepository = bedRepository;
        this.residentRepository = residentRepository;
        this.taskScheduleRepository = taskScheduleRepository;
        this.roomMapper = roomMapper;
    }


    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toResponse)
                .sorted(Comparator
                        .comparing(RoomResponse::getRoomNumber, NaturalSortUtils::compareNatural)
                        .thenComparing(RoomResponse::getId, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Integer id) {
        Room room = getRoomEntityById(id);
        return roomMapper.toResponse(room);
    }


    @Override
    @Transactional(readOnly = true)
    public Room getRoomEntityById(Integer id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ID " + id));
    }


    @Override
    @Transactional
    public RoomResponse createRoom(RoomCreateRequest request) {
        String roomNumber = request.getRoomNumber() != null ? request.getRoomNumber().trim() : "";
        if (roomNumber.isBlank()) {
            throw new BadRequestException("Số phòng không được để trống");
        }


        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new BadRequestException("Phòng số '" + roomNumber + "' đã tồn tại trong hệ thống");
        }


        if (request.getCapacity() == null || request.getCapacity() < 1) {
            throw new BadRequestException("Sức chứa phòng tối thiểu là 1 giường");
        }


        if (request.getBasePrice() == null || request.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Giá phòng không hợp lệ");
        }


        Room room = roomMapper.toEntity(request);
        room.setRoomNumber(roomNumber);
        room.setStatus("AVAILABLE");
        Room saved = roomRepository.save(room);
        return roomMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public RoomResponse updateRoom(Integer id, RoomUpdateRequest request) {
        Room room = getRoomEntityById(id);


        String roomNumber = request.getRoomNumber() != null ? request.getRoomNumber().trim() : "";
        if (roomNumber.isBlank()) {
            throw new BadRequestException("Số phòng không được để trống");
        }


        if (roomRepository.existsByRoomNumberAndIdNot(roomNumber, id)) {
            throw new BadRequestException("Phòng số '" + roomNumber + "' đã được sử dụng bởi một phòng khác");
        }


        long currentBedCount = bedRepository.countByRoomId(id);
        if (request.getCapacity() == null || request.getCapacity() < 1) {
            throw new BadRequestException("Sức chứa phòng tối thiểu là 1 giường");
        }


        if (request.getCapacity() < currentBedCount) {
            throw new BadRequestException("Sức chứa mới (" + request.getCapacity() + " giường) không được nhỏ hơn số giường hiện có trong phòng (" + currentBedCount + " giường)");
        }


        if (request.getBasePrice() == null || request.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Giá phòng không hợp lệ");
        }


        room.setRoomNumber(roomNumber);
        room.setRoomType(request.getRoomType());
        room.setCapacity(request.getCapacity());
        room.setBasePrice(request.getBasePrice());


        Room saved = roomRepository.save(room);
        return roomMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public void deleteRoom(Integer id) {
        Room room = getRoomEntityById(id);
        List<Bed> beds = bedRepository.findByRoomId(id);
        for (Bed bed : beds) {
            if ("OCCUPIED".equalsIgnoreCase(bed.getStatus()) || residentRepository.findByBedId(bed.getId()).isPresent()) {
                throw new BadRequestException("Không thể xóa phòng " + room.getRoomNumber() + " vì đang có giường " + bed.getBedNumber() + " có cư dân lưu trú (OCCUPIED)!");
            }
        }

        // Xóa phân công ca trực của phòng nếu có
        taskScheduleRepository.deleteByRoomId(id);


        // Xóa toàn bộ giường trống trong phòng
        bedRepository.deleteByRoomId(id);


        // Xóa phòng
        roomRepository.delete(room);
    }
}

