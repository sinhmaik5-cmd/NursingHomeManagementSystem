package com.group6.nhms.service;


import com.group6.nhms.dto.request.BedTransferRequest;
import com.group6.nhms.dto.request.HospitalTransferRequest;
import com.group6.nhms.dto.request.ResidentCheckInRequest;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Bed;


import java.util.List;


public interface ResidentService {
    List<Bed> getAvailableBeds();


    ResidentResponse checkIn(ResidentCheckInRequest request);


    ResidentResponse transferBed(BedTransferRequest request);


    void checkOut(Integer residentId, String finalStatus);


    ResidentResponse emergencyHospitalTransfer(Integer residentId, HospitalTransferRequest request);


    ResidentResponse returnFromHospital(Integer residentId);


    ResidentResponse updateMedicalHistory(Integer residentId, String medicalHistory);


    List<ResidentResponse> getAllResidents(String name, String status);


    ResidentResponse getResidentById(Integer id);


    List<ResidentResponse> getResidentsByRelative(Integer relativeId);


    long countActiveResidents();


    List<ResidentResponse> getRecentResidents(int limit);


    ResidentResponse getResidentByBedId(Integer bedId);
}

