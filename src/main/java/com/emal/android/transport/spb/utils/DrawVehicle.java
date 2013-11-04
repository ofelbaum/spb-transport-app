package com.emal.android.transport.spb.utils;

import android.os.AsyncTask;
import com.emal.android.transport.spb.portal.*;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since:
 */
public class DrawVehicle extends AsyncTask<Object, Void, List<Vehicle>> {
    private PortalClient portalClient;
    private GoogleMap mMap;
    private List<Marker> markers;

    public DrawVehicle(PortalClient portalClient, GoogleMap mMap, List<Marker> markers) {
        this.portalClient = portalClient;
        this.mMap = mMap;
        this.markers = markers;
    }

    @Override
    protected List<Vehicle> doInBackground(Object... params) {
        List<Vehicle> list = null;
        try {
            VehicleCollection routeData = portalClient.getRouteData((String) params[0], PortalClient.BBOX);
            list = routeData.getList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<Vehicle> vehicles) {
        for (Vehicle v : vehicles) {
            Double latitude = v.getGeometry().getLatitude();
            Double longtitude = v.getGeometry().getLongtitude();

            LatLng homePoint = new LatLng(latitude, longtitude);

            VehicleProps properties = v.getProperties();
            String stateNumber = properties.getStateNumber() + "#" + properties.getVelocity();
            MarkerOptions title = new MarkerOptions()
                    .position(homePoint)
                    .title(stateNumber)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            Marker marker = mMap.addMarker(title);
            markers.add(marker);


        }
    }
}
