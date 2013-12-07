package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import com.emal.android.transport.spb.R;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/21/13 12:39 AM
 */
public class PreferencesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle(R.string.settings);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home: {
                finish();
            }
        }
        return true;
    }
}
