package com.emal.android.transport.spb;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/12/12 2:31 AM
 */
public enum VehicleType {
    BUS("vehicle_bus"),
    TROLLEY("vehicle_trolley"),
    TRAM("vehicle_tram"),
    SHIP("vehicle_ship");

    private String code;

    private VehicleType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static VehicleType getType(String value) {
        if ("bus".equals(value)) {
            return BUS;
        }
        if ("tram".equals(value)) {
            return TRAM;
        }
        if ("trolley".equals(value)) {
            return TROLLEY;
        }
        if ("ship".equals(value)) {
            return SHIP;
        }
        throw new IllegalStateException("Wrong vehicle type");
    }
}
