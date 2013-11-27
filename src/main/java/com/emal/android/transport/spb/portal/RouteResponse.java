package com.emal.android.transport.spb.portal;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class RouteResponse {
    private static final String COMMA = ",";

    @JsonProperty(value = "sEcho")
    private int echo;

    private List<Route> data;

    @JsonProperty(value = "sColumns")
    private List<String> columnsNames;

    @JsonProperty(value = "iTotalRecords")
    private int iTotalRecords;

    @JsonProperty(value = "iTotalDisplayRecords")
    private int iTotalDisplayRecords;

    public String getColumnsNames() {
        StringBuilder sb = new StringBuilder();
        for (String img : columnsNames) {
            if (sb.length() > 0) {
                sb.append(COMMA);
            }
            sb.append(img);
        }
        return sb.toString();
    }

    public void setColumnsNames(String sColumns) {
        this.columnsNames = Arrays.asList(sColumns.split(COMMA));
    }


    public List<Route> getRoutes() {
        return data;
    }

    @JsonProperty(value = "aaData")
    public void setRoutesData(List<List> data) {

        this.data = new ArrayList<Route>(data.size());
        for (int i = 0; i < data.size(); i++) {
            List routeItem = data.get(i);

            Map<String, String> map = (Map) routeItem.get(1);
            String systemName = map.get("systemName");
            Route route = Route.RouteBuilder.getInstance().id(String.valueOf(routeItem.get(0)))
                    .transportType(systemName)
                    .routeNumber((String) routeItem.get(2))
                    .name((String) routeItem.get(3))
                    .urban((Boolean) routeItem.get(4))
                    .poiStart(routeItem.get(5))
                    .poiFinish(routeItem.get(6))
//                    .cost((Double) routeItem.get(7))
//                    .forDisabled((Boolean) routeItem.get(8))
//                    .scheduleLinkColumn(routeItem.get(9))
//                    .mapLinkColumn(routeItem.get(10))
                    .build();

            this.data.add(route);
        }
    }
}
