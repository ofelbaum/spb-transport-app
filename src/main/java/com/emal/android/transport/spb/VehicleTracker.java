package com.emal.android.transport.spb;

import android.os.AsyncTask;
import android.util.Log;
import com.emal.android.transport.spb.portal.PortalClient;
import com.emal.android.transport.spb.portal.Route;
import com.emal.android.transport.spb.task.DrawVehicleTask;
import com.emal.android.transport.spb.task.SyncVehiclePositionTask;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/18/13 5:06 AM
 */
public class VehicleTracker {
    private static final String TAG = VehicleTracker.class.getName();
    private AsyncTask syncTypesTask;
    private Map<Route, AsyncTask> routeTaskMap;
    private Set<VehicleType> vehicleTypes;
    private VehicleSyncAdapter vehicleSyncAdapter;

    private PortalClient portalClient;
    private final GoogleMap googleMap;
    private Map<String, Map<String, Marker>> markers;

    public VehicleTracker(VehicleSyncAdapter vehicleSyncAdapter,
                          PortalClient portalClient,
                          GoogleMap googleMap) {
        this.vehicleSyncAdapter = vehicleSyncAdapter;
        this.portalClient = portalClient;
        this.googleMap = googleMap;
        this.markers = new ConcurrentHashMap<String, Map<String, Marker>>();
        vehicleTypes = new HashSet<VehicleType>();
        routeTaskMap = new HashMap<Route, AsyncTask>();
    }

    public boolean startTrack(VehicleType vehicleType) {
        return vehicleTypes.add(vehicleType);
    }

    public void startTrack(Route route) {
        AsyncTask asyncTask = routeTaskMap.get(route);
        if (asyncTask == null) {
            routeTaskMap.put(route, null);
        }
    }

    public void stopTrackAllRoutes() {
        for (Map.Entry<Route, AsyncTask> task : routeTaskMap.entrySet()) {
            Route key = task.getKey();
            AsyncTask value = task.getValue();
            if (value != null && !AsyncTask.Status.FINISHED.equals(value.getStatus())) {
                value.cancel(true);
            }

            Map<String, Marker> markers1 = markers.get(key.getId());
            if (markers1 != null) {
                for(Marker marker : markers1.values()) {
                    marker.remove();
                }
            }
        }
        routeTaskMap.clear();
    }

    public void syncVehicles() {
        syncVehicles(false);
    }

    public void syncVehicles(boolean clearBeforeUpdate) {
        if (syncTypesTask != null && !AsyncTask.Status.FINISHED.equals(syncTypesTask.getStatus())) {
            Log.d(TAG, "Reschedule vehicleTypes");
            syncTypesTask.cancel(true);
        }
        if (!vehicleTypes.isEmpty() && routeTaskMap.isEmpty()) {
            Log.d(TAG, "Scheduling typed layout for types: " + vehicleTypes);
            syncTypesTask = new SyncVehiclePositionTask(vehicleSyncAdapter, vehicleTypes, clearBeforeUpdate, portalClient.getHttpClient());
            syncTypesTask.execute();
        }

        for (Route route : routeTaskMap.keySet()) {
            Log.d(TAG, "Scheduling route: " + route);
            String routeId = route.getId();
            AsyncTask value = routeTaskMap.get(routeId);
            if (value != null && !AsyncTask.Status.FINISHED.equals(value.getStatus())) {
                value.cancel(true);
            }
            Map<String, Marker> markersForRoute = markers.get(routeId);
            if (markersForRoute == null) {
                Log.d(TAG, "Add new map for markers for route: " + route);
                markersForRoute = new HashMap<String, Marker>();
                markers.put(routeId, markersForRoute);
            }
            value = new DrawVehicleTask(route, portalClient, googleMap, markersForRoute, vehicleSyncAdapter);
            value.execute();
            routeTaskMap.put(route, value);

        }
    }

    public void stopTracking() {
        vehicleSyncAdapter.clearOverlay();
        vehicleTypes.clear();
        if (syncTypesTask != null && !AsyncTask.Status.FINISHED.equals(syncTypesTask.getStatus())) {
            syncTypesTask.cancel(true);
        }

        stopTrackAllRoutes();
    }
}
