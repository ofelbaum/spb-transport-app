package com.emal.android.transport.spb.task;

import android.os.AsyncTask;
import com.emal.android.transport.spb.VehicleSyncAdapter;
import com.emal.android.transport.spb.VehicleTracker;
import com.emal.android.transport.spb.portal.Route;

import java.io.IOException;
import java.util.Collection;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class LoadTrackRoutesTask extends AsyncTask<Object, Void, Boolean> {
    private Collection<String> routesToTrack;
    private VehicleSyncAdapter vehicleSyncAdapter;
    private VehicleTracker vehicleTracker;

    public LoadTrackRoutesTask(Collection<String> routesToTrack,
                               VehicleSyncAdapter vehicleSyncAdapter,
                               VehicleTracker vehicleTracker) {
        this.routesToTrack = routesToTrack;
        this.vehicleSyncAdapter = vehicleSyncAdapter;
        this.vehicleTracker = vehicleTracker;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        for (String s : routesToTrack) {
            String[] decode = Route.decode(s);
            try {
                Route route = vehicleSyncAdapter.getPortalClient().findRoute(decode[0], decode[1]);
                vehicleTracker.startTrack(route);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPreExecute() {
        vehicleTracker.stopTracking();
    }

    @Override
    protected void onPostExecute(Boolean o) {
        if (Boolean.TRUE.equals(o)) {
            vehicleTracker.syncVehicles();
        } else {
            vehicleSyncAdapter.showError();
        }
    }
}
