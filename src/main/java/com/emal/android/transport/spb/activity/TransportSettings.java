package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.emal.android.transport.spb.R;
import com.emal.android.transport.spb.map.MapUtils;
import com.emal.android.transport.spb.utils.Constants;
import com.emal.android.transport.spb.utils.GeoConverter;

import java.io.IOException;
import java.util.List;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/15/12 4:52 PM
 */
public class TransportSettings extends Activity {
    private static final String TAG = "TransportSettings";
    private static final String SPACE = " ";
    private SharedPreferences sharedPreferences;
    private CheckBox vehicleBus;
    private CheckBox vehicleTrolley;
    private CheckBox vehicleTram;
    private CheckBox vehicleShip;
    private RadioButton streetView;
    private RadioButton satelliteView;
    private Spinner spinner;

    private boolean showBus = false;
    private boolean showTrolley = false;
    private boolean showTram = false;
    private boolean showShip = false;
    private boolean satView = true;
    private int syncTime = Constants.DEFAULT_SYNC_MS;
    private AsyncTask loadAddressTask;

    private class LoadAddressTask extends AsyncTask<Object, Void, String> {
        private TextView textView;

        @Override
        protected String doInBackground(Object... params) {
            textView = (TextView) params[0];
            return getMyPlace();
        }

        @Override
        protected void onPostExecute(String s) {
            textView.setText(s);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        sharedPreferences = getSharedPreferences(Constants.APP_SHARED_SOURCE, 0);
        showBus = sharedPreferences.getBoolean(Constants.SHOW_BUS_FLAG, true);
        showTrolley = sharedPreferences.getBoolean(Constants.SHOW_TROLLEY_FLAG, true);
        showTram = sharedPreferences.getBoolean(Constants.SHOW_TRAM_FLAG, true);
        showShip = sharedPreferences.getBoolean(Constants.SHOW_SHIP_FLAG, true);
        satView = sharedPreferences.getBoolean(Constants.SAT_VIEW_FLAG, false);
        syncTime = sharedPreferences.getInt(Constants.SYNC_TIME_FLAG, Constants.DEFAULT_SYNC_MS);

        vehicleBus = (CheckBox) findViewById(R.id.vehicle_bus);
        vehicleBus.setChecked(showBus);
        vehicleTrolley = (CheckBox) findViewById(R.id.vehicle_trolley);
        vehicleTrolley.setChecked(showTrolley);
        vehicleTram = (CheckBox) findViewById(R.id.vehicle_tram);
        vehicleTram.setChecked(showTram);
        vehicleShip = (CheckBox) findViewById(R.id.vehicle_ship);
        vehicleShip.setChecked(showShip);

        streetView = (RadioButton) findViewById(R.id.street_view);
        satelliteView = (RadioButton) findViewById(R.id.satellite_view);
        if (satView) {
            satelliteView.setChecked(true);
        } else {
            streetView.setChecked(true);
        }

        TextView textView = (TextView)findViewById(R.id.my_place);
        textView.setText(R.string.wait);
        loadAddressTask = new LoadAddressTask();
        loadAddressTask.execute(textView);

        initSplitter();
    }

    @Override
    protected void onStop() {
        loadAddressTask.cancel(true);
        super.onStop();
    }

    private String getMyPlace() {
        String myPlaceString = getResources().getString(R.string.notfound);
        Integer homeLat = sharedPreferences.getInt(Constants.HOME_LOC_LAT_FLAG, MapUtils.SPB_CENTER_LAT_DEF_VALUE);
        Integer homeLong = sharedPreferences.getInt(Constants.HOME_LOC_LONG_FLAG, MapUtils.SPB_CENTER_LONG_DEF_VALUE);
        Geocoder geo = new Geocoder(getApplicationContext());
        try {
            List<Address> myAddrs = geo.getFromLocation(homeLat / 1E6, homeLong / 1E6, 1);
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


    private void initSplitter() {
        spinner = (Spinner) findViewById(R.id.sync_period);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.sync_string_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        for (int i = 0; i < adapter.getCount(); i++) {
            String s = String.valueOf(adapter.getItem(i));
            int val = Integer.valueOf(s.split(SPACE)[0]) * Constants.MS_IN_SEC;
            if (val == syncTime) {
                spinner.setSelection(i, true);
                break;
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String s = String.valueOf(((TextView) selectedItemView).getText());
                syncTime = Integer.parseInt(s.split(SPACE)[0]) * Constants.MS_IN_SEC;
                Log.d(TAG, "Sync time set to " + syncTime);
                SharedPreferences.Editor ed = sharedPreferences.edit();
                ed.putInt(Constants.SYNC_TIME_FLAG, syncTime);
                ed.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //nothing to do
            }
        });
    }

    public void onVehicleBusChanged(View view) {
        boolean isChecked = vehicleBus.isChecked();
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(Constants.SHOW_BUS_FLAG, isChecked);
        ed.commit();
    }

    public void onVehicleTrolleyChanged(View view) {
        boolean isChecked = vehicleTrolley.isChecked();
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(Constants.SHOW_TROLLEY_FLAG, isChecked);
        ed.commit();
    }

    public void onVehicleTramChanged(View view) {
        boolean isChecked = vehicleTram.isChecked();
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(Constants.SHOW_TRAM_FLAG, isChecked);
        ed.commit();
    }

    public void onVehicleShipChanged(View view) {
        boolean isChecked = vehicleShip.isChecked();
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(Constants.SHOW_SHIP_FLAG, isChecked);
        ed.commit();
    }

    public void onMapViewChanged(View view) {
        boolean isChecked = satelliteView.isChecked();
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(Constants.SAT_VIEW_FLAG, isChecked);
        ed.commit();
    }
}
