package com.group6.nhms.service;

import com.group6.nhms.dto.request.BedTransferRequest;
import com.group6.nhms.dto.request.ResidentCheckInRequest;
import com.group6.nhms.dto.response.ResidentResponse;
import com.group6.nhms.entity.Bed;

import java.util.List;

public interface ResidentService {
    List<Bed> getAvailableBeds();

    ResidentResponse checkIn(ResidentCheckInRequest request);

    ResidentResponse transferBed(BedTransferRequest request);

    void checkOut(Integer residentId, String finalStatus);

}
