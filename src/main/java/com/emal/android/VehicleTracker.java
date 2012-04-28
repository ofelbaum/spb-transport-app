package com.emal.android;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
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
    private Map<Overlay, Bitmap> overlayBitmapMap = new HashMap<Overlay, Bitmap>();

    public VehicleTracker(MapView mapView) {
        this.mapView = mapView;
    }

    public MapView getMapView() {
        return mapView;
    }

    public void track(Vehicle vehicle) {
        track(vehicle, false);
    }

    public void track(Vehicle vehicle, boolean clearBeforeUpdate) {
        UpdateOverlayItemAsyncTask task = taskMap.get(vehicle);
        if (task != null) {
            if (!AsyncTask.Status.FINISHED.equals(task.getStatus())) {
                Log.d(TAG, "Reschedule " + vehicle);
                task.cancel(true);
            }
        }
        task = new UpdateOverlayItemAsyncTask(vehicle, this, clearBeforeUpdate);
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
        syncAll(false);
    }

    public void syncAll(boolean clearBeforeUpdate) {
        for (Vehicle vehicle : taskMap.keySet()) {
            track(vehicle, clearBeforeUpdate);
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

    public void addBitmap(Overlay overlay, Bitmap bitmap) {
        overlayBitmapMap.put(overlay, bitmap);
        Log.d(TAG, "Add bitmap " + overlay);
    }

    public void removeBitmap(Overlay overlay) {
        String oId = overlay.toString();
        Bitmap bitmap = overlayBitmapMap.remove(overlay);
        if (bitmap != null) {
            bitmap.recycle();
            Log.d(TAG, "Recycle bitmap " + oId);
        }
    }
}
