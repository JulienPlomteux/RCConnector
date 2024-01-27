package com.plomteux.rcconnector.entity;

public enum RoomType {
    INSIDE("inside"),
    OCEANVIEW("oceanView"),
    BALCONY("balcony");


    private final String fieldName;

    RoomType(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return this.fieldName;
    }
}