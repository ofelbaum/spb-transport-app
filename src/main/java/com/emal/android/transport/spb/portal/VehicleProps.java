package com.emal.android.transport.spb.portal;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class VehicleProps {
    @JsonProperty(value = "stateNumber")
    private String stateNumber;

    @JsonProperty(value = "transportTypeId")
    private String transportTypeId;

    @JsonProperty(value = "wheelchair")
    private boolean wheelchair;

    @JsonProperty(value = "direction")
    private int direction;

    @JsonProperty(value = "velocity")
    private int velocity;

    @JsonProperty(value = "label")
    private int label;

    public String getStateNumber() {
        return stateNumber;
    }

    public void setStateNumber(String stateNumber) {
        this.stateNumber = stateNumber;
    }

    public String getTransportTypeId() {
        return transportTypeId;
    }

    public void setTransportTypeId(String transportTypeId) {
        this.transportTypeId = transportTypeId;
    }

    public boolean isWheelchair() {
        return wheelchair;
    }

    public void setWheelchair(boolean wheelchair) {
        this.wheelchair = wheelchair;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public String getDisplayValue() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(stateNumber).append(" | ");
        if (!stateNumber.equals(String.valueOf(label))) {
            buffer.append(label);
            buffer.append(" | ");
        }
        buffer.append(velocity).append("km/h");

        return buffer.toString();
    }
}
