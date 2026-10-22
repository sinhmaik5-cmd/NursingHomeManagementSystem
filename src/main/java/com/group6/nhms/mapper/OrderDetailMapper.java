package com.group6.nhms.mapper;

import com.group6.nhms.dto.common.OrderDetailDto;
import com.group6.nhms.dto.response.OrderDetailResponse;
import com.group6.nhms.entity.OrderDetail;
import org.springframework.stereotype.Component;

@Component
public class OrderDetailMapper {

    public OrderDetailDto toDto(OrderDetail entity) {
        if (entity == null) return null;
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(entity.getId());
        dto.setAmount(entity.getAmount());

        // Lấy tên dịch vụ từ quan hệ với bảng Service
        if (entity.getService() != null) {
            dto.setServiceId(entity.getService().getId());
            dto.setServiceName(entity.getService().getServiceName());
        }
        return dto;
    }

    public OrderDetailResponse toResponse(OrderDetail entity) {
        if (entity == null) return null;
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(entity.getId());
        response.setAmount(entity.getAmount());

        if (entity.getService() != null) {
            response.setServiceId(entity.getService().getId());
            response.setServiceName(entity.getService().getServiceName());
        }
        return response;
    }
}
