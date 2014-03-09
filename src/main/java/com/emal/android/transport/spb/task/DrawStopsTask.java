package com.emal.android.transport.spb.task;

import android.os.AsyncTask;
import com.emal.android.transport.spb.VehicleSyncAdapter;
import com.emal.android.transport.spb.portal.Stop;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class DrawStopsTask extends AsyncTask<Object, Void, List<Stop>>{
    private VehicleSyncAdapter vehicleSyncAdapter;
    private GoogleMap mMap;
    private List<Marker> stopList;

    public DrawStopsTask(VehicleSyncAdapter vehicleSyncAdapter, GoogleMap mMap, List<Marker> stopList) {
        this.vehicleSyncAdapter = vehicleSyncAdapter;
        this.mMap = mMap;
        this.stopList = stopList;
    }

    @Override
    protected List<Stop> doInBackground(Object... params) {
        List<Stop> stopsList = null;
        try {
            stopsList = vehicleSyncAdapter.getPortalClient().getStopsList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopsList;
    }

    @Override
    protected void onPostExecute(List<Stop> stops) {
        for (Stop stop : stops) {
            String name = stop.getName();
            Double latitude = stop.getLonLat().getLatitude();
            Double longtitude = stop.getLonLat().getLongtitude();
            LatLng homePoint = new LatLng(latitude, longtitude);
            MarkerOptions title = new MarkerOptions().position(homePoint).title(name);
            Marker marker = mMap.addMarker(title);
            stopList.add(marker);
        }
    }
}
