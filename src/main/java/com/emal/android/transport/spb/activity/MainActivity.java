package com.emal.android.transport.spb.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.location.*;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.emal.android.transport.spb.R;
import com.emal.android.transport.spb.Vehicle;
import com.emal.android.transport.spb.VehicleTracker;
import com.emal.android.transport.spb.map.ExtendedMapView;
import com.emal.android.transport.spb.map.TouchOverlay;
import com.emal.android.transport.spb.utils.Constants;
import com.emal.android.transport.spb.utils.GeoConverter;
import com.emal.android.transport.spb.map.MapUtils;
import com.google.android.maps.*;

import java.io.IOException;
import java.util.*;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/15/12 9:34 PM
 */
public class MainActivity extends MapActivity {
    private static final String TAG = "MapActivity";
    private boolean showBus = false;
    private boolean showTrolley = false;
    private boolean showTram = false;
    private boolean showShip = false;
    private boolean satView = true;
    private int syncTime = Constants.DEFAULT_SYNC_MS;
    private int zoomSize;
    private GeoPoint homeLocation;
    private GeoPoint lastLocation;
    private GeoPoint longPressedLocation;
    private ExtendedMapView mapView;
    private SharedPreferences sharedPreferences;
    private Handler mHandler = new Handler();
    private TimerTask timerTask = new MapUpdateTimerTask();
    private VehicleTracker vehicleTracker;
    private MyLocationOverlay mylocationOverlay;
    private LocationManager locationManager;
    private AlertDialog alert;
    private Address myPlace;

    public class MapUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "START Timer Update " + Thread.currentThread().getName() + " with time " + syncTime);
            vehicleTracker.syncVehicles();
            mHandler.postDelayed(this, syncTime);
        }
    }

    private Runnable immediateUpdate = new Runnable() {
        @Override
        public void run() {
            if (timerTask != null) {
                timerTask.cancel();
            }
            mHandler.removeCallbacks(timerTask);
            mHandler.postDelayed(timerTask, 0);
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mapView = (ExtendedMapView) findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        mapView.displayZoomControls(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mylocationOverlay = new MyLocationOverlay(mapView.getContext(), mapView);
        mylocationOverlay.enableMyLocation();
        List<Overlay> overlays = mapView.getOverlays();
        overlays.add(mylocationOverlay);
        overlays.add(new TouchOverlay(this));

        initParams();
        alert = MapUtils.createMyPlaceDialog(mapView,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        homeLocation = longPressedLocation;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        int latitude = homeLocation.getLatitudeE6();
                        int longtitude = homeLocation.getLongitudeE6();
                        editor.putInt(Constants.HOME_LOC_LAT_FLAG, latitude);
                        editor.putInt(Constants.HOME_LOC_LONG_FLAG, longtitude);
                        editor.commit();
                        MapUtils.redrawMyPlace(mapView, homeLocation);
                        dialog.cancel();

                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );
        vehicleTracker = new VehicleTracker(mapView);
        mapView.setVehicleTracker(vehicleTracker);
        mapView.getController().setZoom(zoomSize);
        mapView.setOnLongpressListener(createLongPressListener());
        mapView.setOnZoomListener(createOnZoomListener());

        if (lastLocation == null) {
            moveToCurrentLocation();
        } else {
            moveToLocation(lastLocation);
        }
        MapUtils.addMyPlace(mapView, homeLocation);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        final RelativeLayout errorSign = (RelativeLayout) findViewById(R.id.errorSignLayout);
        errorSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mapView.getContext());

                builder.setTitle(R.string.error_title);
                builder.setMessage(R.string.server_error);
                builder.setPositiveButton(R.string.ok, null);

                final AlertDialog dialog = builder.show();
                TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
                messageText.setGravity(Gravity.CENTER);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                }, 5000);

                errorSign.setVisibility(View.INVISIBLE);
            }
        });
    }

    private ExtendedMapView.OnZoomListener createOnZoomListener() {
        return new ExtendedMapView.OnZoomListener() {
            @Override
            public void onZoom(int oldZoomValue, int newZoomValue) {
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.APP_SHARED_SOURCE, 0);
                SharedPreferences.Editor ed = sharedPreferences.edit();
                ed.putInt(Constants.ZOOM_FLAG, newZoomValue);
                ed.commit();
            }
        };
    }

    private ExtendedMapView.OnLongpressListener createLongPressListener() {
        return new ExtendedMapView.OnLongpressListener() {
            @Override
            public void onLongpress(final MapView view, final int x, final int y) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        longPressedLocation = view.getProjection().fromPixels(x, y - getTitleBarHeight());
                        Log.d(TAG, "Long press. Point " + longPressedLocation);
                        float latitude = (float) (longPressedLocation.getLatitudeE6() / 1E6);
                        float longtitude = (float) (longPressedLocation.getLongitudeE6() / 1E6);

                        Geocoder geo = new Geocoder(view.getContext());
                        try {
                            List<Address> myAddrs = geo.getFromLocation(latitude, longtitude, 1);
                            if (myAddrs.size() > 0) {
                                myPlace = myAddrs.get(0);
                                Log.d(TAG, "My Place selected: " + GeoConverter.convert(myPlace));

                                String msg = getResources().getString(R.string.addmyplace, GeoConverter.convert(myPlace));
                                alert.setMessage(msg);
                                alert.show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            private int getTitleBarHeight() {
                Rect rect = new Rect();
                Window window = getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
                int StatusBarHeight = rect.top;
                int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                return contentViewTop - StatusBarHeight;
            }
        };
    }

    private void initParams() {
        sharedPreferences = getSharedPreferences(Constants.APP_SHARED_SOURCE, 0);
        showBus = sharedPreferences.getBoolean(Constants.SHOW_BUS_FLAG, true);
        showTrolley = sharedPreferences.getBoolean(Constants.SHOW_TROLLEY_FLAG, true);
        showTram = sharedPreferences.getBoolean(Constants.SHOW_TRAM_FLAG, true);
        showShip = sharedPreferences.getBoolean(Constants.SHOW_SHIP_FLAG, true);
        syncTime = sharedPreferences.getInt(Constants.SYNC_TIME_FLAG, Constants.DEFAULT_SYNC_MS);

        Integer homeLat = sharedPreferences.getInt(Constants.HOME_LOC_LAT_FLAG, MapUtils.SPB_CENTER_LAT_DEF_VALUE);
        Integer homeLong = sharedPreferences.getInt(Constants.HOME_LOC_LONG_FLAG, MapUtils.SPB_CENTER_LONG_DEF_VALUE);
        homeLocation = new GeoPoint(homeLat, homeLong);

        Integer currLat = sharedPreferences.getInt(Constants.LAST_LOC_LAT_FLAG, MapUtils.SPB_CENTER_LAT_DEF_VALUE);
        Integer currLong = sharedPreferences.getInt(Constants.LAST_LOC_LONG_FLAG, MapUtils.SPB_CENTER_LONG_DEF_VALUE);
        lastLocation = new GeoPoint(currLat, currLong);

        satView = sharedPreferences.getBoolean(Constants.SAT_VIEW_FLAG, false);
        zoomSize = sharedPreferences.getInt(Constants.ZOOM_FLAG, Constants.DEFAULT_ZOOM_LEVEL);
    }

    private void restoreApplicationState() {
        if (showBus) {
            vehicleTracker.startTrack(Vehicle.BUS);
        } else {
            vehicleTracker.stopTrack(Vehicle.BUS);
        }
        if (showTrolley) {
            vehicleTracker.startTrack(Vehicle.TROLLEY);
        } else {
            vehicleTracker.stopTrack(Vehicle.TROLLEY);
        }
        if (showTram) {
            vehicleTracker.startTrack(Vehicle.TRAM);
        } else {
            vehicleTracker.stopTrack(Vehicle.TRAM);
        }
        if (showShip) {
            vehicleTracker.startTrack(Vehicle.SHIP);
        } else {
            vehicleTracker.stopTrack(Vehicle.SHIP);
        }
        mapView.setSatellite(satView);
        mHandler.postDelayed(timerTask, 0);

        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Log.d(TAG, "Best provider = " + bestProvider);
        locationManager.requestLocationUpdates(bestProvider, 3000, 3, mylocationOverlay);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        initParams();
        restoreApplicationState();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(timerTask);
        super.onPause();
    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacks(timerTask);
        locationManager.removeUpdates(mylocationOverlay);
        super.onStop();
    }

    private void moveToCurrentLocation() {
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        GeoPoint currentPoint = homeLocation;
        if (location != null) {
            currentPoint = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
        }
        moveToLocation(currentPoint);
    }

    private void moveToLocation(GeoPoint geoPoint) {
        MapController controller = mapView.getController();
        controller.animateTo(geoPoint, immediateUpdate);

        MapUtils.saveGeoPoint(this, geoPoint);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
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
                moveToLocation(homeLocation);
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
