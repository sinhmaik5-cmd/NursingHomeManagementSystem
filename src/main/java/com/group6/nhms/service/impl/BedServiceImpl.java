package com.group6.nhms.service.impl;

import com.group6.nhms.dto.request.BedCreateRequest;
import com.group6.nhms.dto.request.BedStatusUpdateRequest;
import com.group6.nhms.dto.request.BedUpdateRequest;
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
import com.group6.nhms.util.NaturalSortUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class BedServiceImpl implements BedService {

    private static final Comparator<BedResponse> BED_ROOM_ORDER = Comparator
            .comparing(BedResponse::getRoomNumber, NaturalSortUtils::compareNatural)
            .thenComparing(BedResponse::getBedNumber, NaturalSortUtils::compareNatural)
            .thenComparing(BedResponse::getId, Comparator.nullsLast(Integer::compareTo));

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
                .sorted(BED_ROOM_ORDER)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedResponse> getBedsByRoom(Integer roomId) {
        return bedRepository.findByRoomId(roomId)
                .stream()
                .map(bedMapper::toResponse)
                .sorted(BED_ROOM_ORDER)
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

        if (BedStatus.OCCUPIED.name().equals(bed.getStatus()) && !BedStatus.OCCUPIED.name().equals(newStatus)) {
            boolean hasActiveResident = residentRepository.findByBedId(id).isPresent();
            if (hasActiveResident) {
                throw new BadRequestException("Giường đang có cư dân lưu trú, không thể đổi trạng thái thủ công!");
            }
        }

        if (BedStatus.OCCUPIED.name().equals(newStatus) && !BedStatus.OCCUPIED.name().equals(bed.getStatus())) {
            throw new BadRequestException("Không thể tự chuyển giường sang OCCUPIED mà không qua tiếp nhận cư dân (Check-in)");
        }

        bed.setStatus(newStatus);
        Bed saved = bedRepository.save(bed);
        return bedMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BedResponse updateBed(Integer id, BedUpdateRequest request) {
        Bed bed = bedRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giường ID " + id));

        String bedNumber = request.getBedNumber() != null ? request.getBedNumber().trim() : "";
        if (bedNumber.isBlank()) {
            throw new BadRequestException("Mã số giường không được để trống");
        }

        if (bedRepository.existsByRoomIdAndBedNumberAndIdNot(bed.getRoom().getId(), bedNumber, id)) {
            throw new BadRequestException("Giường '" + bedNumber + "' đã tồn tại trong phòng " + bed.getRoom().getRoomNumber());
        }

        String targetStatus;
        try {
            targetStatus = BedStatus.valueOf(request.getStatus().trim().toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Trạng thái giường không hợp lệ. Giá trị hợp lệ: AVAILABLE, OCCUPIED, MAINTENANCE");
        }

        boolean hasActiveResident = residentRepository.findByBedId(id).isPresent();

        if (hasActiveResident) {
            if (!BedStatus.OCCUPIED.name().equals(targetStatus)) {
                throw new BadRequestException("Giường " + bed.getBedNumber() + " đang có cư dân lưu trú, không thể chuyển sang trạng thái " + targetStatus + ". Vui lòng chuyển giường hoặc làm thủ tục xuất viện trước.");
            }
        } else {
            if (BedStatus.OCCUPIED.name().equals(targetStatus) && !BedStatus.OCCUPIED.name().equals(bed.getStatus())) {
                throw new BadRequestException("Không thể tự chuyển giường sang OCCUPIED khi chưa tiếp nhận cư dân (vui lòng sử dụng tính năng Check-in).");
            }
        }

        bed.setBedNumber(bedNumber);
        bed.setStatus(targetStatus);

        Bed saved = bedRepository.save(bed);
        return bedMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBed(Integer id) {
        Bed bed = bedRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giường ID " + id));

        if (BedStatus.OCCUPIED.name().equals(bed.getStatus()) || residentRepository.findByBedId(id).isPresent()) {
            throw new BadRequestException("Không thể xóa giường " + bed.getBedNumber() + " vì đang có cư dân lưu trú (OCCUPIED)! Vui lòng chuyển giường hoặc xuất viện trước.");
        }

        bedRepository.delete(bed);
    }

    @Override
    @Transactional
    public BedResponse createBed(BedCreateRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ID " + request.getRoomId()));

        if ("CLINIC".equalsIgnoreCase(room.getRoomType()) || "RECEPTION".equalsIgnoreCase(room.getRoomType())) {
            throw new BadRequestException("Phòng khám Bác sĩ và Quầy Lễ tân không sử dụng giường lưu trú của cư dân!");
        }

        long currentBedCount = bedRepository.countByRoomId(room.getId());
        if (room.getCapacity() != null && currentBedCount >= room.getCapacity()) {
            throw new BadRequestException("Phòng " + room.getRoomNumber() + " đã đạt sức chứa tối đa ("
                    + room.getCapacity() + " giường), không thể thêm giường mới");
        }

        String bedNum = request.getBedNumber() != null ? request.getBedNumber().trim() : "";
        if (bedNum.isBlank()) {
            throw new BadRequestException("Mã số giường không được để trống");
        }

        if (bedRepository.existsByRoomIdAndBedNumber(room.getId(), bedNum)) {
            throw new BadRequestException("Giường '" + bedNum + "' đã tồn tại trong phòng " + room.getRoomNumber());
        }

        Bed bed = new Bed();
        bed.setBedNumber(bedNum);
        bed.setRoom(room);
        bed.setStatus(BedStatus.AVAILABLE.name());

        Bed saved = bedRepository.save(bed);
        return bedMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedResponse> getAllBeds() {
        return bedRepository.findAll().stream()
                .map(bedMapper::toResponse)
                .sorted(BED_ROOM_ORDER)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalBeds() {
        return bedRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countOccupiedBeds() {
        return bedRepository.countByStatus(BedStatus.OCCUPIED.name());
    }

    @Override
    @Transactional(readOnly = true)
    public long countAvailableBeds() {
        return bedRepository.countByStatus(BedStatus.AVAILABLE.name());
    }

    @Override
    @Transactional(readOnly = true)
    public long countMaintenanceBeds() {
        return bedRepository.countByStatus(BedStatus.MAINTENANCE.name());
    }

    @Override
    @Transactional(readOnly = true)
    public double getOccupancyRate() {
        long total = countTotalBeds();
        long occupied = countOccupiedBeds();
        double rate = total > 0 ? ((double) occupied / total) * 100.0 : 0.0;
        return Math.round(rate * 100.0) / 100.0;
    }
}
