package com.emal.android;

import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import java.util.Iterator;
import java.util.List;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/12/12 3:42 AM
 */
public class MapOverlayUpdater {
    private OverlayItem overlayItem;
    private Vehicle vehicle;

    public MapOverlayUpdater(OverlayItem overlayItem, Vehicle vehicle) {
        this.overlayItem = overlayItem;
        this.vehicle = vehicle;
    }

    public void update(MapView mapView) {
        MapOverlayItem mapOverlayItem = MapOverlayItem.create(overlayItem, vehicle);

        List<Overlay> mapOverlays = mapView.getOverlays();
        Iterator<Overlay> iterator = mapOverlays.iterator();
        while (iterator.hasNext()) {
            Overlay overlay = iterator.next();
            if(overlay instanceof MyLocationOverlay) {
                continue;
            }
            iterator.remove();
        }
        mapOverlays.add(mapOverlayItem);
    }
}
