package com.emal.android.transport.spb.model;

import android.content.SharedPreferences;
import android.util.Log;
import com.emal.android.transport.spb.MapProviderType;
import com.emal.android.transport.spb.VehicleType;
import com.emal.android.transport.spb.utils.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/18/13 2:38 AM
 */
public class ApplicationParams {
    public static final int SPB_CENTER_LAT_DEF_VALUE = (int) (59.95f * 1E6);
    public static final int SPB_CENTER_LONG_DEF_VALUE = (int) (30.316667f * 1E6);

    private static final String TAG = "ApplicationParams";
    private SharedPreferences sharedPreferences;
    private boolean showBus = false;
    private boolean showTrolley = false;
    private boolean showTram = false;
    private boolean showShip = false;
    private boolean satView = true;
    private boolean showTraffic = false;
    private MapProviderType mapProviderType;
    private int syncTime = Constants.DEFAULT_SYNC_MS;
    private int zoomSize;
    private GeoPoint homeLocation;
    private GeoPoint lastLocation;
    private Set<String> routesToTrack;
    private Theme theme;
    private int iconSize;

    public ApplicationParams(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        this.showBus = sharedPreferences.getBoolean(Constants.SHOW_BUS_FLAG, true);
        this.showTrolley = sharedPreferences.getBoolean(Constants.SHOW_TROLLEY_FLAG, true);
        this.showTram = sharedPreferences.getBoolean(Constants.SHOW_TRAM_FLAG, true);
        this.showShip = sharedPreferences.getBoolean(Constants.SHOW_SHIP_FLAG, true);
        this.showTraffic = sharedPreferences.getBoolean(Constants.SHOW_TRAFFIC_FLAG, false);
        this.syncTime = sharedPreferences.getInt(Constants.SYNC_TIME_FLAG, Constants.DEFAULT_SYNC_MS);
        this.iconSize = sharedPreferences.getInt(Constants.ICON_SIZE_FLAG, Constants.DEFAULT_ICON_SIZE);

        Integer homeLat = sharedPreferences.getInt(Constants.HOME_LOC_LAT_FLAG, SPB_CENTER_LAT_DEF_VALUE);
        Integer homeLong = sharedPreferences.getInt(Constants.HOME_LOC_LONG_FLAG, SPB_CENTER_LONG_DEF_VALUE);
        this.homeLocation = new GeoPoint(homeLat, homeLong);

        Integer currLat = sharedPreferences.getInt(Constants.LAST_LOC_LAT_FLAG, SPB_CENTER_LAT_DEF_VALUE);
        Integer currLong = sharedPreferences.getInt(Constants.LAST_LOC_LONG_FLAG, SPB_CENTER_LONG_DEF_VALUE);
        this.lastLocation = new GeoPoint(currLat, currLong);

        this.satView = sharedPreferences.getBoolean(Constants.SAT_VIEW_FLAG, false);
        this.mapProviderType = MapProviderType.getByValue(sharedPreferences.getString(Constants.MAP_PROVIDER_TYPE_FLAG, MapProviderType.GMAPSV2.name()));
        this.zoomSize = sharedPreferences.getInt(Constants.ZOOM_FLAG, Constants.DEFAULT_ZOOM_LEVEL);
        Set<String> stringSet = sharedPreferences.getStringSet(Constants.ROUTES_TO_TRACK, Collections.<String>emptySet());
        this.routesToTrack = new ConcurrentSkipListSet<String>(stringSet);
        this.theme = Theme.valueOf(sharedPreferences.getString(Constants.THEME_FLAG, Theme.HOLO_LIGHT.name()));
    }

    public boolean isShowBus() {
        return showBus;
    }

    public boolean isShowTrolley() {
        return showTrolley;
    }

    public boolean isShowTram() {
        return showTram;
    }

    public boolean isShowShip() {
        return showShip;
    }

    public boolean isSatView() {
        return satView;
    }

    public boolean isShowTraffic() {
        return showTraffic;
    }

    public int getSyncTime() {
        return syncTime;
    }

    public int getZoomSize() {
        return zoomSize;
    }

    public GeoPoint getHomeLocation() {
        return homeLocation;
    }

    public GeoPoint getLastLocation() {
        return lastLocation;
    }

    public MapProviderType getMapProviderType() {
        return mapProviderType;
    }

    public void setShowBus(boolean showBus) {
        this.showBus = showBus;
    }

    public void setShowTrolley(boolean showTrolley) {
        this.showTrolley = showTrolley;
    }

    public void setShowTram(boolean showTram) {
        this.showTram = showTram;
    }

    public void setShowShip(boolean showShip) {
        this.showShip = showShip;
    }

    public void setSatView(boolean satView) {
        this.satView = satView;
    }

    public void setShowTraffic(boolean showTraffic) {
        this.showTraffic = showTraffic;
    }

    public void setMapProviderType(MapProviderType mapProviderType) {
        this.mapProviderType = mapProviderType;
    }

    public void setSyncTime(int syncTime) {
        this.syncTime = syncTime;
    }

    public void setZoomSize(int zoomSize) {
        this.zoomSize = zoomSize;
    }

    public void setHomeLocation(GeoPoint homeLocation) {
        this.homeLocation = homeLocation;
    }

    public void setLastLocation(GeoPoint lastLocation) {
        this.lastLocation = lastLocation;
    }

    public Set<String> getRoutesToTrack() {
        Log.d(TAG, "getRoutesToTrack: " + routesToTrack.getClass().getCanonicalName());
        for (String s : routesToTrack) {
            Log.d(TAG, s);
        }
        return routesToTrack;
    }

    public List<VehicleType> getSelectedVehicleTypes() {
        List<VehicleType> list = new ArrayList<VehicleType>();
        if (isShowBus()) {
            list.add(VehicleType.BUS);
        }
        if (isShowTram()) {
            list.add(VehicleType.TRAM);
        }
        if (isShowTrolley()) {
            list.add(VehicleType.TROLLEY);
        }
        if (isShowShip()) {
            list.add(VehicleType.SHIP);
        }

        return list;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public int getIconSize() {
        return iconSize;
    }

    public void setIconSize(int iconSize) {
        this.iconSize = iconSize;
    }

    public void saveAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.SYNC_TIME_FLAG, syncTime);
        editor.putString(Constants.MAP_PROVIDER_TYPE_FLAG, mapProviderType.name());
        editor.putBoolean(Constants.SHOW_BUS_FLAG, Boolean.TRUE.equals(showBus));
        editor.putBoolean(Constants.SHOW_TRAM_FLAG, Boolean.TRUE.equals(showTram));
        editor.putBoolean(Constants.SHOW_TROLLEY_FLAG, Boolean.TRUE.equals(showTrolley));
        editor.putBoolean(Constants.SHOW_SHIP_FLAG, Boolean.TRUE.equals(showShip));
        editor.putBoolean(Constants.SHOW_TRAFFIC_FLAG, Boolean.TRUE.equals(showTraffic));
        editor.putBoolean(Constants.SAT_VIEW_FLAG, Boolean.TRUE.equals(satView));
        editor.putInt(Constants.LAST_LOC_LAT_FLAG, lastLocation.getLatitudeE6());
        editor.putInt(Constants.LAST_LOC_LONG_FLAG, lastLocation.getLongitudeE6());
        editor.putInt(Constants.HOME_LOC_LAT_FLAG, homeLocation.getLatitudeE6());
        editor.putInt(Constants.HOME_LOC_LONG_FLAG, homeLocation.getLongitudeE6());
        editor.putInt(Constants.ZOOM_FLAG, zoomSize);
        editor.putInt(Constants.ICON_SIZE_FLAG, iconSize);
        editor.putStringSet(Constants.ROUTES_TO_TRACK, routesToTrack);
        editor.putString(Constants.THEME_FLAG, theme.name());

        Log.d(TAG, "Saving app prefs: " + this);

        editor.commit();

    }

    public void resetVehicles() {
        showBus = false;
        showShip = false;
        showTram = false;
        showTrolley = false;
    }

    public void setLastLocation(LatLng location) {
        GeoPoint geoPoint = new GeoPoint((int)(location.latitude * 1E6), (int)(location.longitude * 1E6));
        setLastLocation(geoPoint);
    }

    @Override
    public String toString() {
        return "ApplicationParams{" +
                "showBus=" + showBus +
                ", showTrolley=" + showTrolley +
                ", showTram=" + showTram +
                ", showShip=" + showShip +
                ", satView=" + satView +
                ", mapProviderType=" + mapProviderType +
                ", syncTime=" + syncTime +
                ", zoomSize=" + zoomSize +
                ", homeLocation=" + homeLocation +
                ", lastLocation=" + lastLocation +
                '}';
    }
}
