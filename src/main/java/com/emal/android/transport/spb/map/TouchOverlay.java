package com.emal.android.transport.spb.map;

import android.content.ContextWrapper;
import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/10/13 5:43 PM
 */
public class TouchOverlay extends com.google.android.maps.Overlay {
    private ContextWrapper contextWrapper;

    public TouchOverlay(ContextWrapper contextWrapper) {
        this.contextWrapper = contextWrapper;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent, MapView mapView) {
        int actionId = motionEvent.getAction();

        if (actionId == MotionEvent.ACTION_UP) {
            GeoPoint geoPoint = mapView.getMapCenter();
            MapUtils.saveGeoPoint(contextWrapper, geoPoint);
            return true;
        }
        return super.onTouchEvent(motionEvent, mapView);
    }
}
