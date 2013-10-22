package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.emal.android.transport.spb.*;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.emal.android.transport.spb.utils.ApplicationParams;
import com.emal.android.transport.spb.utils.Constants;
import com.emal.android.transport.spb.utils.GeoConverter;
import com.google.android.gms.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.maps.GeoPoint;

import java.io.IOException;
import java.util.List;
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
    private AlertDialog alert;
    private Address longPressLoc;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;

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
        final Activity activity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gmapsv2);

        mTitle = mDrawerTitle = getTitle();
        mPlanetTitles = getResources().getStringArray(com.emal.android.transport.spb.R.array.planets_array);
        mDrawerLayout = (DrawerLayout) findViewById(com.emal.android.transport.spb.R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(com.emal.android.transport.spb.R.id.left_drawer);

        // set activity custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(com.emal.android.transport.spb.R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                com.emal.android.transport.spb.R.layout.drawer_list_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1: {
                        mDrawerLayout.closeDrawer(mDrawerList);
                        moveToLocation(appParams.getHomeLocation());

                        break;
                    }
                    case 2: {
                        startActivity(new Intent(activity, PreferencesActivity.class));
                        break;
                    }
                    default: {
                        mDrawerLayout.closeDrawer(mDrawerList);
                    }
                }
            }
        });

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                com.emal.android.transport.spb.R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                com.emal.android.transport.spb.R.string.drawer_open,  /* "open drawer" description for accessibility */
                com.emal.android.transport.spb.R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        initApplication();
//        getActionBar().hide();
        ImageView settingButton = (ImageView) findViewById(R.id.settingsButton);

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, PreferencesActivity.class));
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
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "Long press. Point " + latLng);

                        Geocoder geo = new Geocoder(mapFragment.getActivity().getApplicationContext());
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

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        vehicleSyncAdapter = new GMapVehicleSyncAdapter(mapFragment);
        newVehicleTracker = new NewVehicleTracker(vehicleSyncAdapter);

        GeoPoint home = appParams.getHomeLocation();
        LatLng homePoint = new LatLng(home.getLatitudeE6() / 1E6, home.getLongitudeE6() / 1E6);
        mMap.addMarker(new MarkerOptions().position(homePoint).title(getResources().getString(R.string.my_place)));



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Resources resources = getResources();
        String yes = resources.getString(com.emal.android.transport.spb.R.string.yes);
        String no = resources.getString(com.emal.android.transport.spb.R.string.no);
        builder.setCancelable(false)
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //longPressLoc
                        GeoPoint homeLocation = new GeoPoint((int)(longPressLoc.getLatitude() * 1E6), (int)(longPressLoc.getLongitude() * 1E6));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.emal.android.transport.spb.R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(com.emal.android.transport.spb.R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            case com.emal.android.transport.spb.R.id.action_websearch:
                // create intent to perform web search for this planet
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
                // catch event that there's no activity to handle intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, com.emal.android.transport.spb.R.string.app_not_available, Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}
