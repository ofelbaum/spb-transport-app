package com.emal.android;

import android.util.Pair;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/11/12 10:06 PM
 */
public class GeoConverter {
    /**
     * Converts given lat/lon in WGS84 Datum to XY in Spherical Mercator EPSG:900913
     */
    public static Pair<Double, Double> fromLatLonToMeters(double lat, double lon) {


        double originShift = 2 * Math.PI * 6378137 / 2.0;
        Double mx = lon * originShift / 180.0;
        Double my = Math.log(Math.tan((90 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0);

        my = my * originShift / 180.0;
        return new Pair<Double, Double>(mx, my);
    }
}
