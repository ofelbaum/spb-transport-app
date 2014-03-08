package com.emal.android.transport.spb;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.emal.android.transport.spb.portal.Route;
import com.emal.android.transport.spb.task.DrawVehicleTask;
import com.emal.android.transport.spb.task.SyncVehiclePositionTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/18/13 5:06 AM
 */
public class VehicleTracker {
    private static final String TAG = VehicleTracker.class.getName();
    private AsyncTask syncTypesTask;
    private Set<VehicleType> vehicleTypes;
    private Map<Route, AsyncTask> routeTaskMap;
    private VehicleSyncAdapter vehicleSyncAdapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private TimerTask timerTask;

    private class MapUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            //TODO
            synchronized (VehicleTracker.this) {
                int syncTime = vehicleSyncAdapter.getSyncTime();
                Log.d(TAG, "START Timer Update " + Thread.currentThread().getName() + " with time " + syncTime);
                scheduleTasks();
                mHandler.postDelayed(this, syncTime);
            }
        }
    }

    public VehicleTracker(VehicleSyncAdapter vehicleSyncAdapter) {
        this.vehicleSyncAdapter = vehicleSyncAdapter;
        this.vehicleTypes = Collections.synchronizedSet(new HashSet<VehicleType>());
        this.routeTaskMap = new ConcurrentHashMap<Route, AsyncTask>();
    }

    public synchronized void start() {
        Log.d(TAG, "start");
        vehicleSyncAdapter.setBBox();
        if (timerTask != null) {
            timerTask.cancel();
        } else {
            timerTask = new MapUpdateTimerTask();
        }
        mHandler.removeCallbacks(timerTask);
        mHandler.postDelayed(timerTask, 0);
    }

    public boolean add(VehicleType vehicleType) {
        return vehicleTypes.add(vehicleType);
    }

    public synchronized void add(Route route) {
        AsyncTask asyncTask = routeTaskMap.get(route);
        if (asyncTask == null) {
            routeTaskMap.put(route, new DrawVehicleTask(route, vehicleSyncAdapter));
        }
    }

    public ArrayList<Route> getTracked() {
        return new ArrayList<Route>(routeTaskMap.keySet());
    }

    public synchronized void stop() {
        mHandler.removeCallbacks(timerTask);
        if (timerTask != null) {
            timerTask.cancel();
        }

        if (syncTypesTask != null && !AsyncTask.Status.FINISHED.equals(syncTypesTask.getStatus())) {
            syncTypesTask.cancel(true);
        }
        vehicleSyncAdapter.clearOverlay();

        Log.d(TAG, "stopTrackAllRoutes <<");
        for (Map.Entry<Route, AsyncTask> task : routeTaskMap.entrySet()) {
            Route key = task.getKey();
            AsyncTask value = task.getValue();
            if (value != null && !AsyncTask.Status.FINISHED.equals(value.getStatus())) {
                value.cancel(true);
            }

            vehicleSyncAdapter.removeMarkers(key);
        }
        routeTaskMap.clear();
        Log.d(TAG, "stopTrackAllRoutes >>");
    }

    private synchronized void scheduleTasks() {
        Log.d(TAG, "scheduleTasks <<");
        if (syncTypesTask != null && !AsyncTask.Status.FINISHED.equals(syncTypesTask.getStatus())) {
            Log.d(TAG, "Reschedule vehicleTypes");
            syncTypesTask.cancel(true);
        }
        if (!vehicleTypes.isEmpty() && routeTaskMap.isEmpty()) {
            Log.d(TAG, "Scheduling typed layout for types: " + vehicleTypes);
            syncTypesTask = new SyncVehiclePositionTask(vehicleSyncAdapter, vehicleTypes).execute();
        }

        for (Route route : routeTaskMap.keySet()) {
            Log.d(TAG, "Scheduling route: " + route);
            AsyncTask task = routeTaskMap.get(route);
            if (task != null && !AsyncTask.Status.FINISHED.equals(task.getStatus())) {
                task.cancel(true);
            }
            task = new DrawVehicleTask(route, vehicleSyncAdapter).execute();
            routeTaskMap.put(route, task);
        }
        Log.d(TAG, "scheduleTasks >>");
    }
}
