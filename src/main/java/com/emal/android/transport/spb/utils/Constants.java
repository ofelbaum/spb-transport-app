package com.emal.android.transport.spb.utils;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/14/12 9:34 PM
 */
public final class Constants {
    public static final String URL_TEMPLATE = "http://transport.orgp.spb.ru/cgi-bin/mapserv?TRANSPARENT=TRUE&FORMAT=image%2Fpng&MAP=vehicle_typed.map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&SRS=EPSG%3A900913&_OLSALT=0.1508798657450825";
    public static final String URL_PARAMS = "&LAYERS=%s&BBOX=%s&WIDTH=%d&HEIGHT=%d";
    public static final String SHOW_BUS_FLAG = "SHOW_BUS_FLAG";
    public static final String SHOW_TROLLEY_FLAG = "SHOW_TROLLEY_FLAG";
    public static final String SHOW_TRAM_FLAG = "SHOW_TRAM_FLAG";
    public static final String SHOW_SHIP_FLAG = "SHOW_SHIP_FLAG";
    public static final String SAT_VIEW_FLAG = "SAT_VIEW_FLAG";
    public static final String MAP_PROVIDER_TYPE_FLAG = "MAP_PROVIDER_TYPE_FLAG";
    public static final String SYNC_TIME_FLAG = "SYNC_TIME_FLAG";
    public static final String ZOOM_FLAG = "ZOOM_FLAG";
    public static final String HOME_LOC_LONG_FLAG = "HOME_LOCATION_LONG_FLAG";
    public static final String HOME_LOC_LAT_FLAG = "HOME_LOCATION_LAT_FLAG";
    public static final String LAST_LOC_LONG_FLAG = "LAST_LOC_LONG_FLAG";
    public static final String LAST_LOC_LAT_FLAG = "LAST_LOC_LAT_FLAG";
    public static final String APP_SHARED_SOURCE = "SPB_TRANSPORT_APP";
    public static final int DEFAULT_ZOOM_LEVEL = 15;
    public static final int MS_IN_SEC = 1000;
    public static final int DEFAULT_SYNC_MS = 10000;
}
