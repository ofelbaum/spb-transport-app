package com.emal.android.transport.spb.portal;


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class StopResponse {
    @JsonProperty(value = "sEcho")
    private int sEcho;

    @JsonProperty(value = "sColumns")
    private String sColumns;

    @JsonProperty(value = "aaData")
    private List<Stop> aaData;

    @JsonProperty(value = "iTotalRecords")
    private int iTotalRecords;

    @JsonProperty(value = "iTotalDisplayRecords")
    private int iTotalDisplayRecords;

    public int getsEcho() {
        return sEcho;
    }

    public void setsEcho(int sEcho) {
        this.sEcho = sEcho;
    }

    public String getsColumns() {
        return sColumns;
    }

    public void setsColumns(String sColumns) {
        this.sColumns = sColumns;
    }

    public List<Stop> getAaData() {
        return aaData;
    }

    public void setAaData(List<List> aaData) {
        this.aaData = new ArrayList<Stop>(aaData.size());
        for (List list : aaData) {
            Integer id = (Integer) list.get(0);
            Stop.Type type = Stop.Type.getById((Integer) ((Map) (list.get(1))).get("id"));
            String name = (String) list.get(2);
            String nearestStreets = (String) list.get(3);

            Map stopPositionMap = (Map) (list.get(4));
            double lon = (Double) (stopPositionMap.get("lon"));
            double lat = (Double) (stopPositionMap.get("lat"));
            Geometry geometry = new Geometry(lat, lon);

            Stop stop = Stop.StopBuilder.getInstance()
                    .id(id)
                    .type(type)
                    .name(name)
                    .nearestStreets(nearestStreets)
                    .lonLat(geometry)
                    .build();
            this.aaData.add(stop);
        }
    }

    public int getiTotalRecords() {
        return iTotalRecords;
    }

    public void setiTotalRecords(int iTotalRecords) {
        this.iTotalRecords = iTotalRecords;
    }

    public int getiTotalDisplayRecords() {
        return iTotalDisplayRecords;
    }

    public void setiTotalDisplayRecords(int iTotalDisplayRecords) {
        this.iTotalDisplayRecords = iTotalDisplayRecords;
    }
}
