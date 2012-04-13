package com.emal.android;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/10/12 2:02 AM
 */
public class MapOverlayItemMarkerAsyncTask extends AsyncTask<String, Void, Bitmap> {
    private static final String TAG = "MapOverlayItemMarkerAsyncTask";

	private OverlayItem overlayitem;
	private MapView mapView;
    private String[] params;
    private MapOverlayUpdater updater;

	public MapOverlayItemMarkerAsyncTask(OverlayItem overlayitem, MapView mapView, MapOverlayUpdater updater) {
		this.overlayitem = overlayitem;
		this.mapView = mapView;
        this.updater = updater;
	}

	@Override
	protected Bitmap doInBackground(String... params) {
        Log.d(TAG, "Start downloading picture: " + Thread.currentThread().getName());

        this.params = params;
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			in = fetch(params[0]);
			bitmap = BitmapFactory.decodeStream(in, null, null);
			in.close();
			return bitmap;
		} catch (IOException e1) {
			return null;
		} finally {
            Log.d(TAG, "Finish downloading picture: " + Thread.currentThread().getName());
        }
	}

	private static InputStream fetch(String address) throws IOException {
	    HttpGet httpRequest = new HttpGet(URI.create(address) );

        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        // The default value is zero, that means the timeout is not used.
        int timeoutConnection = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT)
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 10000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

	    HttpClient httpclient = new DefaultHttpClient(httpParameters);
	    HttpResponse response = httpclient.execute(httpRequest);
	    HttpEntity entity = response.getEntity();
	    BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
        return bufHttpEntity.getContent();
	}

	@Override
	protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (result != null) {
            Log.d(TAG, "Start overlay picture: " + Thread.currentThread().getName() + " " + result.getRowBytes() + " bytes");
            Drawable drawable = new BitmapDrawable(Resources.getSystem(), result);
            int zl = 2;
			drawable.setBounds(-drawable.getIntrinsicWidth() / zl, -drawable.getIntrinsicHeight() / zl, drawable.getIntrinsicWidth() / zl, drawable.getIntrinsicHeight() / zl);
			overlayitem.setMarker(drawable);
			mapView.invalidate();

            if (updater != null) {
                updater.update(mapView);
            }
            //result.recycle();
		}
        Log.d(TAG, "Finish overlay picture: " + Thread.currentThread().getName());
    }
}

