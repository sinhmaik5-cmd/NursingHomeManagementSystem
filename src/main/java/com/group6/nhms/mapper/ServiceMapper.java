package com.group6.nhms.mapper;

import com.group6.nhms.dto.request.ServiceCreateRequest;
import com.group6.nhms.dto.response.ServiceResponse;
import com.group6.nhms.entity.Service; // Cẩn thận import nhầm java.security.Provider.Service
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public Service toEntity(ServiceCreateRequest request) {
        if (request == null) return null;
        Service service = new Service();
        service.setServiceName(request.getServiceName());
        service.setDescription(request.getDescription());
        service.setUnitPrice(request.getUnitPrice());
        return service;
    }

    public ServiceResponse toResponse(Service entity) {
        if (entity == null) return null;
        ServiceResponse response = new ServiceResponse();
        response.setId(entity.getId());
        response.setServiceName(entity.getServiceName());
        response.setDescription(entity.getDescription());
        response.setUnitPrice(entity.getUnitPrice());
        return response;
    }
}