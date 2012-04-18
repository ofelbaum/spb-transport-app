package com.emal.android;

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.maps.MapView;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.*;

import java.util.HashMap;
import java.util.Map;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/16/12 11:23 PM
 */
public class VehicleTracker {
    private static final String TAG = "VehicleTracker";
    private MapView mapView;
    private static DefaultHttpClient CLIENT;
    private Map<Vehicle, UpdateOverlayItemAsyncTask> taskMap = new HashMap<Vehicle, UpdateOverlayItemAsyncTask>();

    public VehicleTracker(MapView mapView) {
        this.mapView = mapView;
    }

    public void track(Vehicle vehicle) {
        UpdateOverlayItemAsyncTask task = taskMap.get(vehicle);
        if (task != null) {
            if (!AsyncTask.Status.FINISHED.equals(task.getStatus())) {
                Log.d(TAG, "Reschedule " + vehicle);
                task.cancel(true);
            }
        }
        task = new UpdateOverlayItemAsyncTask(mapView, vehicle, this);
        taskMap.put(vehicle, task);
        task.execute();
    }

    public void untrack(Vehicle vehicle) {
        UpdateOverlayItemAsyncTask task = taskMap.get(vehicle);
        if (task != null) {
            task.cancel(true);
            taskMap.remove(vehicle);
        }
    }

    public void syncAll() {
        for (Vehicle vehicle : taskMap.keySet()) {
            track(vehicle);
        }
    }

    public HttpClient getHttpClient() {
            if (CLIENT == null) {
                synchronized (VehicleTracker.class) {
                    if (CLIENT == null) {
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
                        CLIENT = new DefaultHttpClient(cm, params);
                        CLIENT.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
                    }
                }
            }
            return CLIENT;
        }
}
