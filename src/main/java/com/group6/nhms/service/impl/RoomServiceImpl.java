package com.group6.nhms.service.impl;

import com.group6.nhms.dto.response.RoomResponse;
import com.group6.nhms.entity.Room;
import com.group6.nhms.exception.ResourceNotFoundException;
import com.group6.nhms.mapper.RoomMapper;
import com.group6.nhms.repository.RoomRepository;
import com.group6.nhms.service.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public RoomServiceImpl(RoomRepository roomRepository, RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.roomMapper = roomMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream().map(roomMapper::toResponse).toList();
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
}
