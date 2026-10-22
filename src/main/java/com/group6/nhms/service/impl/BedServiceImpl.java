package com.group6.nhms.service.impl;

import com.group6.nhms.dto.request.BedCreateRequest;
import com.group6.nhms.dto.request.BedStatusUpdateRequest;
import com.group6.nhms.dto.response.BedResponse;
import com.group6.nhms.entity.Bed;
import com.group6.nhms.entity.Room;
import com.group6.nhms.enums.BedStatus;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.BedMapper;
import com.group6.nhms.repository.BedRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.RoomRepository;
import com.group6.nhms.service.BedService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BedServiceImpl implements BedService {

    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;
    private final ResidentRepository residentRepository;
    private final BedMapper bedMapper;

    public BedServiceImpl(
            BedRepository bedRepository,
            RoomRepository roomRepository,
            ResidentRepository residentRepository,
            BedMapper bedMapper
    ) {
        this.bedRepository = bedRepository;
        this.roomRepository = roomRepository;
        this.residentRepository = residentRepository;
        this.bedMapper = bedMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedResponse> getAvailableBeds() {
        return bedRepository.findByStatus(BedStatus.AVAILABLE.name())
                .stream()
                .map(bedMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedResponse> getBedsByRoom(Integer roomId) {
        return bedRepository.findByRoomId(roomId)
                .stream()
                .map(bedMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BedResponse getBedById(Integer id) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giường ID " + id));
        return bedMapper.toResponse(bed);
    }

    @Override
    @Transactional
    public BedResponse updateBedStatus(Integer id, BedStatusUpdateRequest request) {
        Bed bed = bedRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giường ID " + id));

        String newStatus;
        try {
            newStatus = BedStatus.valueOf(request.getStatus().trim().toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Trạng thái giường không hợp lệ. Giá trị hợp lệ: AVAILABLE, OCCUPIED, MAINTENANCE");
        }

        // Nếu giường đang có người ở (OCCUPIED), không cho phép đổi thủ công thành AVAILABLE hay MAINTENANCE mà phải qua Check-out / Transfer
        if (BedStatus.OCCUPIED.name().equals(bed.getStatus()) && !BedStatus.OCCUPIED.name().equals(newStatus)) {
            boolean hasActiveResident = residentRepository.findByBedId(id).isPresent();
            if (hasActiveResident) {
                throw new BadRequestException("Giường đang có cư dân lưu trú, không thể đổi trạng thái thủ công!");
            }
        }

        // Không cho đổi thủ công sang OCCUPIED (chỉ được gán qua quy trình Check-in hoặc Chuyển giường)
        if (BedStatus.OCCUPIED.name().equals(newStatus) && !BedStatus.OCCUPIED.name().equals(bed.getStatus())) {
            throw new BadRequestException("Không thể tự chuyển giường sang OCCUPIED mà không qua tiếp nhận cư dân (Check-in)");
        }

        bed.setStatus(newStatus);
        Bed saved = bedRepository.save(bed);
        return bedMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BedResponse createBed(BedCreateRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ID " + request.getRoomId()));

        // Nghiệp vụ: Ràng buộc sức chứa phòng (Room.capacity) - Chặn thêm giường nếu đã đạt sức chứa tối đa
        long currentBedCount = bedRepository.countByRoomId(room.getId());
        if (room.getCapacity() != null && currentBedCount >= room.getCapacity()) {
            throw new BadRequestException("Phòng " + room.getRoomNumber() + " đã đạt sức chứa tối đa ("
                    + room.getCapacity() + " giường), không thể thêm giường mới");
        }

        Bed bed = new Bed();
        bed.setBedNumber(request.getBedNumber());
        bed.setRoom(room);
        bed.setStatus(BedStatus.AVAILABLE.name());

        Bed saved = bedRepository.save(bed);
        return bedMapper.toResponse(saved);
    }
}
