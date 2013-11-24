package com.emal.android.transport.spb.portal;

import com.emal.android.transport.spb.VehicleType;

import java.io.Serializable;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */

public class Route implements Serializable{

    private int id;
    private VehicleType transportType;
    private String routeNumber;
    private String name;
    private boolean urban;
    private Object poiStart;
    private Object poiFinish;
    private double cost;
    private boolean forDisabled;
    private Object scheduleLinkColumn;
    private Object mapLinkColumn;

    public int getId() {
        return id;
    }

    public VehicleType getTransportType() {
        return transportType;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public String getName() {
        return name;
    }

    public boolean isUrban() {
        return urban;
    }

    public Object getPoiStart() {
        return poiStart;
    }

    public Object getPoiFinish() {
        return poiFinish;
    }

    public double getCost() {
        return cost;
    }

    public boolean isForDisabled() {
        return forDisabled;
    }

    public Object getScheduleLinkColumn() {
        return scheduleLinkColumn;
    }

    public Object getMapLinkColumn() {
        return mapLinkColumn;
    }

    private Route(RouteBuilder routeBuilder) {
        this.id = routeBuilder.id;
        this.transportType = routeBuilder.transportType;
        this.routeNumber = routeBuilder.routeNumber;
        this.name = routeBuilder.name;
        this.urban = routeBuilder.urban;
        this.poiStart = routeBuilder.poiStart;
        this.poiFinish = routeBuilder.poiFinish;
        this.cost = routeBuilder.cost;
        this.forDisabled = routeBuilder.forDisabled;
        this.scheduleLinkColumn = routeBuilder.scheduleLinkColumn;
        this.mapLinkColumn = routeBuilder.mapLinkColumn;
    }

    public static class RouteBuilder {
        private int id;
        private VehicleType transportType;
        private String routeNumber;
        private String name;
        private boolean urban;
        private Object poiStart;
        private Object poiFinish;
        private double cost;
        private boolean forDisabled;
        private Object scheduleLinkColumn;
        private Object mapLinkColumn;

        public static RouteBuilder getInstance() {
            return new RouteBuilder();
        }

        public RouteBuilder id(int id) {
            this.id = id;
            return this;
        }

        public RouteBuilder transportType(String transportType) {
            this.transportType = VehicleType.getType(transportType);
            return this;
        }

        public RouteBuilder routeNumber(String routeNumber) {
            this.routeNumber = routeNumber;
            return this;
        }

        public RouteBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RouteBuilder urban(boolean urban) {
            this.urban = urban;
            return this;
        }

        public RouteBuilder poiStart(Object poiStart) {
            this.poiStart = poiStart;
            return this;
        }

        public RouteBuilder poiFinish(Object poiFinish) {
            this.poiFinish = poiFinish;
            return this;
        }

        public RouteBuilder cost(double cost) {
            this.cost = cost;
            return this;
        }

        public RouteBuilder forDisabled(boolean forDisabled) {
            this.forDisabled = forDisabled;
            return this;
        }

        public RouteBuilder scheduleLinkColumn(Object scheduleLinkColumn) {
            this.scheduleLinkColumn = scheduleLinkColumn;
            return this;
        }

        public RouteBuilder mapLinkColumn(Object mapLinkColumn) {
            this.mapLinkColumn = mapLinkColumn;
            return this;
        }

        public Route build() {
            return new Route(this);
        }
    }
}
