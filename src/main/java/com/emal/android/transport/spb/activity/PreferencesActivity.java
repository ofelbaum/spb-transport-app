package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.os.Bundle;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/21/13 12:39 AM
 */
public class PreferencesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
