package com.emal.android.transport.spb.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.emal.android.transport.spb.*;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.emal.android.transport.spb.map.MapUtils;
import com.emal.android.transport.spb.utils.ApplicationParams;
import com.emal.android.transport.spb.utils.Constants;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.maps.GeoPoint;

import java.util.TimerTask;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/18/13 2:17 AM
 */
public class GMapsV2Activity extends FragmentActivity {
    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private static final String TAG = GMapsV2Activity.class.getName();

    private int syncTime = Constants.DEFAULT_SYNC_MS;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private ApplicationParams appParams;

    private GeoPoint homeLocation;
    private GeoPoint lastLocation;
    private LocationManager locationManager;
    private NewVehicleTracker newVehicleTracker;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private TimerTask timerTask = new MapUpdateTimerTask();
    private GMapVehicleSyncAdapter vehicleSyncAdapter;

    public class MapUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "START Timer Update " + Thread.currentThread().getName() + " with time " + syncTime);
            newVehicleTracker.syncVehicles();
            mHandler.postDelayed(this, syncTime);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gmapsv2);
        initApplication();

        moveToLocation(lastLocation);
    }

    private void startSync() {
        LatLngBounds latLngBounds = mapFragment.getMap().getProjection().getVisibleRegion().latLngBounds;
        vehicleSyncAdapter.setBBox(latLngBounds);
        if (timerTask != null) {
            timerTask.cancel();
        }
        mHandler.removeCallbacks(timerTask);
        mHandler.postDelayed(timerTask, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initApplication();
    }

    private void initApplication() {
        appParams = new ApplicationParams(getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));

        homeLocation = appParams.getHomeLocation();
        lastLocation = appParams.getLastLocation();

        setUpMapIfNeeded();

        newVehicleTracker.stopTrackAll();
        if (appParams.isShowBus()) {
            newVehicleTracker.startTrack(Vehicle.BUS);
        }
        if (appParams.isShowShip()) {
            newVehicleTracker.startTrack(Vehicle.SHIP);
        }
        if (appParams.isShowTram()) {
            newVehicleTracker.startTrack(Vehicle.TRAM);
        }
        if (appParams.isShowTrolley()) {
            newVehicleTracker.startTrack(Vehicle.TROLLEY);
        }

        mMap.setMapType(Boolean.TRUE.equals(appParams.isSatView()) ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
        syncTime = appParams.getSyncTime();

        startSync();
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                startSync();
            }
        });
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                MapUtils.saveGeoPoint(appParams, location);
            }
        });
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        vehicleSyncAdapter = new GMapVehicleSyncAdapter(mapFragment);
        newVehicleTracker = new NewVehicleTracker(vehicleSyncAdapter);

        GeoPoint home = appParams.getHomeLocation();
        LatLng homePoint = new LatLng(home.getLatitudeE6() / 1E6, home.getLongitudeE6() / 1E6);
        mMap.addMarker(new MarkerOptions().position(homePoint).title("Marker"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    private void moveToLocation(GeoPoint geoPoint) {
        LatLng latLng = new LatLng(geoPoint.getLatitudeE6() / 1E6, geoPoint.getLongitudeE6() / 1E6);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, appParams.getZoomSize()));

        //MapUtils.saveGeoPoint(this, geoPoint);
    }

    private void moveToCurrentLocation() {
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        GeoPoint currentPoint = homeLocation;
        if (location != null) {
            currentPoint = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
        }
        moveToLocation(currentPoint);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(timerTask);
        super.onPause();
    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacks(timerTask);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cur_location: {
                moveToCurrentLocation();
                break;
            }
            case R.id.home_location: {
                moveToLocation(appParams.getHomeLocation());
                break;
            }
            case R.id.settings: {
                startActivity(new Intent(this, PreferencesActivity.class));
                break;
            }
        }
        return true;
    }
}
