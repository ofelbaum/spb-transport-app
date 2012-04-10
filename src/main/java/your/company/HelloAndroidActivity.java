package your.company;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextPaint;
import com.google.android.maps.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;

public class HelloAndroidActivity extends MapActivity {

    private GeoPoint p = new GeoPoint(59950000, 30316667);
    private MyItem myItem;
    private List<Overlay> mapOverlays;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        MapController ctrl = mapView.getController();

        ctrl.setCenter(p);
        ctrl.setZoom(12);                 //Set scale
        mapView.setBuiltInZoomControls(true); //Enable zoom controls

    }

    protected void onResume() {
        System.out.println("onResume");
        super.onResume();

        final MapView mapView = (MapView) findViewById(R.id.mapView);
        mapOverlays = mapView.getOverlays();

        final OverlayItem overlayitem = new OverlayItem(p, "Title", "Snippet");

        Bitmap result = BitmapFactory.decodeResource(getResources(), R.drawable.mapserv);
        Drawable drawable = new BitmapDrawable(result);
        myItem = new MyItem(drawable);
        myItem.addOverlay(overlayitem);

        mapOverlays.add(myItem);


        final MapOverlayItemMarkerAsyncTask task = new MapOverlayItemMarkerAsyncTask(overlayitem, mapView);
        AsyncTask<String, Void, Bitmap> asyncTask = task.execute("http://transport.orgp.spb.ru/cgi-bin/mapserv?TRANSPARENT=TRUE&FORMAT=image%2Fpng&LAYERS=vehicle_bus&MAP=vehicle_typed.map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&SRS=EPSG%3A900913&_OLSALT=0.1508798657450825&BBOX=3321583.9050831,8354817.803403,3433590.4458589,8415634.772084&WIDTH=1431&HEIGHT=777");


//        List<Overlay> listOfOverlays = mapView.getOverlays();
//        listOfOverlays.clear();
//
//        final Drawable drawable = this.getResources().getDrawable(R.drawable.mapserv);
//        //ScaleDrawable drawable = new ScaleDrawable(sourceDrawable, 1, 10, 10);
//
//        drawable.setBounds(mapView.getLeft(), mapView.getTop(), mapView.getRight(), mapView.getBottom());
//        MyItem overlay = new MyItem(drawable);
//        listOfOverlays.add(overlay);
//        mapView.invalidate();
    }

    public class MyItem extends ItemizedOverlay<OverlayItem> {
        private List<OverlayItem> items = new ArrayList<OverlayItem>();

        public MyItem(Drawable drawable) {
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
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

    class MapOverlay extends com.google.android.maps.Overlay {
        @Override
        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
            super.draw(canvas, mapView, shadow);

            //---translate the GeoPoint to screen pixels---
            Point screenPts = new Point();
            mapView.getProjection().toPixels(p, screenPts);

            //---add the marker---
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.mapserv);

            canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 50, null);

            return true;
        }
    }

}
