package com.group6.nhms.mapper;


import com.group6.nhms.dto.request.BedCreateRequest;
import com.group6.nhms.dto.response.BedResponse;
import com.group6.nhms.entity.Bed;
import org.springframework.stereotype.Component;


@Component
public class BedMapper {


   public Bed toEntity(BedCreateRequest request) {
       if (request == null) return null;
       Bed bed = new Bed();
       bed.setBedNumber(request.getBedNumber());
       // Lưu ý: ID của Room sẽ được set ở tầng Service (gọi DB lấy entity Room rồi gắn vào Bed)
       return bed;
   }


   public BedResponse toResponse(Bed entity) {
       if (entity == null) return null;
       BedResponse response = new BedResponse();
       response.setId(entity.getId());
       response.setBedNumber(entity.getBedNumber());
       response.setStatus(entity.getStatus());
       if (entity.getRoom() != null) {
           response.setRoomId(entity.getRoom().getId());
           response.setRoomNumber(entity.getRoom().getRoomNumber());
       }
       return response;
   }
}

