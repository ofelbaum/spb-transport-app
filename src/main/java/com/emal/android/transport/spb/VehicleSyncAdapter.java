package com.emal.android.transport.spb;

import android.graphics.Bitmap;
import com.emal.android.transport.spb.portal.PortalClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/19/13 12:47 AM
 */
public interface VehicleSyncAdapter {
    void beforeSync(boolean clearBeforeUpdate);
    void afterSync(Bitmap result);
    void afterSync(boolean result);
    int getScreenWidth();
    int getScreenHeight();
    String getBBox();
    void setBBox();
    void clearOverlay();

    Marker addMarker(MarkerOptions title);
    PortalClient getPortalClient();
    void hideError();
    void showError();

    void setTrafficEnabled(boolean showTraffic);

    void setMapType(int i);

    void moveCamera(CameraUpdate cameraUpdate);
}
