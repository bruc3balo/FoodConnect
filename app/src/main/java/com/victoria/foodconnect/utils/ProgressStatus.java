package com.victoria.foodconnect.utils;

public enum ProgressStatus {

    NEW(0,"New"),
    PROGRESS(1,"Progress"),
    FAILED(2,"Failed"),
    COMPLETE(3,"Complete");


    private final int code;
    private final String description;

    ProgressStatus(int code, String description) {
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
