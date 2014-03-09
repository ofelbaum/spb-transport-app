package com.emal.android.transport.spb;

import android.graphics.Bitmap;
import com.emal.android.transport.spb.portal.PortalClient;
import com.emal.android.transport.spb.portal.Route;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

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

    void moveCamera(CameraUpdate cameraUpdate);

    float getScaleFactor();

    void setIconSize(int iconSize);

    int getSyncTime();

    void setSyncTime(int syncTime);

    void updateMarkers(Route route, List<Marker> markers);

    void removeMarkers(Route key);
}
