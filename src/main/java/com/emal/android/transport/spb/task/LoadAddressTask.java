package com.emal.android.transport.spb.task;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import com.emal.android.transport.spb.R;
import com.emal.android.transport.spb.model.ApplicationParams;
import com.emal.android.transport.spb.utils.GeoConverter;
import com.google.android.maps.GeoPoint;

import java.io.IOException;
import java.util.List;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 7/13/13 10:29 PM
 */
public abstract class LoadAddressTask extends AsyncTask<Object, Void, String> {
    private static final String TAG = LoadAddressTask.class.getName();
    private Context context;
    private GeoPoint geoPoint;

    public abstract void setValue(String s);

    protected LoadAddressTask(Context context, GeoPoint geoPoint) {
        this.context = context;
        this.geoPoint = geoPoint;
    }

    @Override
    protected String doInBackground(Object... params) {
        String myPlaceString = context.getResources().getString(R.string.notfound);
        Geocoder geo = new Geocoder(context);
        try {
            List<Address> myAddrs = geo.getFromLocation(geoPoint.getLatitudeE6() / 1E6, geoPoint.getLongitudeE6() / 1E6, 1);
            if (myAddrs.size() > 0) {
                Address myPlace = myAddrs.get(0);
                Log.d(TAG, "My Place selected: " + GeoConverter.convert(myPlace));
                myPlaceString = GeoConverter.convert(myPlace);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return myPlaceString;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        setValue(s);
    }
}
