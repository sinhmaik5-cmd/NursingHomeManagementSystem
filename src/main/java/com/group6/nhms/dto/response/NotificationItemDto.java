package com.group6.nhms.dto.response;


public class NotificationItemDto {
    private String id;
    private String title;
    private String description;
    private String link;
    private String iconClass;
    private int count;
    private boolean hasNew;


    public NotificationItemDto() {
    }


    public NotificationItemDto(String id, String title, String description, String link, String iconClass, int count, boolean hasNew) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
        this.iconClass = iconClass;
        this.count = count;
        this.hasNew = hasNew;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getLink() {
        return link;
    }


    public void setLink(String link) {
        this.link = link;
    }


    public String getIconClass() {
        return iconClass;
    }


    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }


    public int getCount() {
        return count;
    }


    public void setCount(int count) {
        this.count = count;
    }


    public boolean isHasNew() {
        return hasNew;
    }


    public void setHasNew(boolean hasNew) {
        this.hasNew = hasNew;
    }
}

