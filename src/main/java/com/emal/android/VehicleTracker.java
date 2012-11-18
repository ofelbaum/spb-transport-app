package com.emal.android;

import android.graphics.Bitmap;
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

import java.util.*;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/16/12 11:23 PM
 */
public class VehicleTracker {
    private static final String TAG = "VehicleTracker";
    private MapView mapView;
    private static DefaultHttpClient CLIENT;
    private UpdateOverlayItemAsyncTask task;
    private Set<Vehicle> vehicles;

    public VehicleTracker(MapView mapView) {
        this.mapView = mapView;
        vehicles = new HashSet<Vehicle>();
    }

    public MapView getMapView() {
        return mapView;
    }

    public boolean startTrack(Vehicle vehicle) {
        return vehicles.add(vehicle);
    }

    public boolean stopTrack(Vehicle vehicle) {
        return vehicles.remove(vehicle);
    }

    public void syncVehicles() {
        syncVehicles(false);
    }

    public void syncVehicles(boolean clearBeforeUpdate) {
        if (task != null) {
            if (!AsyncTask.Status.FINISHED.equals(task.getStatus())) {
                Log.d(TAG, "Reschedule vehicles");
                task.cancel(true);
            }
        }
        task = new UpdateOverlayItemAsyncTask(vehicles, this, clearBeforeUpdate);
        task.execute();
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
