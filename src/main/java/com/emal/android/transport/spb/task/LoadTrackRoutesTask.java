package com.emal.android.transport.spb.task;

import android.os.AsyncTask;
import android.util.Log;
import com.emal.android.transport.spb.VehicleSyncAdapter;
import com.emal.android.transport.spb.VehicleTracker;
import com.emal.android.transport.spb.VehicleType;
import com.emal.android.transport.spb.model.ApplicationParams;
import com.emal.android.transport.spb.portal.PortalClientException;
import com.emal.android.transport.spb.portal.Route;

import java.io.IOException;
import java.util.Collection;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class LoadTrackRoutesTask extends AsyncTask<Object, Void, Boolean> {
    private static final String TAG = LoadTrackRoutesTask.class.getSimpleName();
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

        // This is temporary workaround
        if (routesToTrack.isEmpty()) {
            try {
                vehicleSyncAdapter.getPortalClient().findRoute("1", "1");
            } catch (Exception e) {
                //nothing to do
            }
        }

        for (String s : routesToTrack) {
            String[] decode = Route.decode(s);
            try {
                Route route = vehicleSyncAdapter.getPortalClient().findRoute(decode[0], decode[1]);
                Log.d(TAG, "Get route info: " + route.getRouteNumber());
                vehicleTracker.add(route);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (PortalClientException e) {
                e.printStackTrace();
                return false;
            }
        }

        if (appParams.isShowBus()) {
            vehicleTracker.add(VehicleType.BUS);
        }
        if (appParams.isShowShip()) {
            vehicleTracker.add(VehicleType.SHIP);
        }
        if (appParams.isShowTram()) {
            vehicleTracker.add(VehicleType.TRAM);
        }
        if (appParams.isShowTrolley()) {
            vehicleTracker.add(VehicleType.TROLLEY);
        }

        return true;
    }

    @Override
    protected void onPreExecute() {
        vehicleTracker.stop();
        vehicleSyncAdapter.beforeSync(false);
    }

    @Override
    protected void onPostExecute(Boolean o) {
        //TODO fix false state. Reload tracks?
        vehicleSyncAdapter.afterSync(o);

        if (Boolean.TRUE.equals(o)) {
            vehicleTracker.restart();
        }
    }
}
