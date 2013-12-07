package com.emal.android.transport.spb.model;

import com.emal.android.transport.spb.portal.Route;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class RoutesStorage {
    private List<Route> routeList;
    private Map<String, List<Route>> index = new ConcurrentHashMap<String, List<Route>>(100);

    public void setRouteList(List<Route> routeList) {
        this.routeList = routeList;
        buildIndex();
    }

    private void buildIndex() {
        index.clear();
        for (Route route : routeList) {
            String routeNumber = route.getRouteNumber();
            List<Route> routes = index.get(routeNumber);
            if (routes == null) {
                routes = new ArrayList<Route>();
                index.put(routeNumber, routes);
            }
            routes.add(route);
        }
    }

    public List<Route> find(String key) {
        if (key == null || key.trim().length() == 0) {
            return routeList != null ? routeList : Collections.EMPTY_LIST;
        }
        List<Route> routes = index.get(key);
        return routes != null ? routes : Collections.EMPTY_LIST;
    }
}
