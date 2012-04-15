package com.emal.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/15/12 4:52 PM
 */
public class TransportSettings extends Activity {
    private SharedPreferences sharedPreferences;
    private CheckBox vehicleBus;
    private CheckBox vehicleTrolley;
    private CheckBox vehicleTram;
    private RadioButton streetView;
    private RadioButton satelliteView;

    private boolean showBus = false;
    private boolean showTrolley = false;
    private boolean showTram = false;
    private boolean satView = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        sharedPreferences = getSharedPreferences(Constants.APP_SHARED_SOURCE, 0);
        showBus = sharedPreferences.getBoolean(Constants.SHOW_BUS_FLAG, true);
        showTrolley = sharedPreferences.getBoolean(Constants.SHOW_TROLLEY_FLAG, true);
        showTram = sharedPreferences.getBoolean(Constants.SHOW_TRAM_FLAG, true);
        satView = sharedPreferences.getBoolean(Constants.SAT_VIEW_FLAG, true);

        vehicleBus = (CheckBox) findViewById(R.id.vehicle_bus);
        vehicleBus.setChecked(showBus);
        vehicleTrolley = (CheckBox) findViewById(R.id.vehicle_trolley);
        vehicleTrolley.setChecked(showTrolley);
        vehicleTram = (CheckBox) findViewById(R.id.vehicle_tram);
        vehicleTram.setChecked(showTram);

        streetView = (RadioButton) findViewById(R.id.street_view);
        satelliteView = (RadioButton) findViewById(R.id.satellite_view);
        if (satView) {
            satelliteView.setChecked(true);
        } else {
            streetView.setChecked(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onSaveButtonClick(View view) {
        SharedPreferences.Editor ed = sharedPreferences.edit();

        ed.putBoolean(Constants.SHOW_BUS_FLAG, vehicleBus.isChecked());
        ed.putBoolean(Constants.SHOW_TROLLEY_FLAG, vehicleTrolley.isChecked());
        ed.putBoolean(Constants.SHOW_TRAM_FLAG, vehicleTram.isChecked());
        ed.putBoolean(Constants.SAT_VIEW_FLAG, satelliteView.isChecked());
        ed.commit();

        this.finish();
    }

    public void onCancelButtonClick(View view) {
        this.finish();
    }
}
