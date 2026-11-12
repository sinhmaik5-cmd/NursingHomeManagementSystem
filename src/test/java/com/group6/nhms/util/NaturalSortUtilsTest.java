package com.group6.nhms.util;

import com.group6.nhms.dto.response.BedResponse;
import com.group6.nhms.dto.response.RoomResponse;
import com.group6.nhms.entity.Bed;
import com.group6.nhms.entity.Room;
import com.group6.nhms.mapper.BedMapper;
import com.group6.nhms.mapper.RoomMapper;
import com.group6.nhms.repository.BedRepository;
import com.group6.nhms.repository.ResidentRepository;
import com.group6.nhms.repository.RoomRepository;
import com.group6.nhms.repository.TaskScheduleRepository;
import com.group6.nhms.service.impl.BedServiceImpl;
import com.group6.nhms.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NaturalSortUtilsTest {

    @Mock
    private BedRepository bedRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private TaskScheduleRepository taskScheduleRepository;

    @Test
    void compareNatural_sortsRoomsAndBedsNaturally() {
        List<String> unsorted = List.of("G102-10", "G102-2", "G101-2", "G102-1", "G101-1");
        List<String> sorted = unsorted.stream()
                .sorted(NaturalSortUtils::compareNatural)
                .toList();
        assertEquals(List.of("G101-1", "G101-2", "G102-1", "G102-2", "G102-10"), sorted);
    }

    @Test
    void getAvailableBeds_sortsByRoomThenBedNaturally() {
        BedServiceImpl bedService = new BedServiceImpl(bedRepository, roomRepository, residentRepository, new BedMapper());

        Room p101 = room(1, "P101");
        Room p102 = room(2, "P102");
        Room p201 = room(3, "P201");

        List<Bed> dbOrder = List.of(
                bed(2, "G101-2", p101),
                bed(5, "G201-1", p201),
                bed(3, "G102-2", p102),
                bed(8, "G101-10", p101),
                bed(7, "G102-1", p102)
        );
        when(bedRepository.findByStatus("AVAILABLE")).thenReturn(dbOrder);

        List<String> labels = bedService.getAvailableBeds().stream()
                .map(b -> b.getRoomNumber() + ":" + b.getBedNumber())
                .toList();

        assertEquals(
                List.of("P101:G101-2", "P101:G101-10", "P102:G102-1", "P102:G102-2", "P201:G201-1"),
                labels
        );
    }

    @Test
    void getAllRooms_sortsByRoomNumberNaturally() {
        RoomServiceImpl roomService = new RoomServiceImpl(
                roomRepository, bedRepository, residentRepository, taskScheduleRepository, new RoomMapper());

        when(roomRepository.findAll()).thenReturn(List.of(
                room(3, "P201"),
                room(1, "P102"),
                room(2, "P99"),
                room(4, "P101")
        ));

        List<String> roomNumbers = roomService.getAllRooms().stream()
                .map(RoomResponse::getRoomNumber)
                .toList();

        assertEquals(List.of("P99", "P101", "P102", "P201"), roomNumbers);
    }

    private Room room(Integer id, String roomNumber) {
        Room r = new Room();
        r.setId(id);
        r.setRoomNumber(roomNumber);
        return r;
    }

    private Bed bed(Integer id, String bedNumber, Room room) {
        Bed b = new Bed();
        b.setId(id);
        b.setBedNumber(bedNumber);
        b.setRoom(room);
        b.setStatus("AVAILABLE");
        return b;
    }
}
