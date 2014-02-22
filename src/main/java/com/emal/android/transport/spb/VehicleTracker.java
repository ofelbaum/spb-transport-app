package com.emal.android.transport.spb;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.emal.android.transport.spb.portal.Route;
import com.emal.android.transport.spb.task.DrawVehicleTask;
import com.emal.android.transport.spb.task.SyncVehiclePositionTask;
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

    private Map<String, Map<String, Marker>> markers;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private TimerTask timerTask;

    private class MapUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            //TODO
            int syncTime = vehicleSyncAdapter.getSyncTime();
            Log.d(TAG, "START Timer Update " + Thread.currentThread().getName() + " with time " + syncTime);
            syncVehicles();
            mHandler.postDelayed(this, syncTime);
        }
    }

    public void startSync() {
        Log.d(TAG, "startSync");
        vehicleSyncAdapter.setBBox();
        if (timerTask != null) {
            timerTask.cancel();
        } else {
            timerTask = new MapUpdateTimerTask();
        }
        mHandler.removeCallbacks(timerTask);
        mHandler.postDelayed(timerTask, 0);
    }

    public VehicleTracker(VehicleSyncAdapter vehicleSyncAdapter) {
        this.vehicleSyncAdapter = vehicleSyncAdapter;
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

    public ArrayList<Route> getTracked() {
        return new ArrayList<Route>(routeTaskMap.keySet());
    }

    private void stopTrackAllRoutes() {
        Log.d(TAG, "stopTrackAllRoutes <<");
        for (Map.Entry<Route, AsyncTask> task : routeTaskMap.entrySet()) {
            Route key = task.getKey();
            AsyncTask value = task.getValue();
            if (value != null && !AsyncTask.Status.FINISHED.equals(value.getStatus())) {
                value.cancel(true);
            }

            Map<String, Marker> markers1 = markers.get(key.getId());
            if (markers1 != null) {
                for (Marker marker : markers1.values()) {
                    marker.remove();
                }
            }
        }
        routeTaskMap.clear();
        Log.d(TAG, "stopTrackAllRoutes >>");
    }

    public void syncVehicles() {
        syncVehicles(false);
    }

    public void syncVehicles(boolean clearBeforeUpdate) {
        Log.d(TAG, "syncVehicles <<");
        if (syncTypesTask != null && !AsyncTask.Status.FINISHED.equals(syncTypesTask.getStatus())) {
            Log.d(TAG, "Reschedule vehicleTypes");
            syncTypesTask.cancel(true);
        }
        if (!vehicleTypes.isEmpty() && routeTaskMap.isEmpty()) {
            Log.d(TAG, "Scheduling typed layout for types: " + vehicleTypes);
            syncTypesTask = new SyncVehiclePositionTask(vehicleSyncAdapter, vehicleTypes, clearBeforeUpdate);
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
            value = new DrawVehicleTask(route, markersForRoute, vehicleSyncAdapter);
            value.execute();
            routeTaskMap.put(route, value);

        }
        Log.d(TAG, "syncVehicles >>");
    }

    public void stopTracking() {
        mHandler.removeCallbacks(timerTask);
        if (timerTask != null) {
            timerTask.cancel();
        }

        vehicleSyncAdapter.clearOverlay();
        vehicleTypes.clear();
        if (syncTypesTask != null && !AsyncTask.Status.FINISHED.equals(syncTypesTask.getStatus())) {
            syncTypesTask.cancel(true);
        }

        stopTrackAllRoutes();
    }
}
