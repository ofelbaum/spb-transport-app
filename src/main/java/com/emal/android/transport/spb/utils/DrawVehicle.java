package com.emal.android.transport.spb.utils;

import android.os.AsyncTask;
import com.emal.android.transport.spb.VehicleSyncAdapter;
import com.emal.android.transport.spb.portal.*;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since:
 */
public class DrawVehicle extends AsyncTask<Object, Void, List<Vehicle>> {
    private String vehicleId;
    private PortalClient portalClient;
    private GoogleMap mMap;
    private Map<String, List<Marker>> markers;
    private VehicleSyncAdapter vehicleSyncAdapter;

    public DrawVehicle(String vehicleId, PortalClient portalClient, GoogleMap mMap, Map<String, List<Marker>> markers, VehicleSyncAdapter vehicleSyncAdapter) {
        this.vehicleId = vehicleId;
        this.portalClient = portalClient;
        this.mMap = mMap;
        this.markers = markers;
        this.vehicleSyncAdapter = vehicleSyncAdapter;
    }

    @Override
    protected void onPreExecute() {
        vehicleSyncAdapter.beforeSync(false);
    }

    @Override
    protected List<Vehicle> doInBackground(Object... params) {
        List<Vehicle> list = null;
        try {
            VehicleCollection routeData = portalClient.getRouteData(vehicleId, PortalClient.BBOX);
            list = routeData.getList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<Vehicle> vehicles) {
        List<Marker> markers1 = markers.get(vehicleId);
        if (markers1 != null && !markers1.isEmpty()) {
            for (Marker marker : markers1) {
                marker.remove();
            }
        } else {
            markers1 = new ArrayList<Marker>();
            markers.put(vehicleId, markers1);
        }

        for (Vehicle v : vehicles) {
            Double latitude = v.getGeometry().getLatitude();
            Double longtitude = v.getGeometry().getLongtitude();

            LatLng homePoint = new LatLng(latitude, longtitude);

            VehicleProps properties = v.getProperties();
            String stateNumber = properties.getStateNumber() + "#" + properties.getVelocity();
            MarkerOptions title = new MarkerOptions()
                    .position(homePoint)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(stateNumber);


            Marker marker = mMap.addMarker(title);
            markers1.add(marker);


        }
        vehicleSyncAdapter.afterSync(true);
    }
}
