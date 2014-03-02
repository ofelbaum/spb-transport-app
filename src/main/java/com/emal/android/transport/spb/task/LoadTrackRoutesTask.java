package com.emal.android.transport.spb.task;

import android.os.AsyncTask;
import com.emal.android.transport.spb.VehicleSyncAdapter;
import com.emal.android.transport.spb.VehicleTracker;
import com.emal.android.transport.spb.VehicleType;
import com.emal.android.transport.spb.model.ApplicationParams;
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
    private ApplicationParams appParams;
    private VehicleTracker vehicleTracker;

    public LoadTrackRoutesTask(Collection<String> routesToTrack,
                               VehicleSyncAdapter vehicleSyncAdapter,
                               VehicleTracker vehicleTracker,
                               ApplicationParams appParams) {
        this.routesToTrack = routesToTrack;
        this.vehicleSyncAdapter = vehicleSyncAdapter;
        this.vehicleTracker = vehicleTracker;
        this.appParams = appParams;
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

        if (appParams.isShowBus()) {
            vehicleTracker.startTrack(VehicleType.BUS);
        }
        if (appParams.isShowShip()) {
            vehicleTracker.startTrack(VehicleType.SHIP);
        }
        if (appParams.isShowTram()) {
            vehicleTracker.startTrack(VehicleType.TRAM);
        }
        if (appParams.isShowTrolley()) {
            vehicleTracker.startTrack(VehicleType.TROLLEY);
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
            vehicleTracker.startSync();
        } else {
            vehicleSyncAdapter.showError();
        }
    }
}
