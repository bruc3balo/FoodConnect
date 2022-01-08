package com.victoria.foodconnect.utils;

public enum DistributionStatus {

    ACCEPTED(0, "Accepted by transporter"),
    COLLECTING_ITEMS(1, "Collecting items"),
    ON_THE_WAY(2, "On the way to beneficiary"),
    ARRIVED(3, "Arrived"),
    COMPLETE(4, "Complete"),
    DNF(5, "Did not finish");

    private final int code;
    private final String description;

    DistributionStatus(int code, String description) {
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
