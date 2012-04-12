package com.emal.android;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import java.util.ArrayList;
import java.util.List;

public class MapOverlayItem extends ItemizedOverlay<OverlayItem> {
    private static final Drawable drawable = new BitmapDrawable(Resources.getSystem());
    private List<OverlayItem> items;
    private Vehicle vehicle;

    public MapOverlayItem(Vehicle vehicle) {
        super(drawable);
        this.vehicle = vehicle;
        items = new ArrayList<OverlayItem>();
    }

    public void addOverlay(OverlayItem overlay) {
        items.add(overlay);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return items.get(i);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean b) {
        super.draw(canvas, mapView, false); //we don't want to draw shadows
    }

    public static MapOverlayItem create(OverlayItem overlayItem, Vehicle vehicle) {
        MapOverlayItem mapOverlayItem = new MapOverlayItem(vehicle);
        mapOverlayItem.addOverlay(overlayItem);
        return mapOverlayItem;
    }
}