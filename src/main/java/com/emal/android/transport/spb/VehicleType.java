package com.emal.android.transport.spb;

import android.graphics.Color;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/12/12 2:31 AM
 */
public enum VehicleType {
    BUS("vehicle_bus", "0", "A", false, Color.BLUE),
    TROLLEY("vehicle_trolley", "1", "Ш", true, Color.GREEN),
    TRAM("vehicle_tram", "2", "T", false, Color.RED),
    SHIP("vehicle_ship", "46", "S", false, Color.YELLOW);

    private String code;
    private String id;
    private String letter;
    private boolean upsideDown;
    private int color;

    private VehicleType(String code, String id, String letter, boolean upsideDown, int color) {
        this.code = code;
        this.id = id;
        this.letter = letter;
        this.color = color;
        this.upsideDown = upsideDown;
    }

    public String getCode() {
        return code;
    }

    public String getId() {
        return id;
    }

    public String getLetter() {
        return letter;
    }

    public int getColor() {
        return color;
    }

    public boolean isUpsideDown() {
        return upsideDown;
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
