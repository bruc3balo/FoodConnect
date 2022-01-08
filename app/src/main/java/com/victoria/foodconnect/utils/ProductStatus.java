package com.victoria.foodconnect.utils;

public enum ProductStatus {

    NOT_YET(0,"Not yet"),
    ON_THE_WAY(1,"On the way"),
    FAILED(2,"Failed"),
    COLLECTED(3,"Collected");


    private final int code;
    private final String description;

    ProductStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }


    public int getCode () {
        return this.code;
    }

    public String getDescription () {
        return this.description;
    }
}
