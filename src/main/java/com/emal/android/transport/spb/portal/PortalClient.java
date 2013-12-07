package com.emal.android.transport.spb.portal;

import com.emal.android.transport.spb.VehicleTracker;
import com.emal.android.transport.spb.VehicleType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.*;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class PortalClient {
    private static final String SCOPE_PARAM_PATTERN = "(?:.*(scope:.\"))([a-zA-Z0-9+/]*)(?:\".*)";
    private static final String TAG = PortalClient.class.getName();
    private static String GET_ROUTES_LIST_QUERY = "http://transport.orgp.spb.ru/Portal/transport/routes/list";
    private static String GET_STOPS_LIST_QUERY = "http://transport.orgp.spb.ru/Portal/transport/stops/list";
    private static String GET_ROUTE_QUERY = "http://transport.orgp.spb.ru/Portal/transport/route/%s";
    private static String GET_ROUTE_INFO_QUERY = "http://transport.orgp.spb.ru/Portal/transport/mapx/innerRouteVehicle?ROUTE={1}&SCOPE={2}&SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature&SRS=EPSG%3A900913&LAYERS=&WHEELCHAIRONLY=false&_OLSALT=0.6481046043336391&BBOX={3}";
//    private static String GET_ROUTE_INFO_QUERY2 = "http://transport.orgp.spb.ru/Portal/transport/map/poi?ROUTE={1}&REQUEST=GetFeature&_=1385406049565";
//    http://transport.orgp.spb.ru/Portal/transport/route/1504/stops/direct
//    http://transport.orgp.spb.ru/Portal/transport/route/1504/stops/return

    public static final String BBOX = "3236938.2945543,8256172.549016,3492103.4398571,8480968.3457368";

    private DefaultHttpClient httpClient;
    private String scope;

    public PortalClient() {
        BasicHttpParams params = new BasicHttpParams();
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        httpClient = new DefaultHttpClient(cm, params);
    }

    //TODO
    public HttpClient getHttpClient() {
        DefaultHttpClient httpClient = null;
        if (httpClient == null) {
            synchronized (VehicleTracker.class) {
                if (httpClient == null) {
                    SchemeRegistry schemeRegistry = new SchemeRegistry();
                    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

                    HttpParams params = new BasicHttpParams();
                    params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 6);
                    params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(3));
                    params.setParameter(CoreConnectionPNames.TCP_NODELAY, true);
                    params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
                    int timeoutConnection = 5000;
                    HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
                    int timeoutSocket = 5000;
                    HttpConnectionParams.setSoTimeout(params, timeoutSocket);

                    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

                    ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
                    httpClient = new DefaultHttpClient(cm, params);
                    ((DefaultHttpClient)httpClient).setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
                }
            }
        }
        return httpClient;
    }


    public VehicleCollection getRouteData(String route, String bbox) throws IOException {
        HttpResponse httpResponse;
        String content;
        if (scope == null || scope.length() == 0) {
            HttpGet httppost = new HttpGet(String.format(GET_ROUTE_QUERY, route));
            httpResponse = httpClient.execute(httppost);
            content = readResponse(httpResponse);

            Pattern p = Pattern.compile(SCOPE_PARAM_PATTERN);
            Matcher m = p.matcher(content);

            while (m.find()) {
                scope = URLEncoder.encode(m.group(2), "UTF-8");
                break;
            }

            if (scope == null || scope.length() == 0) {
                throw new IllegalStateException("SCOPE is not defined");
            }
        }

        String format = GET_ROUTE_INFO_QUERY.replace("{1}", route).replace("{2}", scope).replace("{3}", bbox);
        HttpGet httpGet = new HttpGet(format);

        httpResponse = httpClient.execute(httpGet);
        content = readResponse(httpResponse);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(content, VehicleCollection.class);
    }

    private static String readResponse(HttpResponse httpResponse) {
        StringBuffer respBuf = new StringBuffer();

        HttpEntity httpEntity = httpResponse.getEntity();
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                respBuf.append(strLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return respBuf.toString();
    }


    public List<Route> findAllRoutes() throws IOException {
        return findRoutes("");
    }

    public List<Route> findRoutes(String s) throws IOException {
        return findRoutes(s, VehicleType.values());
    }

    public Route findRoute(String id, String number) throws IOException {
        List<Route> routes = findRoutes(number);
        for (Route route : routes) {
            if (id.equals(route.getId())) {
                return route;
            }
        }
        return null;
    }

    public List<Route> findRoutes(String s, VehicleType... types) throws IOException {
        HttpPost httppost = new HttpPost(GET_ROUTES_LIST_QUERY);
        httppost.setEntity(getRoutesFormParams(s, types));

        HttpResponse httpResponse = httpClient.execute(httppost);
        String content = readResponse(httpResponse);

        ObjectMapper mapper = new ObjectMapper();
        RouteResponse routeResponse = mapper.readValue(content, RouteResponse.class);
        return routeResponse.getRoutes();
    }

    public List<Stop> getStopsList() throws IOException {
        HttpPost httppost = new HttpPost(GET_STOPS_LIST_QUERY);
        httppost.setEntity(getStopsFormParams());

        HttpResponse httpResponse = httpClient.execute(httppost);
        String content = readResponse(httpResponse);

        ObjectMapper mapper = new ObjectMapper();
        StopResponse stopResponse = mapper.readValue(content, StopResponse.class);
        return stopResponse.getAaData();
    }

    public Map<String, List<Route>> getRoutesIndex(List<Route> routeList) {
        Map<String, List<Route>> routesIndex = new HashMap<String, List<Route>>();

        for (Route route : routeList) {
            String routeNumber = route.getRouteNumber();

            List<Route> routes = routesIndex.get(routeNumber);
            if (routes == null) {
                routes = new ArrayList<Route>();
                routesIndex.put(routeNumber, routes);
            }
            routes.add(route);
        }
        return routesIndex;
    }

    private static UrlEncodedFormEntity getRoutesFormParams(String routerNumber, VehicleType... types) throws UnsupportedEncodingException {
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("sEcho", "2"));
        formparams.add(new BasicNameValuePair("iColumns", "10"));
        formparams.add(new BasicNameValuePair("sColumns", "id,transportType,routeNumber,name,urban,poiStart,poiFinish,cost,scheduleLinkColumn,mapLinkColumn"));
        formparams.add(new BasicNameValuePair("iDisplayStart", "0"));
        formparams.add(new BasicNameValuePair("iDisplayLength", String.valueOf(Integer.MAX_VALUE)));
        formparams.add(new BasicNameValuePair("sNames", "id,transportType,routeNumber,name,urban,poiStart,poiFinish,cost,scheduleLinkColumn,mapLinkColumn"));
        formparams.add(new BasicNameValuePair("iSortingCols", "1"));
        formparams.add(new BasicNameValuePair("iSortCol_0", "2"));
        formparams.add(new BasicNameValuePair("sSortDir_0", "asc"));
        formparams.add(new BasicNameValuePair("bSortable_0", "true"));
        formparams.add(new BasicNameValuePair("bSortable_1", "true"));
        formparams.add(new BasicNameValuePair("bSortable_2", "true"));
        formparams.add(new BasicNameValuePair("bSortable_3", "true"));
        formparams.add(new BasicNameValuePair("bSortable_4", "true"));
        formparams.add(new BasicNameValuePair("bSortable_5", "true"));
        formparams.add(new BasicNameValuePair("bSortable_6", "true"));
        formparams.add(new BasicNameValuePair("bSortable_7", "true"));
        formparams.add(new BasicNameValuePair("bSortable_8", "false"));
        formparams.add(new BasicNameValuePair("bSortable_9", "false"));

        for (VehicleType type : types) {
            formparams.add(new BasicNameValuePair("transport-type", type.getId()));
        }

        if (routerNumber != null && routerNumber.length() > 0) {
            formparams.add(new BasicNameValuePair("route-number", routerNumber));
        }

        return new UrlEncodedFormEntity(formparams, "UTF-8");
    }

    private static UrlEncodedFormEntity getStopsFormParams() throws UnsupportedEncodingException {

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();

        formparams.add(new BasicNameValuePair("sEcho", "3"));
        formparams.add(new BasicNameValuePair("iColumns", "7"));
        formparams.add(new BasicNameValuePair("sColumns", "id,transportType,name,nearestStreets,lonLat"));
        formparams.add(new BasicNameValuePair("iDisplayStart", "0"));
        formparams.add(new BasicNameValuePair("iDisplayLength", String.valueOf(Integer.MAX_VALUE)));
//        formparams.add(new BasicNameValuePair("iDisplayLength", "50"));

        formparams.add(new BasicNameValuePair("sNames", "id,transportType,name,nearestStreets,lonLat"));
        formparams.add(new BasicNameValuePair("iSortingCols", "1"));
        formparams.add(new BasicNameValuePair("iSortCol_0", "0"));
        formparams.add(new BasicNameValuePair("sSortDir_0", "asc"));
        formparams.add(new BasicNameValuePair("bSortable_0", "true"));
        formparams.add(new BasicNameValuePair("bSortable_1", "true"));
        formparams.add(new BasicNameValuePair("bSortable_2", "true"));
        formparams.add(new BasicNameValuePair("bSortable_3", "false"));
        formparams.add(new BasicNameValuePair("bSortable_4", "true"));
        formparams.add(new BasicNameValuePair("bSortable_5", "false"));
        formparams.add(new BasicNameValuePair("bSortable_6", "false"));
        formparams.add(new BasicNameValuePair("transport-type", "0"));
        formparams.add(new BasicNameValuePair("transport-type", "46"));
        formparams.add(new BasicNameValuePair("transport-type", "2"));
        formparams.add(new BasicNameValuePair("transport-type", "1"));


        return new UrlEncodedFormEntity(formparams, "UTF-8");
    }
}
