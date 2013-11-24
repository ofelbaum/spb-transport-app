package com.emal.android.transport.spb;

import android.os.AsyncTask;
import android.util.Log;
import com.emal.android.transport.spb.portal.PortalClient;
import com.emal.android.transport.spb.portal.Route;
import com.emal.android.transport.spb.utils.DrawVehicle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

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
    private Set<Route> routes;
    private VehicleSyncAdapter vehicleSyncAdapter;

    private PortalClient portalClient;
    private final GoogleMap googleMap;
    private Map<String, List<Marker>> markers;

    public VehicleTracker(VehicleSyncAdapter vehicleSyncAdapter,
                          PortalClient portalClient,
                          GoogleMap googleMap,
                          Map<String, List<Marker>> markers) {
        this.vehicleSyncAdapter = vehicleSyncAdapter;
        this.portalClient = portalClient;
        this.googleMap = googleMap;
        this.markers = markers;
        vehicleTypes = new HashSet<VehicleType>();
        routes = new HashSet<Route>();
        taskMap = new HashMap<String, AsyncTask>();
    }

    public boolean startTrack(VehicleType vehicleType) {
        return vehicleTypes.add(vehicleType);
    }

    public boolean startTrack(Route route) {
        return routes.add(route);
    }

    public void startTrack(Set<String> routesToTrack) {
        for (String s : routesToTrack) {
            //TODO
            //startTrack(s);
        }
    }

    public void stopTrackAllIds() {
        routes.clear();
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
        if (!vehicleTypes.isEmpty() && routes.isEmpty()) {
            syncTypesTask = new SyncVehiclePositionTask(vehicleSyncAdapter, vehicleTypes, clearBeforeUpdate, portalClient.getHttpClient());
            syncTypesTask.execute();
        }

        for (Route route : routes) {
            String routeId = String.valueOf(route.getId());
            AsyncTask value = taskMap.get(routeId);
            if (value != null && !AsyncTask.Status.FINISHED.equals(value.getStatus())) {
                value.cancel(true);
            }
            value = new DrawVehicle(route, portalClient, googleMap, markers, vehicleSyncAdapter);
            value.execute();
            taskMap.put(routeId, value);

        }
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

    public Set<String> getRoutes() {
        //TODO
        return Collections.emptySet();
    }
}
