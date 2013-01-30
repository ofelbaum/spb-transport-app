package com.emal.android.transport.spb.map;

import android.graphics.drawable.Drawable;
import com.google.android.maps.*;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/22/12 2:35 AM
 */
public class MyPlaceOverlay extends ItemizedOverlay {
    private static final String TAG = MyPlaceOverlay.class.getName();
    private OverlayItem overlayItem;

    public MyPlaceOverlay(Drawable drawable, GeoPoint geoPoint) {
        super(boundCenterBottom(drawable));
        setPlace(geoPoint);

    }

    public void setPlace(GeoPoint geoPoint) {
        overlayItem = new OverlayItem(geoPoint, "", "");
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return overlayItem;
    }

    @Override
    public int size() {
        return overlayItem != null ? 1 : 0;
    }
}
