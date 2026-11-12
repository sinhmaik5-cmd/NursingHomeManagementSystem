package com.group6.nhms.mapper;


import com.group6.nhms.dto.response.OrderDetailResponse;
import com.group6.nhms.dto.response.OrderResponse;
import com.group6.nhms.entity.Order;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@Component
public class OrderMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


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
       populateRoomBillingInfo(entity, response);


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

   private void populateRoomBillingInfo(Order entity, OrderResponse response) {
       LocalDate issueDate = entity.getIssueDate();
       if (issueDate == null) {
           response.setRoomQuantityLabel("1 tháng");
           return;
       }
       LocalDate monthStart = issueDate.withDayOfMonth(1);
       LocalDate monthEnd = issueDate.withDayOfMonth(issueDate.lengthOfMonth());
       LocalDate admissionDate = entity.getResident() != null ? entity.getResident().getAdmissionDate() : null;

       LocalDate periodStart = (admissionDate != null && admissionDate.isAfter(monthStart) && !admissionDate.isAfter(monthEnd))
               ? admissionDate
               : monthStart;

       int totalDays = issueDate.lengthOfMonth();
       long billableDays = ChronoUnit.DAYS.between(periodStart, monthEnd) + 1;

       BigDecimal roomBasePrice = (entity.getResident() != null
               && entity.getResident().getBed() != null
               && entity.getResident().getBed().getRoom() != null)
               ? entity.getResident().getBed().getRoom().getBasePrice()
               : null;

       response.setRoomBillingPeriod(periodStart.format(DATE_FORMATTER) + " - " + monthEnd.format(DATE_FORMATTER));
       if (entity.getRoomPrice() != null && entity.getRoomPrice().compareTo(BigDecimal.ZERO) == 0) {
           response.setRoomQuantityLabel("Chưa chốt phòng");
       } else if (billableDays < totalDays
               && entity.getRoomPrice() != null
               && entity.getRoomPrice().compareTo(BigDecimal.ZERO) > 0
               && roomBasePrice != null
               && entity.getRoomPrice().compareTo(roomBasePrice) < 0) {
           response.setRoomQuantityLabel(billableDays + "/" + totalDays + " ngày");
       } else if (entity.getRoomPrice() != null
               && entity.getRoomPrice().compareTo(BigDecimal.ZERO) > 0
               && roomBasePrice != null
               && entity.getRoomPrice().compareTo(roomBasePrice) != 0) {
           response.setRoomQuantityLabel("Theo ngày thực tế (" + totalDays + " ngày/tháng)");
       } else {
           response.setRoomQuantityLabel("1 tháng");
       }
   }
}

