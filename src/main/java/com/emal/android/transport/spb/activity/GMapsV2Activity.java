package com.emal.android.transport.spb.activity;

import android.app.AlertDialog;
import android.content.*;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.emal.android.transport.spb.*;
import android.os.Bundle;
import com.emal.android.transport.spb.model.ApplicationParams;
import com.emal.android.transport.spb.portal.*;
import com.emal.android.transport.spb.task.LoadTrackRoutesTask;
import com.emal.android.transport.spb.utils.*;
import com.google.analytics.tracking.android.EasyTracker;
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
    private ApplicationParams appParams;
    private VehicleTracker vehicleTracker;
    private VehicleSyncAdapter vehicleSyncAdapter;
    private AlertDialog addMyPlaceDialog;
    private AlertDialog removeMyPlaceDialog;
    private Address longPressLoc;
    private PortalClient portalClient = PortalClient.getInstance();
    private boolean mMapIsTouched = false;
    private BroadcastReceiver networkStatusReceiver;
    private Marker myPlaceMarker;
    private AsyncTask<Object, Void, Boolean> initVehicleTrackerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appParams = new ApplicationParams(getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));
        createMapFragment();
        EasyTracker.getInstance(this).activityStart(this);
    }

    private void createMapFragment() {
        mapFragment = (TouchableMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        final TouchableMapFragment.ZoomSupport zoomSupport = new TouchableMapFragment.ZoomSupport() {
            @Override
            public void before() {
                vehicleTracker.pause();
            }

            @Override
            public void after() {
                vehicleTracker.restart();
            }
        };
        mapFragment.setZoomSupport(zoomSupport);
        mapFragment.setOnMapReadyCallback(new TouchableMapFragment.onMapReady() {
            @Override
            public void setMap(final GoogleMap map) {
                map.setMyLocationEnabled(true);

                //TODO refactor zoomin/out buttons
                final View zoomIn = findViewById(R.id.zoomin);
                zoomIn.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            map.animateCamera(CameraUpdateFactory.zoomIn());
                            mMapIsTouched = false;
                            zoomIn.setBackgroundResource(R.drawable.zoom_in_button);
                            return true;
                        }
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            zoomSupport.before();
                            mMapIsTouched = true;
                            zoomIn.setBackgroundResource(R.drawable.zoom_in_button_pressed);
                            return true;
                        }
                        return true;
                    }
                });
                final View zoomOut = findViewById(R.id.zoomout);
                zoomOut.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            map.animateCamera(CameraUpdateFactory.zoomOut());
                            mMapIsTouched = false;
                            zoomOut.setBackgroundResource(R.drawable.zoom_out_button);
                            return true;
                        }
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            zoomSupport.before();
                            mMapIsTouched = true;
                            zoomOut.setBackgroundResource(R.drawable.zoom_out_button_pressed);
                            return true;
                        }
                        return true;
                    }
                });

                map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        appParams.setLastLocation(cameraPosition.target);
                        appParams.setZoomSize((int) cameraPosition.zoom);
                        if (Boolean.FALSE.equals(mMapIsTouched) && vehicleTracker != null) {
                            vehicleTracker.restart();
                        }
                    }
                });
                map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
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
                                        addMyPlaceDialog.setMessage(msg);
                                        addMyPlaceDialog.show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                });
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (marker.equals(myPlaceMarker)) {
                            removeMyPlaceDialog.show();
                            return true;
                        }
                        return false;
                    }
                });

                GeoPoint home = appParams.getHomeLocation();
                if (home != null) {
                    LatLng homePoint = new LatLng(home.getLatitudeE6() / 1E6, home.getLongitudeE6() / 1E6);
                    myPlaceMarker = map.addMarker(new MarkerOptions().position(homePoint).title(getResources().getString(R.string.my_place)));
                }

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
                                if (myPlaceMarker != null) {
                                    myPlaceMarker.remove();
                                }
                                myPlaceMarker = map.addMarker(new MarkerOptions().position(homePoint).title(getResources().getString(R.string.my_place)));
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton(no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                addMyPlaceDialog = builder.create();

                builder = new AlertDialog.Builder(GMapsV2Activity.this);
                builder.setCancelable(false)
                        .setMessage(getResources().getString(com.emal.android.transport.spb.R.string.removemyplace))
                        .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myPlaceMarker.remove();
                                appParams.setHomeLocation(null);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton(no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                removeMyPlaceDialog = builder.create();
            }

            @Override
            public void updateMap(GoogleMap map) {
                map.setTrafficEnabled(appParams.isShowTraffic());
                map.setMapType(Boolean.TRUE.equals(appParams.isSatView()) ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
                moveToLocation(map, appParams.getLastLocation());
            }
        });
    }

    private void moveToLocation(GoogleMap map, GeoPoint geoPoint) {
        Log.d(TAG, "Move to location: " + geoPoint.toString());
        map.moveCamera(GeoConverter.toCameraUpdate(geoPoint, appParams.getZoomSize()));
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

        GeoPoint homeLocation = appParams.getHomeLocation();
        if (homeLocation == null && myPlaceMarker != null) {
            myPlaceMarker.remove();
        }
        DrawHelper.evictCaches();
        vehicleSyncAdapter = new GMapVehicleSyncAdapter(mapFragment, menu);
        vehicleSyncAdapter.setSyncTime(appParams.getSyncTime());
        vehicleSyncAdapter.setIconSize(appParams.getIconSize());

        vehicleTracker = new VehicleTracker(vehicleSyncAdapter);

        if (networkStatusReceiver == null) {
            networkStatusReceiver = new BroadcastReceiver() {
                private boolean isConnected = true;

                @Override
                public void onReceive(Context context, Intent intent) {
                    ConnectivityManager connectivity = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);

                    NetworkInfo activeNetworkInfo = connectivity.getActiveNetworkInfo();
                    if (activeNetworkInfo == null) {
                        Log.d(TAG, "Network OFF");
                        isConnected = false;
                        vehicleTracker.stop();
                        return;
                    }
                    Log.d(TAG, "Network ON " + activeNetworkInfo.toString());
                    if (activeNetworkInfo.isConnected() && !isConnected) {
                        Log.d(TAG, "connected");
                        isConnected = true;
                        vehicleTracker.start();
                    }
                }
            };

        }
        loadTrackedRoutes();
        registerReceiver(networkStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void loadTrackedRoutes() {
        final Set<String> routesToTrack = appParams.getRoutesToTrack();
        if (initVehicleTrackerTask != null && !AsyncTask.Status.FINISHED.equals(initVehicleTrackerTask.getStatus())) {
            initVehicleTrackerTask.cancel(true);
        }
        initVehicleTrackerTask = new LoadTrackRoutesTask(routesToTrack, vehicleSyncAdapter, vehicleTracker, appParams).execute();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        portalClient.destroy();
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        if (initVehicleTrackerTask != null && !AsyncTask.Status.FINISHED.equals(initVehicleTrackerTask.getStatus())) {
            initVehicleTrackerTask.cancel(true);
        }
        if (vehicleTracker != null) {
            vehicleTracker.stop();
        }
        try {
            unregisterReceiver(networkStatusReceiver);
        } catch (Exception e) {
            Log.d(TAG, "Exception during unregistration, Cause: " + e.getMessage());
        }
        vehicleSyncAdapter = null;
        vehicleTracker = null;
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
                GeoPoint homeLocation = appParams.getHomeLocation();
                if (homeLocation != null) {
                    moveToLocation(mapFragment.getMap(), homeLocation);
                }
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
