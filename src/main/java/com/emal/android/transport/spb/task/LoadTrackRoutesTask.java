package com.emal.android.transport.spb.task;

import android.os.AsyncTask;
import android.view.View;
import com.emal.android.transport.spb.VehicleTracker;
import com.emal.android.transport.spb.portal.PortalClient;
import com.emal.android.transport.spb.portal.Route;

import java.io.IOException;
import java.util.Collection;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class LoadTrackRoutesTask extends AsyncTask<Object, Void, Boolean> {
    private Collection<String> routesToTrack;
    private PortalClient portalClient;
    private VehicleTracker vehicleTracker;
    private View errorSignLayout;

    public LoadTrackRoutesTask(Collection<String> routesToTrack,
                               PortalClient portalClient,
                               VehicleTracker vehicleTracker,
                               View errorSignLayout) {
        this.routesToTrack = routesToTrack;
        this.portalClient = portalClient;
        this.vehicleTracker = vehicleTracker;
        this.errorSignLayout = errorSignLayout; //TODO remove it
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        for (String s : routesToTrack) {
            String[] decode = Route.decode(s);
            try {
                Route route = portalClient.findRoute(decode[0], decode[1]);
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
            errorSignLayout.setVisibility(View.VISIBLE);
        }
    }
}
