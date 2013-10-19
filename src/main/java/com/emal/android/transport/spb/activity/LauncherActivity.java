package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.content.Intent;
import com.emal.android.transport.spb.MapProviderType;
import com.emal.android.transport.spb.utils.ApplicationParams;
import com.emal.android.transport.spb.utils.Constants;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 7/11/13 12:20 AM
 */
public class LauncherActivity extends Activity {
    @Override
    protected void onStart() {
        super.onStart();
        initActivity();
        getActionBar().hide();
    }

    private void initActivity() {
        ApplicationParams appParams = new ApplicationParams(getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));

        Intent intent = new Intent(this, GMapsV2Activity.class);
        if (MapProviderType.GMAPSV1.equals(appParams.getMapProviderType())) {
            intent = new Intent(this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        LauncherActivity.this.startActivity(intent);
        LauncherActivity.this.finish();
    }
}
