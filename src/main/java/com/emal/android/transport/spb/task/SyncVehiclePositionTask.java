package com.emal.android.transport.spb.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import com.emal.android.transport.spb.VehicleSyncAdapter;
import com.emal.android.transport.spb.VehicleType;
import com.emal.android.transport.spb.utils.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/18/13 5:11 AM
 */
public class SyncVehiclePositionTask extends AsyncTask<Object, Void, Bitmap> {
    private static final String TAG = SyncVehiclePositionTask.class.getName();

    private HttpClient httpClient;
    private VehicleSyncAdapter vehicleSyncAdapter;
    private String vehiclesStr;
    private boolean clearBeforeUpdate = false;
    private Set<VehicleType> vehicleTypes;

    public SyncVehiclePositionTask(VehicleSyncAdapter vehicleSyncAdapter, Set<VehicleType> vehicleTypes, boolean clearBeforeUpdate, HttpClient httpClient) {
        this.vehicleSyncAdapter = vehicleSyncAdapter;
        this.httpClient = httpClient;
        this.clearBeforeUpdate = clearBeforeUpdate;
        this.vehicleTypes = vehicleTypes;
    }

    @Override
    protected void onPreExecute() {
        vehicleSyncAdapter.beforeSync(clearBeforeUpdate);
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        String bbox = vehicleSyncAdapter.getBBox();
        StringBuffer vs = new StringBuffer();
        Iterator<VehicleType> iterator = vehicleTypes.iterator();
        while (iterator.hasNext()) {
            VehicleType next = iterator.next();
            vs.append(next.getCode());
            if (iterator.hasNext()) {
                vs.append(",");
            }
        }
        vehiclesStr = vs.toString();
        Object[] paramss = new Object[]{vehiclesStr,
                bbox,
                (int)(vehicleSyncAdapter.getScreenWidth()/1.8),
                (int)(vehicleSyncAdapter.getScreenHeight()/1.8)};
        String url = Constants.URL_TEMPLATE + String.format(Constants.URL_PARAMS, paramss);

        long start = System.currentTimeMillis();
        Log.d(TAG, "Download " + vehiclesStr + " START for " + Thread.currentThread().getName());

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
            Log.d(TAG, "Download " + vehiclesStr + " CANCELLED for " + Thread.currentThread().getName());
            return null;
        } finally {
            double duration = (System.currentTimeMillis() - start) / 1000d;
            Log.d(TAG, "Download " + vehiclesStr + " FINISHED takes " + duration + " sec for " + Thread.currentThread().getName());
        }
    }

    private InputStream fetch(String address) throws IOException {
        HttpGet httpRequest = new HttpGet(URI.create(address));
        httpRequest.setHeader("User-Agent","Mozilla/5.0 (X11; Linux i686)");
        HttpResponse response = httpClient.execute(httpRequest);
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

        vehicleSyncAdapter.afterSync(result);

        if (result != null) {
            Log.d(TAG, "Overlay " + vehiclesStr + " FINISHED size " + result.getRowBytes() + " bytes for " + Thread.currentThread().getName());
        } else {
            Log.d(TAG, "Overlay " + vehiclesStr + " CANCELLED for " + Thread.currentThread().getName());
            cancel(true);
        }
    }
}
