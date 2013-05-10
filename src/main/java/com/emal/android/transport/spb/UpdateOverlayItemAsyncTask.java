package com.emal.android.transport.spb;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.emal.android.transport.spb.activity.MainActivity;
import com.emal.android.transport.spb.map.MapOverlay;
import com.emal.android.transport.spb.utils.Constants;
import com.emal.android.transport.spb.utils.GeoConverter;
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
import java.util.Set;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/10/12 2:02 AM
 */
public class UpdateOverlayItemAsyncTask extends AsyncTask<String, Void, Bitmap> {
    private static final String TAG = "UpdateOverlayItemAsyncTask";

    private boolean clearBeforeUpdate;
    private MapView mapView;
    private Set<Vehicle> vehicles;
    private OverlayItem overlayItem;
    private VehicleTracker vehicleTracker;
    private Bitmap bitmap;
    private ProgressBar progressBar;
    private RelativeLayout errorSign;

    public UpdateOverlayItemAsyncTask(Set<Vehicle> vehicles, VehicleTracker vehicleTracker, boolean clearBeforeUpdate) {
        this.mapView = vehicleTracker.getMapView();
        this.vehicles = vehicles;
        this.vehicleTracker = vehicleTracker;
        this.clearBeforeUpdate = clearBeforeUpdate;

        MainActivity context = (MainActivity) mapView.getContext();
        this.progressBar = (ProgressBar) context.findViewById(com.emal.android.transport.spb.R.id.progressBar);
        this.errorSign = (RelativeLayout) context.findViewById(com.emal.android.transport.spb.R.id.errorSignLayout);
    }

    @Override
    protected void onPreExecute() {
        if (errorSign.getVisibility() == View.INVISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
        }

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
        StringBuffer vs = new StringBuffer();
        Iterator<Vehicle> iterator = vehicles.iterator();
        while (iterator.hasNext()) {
            Vehicle next = iterator.next();
            vs.append(next.getCode());
            if (iterator.hasNext()) {
                vs.append(",");
            }
        }
        Object[] paramss = new Object[]{vs.toString(), bbox, screenWidth, screenHeight};
        String url = Constants.URL_TEMPLATE + String.format(Constants.URL_PARAMS, paramss);

        long start = System.currentTimeMillis();
        Log.d(TAG, "Download " + vehicles + " START for " + Thread.currentThread().getName());

        try {
            InputStream in = fetch(url);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
            in.close();
            return bitmap;
        } catch (Exception e) {
            String message = e.getMessage();
            Log.e(TAG, message != null ? message : e.getClass().getName());
            Log.d(TAG, "Download " + vehicles + " CANCELLED for " + Thread.currentThread().getName());
            return null;
        } finally {
            double duration = (System.currentTimeMillis() - start) / 1000d;
            Log.d(TAG, "Download " + vehicles + " FINISHED takes " + duration + " sec for " + Thread.currentThread().getName());
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
        progressBar.setVisibility(View.INVISIBLE);

        if (isCancelled()) {
            Log.d(TAG, "onPostExecute skipped");
            return;
        }
        super.onPostExecute(result);
        if (result != null) {
            errorSign.setVisibility(View.INVISIBLE);

            Log.d(TAG, "Overlay " + vehicles + " START size " + result.getRowBytes() + " bytes for " + Thread.currentThread().getName());
            Drawable drawable = new BitmapDrawable(Resources.getSystem(), result);
            int zl = 2;
            drawable.setBounds(-drawable.getIntrinsicWidth() / zl, -drawable.getIntrinsicHeight() / zl, drawable.getIntrinsicWidth() / zl, drawable.getIntrinsicHeight() / zl);
            overlayItem.setMarker(drawable);

            updateOverlayItem();
            bitmap = result;

            Log.d(TAG, "Overlay " + vehicles + " FINISHED for " + Thread.currentThread().getName());
        } else {
            Log.d(TAG, "Overlay " + vehicles + " CANCELLED for " + Thread.currentThread().getName());
            this.cancel(true);

            new Handler().postAtTime(new Runnable() {
                @Override
                public void run() {
                    errorSign.setVisibility(View.VISIBLE);
                }
            }, 1000);
        }
    }

    private Overlay updateOverlayItem() {
        if (!clearBeforeUpdate) {
            dropOverlayItem();
        }
        List<Overlay> mapOverlays = mapView.getOverlays();
        MapOverlay mapOverlay = new MapOverlay(vehicleTracker);
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
                if (bitmap != null) {
                    bitmap.recycle();
                }
                iterator.remove();
                break;
            }
        }
        mapView.invalidate();
    }
}

