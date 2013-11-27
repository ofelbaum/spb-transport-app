package com.emal.android.transport.spb;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/12/12 2:31 AM
 */
public enum VehicleType {
    BUS("vehicle_bus", "0"),
    TROLLEY("vehicle_trolley", "1"),
    TRAM("vehicle_tram", "2"),
    SHIP("vehicle_ship", "46");

    private String code;
    private String id;

    private VehicleType(String code, String id) {
        this.code = code;
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public String getId() {
        return id;
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
