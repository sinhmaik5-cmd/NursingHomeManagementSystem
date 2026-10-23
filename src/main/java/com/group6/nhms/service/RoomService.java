package com.group6.nhms.service;

import com.group6.nhms.dto.response.RoomResponse;
import com.group6.nhms.entity.Room;

import java.util.List;

public interface RoomService {
    List<RoomResponse> getAllRooms();

    RoomResponse getRoomById(Integer id);

    Room getRoomEntityById(Integer id);
}
