package com.emal.android;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import java.util.ArrayList;
import java.util.List;

public class MapOverlayItem extends ItemizedOverlay<OverlayItem> {
    private List<OverlayItem> items = new ArrayList<OverlayItem>();

    public MapOverlayItem(Drawable drawable) {
        super(drawable);
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
}