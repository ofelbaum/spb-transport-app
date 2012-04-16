package com.emal.android;

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.maps.MapView;

import java.util.HashMap;
import java.util.Map;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/16/12 11:23 PM
 */
public class VehicleTracker {
    private static final String TAG = "VehicleTracker";
    private MapView mapView;
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
}
