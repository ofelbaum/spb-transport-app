package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.*;
import android.util.Log;
import com.emal.android.transport.spb.R;
import android.os.Bundle;
import com.emal.android.transport.spb.VehicleType;
import com.emal.android.transport.spb.model.ApplicationParams;
import com.emal.android.transport.spb.model.Theme;
import com.emal.android.transport.spb.utils.Constants;
import com.emal.android.transport.spb.task.LoadAddressTask;
import com.emal.android.transport.spb.component.SeekBarDialogPreference;
import com.google.android.maps.GeoPoint;

import java.util.*;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/21/13 12:40 AM
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getName();
    private static final String MAP_SYNC_TIME = "map_sync_time";
    private static final String MAP_TYPE = "map_type";
    private static final String VEHICLE_TYPES = "vehicle_types";
    private static final String MY_PLACE = "my_place";
    private static final String MAP_SHOW_TRAFFIC = "map_show_traffic";
    private static final String APP_THEME = "app_theme";
    private static final String ICON_SIZE = "icon_size";

    private ListPreference syncTimePref;
    private MultiSelectListPreference vehicleTypes;
    private ListPreference mapTypePref;
    private ListPreference appTheme;
    private ApplicationParams appParams;
    private Preference myPlace;
    private CheckBoxPreference showTraffic;
    private SeekBarDialogPreference iconSize;
    private Resources resources;
    private AlertDialog removeMyPlaceDialog;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String yes = resources.getString(com.emal.android.transport.spb.R.string.yes);
        String no = resources.getString(com.emal.android.transport.spb.R.string.no);
        builder.setCancelable(false)
                .setMessage(resources.getString(R.string.removemyplace))
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        appParams.setHomeLocation(null);
                        myPlace.setSummary(R.string.place_not_defined);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        removeMyPlaceDialog = builder.create();

        initParams();
    }

    private void initParams() {
        appParams = new ApplicationParams(getActivity().getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));

        int syncTime = appParams.getSyncTime();
        int mapType = Boolean.TRUE.equals(appParams.isSatView()) ? 2 : 1;
        int iconSizeValue = appParams.getIconSize();

        syncTimePref = (ListPreference) findPreference(MAP_SYNC_TIME);
        this.vehicleTypes = (MultiSelectListPreference) findPreference(VEHICLE_TYPES);
        mapTypePref = (ListPreference) findPreference(MAP_TYPE);
        appTheme = (ListPreference) findPreference(APP_THEME);
        myPlace = findPreference(MY_PLACE);
        iconSize = (SeekBarDialogPreference) findPreference(ICON_SIZE);

        showTraffic = (CheckBoxPreference) findPreference(MAP_SHOW_TRAFFIC);
        showTraffic.setChecked(appParams.isShowTraffic());

        PrefData prefData = getEntryByValue(String.valueOf(syncTime), R.array.sync_string_array, R.array.sync_string_values);
        syncTimePref.setSummary(prefData.getValue());
        syncTimePref.setValueIndex(prefData.getIndex());

        prefData = getEntryByValue(String.valueOf(mapType), R.array.map_types_entries, R.array.map_types_values);
        mapTypePref.setSummary(prefData.getValue());
        mapTypePref.setValueIndex(prefData.getIndex());

        prefData = getEntryByValue(appParams.getTheme().name(), R.array.app_theme_entries, R.array.app_theme_values);
        appTheme.setSummary(prefData.getValue());
        appTheme.setValueIndex(prefData.getIndex());

        prefData = getEntryByIndex(iconSizeValue, R.array.icon_size_array);
        iconSize.setSummary(prefData.getValue());
        iconSize.setProgress(prefData.getIndex());

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

        Resources res = getResourcesSilently();
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

        myPlace.setEnabled(false);
        myPlace.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                removeMyPlaceDialog.show();
                return true;
            }
        });

        GeoPoint homeLocation = appParams.getHomeLocation();
        if (homeLocation == null) {
            myPlace.setSummary(resources.getString(R.string.place_not_defined));
        } else {
            LoadAddressTask loadAddressTask = new LoadAddressTask(getActivity().getApplicationContext(), homeLocation) {
                @Override
                public void setValue(String s) {
                    myPlace.setSummary(s);
                    myPlace.setEnabled(true);
                }
            };
            loadAddressTask.execute();
        }
    }

    private PrefData getEntryByValue(String value, int entriesId, int valuesId) {
        Resources res = getResourcesSilently();
        List<String> entries = Arrays.asList(res.getStringArray(entriesId));
        List<String> values = Arrays.asList(res.getStringArray(valuesId));
        int i = values.indexOf(value);
        return new PrefData(i, value, entries.get(i));
    }

    private PrefData getEntryByIndex(int index, int entriesId) {
        Resources res = getResourcesSilently();
        int[] entries = res.getIntArray(entriesId);
        return new PrefData(index, String.valueOf(entries[index]), String.valueOf(entries[index]));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Choose key=" + key);
        if (APP_THEME.equals(key)) {
            String res = setSummary(sharedPreferences, key, R.array.app_theme_entries, R.array.app_theme_values);
            Theme theme = Theme.valueOf(res);
            appParams.setTheme(theme);
            appParams.saveAll();
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
                activity.startActivity(new Intent(activity, activity.getClass()));
            }
        }
        if (MAP_SYNC_TIME.equals(key)) {
            String res = setSummary(sharedPreferences, key, R.array.sync_string_array, R.array.sync_string_values);
            appParams.setSyncTime(Integer.valueOf(res));
        }
        if (ICON_SIZE.equals(key)) {
            int progress = iconSize.getProgress();
            setSummary(sharedPreferences, key, progress);
            appParams.setIconSize(progress);
        }
        if (MAP_TYPE.equals(key)) {
            String res = setSummary(sharedPreferences, key, R.array.map_types_entries, R.array.map_types_values);
            appParams.setSatView(Integer.valueOf(res) == 2);
        }
        if (MAP_SHOW_TRAFFIC.equals(key)) {
            appParams.setShowTraffic(showTraffic.isChecked());
        }
        if (VEHICLE_TYPES.equals(key)) {
            Set<String> selected = sharedPreferences.getStringSet(VEHICLE_TYPES, new HashSet<String>());
            Resources res = getResourcesSilently();
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

        Resources res = getResourcesSilently();
        List<String> entries = Arrays.asList(res.getStringArray(entriesId));
        List<String> values = Arrays.asList(res.getStringArray(valuesId));

        String string = sharedPreferences.getString(key, "");
        String summary = entries.get(values.indexOf(string));
        exercisesPref.setSummary(summary);
        return string;
    }

    private void setSummary(SharedPreferences sharedPreferences, String key, int entriesId) {
        Preference exercisesPref = findPreference(key);

        String summary = String.valueOf(entriesId);
        exercisesPref.setSummary(summary);
    }

    private Resources getResourcesSilently() {
        return resources;
    }

    @Override
    public void onAttach(Activity activity) {
        resources = activity.getResources();
        super.onAttach(activity);
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