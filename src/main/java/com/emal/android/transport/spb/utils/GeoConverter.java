package com.emal.android.transport.spb.utils;

import android.location.Address;
import android.util.Pair;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.maps.MapView;

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

    /**
     * Get bbox parameter from map view
     * @param mapView
     * @return
     */
    public static String calculateBBox(MapView mapView) {
        int latitudeSpan = mapView.getLatitudeSpan();
        int longitudeSpan = mapView.getLongitudeSpan();
        int latitudeE6 = mapView.getMapCenter().getLatitudeE6();
        int longitudeE6 = mapView.getMapCenter().getLongitudeE6();

        int x1 = latitudeE6 - latitudeSpan / 2;
        int y1 = longitudeE6 - longitudeSpan / 2;
        int x2 = latitudeE6 + latitudeSpan / 2;
        int y2 = longitudeE6 + longitudeSpan / 2;


        Pair<Double, Double> p1 = GeoConverter.fromLatLonToMeters(x1 / 1E6, y1 / 1E6);
        Pair<Double, Double> p2 = GeoConverter.fromLatLonToMeters(x2 / 1E6, y2 / 1E6);

        return p1.first.toString() + "," + p1.second.toString() + "," + p2.first.toString() + "," + p2.second.toString();
    }

    public static String convert(Address address) {
        StringBuffer buffer = new StringBuffer();
        int maxAddressLineIndex = address.getMaxAddressLineIndex();
        for (int i = 0; i <= maxAddressLineIndex; i++) {
            buffer.append(address.getAddressLine(i).trim());
            if (i < maxAddressLineIndex) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }

    public static String calculateBBox(LatLngBounds latLngBounds) {
        LatLng southwest = latLngBounds.southwest;
        LatLng northeast = latLngBounds.northeast;
        Pair<Double, Double> p1 = GeoConverter.fromLatLonToMeters(southwest.latitude, southwest.longitude);
        Pair<Double, Double> p2 = GeoConverter.fromLatLonToMeters(northeast.latitude, northeast.longitude);

        return p1.first.toString() + "," + p1.second.toString() + "," + p2.first.toString() + "," + p2.second.toString();
    }
}
