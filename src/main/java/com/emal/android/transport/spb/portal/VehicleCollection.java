package com.emal.android.transport.spb.portal;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
@JsonIgnoreProperties(value = {"type"})
public class VehicleCollection {
    @JsonProperty(value = "features")
    List<Vehicle> list;

    public List<Vehicle> getList() {
        return list;
    }

    public void setList(List<Vehicle> list) {
        this.list = list;
    }
}
