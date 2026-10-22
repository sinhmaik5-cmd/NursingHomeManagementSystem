package com.group6.nhms.service;

import com.group6.nhms.dto.request.BedCreateRequest;
import com.group6.nhms.dto.request.BedStatusUpdateRequest;
import com.group6.nhms.dto.response.BedResponse;

import java.util.List;

public interface BedService {

    List<BedResponse> getAvailableBeds();

    List<BedResponse> getBedsByRoom(Integer roomId);

    BedResponse getBedById(Integer id);

    BedResponse updateBedStatus(Integer id, BedStatusUpdateRequest request);

    BedResponse createBed(BedCreateRequest request);
}
