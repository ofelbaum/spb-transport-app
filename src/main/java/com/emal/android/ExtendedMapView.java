package com.emal.android;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import com.google.android.maps.MapView;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/15/12 1:36 AM
 */
public class ExtendedMapView extends MapView {
    private static final String TAG = ExtendedMapView.class.getName();
    private int oldZoomLevel = -1;
    private VehicleTracker vehicleTracker;

    public ExtendedMapView(Context context, String s) {
        super(context, s);
    }

    public ExtendedMapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ExtendedMapView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setVehicleTracker(VehicleTracker vehicleTracker) {
        this.vehicleTracker = vehicleTracker;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (getZoomLevel() != oldZoomLevel) {
            Log.d(TAG, "ZOOM event");
            oldZoomLevel = getZoomLevel();
            vehicleTracker.syncAll(true);
        }
        super.dispatchDraw(canvas);
    }
}
