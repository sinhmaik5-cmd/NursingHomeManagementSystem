package com.group6.nhms.mapper;

import com.group6.nhms.dto.response.OrderDetailResponse;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.entity.Order;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMapper {

    private final ModelMapper modelMapper;
    private final OrderDetailMapper orderDetailMapper;

    public OrderMapper(ModelMapper modelMapper, OrderDetailMapper orderDetailMapper) {
        this.modelMapper = modelMapper;
        this.orderDetailMapper = orderDetailMapper;
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
        response.setRoomPrice(entity.getRoomPrice());
        response.setServicesPrice(entity.getServicesPrice());
        response.setMedicationsPrice(entity.getMedicationsPrice());
        response.setTotalAmount(entity.getTotalAmount());
        response.setPaymentDate(entity.getPaymentDate());
        response.setIssueDate(entity.getIssueDate());
        response.setDueDate(entity.getDueDate());

        if (entity.getOrderDetails() != null && !entity.getOrderDetails().isEmpty()) {
            List<OrderDetailResponse> details = entity.getOrderDetails().stream()
                    .map(orderDetailMapper::toResponse)
                    .toList();
            response.setOrderDetails(details);
        } else if (response.getOrderDetails() == null) {
            response.setOrderDetails(new ArrayList<>());
        }

        return response;
    }
}
