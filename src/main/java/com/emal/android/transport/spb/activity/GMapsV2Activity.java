package com.emal.android.transport.spb.activity;

import android.app.AlertDialog;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.view.MenuItem;
import android.widget.*;
import com.emal.android.transport.spb.*;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.emal.android.transport.spb.VehicleType;
import com.emal.android.transport.spb.model.ApplicationParams;
import com.emal.android.transport.spb.model.MenuItemAdapter;
import com.emal.android.transport.spb.model.MenuModel;
import com.emal.android.transport.spb.model.Theme;
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
public class GMapsV2Activity extends FragmentActivity {
    private static final String TAG = GMapsV2Activity.class.getName();

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private ApplicationParams appParams;
    private VehicleTracker vehicleTracker;
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
    private String[] menuItems;
    private PortalClient portalClient = PortalClient.getInstance();
    private List<Marker> stopsMarkers = new ArrayList<Marker>();
    private boolean mMapIsTouched = false;
    private Menu menu;
    private ErrorSignCallback errorSignCallback;
    private BroadcastReceiver networkStatusReceiver;
    private Theme currentTheme;

    public class MapUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            int syncTime = appParams.getSyncTime();
            Log.d(TAG, "START Timer Update " + Thread.currentThread().getName() + " with time " + syncTime);
            vehicleTracker.syncVehicles();
            mHandler.postDelayed(this, syncTime);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appParams = new ApplicationParams(getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));

        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        currentTheme = appParams.getTheme();
        setTheme(currentTheme.getCode());
        setContentView(R.layout.main);

        mTitle = mDrawerTitle = getTitle();
        menuItems = getResources().getStringArray(com.emal.android.transport.spb.R.array.menu_items_array);
        mDrawerLayout = (DrawerLayout) findViewById(com.emal.android.transport.spb.R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(com.emal.android.transport.spb.R.id.left_drawer);

        // set activity custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        MenuModel menuModel = new MenuModel(getResources());

        mDrawerList.setAdapter(new MenuItemAdapter(this, com.emal.android.transport.spb.R.layout.drawer_list_item, menuModel));

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                mDrawerLayout.closeDrawer(mDrawerList);
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
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
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

        errorSignCallback = new ErrorSignCallback() {
            @Override
            public void show() {
                if (menu == null) {
                    return;
                }
                menu.getItem(0).setVisible(true);
            }

            @Override
            public void hide() {
                if (menu == null) {
                    return;
                }

                menu.getItem(0).setVisible(false);
            }

            @Override
            public boolean isShowed() {
                return menu != null && menu.getItem(0).isVisible();
            }
        };

        setUpMapIfNeeded();

        initApplication();
    }

    private void startSync() {
        Log.d(TAG, "startSync");
        GoogleMap map = mapFragment.getMap();
        if (map == null) {
            TimerTask waitForMapTask = new TimerTask() {
                @Override
                public void run() {
                    startSync();
                }
            };
            mHandler.removeCallbacks(waitForMapTask);
            mHandler.postDelayed(waitForMapTask, 50);
            return;
        }
        LatLngBounds latLngBounds = map.getProjection().getVisibleRegion().latLngBounds;
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


        mMap.setTrafficEnabled(appParams.isShowTraffic());
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
                if (Boolean.FALSE.equals(mMapIsTouched)) {
                    startSync();
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

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        vehicleSyncAdapter = new GMapVehicleSyncAdapter(this, mapFragment, errorSignCallback);
        vehicleTracker = new VehicleTracker(vehicleSyncAdapter, portalClient, mMap);

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
    }

    private void moveToLocation(GeoPoint geoPoint) {
        Log.d(TAG, "Move to location: " + geoPoint.toString());
        LatLng latLng = new LatLng(geoPoint.getLatitudeE6() / 1E6, geoPoint.getLongitudeE6() / 1E6);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, appParams.getZoomSize()));
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
                        new LoadTrackRoutesTask(routesToTrack, portalClient, vehicleTracker, errorSignCallback).execute();
                    } else {
                        Log.d(TAG, "Start all routes tracking");

                    }
                    isConnected = true;
                }
            }
        };
        registerReceiver(networkStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        initApplication();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
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
        mHandler.removeCallbacks(timerTask);
        if (timerTask != null) {
            timerTask.cancel();
        }
        vehicleTracker.stopTracking();
        appParams.saveAll();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
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
        switch (item.getItemId()) {
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

            }
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

    public void setTouched(boolean touched) {
        this.mMapIsTouched = touched;
    }
}
