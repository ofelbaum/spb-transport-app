package com.emal.android.transport.spb;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/23/13 1:54 AM
 */
public enum MapProviderType {
    GMAPSV1, GMAPSV2, OSM;

    public static MapProviderType getByValue(String value) {
        for (MapProviderType type: MapProviderType.values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
