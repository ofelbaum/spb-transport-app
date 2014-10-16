package com.emal.android.transport.spb.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import com.emal.android.transport.spb.R;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class InfoFragment extends PreferenceFragment {
    private static final String TAG = InfoFragment.class.getSimpleName();
    private static final String FEEDBACK_KEY = "pref_feedback";
    private static final String DONATE_KEY = "pref_donate";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.info);

        findPreference(FEEDBACK_KEY).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(TAG, "Choose feedback");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.emal.android.transport.spb"));
                startActivity(browserIntent);
                return true;
            }
        });

        findPreference(DONATE_KEY).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(TAG, "Choose donate");
                startActivity(new Intent(getActivity().getApplicationContext(), DonateActivity.class));
                return true;
            }
        });
    }
}
