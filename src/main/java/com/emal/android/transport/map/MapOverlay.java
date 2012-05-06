package com.emal.android.transport.map;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import com.emal.android.VehicleTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/15/12 9:34 PM
 */
public class MapOverlay extends ItemizedOverlay<OverlayItem> {
    private static final String TAG = "MapOverlay";
    private static final Drawable drawable = new BitmapDrawable(Resources.getSystem());
    private OverlayItem item;
    private VehicleTracker vehicleTracker;

    public MapOverlay(VehicleTracker vehicleTracker) {
        super(drawable);
        this.item = defaultOverlayItem();
        this.vehicleTracker = vehicleTracker;
        populate();
    }

    public void addItem(OverlayItem item) {
        this.item = item;
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return item;
    }

    @Override
    public int size() {
        return item != null ? 1 : 0;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean b) {
        super.draw(canvas, mapView, false); //we don't want to draw shadows
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent, MapView mapView) {
        boolean result = super.onTouchEvent(motionEvent, mapView);

        if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
            vehicleTracker.syncVehicles();
        }
        return result;
    }

    private static OverlayItem defaultOverlayItem() {
        return new OverlayItem(new GeoPoint(0, 0), "", "");
    }
}