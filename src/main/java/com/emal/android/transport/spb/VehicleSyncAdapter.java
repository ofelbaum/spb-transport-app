package com.emal.android.transport.spb;

import android.graphics.Bitmap;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Set;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/19/13 12:47 AM
 */
public interface VehicleSyncAdapter {
    void beforeSync(boolean clearBeforeUpdate);
    void afterSync(Bitmap result);
    int getScreenWidth();
    int getScreenHeight();
    String getBBox();
    void setBBox(LatLngBounds latLngBounds);

}
