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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ResidentServiceImpl implements ResidentService {

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
    @Transactional(readOnly = true)
    public List<ResidentResponse> getAllResidents(String name, String status) {
        List<Resident> residents;
        if (name != null && !name.trim().isEmpty()) {
            residents = residentRepository.findByFullNameContainingIgnoreCase(name.trim());
        } else if (status != null && !status.trim().isEmpty()) {
            residents = residentRepository.findByStatus(status.trim().toUpperCase());
        } else {
            residents = residentRepository.findAll();
        }
        return residents.stream().map(residentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResidentResponse getResidentById(Integer id) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cư dân ID " + id));
        return residentMapper.toResponse(resident);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidentResponse> getResidentsByRelative(Integer relativeId) {
        return residentRepository.findByRelativeId(relativeId)
                .stream()
                .map(residentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveResidents() {
        return residentRepository.countByStatus(ResidentStatus.ACTIVE.name());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidentResponse> getRecentResidents(int limit) {
        return residentRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "admissionDate", "id"))
        ).getContent().stream().map(residentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResidentResponse getResidentByBedId(Integer bedId) {
        return residentRepository.findByBedId(bedId)
                .map(residentMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public ResidentResponse checkIn(ResidentCheckInRequest request) {
        Relative relative;

        if (request.getRelativeId() != null) {
            relative = relativeRepository.findById(request.getRelativeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người thân ID " + request.getRelativeId()));
        } else {
            if (isBlank(request.getRelativeFullName()) || isBlank(request.getRelativePhone())) {
                throw new BadRequestException("Cần cung cấp thông tin người thân hoặc chọn người thân có sẵn");
            }

            relative = relativeRepository.findFirstByPhoneNumber(request.getRelativePhone().trim())
                    .orElseGet(() -> {
                        Relative newRel = new Relative();
                        newRel.setFullName(request.getRelativeFullName().trim());
                        newRel.setPhoneNumber(request.getRelativePhone().trim());
                        newRel.setAddress(request.getRelativeAddress());
                        newRel.setRelationship(request.getRelationship());
                        return relativeRepository.save(newRel);
                    });
        }

        Bed bed = bedRepository.findByIdForUpdate(request.getBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giường ID " + request.getBedId()));

        if (!BedStatus.AVAILABLE.name().equals(bed.getStatus())) {
            throw new BadRequestException("Giường " + bed.getBedNumber() + " hiện không ở trạng thái AVAILABLE để tiếp nhận");
        }

        bed.setStatus(BedStatus.OCCUPIED.name());

        Resident resident = new Resident();
        resident.setFullName(request.getFullName().trim());
        resident.setGender(request.getGender());
        resident.setDob(request.getDateOfBirth());
        resident.setAdmissionDate(LocalDate.now());
        resident.setInitialHealthStatus(request.getMedicalHistory());
        resident.setStatus(ResidentStatus.ACTIVE.name());
        resident.setRelative(relative);
        resident.setBed(bed);

        Resident saved = residentRepository.save(resident);
        return residentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ResidentResponse transferBed(BedTransferRequest request) {
        Resident resident = residentRepository.findByIdForUpdate(request.getResidentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cư dân ID " + request.getResidentId()));

        if (!ResidentStatus.ACTIVE.name().equals(resident.getStatus())) {
            throw new BadRequestException("Cư dân không ở trạng thái đang lưu trú");
        }

        Bed oldBed = resident.getBed();
        if (oldBed == null) {
            throw new BadRequestException("Cư dân hiện tại chưa được gán giường");
        }

        if (oldBed.getId().equals(request.getNewBedId())) {
            throw new BadRequestException("Giường mới phải khác giường hiện tại");
        }

        Bed newBed = bedRepository.findByIdForUpdate(request.getNewBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giường đích ID " + request.getNewBedId()));

        if (!BedStatus.AVAILABLE.name().equals(newBed.getStatus())) {
            throw new BadRequestException("Giường mới " + newBed.getBedNumber() + " hiện không ở trạng thái AVAILABLE");
        }

        LocalDate now = LocalDate.now();
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        boolean isEndOfMonth = ChronoUnit.DAYS.between(now, lastDayOfMonth) <= 3;

        BigDecimal oldPrice = oldBed.getRoom().getBasePrice();
        BigDecimal newPrice = newBed.getRoom().getBasePrice();

        if (newPrice.compareTo(oldPrice) > 0 && !isEndOfMonth) {
            // Cho phép nhưng có cảnh báo hoặc lưu log
        }

        oldBed.setStatus(BedStatus.MAINTENANCE.name());
        newBed.setStatus(BedStatus.OCCUPIED.name());

        resident.setBed(newBed);
        Resident saved = residentRepository.save(resident);

        return residentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void checkOut(Integer residentId, String finalStatus) {
        Resident resident = residentRepository.findByIdForUpdate(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cư dân ID " + residentId));

        if (!ResidentStatus.ACTIVE.name().equals(resident.getStatus())) {
            throw new BadRequestException("Cư dân không ở trạng thái đang lưu trú");
        }

        List<Order> unpaidOrders = orderRepository.findByResidentIdAndPaymentStatus(
                residentId,
                PaymentStatus.PENDING.name()
        );

        if (!unpaidOrders.isEmpty()) {
            throw new BadRequestException(
                    "Cư dân còn " + unpaidOrders.size() + " hóa đơn chưa thanh toán, cần tất toán trước khi làm thủ tục xuất viện");
        }

        if (resident.getBed() != null) {
            Bed bed = bedRepository.findByIdForUpdate(resident.getBed().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giường hiện tại của cư dân"));

            bed.setStatus(BedStatus.MAINTENANCE.name());
            resident.setBed(null);
        }

        resident.setStatus(finalStatus);
        residentRepository.save(resident);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
