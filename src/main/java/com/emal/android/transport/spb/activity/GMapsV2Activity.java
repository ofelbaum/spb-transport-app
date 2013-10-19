package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import com.emal.android.transport.spb.*;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.emal.android.transport.spb.utils.ApplicationParams;
import com.emal.android.transport.spb.utils.Constants;
import com.google.android.gms.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.maps.GeoPoint;

import java.util.TimerTask;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class GMapsV2Activity extends FragmentActivity {
    private static final String TAG = GMapsV2Activity.class.getName();

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private ApplicationParams appParams;

    private NewVehicleTracker newVehicleTracker;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private TimerTask timerTask;
    private VehicleSyncAdapter vehicleSyncAdapter;

    public class MapUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            int syncTime = appParams.getSyncTime();
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

        getActionBar().hide();

        ImageView settingButton = (ImageView) findViewById(R.id.settingsButton);
        final Activity a = this;
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(a, PreferencesActivity.class));
            }
        });

        ImageView myPlaceButton = (ImageView) findViewById(R.id.myPlaceButton);
        myPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToLocation(appParams.getHomeLocation());
            }
        });
        final View errorSignLayout = mapFragment.getActivity().findViewById(com.emal.android.transport.spb.R.id.errorSignLayout);
        errorSignLayout.setOnClickListener(new ErrorSignOnClickListener(errorSignLayout));

    }

    private void startSync() {
        LatLngBounds latLngBounds = mapFragment.getMap().getProjection().getVisibleRegion().latLngBounds;
        vehicleSyncAdapter.setBBox(latLngBounds);
        if (timerTask != null) {
            timerTask.cancel();
        } else {
            timerTask = new MapUpdateTimerTask();
        }
        mHandler.removeCallbacks(timerTask);
        mHandler.postDelayed(timerTask, 0);
    }

    private void initApplication() {
        appParams = new ApplicationParams(getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));

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

        moveToLocation(appParams.getLastLocation());
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
        }

        mMap.setMyLocationEnabled(true);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                appParams.setLastLocation(cameraPosition.target);
                appParams.setZoomSize((int) cameraPosition.zoom);

                startSync();
            }
        });
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        vehicleSyncAdapter = new GMapVehicleSyncAdapter(mapFragment);
        newVehicleTracker = new NewVehicleTracker(vehicleSyncAdapter);

        GeoPoint home = appParams.getHomeLocation();
        LatLng homePoint = new LatLng(home.getLatitudeE6() / 1E6, home.getLongitudeE6() / 1E6);
        mMap.addMarker(new MarkerOptions().position(homePoint).title(getResources().getString(R.string.my_place)));

    }

    private void moveToLocation(GeoPoint geoPoint) {
        Log.d(TAG, "Move to location: " + geoPoint.toString());
        LatLng latLng = new LatLng(geoPoint.getLatitudeE6() / 1E6, geoPoint.getLongitudeE6() / 1E6);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, appParams.getZoomSize()));
        appParams.setLastLocation(geoPoint);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(timerTask);
        appParams.saveAll();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacks(timerTask);
        appParams.saveAll();
        super.onStop();
    }
}
