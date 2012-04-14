package com.emal.android;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.maps.*;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/15/12 9:34 PM
 */
public class MainActivity extends MapActivity {
    private static final String TAG = "MapActivity";
    private ExtendedMapView mapView;
    private Set<Vehicle> vehicles;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mapView = (ExtendedMapView) findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        mapView.displayZoomControls(true);
        mapView.getController().setZoom(15);

        MyLocationOverlay mylocationOverlay = new MyLocationOverlay(mapView.getContext(), mapView);
        mylocationOverlay.enableMyLocation();
        mapView.getOverlays().add(mylocationOverlay);

        vehicles = new LinkedHashSet<Vehicle>();
        mapView.setVehicles(vehicles);
        trackVehicle(Vehicle.BUS);
        trackVehicle(Vehicle.TROLLEY);
        trackVehicle(Vehicle.TRAM);

        moveToCurrentLocation();
    }

    private void moveToCurrentLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        GeoPoint currentPoint = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
        mapView.getController().animateTo(currentPoint);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public void trackVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        MapOverlay mapOverlay = new MapOverlay(vehicle);
        List<Overlay> overlays = mapView.getOverlays();
        overlays.add(mapOverlay);
    }

    public void untrackVehicle(Vehicle vehicle) {
        List<Overlay> mapOverlays = mapView.getOverlays();
        Iterator<Overlay> iterator = mapOverlays.iterator();
        while (iterator.hasNext()) {
            Overlay overlay = iterator.next();
            if (overlay instanceof MapOverlay) {
                Vehicle vehicle1 = ((MapOverlay) overlay).getVehicle();
                if (vehicle1.equals(vehicle)) {
                    iterator.remove();
                    mapView.invalidate();
                    break;
                }
            }
        }
    }
}
