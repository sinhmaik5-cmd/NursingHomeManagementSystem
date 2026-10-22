package com.group6.nhms.service.impl;

import com.group6.nhms.dto.request.BedTransferRequest;
import com.group6.nhms.dto.request.ResidentCheckInRequest;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Bed;
import com.group6.nhms.entity.Order;
import com.group6.nhms.entity.Relative;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.enums.BedStatus;
import com.group6.nhms.enums.PaymentStatus;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.exception.BadRequestException;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.ResidentMapper;
import com.group6.nhms.repository.BedRepository;
import com.group6.nhms.repository.OrderRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.service.ResidentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ResidentServiceImpl implements ResidentService{

    private final ResidentRepository residentRepository;
    private final RelativeRepository relativeRepository;
    private final BedRepository bedRepository;
    private final OrderRepository orderRepository;
    private final ResidentMapper residentMapper;

    public ResidentServiceImpl(
            ResidentRepository residentRepository,
            RelativeRepository relativeRepository,
            BedRepository bedRepository,
            OrderRepository orderRepository,
            ResidentMapper residentMapper
    ) {
        this.residentRepository = residentRepository;
        this.relativeRepository = relativeRepository;
        this.bedRepository = bedRepository;
        this.orderRepository = orderRepository;
        this.residentMapper = residentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bed> getAvailableBeds() {
        return bedRepository.findByStatus(BedStatus.AVAILABLE.name());
    }

    @Override
    @Transactional
    public ResidentResponse checkIn(ResidentCheckInRequest request) {
        Bed bed = bedRepository.findByIdForUpdate(request.getBedId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy giường ID " + request.getBedId()));

        if (!BedStatus.AVAILABLE.name().equals(bed.getStatus())) {
            throw new BadRequestException("Giường đã được sử dụng hoặc không ở trạng thái sẵn sàng");
        }

        Relative relative;

        if (request.getRelativeId() != null) {
            relative = relativeRepository.findById(request.getRelativeId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Không tìm thấy người thân ID " + request.getRelativeId()));
        } else {
            if (isBlank(request.getRelativeFullName()) || isBlank(request.getRelativePhone())) {
                throw new BadRequestException(
                        "Cần nhập tên và số điện thoại người thân, hoặc chọn người thân đã có");
            }

            relative = new Relative();
            relative.setFullName(request.getRelativeFullName().trim());
            relative.setPhone(request.getRelativePhone().trim());
            relative.setAddress(request.getRelativeAddress());
            relative.setRelationship(request.getRelationship());
            relative = relativeRepository.save(relative);
        }

        Resident resident = residentMapper.toEntity(request);
        resident.setRelative(relative);
        resident.setBed(bed);
        resident.setAdmissionDate(LocalDate.now());
        resident.setStatus(ResidentStatus.ACTIVE.name());

        bed.setStatus(BedStatus.OCCUPIED.name());

        Resident savedResident = residentRepository.save(resident);
        return residentMapper.toResponse(savedResident);
    }

    @Override
    @Transactional
    public ResidentResponse transferBed(BedTransferRequest request) {
        Resident resident = residentRepository.findByIdForUpdate(request.getResidentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy cư dân ID " + request.getResidentId()));

        if (!ResidentStatus.ACTIVE.name().equals(resident.getStatus())) {
            throw new BadRequestException("Chỉ có thể chuyển giường cho cư dân đang lưu trú");
        }

        if (resident.getBed() == null) {
            throw new BadRequestException("Cư dân hiện không được gán giường");
        }

        Integer oldBedId = resident.getBed().getId();
        Integer newBedId = request.getNewBedId();

        if (oldBedId.equals(newBedId)) {
            throw new BadRequestException("Giường mới phải khác giường hiện tại");
        }

        Integer firstId = Math.min(oldBedId, newBedId);
        Integer secondId = Math.max(oldBedId, newBedId);

        Bed firstBed = bedRepository.findByIdForUpdate(firstId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy giường ID " + firstId));

        Bed secondBed = bedRepository.findByIdForUpdate(secondId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy giường ID " + secondId));

        Bed oldBed = oldBedId.equals(firstId) ? firstBed : secondBed;
        Bed newBed = newBedId.equals(firstId) ? firstBed : secondBed;

        if (!BedStatus.OCCUPIED.name().equals(oldBed.getStatus())) {
            throw new BadRequestException("Giường hiện tại không ở trạng thái đang sử dụng");
        }

        if (!BedStatus.AVAILABLE.name().equals(newBed.getStatus())) {
            throw new BadRequestException("Giường mới không còn trống");
        }

        newBed.setStatus(BedStatus.OCCUPIED.name());
        oldBed.setStatus(BedStatus.AVAILABLE.name());
        resident.setBed(newBed);

        Resident savedResident = residentRepository.save(resident);
        return residentMapper.toResponse(savedResident);
    }

    @Override
    @Transactional
    public void checkOut(Integer residentId, String finalStatus) {
        if (!ResidentStatus.DISCHARGED.name().equals(finalStatus)
                && !ResidentStatus.DECEASED.name().equals(finalStatus)) {
            throw new BadRequestException("Trạng thái xuất viện phải là DISCHARGED hoặc DECEASED");
        }

        Resident resident = residentRepository.findByIdForUpdate(residentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy cư dân ID " + residentId));

        if (!ResidentStatus.ACTIVE.name().equals(resident.getStatus())) {
            throw new BadRequestException("Cư dân không ở trạng thái đang lưu trú");
        }

        List<Order> unpaidOrders = orderRepository.findByResidentIdAndPaymentStatus(
                residentId,
                PaymentStatus.PENDING.name()
        );

        if (!unpaidOrders.isEmpty()) {
            throw new BadRequestException(
                    "Cư dân còn hóa đơn chưa thanh toán, không thể hoàn tất xuất viện");
        }

        if (resident.getBed() != null) {
            Bed bed = bedRepository.findByIdForUpdate(resident.getBed().getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Không tìm thấy giường hiện tại của cư dân"));

            bed.setStatus(BedStatus.AVAILABLE.name());
            resident.setBed(null);
        }

        resident.setStatus(finalStatus);
        residentRepository.save(resident);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
