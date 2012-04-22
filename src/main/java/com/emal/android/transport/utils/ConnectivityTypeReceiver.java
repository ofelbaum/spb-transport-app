package com.emal.android.transport.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/17/12 11:43 PM
 */
public class ConnectivityTypeReceiver extends BroadcastReceiver {
    private static final String TAG = ConnectivityTypeReceiver.class.getName();
    public static Context mContext;

    private PhoneStateListener phoneListener=new PhoneStateListener() {

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            super.onDataConnectionStateChanged(state, networkType);
            switch (networkType) {
                case ConnectivityManager.TYPE_WIFI : {
                    Log.d(TAG, "Network type : WI-FI");
                }
                case ConnectivityManager.TYPE_MOBILE : {
                    Log.d(TAG, "Network type : MOBILE");
                }
            }
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {

        if (mContext == null) mContext = context;

        TelephonyManager telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
//
//        final ConnectivityManager connMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//
//        if (wifi.isAvailable()) {
//            Log.d(TAG, "WI-FI enabled");
//        }
//        if (mobile.isAvailable()) {
//            Log.d(TAG, "MOBILE enabled");
//        }
    }
}
