package com.group6.nhms.dto.response;


import java.util.ArrayList;
import java.util.List;


public class NotificationSummaryDto {
    private boolean hasNew;
    private int totalNewCount;
    private List<NotificationItemDto> items;


    public NotificationSummaryDto() {
        this.items = new ArrayList<>();
    }


    public NotificationSummaryDto(boolean hasNew, int totalNewCount, List<NotificationItemDto> items) {
        this.hasNew = hasNew;
        this.totalNewCount = totalNewCount;
        this.items = items != null ? items : new ArrayList<>();
    }


    public boolean isHasNew() {
        return hasNew;
    }


    public void setHasNew(boolean hasNew) {
        this.hasNew = hasNew;
    }


    public int getTotalNewCount() {
        return totalNewCount;
    }


    public void setTotalNewCount(int totalNewCount) {
        this.totalNewCount = totalNewCount;
    }


    public List<NotificationItemDto> getItems() {
        return items;
    }


    public void setItems(List<NotificationItemDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}

