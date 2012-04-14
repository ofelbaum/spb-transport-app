package com.emal.android;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/14/12 9:34 PM
 */
public final class Constants {
    public static final String URL_TEMPLATE = "http://transport.orgp.spb.ru/cgi-bin/mapserv?TRANSPARENT=TRUE&FORMAT=image%2Fpng&MAP=vehicle_typed.map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&SRS=EPSG%3A900913&_OLSALT=0.1508798657450825";
    public static final String URL_PARAMS = "&LAYERS=%s&BBOX=%s&WIDTH=%d&HEIGHT=%d";
}
