package com.group6.nhms.mapper;


import com.group6.nhms.dto.response.OrderDetailResponse;
import com.group6.nhms.entity.OrderDetail;
import org.springframework.stereotype.Component;


@Component
public class OrderDetailMapper {


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

