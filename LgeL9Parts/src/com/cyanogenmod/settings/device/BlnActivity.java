package com.cyanogenmod.settings.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.content.SharedPreferences;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.util.Log;

public class BlnActivity extends PreferenceActivity  {

    public static final String KEY_BLN_ENABLE = "bln_enable_preference";

    private static final String BLN_ENABLE_FILE = "/sys/class/misc/backlightnotification/enabled";

    private ListPreference mTimeout, mBlinkOff, mBlinkOn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.touch_blink);

        Preference blnEnablePref = findPreference(KEY_BLN_ENABLE);
        blnEnablePref.setOnPreferenceChangeListener(
          new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Utils.writeValue(BLN_ENABLE_FILE, (Boolean) newValue ? "1" : "0");
                mTimeout.setEnabled(isEnabled());
                mBlinkOff.setEnabled(isEnabled());
                mBlinkOn.setEnabled(isEnabled());
                GeneralFragment.updateBlnSummary();
                return true;
            }
          });

        mTimeout = (ListPreference) findPreference(BlnTimeout.KEY_BLN_TIMEOUT);
        if (BlnTimeout.isSupported()) {
            mTimeout.setEnabled(isEnabled());
            mTimeout.setOnPreferenceChangeListener(new BlnTimeout());
            BlnTimeout.updateSummary(mTimeout, Integer.parseInt(mTimeout.getValue()));
        } else {
            PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().findPreference("category_bln"); 
            category.removePreference(mTimeout);
        }

        mBlinkOff = (ListPreference) findPreference(TouchBlinkOff.KEY_TOUCH_BLINK_OFF);
        mBlinkOn = (ListPreference) findPreference(TouchBlinkOn.KEY_TOUCH_BLINK_ON);
        if (TouchBlinkOff.isSupported()) {
            mBlinkOff.setEnabled(isEnabled());
            mBlinkOff.setOnPreferenceChangeListener(new TouchBlinkOff());
            TouchBlinkOff.updateSummary(mBlinkOff, Integer.parseInt(mBlinkOff.getValue()));
            mBlinkOn.setEnabled(isEnabled());
            mBlinkOn.setOnPreferenceChangeListener(new TouchBlinkOn());
            TouchBlinkOn.updateSummary(mBlinkOn, Integer.parseInt(mBlinkOn.getValue()));
        } else {
            PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().findPreference("category_touch_blink"); 
            category.removePreference(mBlinkOff);
            category.removePreference(mBlinkOn);
            getPreferenceScreen().removePreference(category);
        }
    }

    public static boolean isSupported() {
    	return Utils.fileExists(BLN_ENABLE_FILE); 
    }

    public static boolean isEnabled() {
        if (!isSupported()) return false;
        String text = Utils.readOneLine(BLN_ENABLE_FILE);
        return Integer.parseInt(text) != 0;
    }

    public static void restore(Context context) {
        if (isSupported()) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            if (isEnabled()) { // if set already by BLN update preference
               SharedPreferences.Editor editor = sp.edit();
               editor.putBoolean(KEY_BLN_ENABLE, true);
               editor.commit();
            } else {
                if (sp.getBoolean(KEY_BLN_ENABLE, false)) Utils.writeValue(BLN_ENABLE_FILE, "1");
            }
            BlnTimeout.restore(context);
            TouchBlinkOff.restore(context);
            TouchBlinkOn.restore(context);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
