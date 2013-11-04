package com.emal.android.transport.spb.portal;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
@JsonIgnoreProperties(value = {"type"})
public class Vehicle {

    @JsonProperty(value = "properties")
    private VehicleProps properties;

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "geometry")
    private Geometry geometry;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public VehicleProps getProperties() {
        return properties;
    }

    public void setProperties(VehicleProps properties) {
        this.properties = properties;
    }
}
