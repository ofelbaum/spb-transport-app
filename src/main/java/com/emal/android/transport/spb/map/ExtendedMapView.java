package com.emal.android.transport.spb.map;

import android.content.Context;

import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.emal.android.transport.spb.VehicleTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/15/12 1:36 AM
 */
public class ExtendedMapView extends MapView {

    public interface OnLongpressListener {
        public void onLongpress(MapView view, int x, int y);
    }

    public interface OnZoomListener {
        public void onZoom(int oldZoomValue, int newZoomValue);
    }

    private static final String TAG = ExtendedMapView.class.getName();
    private int oldZoomLevel = -1;
    private VehicleTracker vehicleTracker;

    /**
     * Time in ms before the OnLongpressListener is triggered.
     */
    static final int LONGPRESS_THRESHOLD = 500;

    /**
     * Keep a record of the center of the map, to know if the map
     * has been panned.
     */
    private GeoPoint lastMapCenter;

    private Timer longpressTimer = new Timer();
    private OnLongpressListener longpressListener;
    private OnZoomListener onZoomListener;

    public ExtendedMapView(Context context, String apiKey) {
        super(context, apiKey);
    }

    public ExtendedMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setVehicleTracker(VehicleTracker vehicleTracker) {
        this.vehicleTracker = vehicleTracker;
    }

    public void setOnLongpressListener(OnLongpressListener listener) {
        longpressListener = listener;
    }

    public void setOnZoomListener(OnZoomListener onZoomListener) {
        this.onZoomListener = onZoomListener;
    }

    /**
     * This method is called every time user touches the map,
     * drags a finger on the map, or removes finger from the map.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleLongpress(event);

        return super.onTouchEvent(event);
    }

    /**
     * This method takes MotionEvents and decides whether or not
     * a longpress has been detected. This is the meat of the
     * OnLongpressListener.
     * <p/>
     * The Timer class executes a TimerTask after a given time,
     * and we start the timer when a finger touches the screen.
     * <p/>
     * We then listen for map movements or the finger being
     * removed from the screen. If any of these events occur
     * before the TimerTask is executed, it gets cancelled. Else
     * the listener is fired.
     *
     * @param event
     */
    private void handleLongpress(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Finger has touched screen.
            longpressTimer = new Timer();
            longpressTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    longpressListener.onLongpress(ExtendedMapView.this, (int) event.getX(), (int) event.getY());
                }

            }, LONGPRESS_THRESHOLD);

            lastMapCenter = getMapCenter();
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (!getMapCenter().equals(lastMapCenter)) {
                // User is panning the map, this is no longpress
                longpressTimer.cancel();
            }

            lastMapCenter = getMapCenter();
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            // User has removed finger from map.
            longpressTimer.cancel();
        }

        if (event.getPointerCount() > 1) {
            // This is a multitouch event, probably zooming.
            longpressTimer.cancel();
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int zoomLevel = getZoomLevel();
        if (zoomLevel != oldZoomLevel) {
            Log.d(TAG, "ZOOM event");
            if (vehicleTracker != null) {
                vehicleTracker.syncVehicles(true);
            }
            onZoomListener.onZoom(oldZoomLevel, zoomLevel);
            oldZoomLevel = zoomLevel;
        }
    }
}
