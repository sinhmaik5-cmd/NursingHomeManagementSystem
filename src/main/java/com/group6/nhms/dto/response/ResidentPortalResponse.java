package com.group6.nhms.dto.response;


import java.util.List;


public class ResidentPortalResponse {


   private final ResidentResponse resident;
   private final HealthRecordResponse latestRecord;
   private final List<IncidentReportResponse> incidents;
   private final List<OrderResponse> orders;


   public ResidentPortalResponse(
           ResidentResponse resident,
           HealthRecordResponse latestRecord,
           List<IncidentReportResponse> incidents,
           List<OrderResponse> orders
   ) {
       this.resident = resident;
       this.latestRecord = latestRecord;
       this.incidents = List.copyOf(incidents);
       this.orders = List.copyOf(orders);
   }


   public ResidentResponse getResident() {
       return resident;
   }


   public HealthRecordResponse getLatestRecord() {
       return latestRecord;
   }


   public List<IncidentReportResponse> getIncidents() {
       return incidents;
   }


   public List<OrderResponse> getOrders() {
       return orders;
   }
}

