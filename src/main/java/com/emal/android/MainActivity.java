package com.emal.android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.maps.*;

import java.util.List;

public class MainActivity extends MapActivity {
    private static final String URL_TEMPLATE = "http://transport.orgp.spb.ru/cgi-bin/mapserv?TRANSPARENT=TRUE&FORMAT=image%2Fpng&MAP=vehicle_typed.map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&SRS=EPSG%3A900913&_OLSALT=0.1508798657450825";
    private static final String URL_PARAMS = "&LAYERS=%s&BBOX=%s&WIDTH=%d&HEIGHT=%d";
    private MapView mapView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        mapView.displayZoomControls(true);
        mapView.getController().setZoom(15);

        MyLocationOverlay mylocationOverlay = new MyLocationOverlay(mapView.getContext(), mapView);
        mylocationOverlay.enableMyLocation();
        mapView.getOverlays().add(mylocationOverlay);

        moveToCurrentLocation();

        mapView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            private MapOverlayItem newMapOverlayItem;
            private MapOverlayItem oldMapOverlayItem;

            public boolean onGenericMotion(View v, MotionEvent event) {
                if (MotionEvent.ACTION_HOVER_MOVE == event.getAction()) {
                    MapView mapView = (MapView) v;

                    int screenWidth = mapView.getWidth();
                    int screenHeight = mapView.getHeight();


                    String url = null;
                    String bbox = calculateBBox(mapView);
                    Object[] params = new Object[]{Vehicle.BUS.getCode(), bbox, screenWidth, screenHeight};
                    url = URL_TEMPLATE + String.format(URL_PARAMS, params);

                    OverlayItem overlayItem = new OverlayItem(mapView.getMapCenter(), null, null);
                    newMapOverlayItem = createMapOverlayItem(overlayItem);
                    oldMapOverlayItem = updateMapOverlay(newMapOverlayItem, oldMapOverlayItem);

                    final MapOverlayItemMarkerAsyncTask task = new MapOverlayItemMarkerAsyncTask(overlayItem, mapView);
                    AsyncTask<String, Void, Bitmap> asyncTask = task.execute(url);

                    return true;
                }

                return false;
            }
        });

    }

    private String calculateBBox(MapView mapView) {
        int latitudeSpan = mapView.getLatitudeSpan();
        int longitudeSpan = mapView.getLongitudeSpan();
        int latitudeE6 = mapView.getMapCenter().getLatitudeE6();
        int longitudeE6 = mapView.getMapCenter().getLongitudeE6();

        int x1 = latitudeE6 - latitudeSpan / 2;
        int y1 = longitudeE6 - longitudeSpan / 2;
        int x2 = latitudeE6 + latitudeSpan / 2;
        int y2 = longitudeE6 + longitudeSpan / 2;


        Pair<Double, Double> p1 = GeoConverter.fromLatLonToMeters(x1 / 1E6, y1 / 1E6);
        Pair<Double, Double> p2 = GeoConverter.fromLatLonToMeters(x2 / 1E6, y2 / 1E6);

        //Toast.makeText(mapView.getContext(), bbox, Toast.LENGTH_LONG).show();
        return p1.first.toString() + "," + p1.second.toString() + "," + p2.first.toString() + "," + p2.second.toString();
    }

    private void moveToCurrentLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        GeoPoint currentPoint = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
        mapView.getController().animateTo(currentPoint);
    }

    private MapOverlayItem updateMapOverlay(MapOverlayItem mapOverlayItem, MapOverlayItem oldItem) {
        List<Overlay> mapOverlays = mapView.getOverlays();
        if (oldItem != null) {
            mapOverlays.remove(oldItem);
        }
        mapOverlays.add(mapOverlayItem);
        return mapOverlayItem;
    }

    private MapOverlayItem createMapOverlayItem(OverlayItem overlayItem) {
        Drawable drawable = new BitmapDrawable(Resources.getSystem());
        MapOverlayItem mapOverlayItem = new MapOverlayItem(drawable);
        mapOverlayItem.addOverlay(overlayItem);
        return mapOverlayItem;
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
