package com.group6.nhms.service;

import com.group6.nhms.dto.request.BedTransferRequest;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Bed;
import com.group6.nhms.entity.HealthRecord;
import com.group6.nhms.entity.Resident;
import com.group6.nhms.entity.Room;
import com.group6.nhms.entity.Staff;
import com.group6.nhms.enums.BedStatus;
import com.group6.nhms.enums.ResidentStatus;
import com.group6.nhms.mapper.ResidentMapper;
import com.group6.nhms.repository.BedRepository;
import com.group6.nhms.repository.HealthRecordRepository;
import com.group6.nhms.repository.OrderRepository;
import com.group6.nhms.repository.RelativeRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.StaffRepository;
import com.group6.nhms.service.impl.ResidentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResidentServiceImplTest {

    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private RelativeRepository relativeRepository;
    @Mock
    private BedRepository bedRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ResidentMapper residentMapper;
    @Mock
    private HealthRecordRepository healthRecordRepository;
    @Mock
    private StaffRepository staffRepository;

    private ResidentServiceImpl residentService;

    @BeforeEach
    void setUp() {
        residentService = new ResidentServiceImpl(
                residentRepository,
                relativeRepository,
                bedRepository,
                orderRepository,
                residentMapper,
                healthRecordRepository,
                staffRepository
        );
    }

    @Test
    void transferBed_logsRoomTransferAuditNoteToHealthRecord() {
        Room oldRoom = new Room();
        oldRoom.setId(1);
        oldRoom.setRoomNumber("P101");
        oldRoom.setBasePrice(new BigDecimal("5000000"));

        Bed oldBed = new Bed();
        oldBed.setId(10);
        oldBed.setBedNumber("G101-1");
        oldBed.setStatus(BedStatus.OCCUPIED.name());
        oldBed.setRoom(oldRoom);

        Room newRoom = new Room();
        newRoom.setId(2);
        newRoom.setRoomNumber("P201");
        newRoom.setBasePrice(new BigDecimal("8000000"));

        Bed newBed = new Bed();
        newBed.setId(20);
        newBed.setBedNumber("G201-1");
        newBed.setStatus(BedStatus.AVAILABLE.name());
        newBed.setRoom(newRoom);

        Resident resident = new Resident();
        resident.setId(1);
        resident.setFullName("Nguyễn Văn An");
        resident.setStatus(ResidentStatus.ACTIVE.name());
        resident.setBed(oldBed);

        Staff doctor = new Staff();
        doctor.setId(3);
        doctor.setJobTitle("DOCTOR");

        BedTransferRequest req = new BedTransferRequest();
        req.setResidentId(1);
        req.setNewBedId(20);
        req.setReason("Gia đình nâng cấp phòng VIP");

        when(residentRepository.findByIdForUpdate(1)).thenReturn(Optional.of(resident));
        when(bedRepository.findByIdForUpdate(20)).thenReturn(Optional.of(newBed));
        when(residentRepository.save(any(Resident.class))).thenAnswer(inv -> inv.getArgument(0));
        when(staffRepository.findByJobTitle("DOCTOR")).thenReturn(List.of(doctor));
        when(residentMapper.toResponse(any(Resident.class))).thenReturn(new ResidentResponse());

        residentService.transferBed(req);

        assertEquals(BedStatus.MAINTENANCE.name(), oldBed.getStatus());
        assertEquals(BedStatus.OCCUPIED.name(), newBed.getStatus());

        ArgumentCaptor<HealthRecord> recordCaptor = ArgumentCaptor.forClass(HealthRecord.class);
        verify(healthRecordRepository).save(recordCaptor.capture());
        String notes = recordCaptor.getValue().getNotes();
        assertTrue(notes.startsWith("[Đổi phòng "));
        assertTrue(notes.contains("Từ phòng P101 (5000000 VNĐ/tháng) sang phòng P201 (8000000 VNĐ/tháng)."));
        assertTrue(notes.contains("Gia đình nâng cấp phòng VIP"));
    }
}
