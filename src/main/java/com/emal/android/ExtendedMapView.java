package com.emal.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import com.google.android.maps.MapView;

import java.util.Set;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/15/12 1:36 AM
 */
public class ExtendedMapView extends MapView {
    private static final String TAG = ExtendedMapView.class.getName();
    private int oldZoomLevel = -1;
    private Set<Vehicle> vehicles;

    public ExtendedMapView(Context context, String s) {
        super(context, s);
    }

    public ExtendedMapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ExtendedMapView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (getZoomLevel() != oldZoomLevel) {
            Log.d(TAG, "ZOOM event");
            oldZoomLevel = getZoomLevel();

            for (Vehicle vehicle : vehicles) {
                UpdateOverlayItemAsyncTask task = new UpdateOverlayItemAsyncTask(this, vehicle);
                AsyncTask<String, Void, Bitmap> asyncTask = task.execute();
            }
        }
    }

    public void setVehicles(Set<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }
}
