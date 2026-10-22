package com.group6.nhms.mapper;

import com.group6.nhms.dto.request.RoomCreateRequest;
import com.group6.nhms.dto.response.RoomResponse;
import com.group6.nhms.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public Room toEntity(RoomCreateRequest request) {
        if (request == null) return null;
        Room room = new Room();
        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(request.getRoomType());
        room.setCapacity(request.getCapacity());
        room.setBasePrice(request.getBasePrice());
        return room;
    }

    public RoomResponse toResponse(Room entity) {
        if (entity == null) return null;
        RoomResponse response = new RoomResponse();
        response.setId(entity.getId());
        response.setRoomNumber(entity.getRoomNumber());
        response.setRoomType(entity.getRoomType());
        response.setCapacity(entity.getCapacity());
        response.setBasePrice(entity.getBasePrice());
        return response;
    }
}