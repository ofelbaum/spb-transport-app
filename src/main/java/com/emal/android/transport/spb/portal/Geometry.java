package com.emal.android.transport.spb.portal;

import com.emal.android.transport.spb.utils.GeoConverter;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class Geometry {
    @JsonProperty(value = "type")
    private String type;


    private Double longtitude;

    private Double latitude;

    public Geometry() {
    }

    public Geometry(Double latitude, Double longtitude) {
        setCoordinates(Arrays.asList(longtitude, latitude));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty(value = "coordinates")
    public void setCoordinates(List<Double> coordinates) {
        Pair<Double,Double> doubleDoublePair = GeoConverter.convertMetersToLatLon(coordinates.get(0), coordinates.get(1));
        this.latitude = doubleDoublePair.getLeft();
        this.longtitude = doubleDoublePair.getRight();
    }

    public Double getLongtitude() {
        return longtitude;
    }

    public Double getLatitude() {
        return latitude;
    }
}
