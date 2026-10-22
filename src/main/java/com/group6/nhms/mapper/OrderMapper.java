package com.group6.nhms.mapper;

import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.entity.Order;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class OrderMapper {

    private final ModelMapper modelMapper;

    public OrderMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public OrderResponse toResponse(Order entity) {
        if (entity == null) return null;

        OrderResponse response = modelMapper.map(entity, OrderResponse.class);

        if (entity.getResident() != null) {
            response.setResidentId(entity.getResident().getId());
            response.setResidentName(entity.getResident().getFullName());
            if (entity.getResident().getBed() != null && entity.getResident().getBed().getRoom() != null) {
                response.setRoomNumber(entity.getResident().getBed().getRoom().getRoomNumber());
                response.setRoomBasePrice(entity.getResident().getBed().getRoom().getBasePrice());
            }
        }

        response.setPaymentStatus(entity.getPaymentStatus());

        if (response.getOrderDetails() == null) {
            response.setOrderDetails(new ArrayList<>());
        }

        return response;
    }
}