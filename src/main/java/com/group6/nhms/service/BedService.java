package com.group6.nhms.service;

import com.group6.nhms.dto.request.BedCreateRequest;
import com.group6.nhms.dto.request.BedStatusUpdateRequest;
import com.group6.nhms.dto.request.BedUpdateRequest;
import com.group6.nhms.dto.response.BedResponse;

import java.util.List;

public interface BedService {

    List<BedResponse> getAvailableBeds();

    List<BedResponse> getBedsByRoom(Integer roomId);

    BedResponse getBedById(Integer id);

    BedResponse updateBedStatus(Integer id, BedStatusUpdateRequest request);

    BedResponse updateBed(Integer id, BedUpdateRequest request);

    void deleteBed(Integer id);

    BedResponse createBed(BedCreateRequest request);

    List<BedResponse> getAllBeds();

    long countTotalBeds();

    long countOccupiedBeds();

    long countAvailableBeds();

    long countMaintenanceBeds();

    double getOccupancyRate();
}