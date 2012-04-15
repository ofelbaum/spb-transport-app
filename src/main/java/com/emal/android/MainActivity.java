package com.emal.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private boolean showBus = false;
    private boolean showTrolley = false;
    private boolean showTram = false;
    private boolean satView = true;
    private int syncTime = Constants.DEFAULT_SYNC_MS;
    private int zoomSize;
    private GeoPoint homeLocation;
    private Set<Vehicle> vehicles;
    private ExtendedMapView mapView;
    private SharedPreferences sharedPreferences;

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

        MyLocationOverlay mylocationOverlay = new MyLocationOverlay(mapView.getContext(), mapView);
        mylocationOverlay.enableMyLocation();
        mapView.getOverlays().add(mylocationOverlay);

        vehicles = new LinkedHashSet<Vehicle>();
        mapView.setVehicles(vehicles);
        initParams();
        moveToCurrentLocation();
    }

    private void initParams() {
        sharedPreferences = getSharedPreferences(Constants.APP_SHARED_SOURCE, 0);
            showBus = sharedPreferences.getBoolean(Constants.SHOW_BUS_FLAG, true);
            showTrolley = sharedPreferences.getBoolean(Constants.SHOW_TROLLEY_FLAG, true);
            showTram = sharedPreferences.getBoolean(Constants.SHOW_TRAM_FLAG, true);
            syncTime = sharedPreferences.getInt(Constants.SYNC_TIME_FLAG, Constants.DEFAULT_SYNC_MS);

            Float homeLat = sharedPreferences.getFloat(Constants.HOME_LOC_LAT_FLAG, 59.95f);
            Float homeLong = sharedPreferences.getFloat(Constants.HOME_LOC_LONG_FLAG, 30.316667f);
            homeLocation = new GeoPoint((int)(homeLat * 1E6), (int)(homeLong * 1E6));

            satView = sharedPreferences.getBoolean(Constants.SAT_VIEW_FLAG, true);
            zoomSize = sharedPreferences.getInt(Constants.ZOOM_FLAG, Constants.DEFAULT_ZOOM_LEVEL);
        if (showBus) {
            trackVehicle(Vehicle.BUS);
        } else {
            untrackVehicle(Vehicle.BUS);
        }
        if (showTrolley) {
            trackVehicle(Vehicle.TROLLEY);
        } else {
            untrackVehicle(Vehicle.TROLLEY);
        }
        if (showTram) {
            trackVehicle(Vehicle.TRAM);
        } else {
            untrackVehicle(Vehicle.TRAM);
        }
        mapView.setSatellite(satView);
    }

    @Override
    protected void onResume() {
        initParams();
        super.onResume();
    }

    private void moveToCurrentLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        GeoPoint currentPoint = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
        MapController controller = mapView.getController();
        controller.animateTo(currentPoint);
        controller.setZoom(zoomSize);
    }

    private void moveToHomeLocation() {
        MapController controller = mapView.getController();
        controller.animateTo(homeLocation);
        controller.setZoom(zoomSize);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public void trackVehicle(Vehicle vehicle) {
        Log.d(TAG, "track " + vehicle);
        vehicles.add(vehicle);
        MapOverlay mapOverlay = new MapOverlay(vehicle);
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay overlay : overlays) {
            if (overlay instanceof MapOverlay) {
                Vehicle v = ((MapOverlay) overlay).getVehicle();
                if (v.equals(vehicle)) {
                    Log.d(TAG, "vehicle exist " + vehicle);
                    return;
                }
            }
        }
        overlays.add(mapOverlay);
        mapView.invalidate();
    }

    public void untrackVehicle(Vehicle vehicle) {
        Log.d(TAG, "untrack " + vehicle);
        vehicles.remove(vehicle);

        List<Overlay> mapOverlays = mapView.getOverlays();
        Iterator<Overlay> iterator = mapOverlays.iterator();
        while (iterator.hasNext()) {
            Overlay overlay = iterator.next();
            if (overlay instanceof MapOverlay) {
                Vehicle vehicle1 = ((MapOverlay) overlay).getVehicle();
                if (vehicle1.equals(vehicle)) {
                    iterator.remove();
                    mapView.invalidate();
                    //break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cur_location: {
                moveToCurrentLocation();
                break;
            }
            case R.id.home_location: {
                moveToHomeLocation();
                break;
            }
            case R.id.settings: {
                startActivity(new Intent(this, TransportSettings.class));
                break;
            }
        }
        return true;
    }
}
