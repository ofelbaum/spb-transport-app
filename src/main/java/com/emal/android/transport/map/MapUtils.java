package com.emal.android.transport.map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import com.emal.android.R;
import com.emal.android.transport.map.MyPlaceOverlay;
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

    public static void showMyPlace(MapView mapView, GeoPoint homeLocation) {
        List<Overlay> overlays = mapView.getOverlays();
        for (Iterator<Overlay> iterator = overlays.iterator(); iterator.hasNext(); ) {
            Overlay overlay = iterator.next();
            if (overlay instanceof MyPlaceOverlay) {
                iterator.remove();
                break;
            }
        }
        overlays.add(new MyPlaceOverlay(homeLocation, mapView.getResources()));
        mapView.invalidate();
    }
}
