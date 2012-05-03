package com.emal.android;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import com.emal.android.transport.map.MapOverlay;
import com.emal.android.transport.utils.Constants;
import com.emal.android.transport.utils.GeoConverter;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;

import java.io.*;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/10/12 2:02 AM
 */
public class UpdateOverlayItemAsyncTask extends AsyncTask<String, Void, Bitmap> {
    private static final String TAG = "UpdateOverlayItemAsyncTask";

    private boolean clearBeforeUpdate;
    private MapView mapView;
    private Vehicle vehicle;
    private OverlayItem overlayItem;
    private VehicleTracker vehicleTracker;


    public UpdateOverlayItemAsyncTask(Vehicle vehicle, VehicleTracker vehicleTracker, boolean clearBeforeUpdate) {
        this.mapView = vehicleTracker.getMapView();
        this.vehicle = vehicle;
        this.vehicleTracker = vehicleTracker;
        this.clearBeforeUpdate = clearBeforeUpdate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (clearBeforeUpdate) {
            dropOverlayItem();
        }
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        overlayItem = new OverlayItem(mapView.getMapCenter(), "", "");

        String bbox = GeoConverter.calculateBBox(mapView);
        int screenWidth = mapView.getWidth();
        int screenHeight = mapView.getHeight();
        Object[] paramss = new Object[]{vehicle.getCode(), bbox, screenWidth, screenHeight};
        String url = Constants.URL_TEMPLATE + String.format(Constants.URL_PARAMS, paramss);


        long start = System.currentTimeMillis();
        Log.d(TAG, "Download " + vehicle + " START for " + Thread.currentThread().getName());

        Bitmap bitmap = null;
        InputStream in = null;
        try {
            in = fetch(url);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            bitmap = BitmapFactory.decodeStream(in, null, options);
            in.close();
            return bitmap;
        } catch (IOException e) {
            String message = e.getMessage();
            Log.e(TAG, message != null ? message : e.getClass().getName());
            Log.d(TAG, "Download " + vehicle + " CANCELLED for " + Thread.currentThread().getName());
            return null;
        } finally {
            double duration = (System.currentTimeMillis() - start) / 1000d;
            Log.d(TAG, "Download " + vehicle + " FINISHED takes " + duration + " sec for " + Thread.currentThread().getName());
        }
    }

    private InputStream fetch(String address) throws IOException {
        HttpClient httpclient = vehicleTracker.getHttpClient();
        HttpGet httpRequest = new HttpGet(URI.create(address));
        httpRequest.setHeader("User-Agent","Mozilla/5.0 (X11; Linux i686)");
        HttpResponse response = httpclient.execute(httpRequest);
        HttpEntity entity = response.getEntity();
        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
        return bufHttpEntity.getContent();
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (isCancelled()) {
            Log.d(TAG, "onPostExecute skipped");
            return;
        }
        super.onPostExecute(result);
        if (result != null) {
            Log.d(TAG, "Overlay " + vehicle + " START size " + result.getRowBytes() + " bytes for " + Thread.currentThread().getName());
            Drawable drawable = new BitmapDrawable(Resources.getSystem(), result);
            int zl = 2;
            drawable.setBounds(-drawable.getIntrinsicWidth() / zl, -drawable.getIntrinsicHeight() / zl, drawable.getIntrinsicWidth() / zl, drawable.getIntrinsicHeight() / zl);
            overlayItem.setMarker(drawable);

            Overlay mapOverlay = updateOverlayItem();
            vehicleTracker.addBitmap(mapOverlay, result);

            Log.d(TAG, "Overlay " + vehicle + " FINISHED for " + Thread.currentThread().getName());
        } else {
            Log.d(TAG, "Overlay " + vehicle + " CANCELLED for " + Thread.currentThread().getName());
            this.cancel(true);
        }
    }

    private Overlay updateOverlayItem() {
        if (!clearBeforeUpdate) {
            dropOverlayItem();
        }
        List<Overlay> mapOverlays = mapView.getOverlays();
        MapOverlay mapOverlay = new MapOverlay(vehicle, vehicleTracker);
        mapOverlay.addItem(overlayItem);
        mapOverlays.add(mapOverlay);
        mapView.invalidate();
        return mapOverlay;
    }

    private void dropOverlayItem() {
        List<Overlay> mapOverlays = mapView.getOverlays();
        Iterator<Overlay> iterator = mapOverlays.iterator();
        while (iterator.hasNext()) {
            Overlay overlay = iterator.next();
            if (overlay instanceof MyLocationOverlay) {
                continue;
            }
            if (overlay instanceof MapOverlay) {
                Vehicle vehicle1 = ((MapOverlay) overlay).getVehicle();
                if (vehicle1.equals(vehicle)) {
                    vehicleTracker.removeBitmap(overlay);
                    iterator.remove();
                    mapView.invalidate();
                    break;
                }
            }
        }
        mapView.invalidate();
    }
}

