package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import com.emal.android.transport.spb.R;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class InfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.info);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new InfoFragment())
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