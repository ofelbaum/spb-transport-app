package com.emal.android.transport.spb.map;

import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import com.emal.android.transport.spb.R;
import com.emal.android.transport.spb.utils.ApplicationParams;
import com.emal.android.transport.spb.utils.Constants;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import java.util.Iterator;
import java.util.List;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/22/12 4:02 AM
 */
public class MapUtils {
    public static final int SPB_CENTER_LAT_DEF_VALUE = (int) (59.95f * 1E6);
    public static final int SPB_CENTER_LONG_DEF_VALUE = (int) (30.316667f * 1E6);

    public static AlertDialog createMyPlaceDialog(MapView mapView, DialogInterface.OnClickListener onYes, DialogInterface.OnClickListener onNo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mapView.getContext());
        Resources resources = mapView.getResources();
        String yes = resources.getString(R.string.yes);
        String no = resources.getString(R.string.no);
        builder.setCancelable(false)
               .setPositiveButton(yes, onYes)
               .setNegativeButton(no, onNo);
        return builder.create();
    }

    public static void addMyPlace(MapView mapView, GeoPoint homeLocation) {
        List<Overlay> overlays = mapView.getOverlays();
        for (Iterator<Overlay> iterator = overlays.iterator(); iterator.hasNext(); ) {
            Overlay overlay = iterator.next();
            if (overlay instanceof MyPlaceOverlay) {
                iterator.remove();
                break;
            }
        }

        Drawable drawable = mapView.getResources().getDrawable(R.drawable.btn_star_big_on);
        MyPlaceOverlay placeOverlay = new MyPlaceOverlay(drawable, homeLocation);
        overlays.add(placeOverlay);
        mapView.invalidate();
    }

    public static void redrawMyPlace(MapView mapView, GeoPoint homeLocation) {
        List<Overlay> overlays = mapView.getOverlays();
        for (Iterator<Overlay> iterator = overlays.iterator(); iterator.hasNext(); ) {
            Overlay overlay = iterator.next();
            if (overlay instanceof MyPlaceOverlay) {
                ((MyPlaceOverlay) overlay).setPlace(homeLocation);
                mapView.invalidate();
                break;
            }
        }
    }

    public static void saveGeoPoint(ContextWrapper wrapper, GeoPoint geoPoint) {
        SharedPreferences sharedPreferences = wrapper.getSharedPreferences(Constants.APP_SHARED_SOURCE, 0);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putInt(Constants.LAST_LOC_LAT_FLAG, geoPoint.getLatitudeE6());
        ed.putInt(Constants.LAST_LOC_LONG_FLAG, geoPoint.getLongitudeE6());
        ed.commit();
    }

    public static void saveGeoPoint(ApplicationParams applicationParams, Location location) {
        GeoPoint lastLocation = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
        applicationParams.setLastLocation(lastLocation);
    }
}
