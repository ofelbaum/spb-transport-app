package com.emal.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.google.android.maps.*;

import java.util.List;

public class MainActivity extends MapActivity {
    private MapView mapView;
    private LocationManager lm;
    private GeoPoint currentPoint;
    private MapOverlayItem mapOverlayItem;
    private List<Overlay> mapOverlays;

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
        mapOverlays = mapView.getOverlays();

        MyLocationOverlay mylocationOverlay = new MyLocationOverlay(mapView.getContext(), mapView);
        mylocationOverlay.enableMyLocation();
        mapView.getOverlays().add(mylocationOverlay);


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
            public void onLocationChanged(Location location) {
                currentPoint = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
                mapView.getController().animateTo(currentPoint);
            }

            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            public void onProviderEnabled(String s) {
            }

            public void onProviderDisabled(String s) {
            }
        });

        final OverlayItem overlayitem = new OverlayItem(mapView.getMapCenter(), "Title", "Snippet");

        Bitmap result = BitmapFactory.decodeResource(getResources(), R.drawable.autobus);
        Drawable drawable = new BitmapDrawable(result);
        mapOverlayItem = new MapOverlayItem(drawable);
        mapOverlayItem.addOverlay(overlayitem);
        mapOverlays.add(mapOverlayItem);

        mapView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            public boolean onGenericMotion(View v, MotionEvent event) {
                if (MotionEvent.ACTION_HOVER_MOVE == event.getAction()) {
                    MapView mapView = (MapView) v;

                    int latitudeSpan = mapView.getLatitudeSpan();
                    int longitudeSpan = mapView.getLongitudeSpan();
                    int latitudeE6 = mapView.getMapCenter().getLatitudeE6();
                    int longitudeE6 = mapView.getMapCenter().getLongitudeE6();
                    int windowH = mapView.getHeight();
                    int windowW = mapView.getWidth();

                    int x1 = latitudeE6 + latitudeSpan / 2;
                    int y1 = longitudeE6 - longitudeSpan / 2;
                    int x2 = latitudeE6 - latitudeSpan / 2;
                    int y2 = longitudeE6 + longitudeSpan / 2;
                    String str = "[" + windowH + " ," + windowW + "] [" + latitudeSpan + " ," + longitudeSpan + "] {" + x1 + "," + y1 + "} - {" + x2 + "," + y2 + "}";
                    System.out.println(str);

                    Toast.makeText(mapView.getContext(), str, Toast.LENGTH_LONG).show();

                    final MapOverlayItemMarkerAsyncTask task = new MapOverlayItemMarkerAsyncTask(overlayitem, mapView);
                    AsyncTask<String, Void, Bitmap> asyncTask = task.execute("http://transport.orgp.spb.ru/cgi-bin/mapserv?TRANSPARENT=TRUE&FORMAT=image%2Fpng&LAYERS=vehicle_bus&MAP=vehicle_typed.map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&SRS=EPSG%3A900913&_OLSALT=0.1508798657450825&BBOX=3321583.9050831,8354817.803403,3433590.4458589,8415634.772084&WIDTH=320&HEIGHT=483");

                    return true;
                }

                return false;
            }
        });

    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
