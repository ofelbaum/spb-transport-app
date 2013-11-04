package com.emal.android.transport.spb.activity;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.*;
import android.util.Log;
import com.emal.android.transport.spb.MapProviderType;
import com.emal.android.transport.spb.R;
import android.os.Bundle;
import com.emal.android.transport.spb.VehicleType;
import com.emal.android.transport.spb.utils.ApplicationParams;
import com.emal.android.transport.spb.utils.Constants;
import com.emal.android.transport.spb.utils.LoadAddressTask;

import java.util.*;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/21/13 12:40 AM
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getName();
    private static final String MAP_PROVIDER_TYPE = "map_provider_type";
    private static final String MAP_SYNC_TIME = "map_sync_time";
    private static final String MAP_TYPE = "map_type";
    private static final String VEHICLE_TYPES = "vehicle_types";
    private static final String MY_PLACE = "my_place";
    private static final String MAP_SHOW_TRAFFIC = "map_show_traffic";

    private ListPreference syncTimePref;
    private MultiSelectListPreference vehicleTypes;
    private ListPreference mapTypePref;
    private ListPreference mapProviderType;
    private ApplicationParams appParams;
    private Preference myPlace;
    private CheckBoxPreference showTraffic;

    public class PrefData {
        private int index;
        private String entry;
        private String value;

        public PrefData(int index, String entry, String value) {
            this.index = index;
            this.entry = entry;
            this.value = value;
        }

        public int getIndex() {
            return index;
        }

        public String getEntry() {
            return entry;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferencies);
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        initParams();

    }

    private void initParams() {
        appParams = new ApplicationParams(getActivity().getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));

        int syncTime = appParams.getSyncTime();
        int mapType = Boolean.TRUE.equals(appParams.isSatView()) ? 2 : 1;
        MapProviderType providerType = appParams.getMapProviderType();


        syncTimePref = (ListPreference) findPreference(MAP_SYNC_TIME);
        this.vehicleTypes = (MultiSelectListPreference) findPreference(VEHICLE_TYPES);
        mapTypePref = (ListPreference) findPreference(MAP_TYPE);
        mapProviderType = (ListPreference) findPreference(MAP_PROVIDER_TYPE);
        myPlace= findPreference(MY_PLACE);

        showTraffic = (CheckBoxPreference) findPreference(MAP_SHOW_TRAFFIC);
        showTraffic.setChecked(appParams.isShowTraffic());

        PrefData prefData = getEntryByValue(String.valueOf(syncTime), R.array.sync_string_array, R.array.sync_string_values);
        syncTimePref.setSummary(prefData.getValue());
        syncTimePref.setValueIndex(prefData.getIndex());

        prefData = getEntryByValue(String.valueOf(mapType), R.array.map_types_entries, R.array.map_types_values);
        mapTypePref.setSummary(prefData.getValue());
        mapTypePref.setValueIndex(prefData.getIndex());

        prefData = getEntryByValue(providerType.name(), R.array.map_provider_entries, R.array.map_provider_values);
        mapProviderType.setSummary(prefData.getValue());
        mapProviderType.setValueIndex(prefData.getIndex());

        Set<VehicleType> vehicleTypes = new HashSet<VehicleType>();
        if (appParams.isShowBus()) {
            vehicleTypes.add(VehicleType.BUS);
        }
        if (appParams.isShowTrolley()) {
            vehicleTypes.add(VehicleType.TROLLEY);
        }
        if (appParams.isShowTram()) {
            vehicleTypes.add(VehicleType.TRAM);
        }
        if (appParams.isShowShip()) {
            vehicleTypes.add(VehicleType.SHIP);
        }

        Resources res = getResources();
        List<String> entries = Arrays.asList(res.getStringArray(R.array.vehicle_types_entries));
        List<String> values = Arrays.asList(res.getStringArray(R.array.vehicle_types_values));

        StringBuffer buffer = new StringBuffer();
        Iterator<VehicleType> iterator = vehicleTypes.iterator();
        Set<String> selectedVehicles = new HashSet<String>();
        while (iterator.hasNext()) {
            String name = iterator.next().name();
            int i = values.indexOf(name);
            String s = entries.get(i);
            buffer.append(s);
            selectedVehicles.add(name);
            if (iterator.hasNext()) {
                buffer.append(", ");
            }

        }
        this.vehicleTypes.setValues(selectedVehicles);
        this.vehicleTypes.setSummary(buffer.toString());

        this.vehicleTypes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return true;
            }
        });

        LoadAddressTask loadAddressTask = new LoadAddressTask(getActivity().getApplicationContext(), appParams) {
            @Override
            public void setValue(String s) {
                myPlace.setSummary(s);
            }
        };
        loadAddressTask.execute();
    }

    private PrefData getEntryByValue(String value, int entriesId, int valuesId) {
        Resources res = getResources();
        List<String> entries = Arrays.asList(res.getStringArray(entriesId));
        List<String> values = Arrays.asList(res.getStringArray(valuesId));
        int i = values.indexOf(value);
        return new PrefData(i, value, entries.get(i));
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Choose key=" + key);
        if (MAP_PROVIDER_TYPE.equals(key))
        {
            String res = setSummary(sharedPreferences, key, R.array.map_provider_entries, R.array.map_provider_values);
            appParams.setMapProviderType(MapProviderType.valueOf(res));
        }
        if (MAP_SYNC_TIME.equals(key))
        {
            String res = setSummary(sharedPreferences, key, R.array.sync_string_array, R.array.sync_string_values);
            appParams.setSyncTime(Integer.valueOf(res));
        }
        if (MAP_TYPE.equals(key))
        {
            String res = setSummary(sharedPreferences, key, R.array.map_types_entries, R.array.map_types_values);
            appParams.setSatView(Integer.valueOf(res) == 2);
        }
        if (MAP_SHOW_TRAFFIC.equals(key)) {
            appParams.setShowTraffic(showTraffic.isChecked());
        }
        if (VEHICLE_TYPES.equals(key)) {
            Set<String> selected = sharedPreferences.getStringSet(VEHICLE_TYPES, new HashSet<String>());
            Resources res = getResources();
            List<String> entries = Arrays.asList(res.getStringArray(R.array.vehicle_types_entries));
            List<String> values = Arrays.asList(res.getStringArray(R.array.vehicle_types_values));

            StringBuffer buffer = new StringBuffer();
            Iterator<String> iterator = selected.iterator();

            appParams.resetVehicles();

            while (iterator.hasNext()) {
                String next = iterator.next();
                int i = values.indexOf(next);
                if (i != -1) {
                    buffer.append(entries.get(i));
                }
                if (iterator.hasNext()) {
                    buffer.append(", ");
                }

                if (VehicleType.SHIP.name().equals(next)) {
                    appParams.setShowShip(true);
                } else if (VehicleType.BUS.name().equals(next)) {
                    appParams.setShowBus(true);
                } else if (VehicleType.TROLLEY.name().equals(next)) {
                    appParams.setShowTrolley(true);
                } else if (VehicleType.TRAM.name().equals(next)) {
                    appParams.setShowTram(true);
                }

            }
            vehicleTypes.setSummary(buffer.toString());
        }
    }

    private String setSummary(SharedPreferences sharedPreferences, String key, int entriesId, int valuesId) {
        Preference exercisesPref = findPreference(key);

        Resources res = getResources();
        List<String> entries = Arrays.asList(res.getStringArray(entriesId));
        List<String> values = Arrays.asList(res.getStringArray(valuesId));

        String string = sharedPreferences.getString(key, "");
        String summary = entries.get(values.indexOf(string));
        exercisesPref.setSummary(summary);
        return string;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        initParams();
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        appParams.saveAll();
        super.onPause();
    }
}