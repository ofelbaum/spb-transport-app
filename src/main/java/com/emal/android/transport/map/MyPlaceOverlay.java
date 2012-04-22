package com.emal.android.transport.map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import com.emal.android.R;
import com.google.android.maps.*;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/22/12 2:35 AM
 */
public class MyPlaceOverlay extends Overlay {
    private GeoPoint p;
    private Resources resources;

    public MyPlaceOverlay(GeoPoint p, Resources resources) {
        this.p = p;
        this.resources = resources;
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView,
                        boolean shadow, long when) {
        super.draw(canvas, mapView, shadow);

        //---translate the GeoPoint to screen pixels---
        Point screenPts = new Point();
        mapView.getProjection().toPixels(p, screenPts);

        //---add the marker---
        Bitmap bmp = BitmapFactory.decodeResource(resources, R.drawable.btn_star_big_on);
        canvas.drawBitmap(bmp, screenPts.x, screenPts.y, null);
        return true;
    }
}
