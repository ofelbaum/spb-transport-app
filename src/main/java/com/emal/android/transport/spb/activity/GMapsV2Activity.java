package com.emal.android.transport.spb.activity;

import android.app.AlertDialog;
import android.content.*;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import com.emal.android.transport.spb.*;
import android.os.Bundle;
import com.emal.android.transport.spb.VehicleType;
import com.emal.android.transport.spb.model.ApplicationParams;
import com.emal.android.transport.spb.portal.*;
import com.emal.android.transport.spb.task.LoadTrackRoutesTask;
import com.emal.android.transport.spb.utils.*;
import com.google.android.gms.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.maps.GeoPoint;

import java.io.IOException;
import java.util.*;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class GMapsV2Activity extends AbstractDrawerActivity {
    private static final String TAG = GMapsV2Activity.class.getName();

    private TouchableMapFragment mapFragment;
    private UiSettings mUiSettings;
    private ApplicationParams appParams;
    private VehicleTracker vehicleTracker;
    private VehicleSyncAdapter vehicleSyncAdapter;
    private AlertDialog alert;
    private Address longPressLoc;
    private PortalClient portalClient = PortalClient.getInstance();
    private boolean mMapIsTouched = false;
    private BroadcastReceiver networkStatusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appParams = new ApplicationParams(getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));
        createMapFragment();
    }

    private void initApplication() {
        if (appParams.isShowBus()) {
            vehicleTracker.startTrack(VehicleType.BUS);
        }
        if (appParams.isShowShip()) {
            vehicleTracker.startTrack(VehicleType.SHIP);
        }
        if (appParams.isShowTram()) {
            vehicleTracker.startTrack(VehicleType.TRAM);
        }
        if (appParams.isShowTrolley()) {
            vehicleTracker.startTrack(VehicleType.TROLLEY);
        }


        vehicleSyncAdapter.setTrafficEnabled(appParams.isShowTraffic());
        vehicleSyncAdapter.setMapType(Boolean.TRUE.equals(appParams.isSatView()) ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);

        moveToLocation(appParams.getLastLocation());

        networkStatusReceiver = new BroadcastReceiver() {
            private boolean isConnected = false;

            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager connectivity = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);


                NetworkInfo activeNetworkInfo = connectivity.getActiveNetworkInfo();
                if (activeNetworkInfo == null) {
                    Log.d(TAG, "Network OFF");
                    isConnected = false;
                    return;
                }
                Log.d(TAG, "Network ON " + activeNetworkInfo.toString());
                if (activeNetworkInfo.isConnected() && !isConnected) {
                    Log.d(TAG, "connected");

                    final Set<String> routesToTrack = appParams.getRoutesToTrack();
                    if (!routesToTrack.isEmpty()) {
                        Log.d(TAG, "Start routes tracking");
                        new LoadTrackRoutesTask(routesToTrack, vehicleSyncAdapter, vehicleTracker).execute();
                    } else {
                        Log.d(TAG, "Start all routes tracking");

                    }
                    isConnected = true;
                }
            }
        };
        registerReceiver(networkStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void createMapFragment() {
        if (mapFragment != null) {
            return;
        }

        mapFragment = (TouchableMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.setOnMapReadyCallback(new TouchableMapFragment.onMapReady() {
            @Override
            public void setMap(final GoogleMap mMap) {
                mMap.setMyLocationEnabled(true);

                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        appParams.setLastLocation(cameraPosition.target);
                        appParams.setZoomSize((int) cameraPosition.zoom);
                        if (Boolean.FALSE.equals(mMapIsTouched)) {
                            vehicleTracker.startSync();
                        }
                    }
                });
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(final LatLng latLng) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d(TAG, "Long press. Point " + latLng);

                                Geocoder geo = new Geocoder(getApplicationContext());
                                try {
                                    List<Address> myAddrs = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                    if (myAddrs.size() > 0) {
                                        Address myPlace = myAddrs.get(0);
                                        Log.d(TAG, "My Place selected: " + GeoConverter.convert(myPlace));

                                        longPressLoc = myPlace;

                                        String msg = getResources().getString(com.emal.android.transport.spb.R.string.addmyplace, GeoConverter.convert(myPlace));
                                        alert.setMessage(msg);
                                        alert.show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                });
                mUiSettings = mMap.getUiSettings();
                mUiSettings.setMyLocationButtonEnabled(true);
                mUiSettings.setZoomControlsEnabled(true);
                mUiSettings.setCompassEnabled(false);
                mUiSettings.setRotateGesturesEnabled(false);

                //TODO menu null?
                vehicleSyncAdapter = new GMapVehicleSyncAdapter(mapFragment, menu);
                vehicleTracker = new VehicleTracker(vehicleSyncAdapter);

                GeoPoint home = appParams.getHomeLocation();
                LatLng homePoint = new LatLng(home.getLatitudeE6() / 1E6, home.getLongitudeE6() / 1E6);
                mMap.addMarker(new MarkerOptions().position(homePoint).title(getResources().getString(R.string.my_place)));


                AlertDialog.Builder builder = new AlertDialog.Builder(GMapsV2Activity.this);
                Resources resources = getResources();
                String yes = resources.getString(com.emal.android.transport.spb.R.string.yes);
                String no = resources.getString(com.emal.android.transport.spb.R.string.no);
                builder.setCancelable(false)
                        .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //longPressLoc
                                GeoPoint homeLocation = new GeoPoint((int) (longPressLoc.getLatitude() * 1E6), (int) (longPressLoc.getLongitude() * 1E6));
                                appParams.setHomeLocation(homeLocation);

                                LatLng homePoint = new LatLng(longPressLoc.getLatitude(), longPressLoc.getLongitude());
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(homePoint).title(getResources().getString(R.string.my_place)));
                                dialog.cancel();

                            }
                        })
                        .setNegativeButton(no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alert = builder.create();
                initApplication();
            }
        });
    }

    private void moveToLocation(GeoPoint geoPoint) {
        if (vehicleSyncAdapter == null) {
            return;
        }
        Log.d(TAG, "Move to location: " + geoPoint.toString());
        LatLng latLng = new LatLng(geoPoint.getLatitudeE6() / 1E6, geoPoint.getLongitudeE6() / 1E6);
        vehicleSyncAdapter.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, appParams.getZoomSize()));
        appParams.setLastLocation(geoPoint);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        appParams = new ApplicationParams(getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));

        if (currentTheme != appParams.getTheme()) {
            finish();
            this.startActivity(new Intent(this, this.getClass()));
        }

        if (networkStatusReceiver != null) {
            registerReceiver(networkStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

            final Set<String> routesToTrack = appParams.getRoutesToTrack();
            if (!routesToTrack.isEmpty()) {
                Log.d(TAG, "Start routes tracking");
                new LoadTrackRoutesTask(routesToTrack, vehicleSyncAdapter, vehicleTracker).execute();
            } else {
                Log.d(TAG, "Start all routes tracking");

            }
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        try {
            unregisterReceiver(networkStatusReceiver);
        } catch (Exception e) {
            Log.d(TAG, "Exception during unregistration, Cause: " + e.getMessage());
        }
        portalClient.reset();
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        if (vehicleTracker != null) {
            vehicleTracker.stopTracking();
        }
        appParams.saveAll();
        super.onPause();
    }

    @Override
    protected boolean onOptionsItemSelected(int pos) {
        switch (pos) {
            case com.emal.android.transport.spb.R.id.action_websearch: {
                Intent intent = new Intent(GMapsV2Activity.this, SearchActivity.class);
                intent.putExtra(SearchActivity.SELECTED_ROUTES, vehicleTracker.getTracked());
                startActivity(intent);
                return true;
            }
            case R.id.errorIcon: {
                final AlertDialog dialog = UIHelper.getErrorDialog(this);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                }, 5000);
                return true;

            }
            default:
                return true;
        }
    }

    @Override
    protected void onMenuItemClick(int position) {
        switch (position) {
            case 0: {
                Intent intent = new Intent(GMapsV2Activity.this, SearchActivity.class);
                intent.putExtra(SearchActivity.SELECTED_ROUTES, vehicleTracker.getTracked());
                startActivity(intent);
                break;
            }
            case 1: {
                moveToLocation(appParams.getHomeLocation());
                break;
            }
            case 2: {
                startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
                break;
            }
            case 3: {
                startActivity(new Intent(getApplicationContext(), InfoActivity.class));
                break;
            }
        }
    }

    public void setTouched(boolean touched) {
        this.mMapIsTouched = touched;
    }
}
