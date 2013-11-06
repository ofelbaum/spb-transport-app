package com.emal.android.transport.spb;

import android.os.AsyncTask;
import android.util.Log;
import com.emal.android.transport.spb.portal.PortalClient;
import com.emal.android.transport.spb.utils.DrawVehicle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
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
 * Date: 5/18/13 5:06 AM
 */
public class VehicleTracker {
    private static final String TAG = VehicleTracker.class.getName();
    private AsyncTask syncTypesTask;
    private Map<String, AsyncTask> taskMap;
    private Set<VehicleType> vehicleTypes;
    private Set<String> vehicleIds;
    private VehicleSyncAdapter vehicleSyncAdapter;

    private PortalClient portalClient;
    private final GoogleMap googleMap;
    private Map<String, List<Marker>> markers;

    private static DefaultHttpClient CLIENT;

    public VehicleTracker(VehicleSyncAdapter vehicleSyncAdapter, PortalClient portalClient, GoogleMap googleMap, Map<String, List<Marker>> markers) {
        this.vehicleSyncAdapter = vehicleSyncAdapter;
        this.portalClient = portalClient;
        this.googleMap = googleMap;
        this.markers = markers;
        vehicleTypes = new HashSet<VehicleType>();
        vehicleIds = new HashSet<String>();
        taskMap = new HashMap<String, AsyncTask>();
    }

    public boolean startTrack(VehicleType vehicleType) {
        return vehicleTypes.add(vehicleType);
    }

    public boolean startTrack(String vehicleId) {
        return vehicleIds.add(vehicleId);
    }

    public void stopTrackAllIds() {
        vehicleIds.clear();
        stopAllTasks();
    }

    public void syncVehicles() {
        syncVehicles(false);
    }

    public void syncVehicles(boolean clearBeforeUpdate) {
        if (syncTypesTask != null && !AsyncTask.Status.FINISHED.equals(syncTypesTask.getStatus())) {
            Log.d(TAG, "Reschedule vehicleTypes");
            syncTypesTask.cancel(true);
        }
        if (!vehicleTypes.isEmpty() && vehicleIds.isEmpty()) {
            syncTypesTask = new SyncVehiclePositionTask(vehicleSyncAdapter, vehicleTypes, clearBeforeUpdate, getHttpClient());
            syncTypesTask.execute();
        }

        for (String vId : vehicleIds) {
            AsyncTask value = taskMap.get(vId);
            if (value != null && !AsyncTask.Status.FINISHED.equals(value.getStatus())) {
                value.cancel(true);
            }
            value = new DrawVehicle(vId, portalClient, googleMap, markers, vehicleSyncAdapter);
            value.execute();
            taskMap.put(vId, value);

        }
    }

    private static HttpClient getHttpClient() {
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

    public void stopTrackAll() {
        vehicleSyncAdapter.clearOverlay();
        vehicleTypes.clear();
        if (syncTypesTask != null && !AsyncTask.Status.FINISHED.equals(syncTypesTask.getStatus())) {
            syncTypesTask.cancel(true);
        }

        stopAllTasks();
    }

    private void stopAllTasks() {
        for (Map.Entry<String, AsyncTask> task : taskMap.entrySet()) {
            AsyncTask value = task.getValue();
            if (value != null && !AsyncTask.Status.FINISHED.equals(value.getStatus())) {
                value.cancel(true);
            }

            List<Marker> markers1 = markers.get(task.getKey());
            if (markers1 == null || markers1.isEmpty()) {
                return;
            }
            for(Marker marker : markers1) {
                marker.remove();
            }
        }
    }
}
